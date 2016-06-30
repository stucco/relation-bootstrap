package gov.ornl.stucco;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class TrainRelationSVMClassifier 
{
	private static File preprocessedfile = new File("/Users/p5r/stuccovm/preprocesseddata/aliassubstitutedentitynamesprocesseddocuments");
	static File traininginstancesfile = new File("/Users/p5r/stuccovm/models/traininginstances");
	
	private static int relationshiptype;
	private static String contexts;
	
	
	public static void main(String[] args)
	{
		readArgs(args);
		
		WordToVectorMap wvm = WordToVectorMap.getWordToVectorMap();
		
		buildTrainingInstances(wvm);
	}
	
	//Arguments: relationshiptype(See constants at top of AllKnownDatabaseRelationships.) contexts
	private static void readArgs(String[] args)
	{
		relationshiptype = Integer.parseInt(args[0]);
		
		//Contexts is 000, 001, 010, 011, 100, 101, 110, or 111.  It tells us whether or not we want to use the context
		//preceding the first entity (first digit), whether or not we want to use the context between entities (second digit),
		//and whether or not we want to use the context after the second entity (third digit).
		contexts = args[1];
		if(contexts.length() != 3 || 
				!(contexts.charAt(0) == '0' || contexts.charAt(0) == '1') ||
				!(contexts.charAt(1) == '0' || contexts.charAt(1) == '1') ||
				!(contexts.charAt(2) == '0' || contexts.charAt(2) == '1'))
		{
			System.err.println("Error, invalid context.  Context must be 000, 001, 010, 011, 100, 101, 110, or 111.");
			System.exit(3);
		}
	}
	
	private static void buildTrainingInstances(WordToVectorMap wvm)
	{
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(preprocessedfile));
			String line;
			while((line = in.readLine()) != null)
			{
				String[] tokens = line.split(" ");
				
				CyberEntityText[] cyberentitytexts = new CyberEntityText[tokens.length];
				for(int i = 0; i < tokens.length; i++)
					cyberentitytexts[i] = CyberEntityText.getCyberEntityTextFromToken(tokens[i]);
				
				for(int i = 0; i < tokens.length; i++)
				{
					if(cyberentitytexts[i] != null)
					{
						for(int j = i+1; j < tokens.length; j++)
						{
							if(cyberentitytexts[j] != null)
							{
								GenericCyberEntityTextRelationship relationship = new GenericCyberEntityTextRelationship(cyberentitytexts[i], cyberentitytexts[j]);
								
								Integer relationtype = relationship.getRelationType();
								if(relationtype != null)
								{
									ArrayList<Double> concatenatedvectors = new ArrayList<Double>();
									
									if(contexts.charAt(0) == '1')
									{
										String[] context1 = Arrays.copyOfRange(tokens, 0, i);
										double[] context1vector = wvm.getContextVector(context1);
										for(double value : context1vector)
											concatenatedvectors.add(value);
									}
									
									if(contexts.charAt(1) == '1')
									{
										String[] context2 = Arrays.copyOfRange(tokens, i+1, j);
										double[] context2vector = wvm.getContextVector(context2);
										for(double value : context2vector)
											concatenatedvectors.add(value);
									}
									
									if(contexts.charAt(1) == '2')
									{
										String[] context3 = Arrays.copyOfRange(tokens, j, tokens.length);
										double[] context3vector = wvm.getContextVector(context3);
										for(double value : context3vector)
											concatenatedvectors.add(value);
									}
								}
							}
						}
					}
				}
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
