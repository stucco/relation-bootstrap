package gov.ornl.stucco;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class WordToVectorMap extends HashMap<String,double[]>
{
	private static File wordvectorsfile = new File("/Users/p5r/stuccovm/models/wordvectors");
	
	private static WordToVectorMap themap = null;
	
	private int vectorlength;
	
	
	WordToVectorMap(File wvf)
	{
		readWordToVectorsFile(wvf);
	}
	
	private void readWordToVectorsFile(File wvf)
	{
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(wordvectorsfile));
			String line;
			while((line = in.readLine()) != null)
			{
				String[] splitline = line.split(" ");
				String word = splitline[0];
				double[] vector = new double[splitline.length-1];
				for(int i = 1; i < splitline.length; i++)
					vector[i-1] = Double.parseDouble(splitline[i]);
				put(word, vector);
				
				vectorlength = vector.length;
			}
			in.close();
		}catch(IOException e)
		{
			System.out.println(e);
			e.printStackTrace();
			System.exit(3);
		}
	}
	
	
	
	public static WordToVectorMap getWordToVectorMap()
	{
		if(themap == null)
			themap = new WordToVectorMap(wordvectorsfile);
		
		return themap;
	}
	
	
	//Construct a vector corresponding to this context by averaging the vectors of its words.
	public double[] getContextVector(String[] context)
	{
		double[] result = new double[vectorlength];
		
		int existingtokenscount = 0;	//Count the number of tokens in this context that we appear in our map.
		for(String token : context)
		{
			double[] tokenvector = get(token);
			
			if(tokenvector != null)
			{
				existingtokenscount++;
				
				for(int i = 0; i < vectorlength; i++)
					result[i] += tokenvector[i];
			}
		}
		
		if(existingtokenscount != 0)
		{
			for(int i = 0; i < vectorlength; i++)
				result[i] /= existingtokenscount;
		}
		
		return result;
	}
}
