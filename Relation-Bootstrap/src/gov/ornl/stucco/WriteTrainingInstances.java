package gov.ornl.stucco;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class WriteTrainingInstances 
{
	private static File preprocessedfile = new File("/Users/p5r/stuccovm/preprocesseddata/aliassubstitutedentitynamesprocesseddocuments");
	private static File traininginstancesfile = new File("/Users/p5r/stuccovm/models/traininginstances");
	
	private static int relationshiptype;
	

	public static void main(String[] args)
	{
		readArgs(args);
		
		WordToVectorMap wvm = WordToVectorMap.getWordToVectorMap();
		
		writeTrainingInstances(wvm);
	}
	
	//Arguments: relationshiptype(See constants at top of AllKnownDatabaseRelationships)
	private static void readArgs(String[] args)
	{
		relationshiptype = Integer.parseInt(args[0]);
	}
	
	private static void writeTrainingInstances(WordToVectorMap wvm)
	{
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(preprocessedfile));
			String line;
			while((line = in.readLine()) != null)
			{
				
			}
			in.close();
		}catch(IOException e)
		{
			System.out.println(e);
			e.printStackTrace();
			System.exit(3);
		}
	}
	
	
}
