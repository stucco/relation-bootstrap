/*
 * This program's purpose is to read the word vectors written by TrainModel.py and the preprocessed files
 * written by PrintPreprocessedDocuments in order to construct training instances for some learner.
 * The resulting training files are written in SVM light format.
 */


package gov.ornl.stucco.relationprediction;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;

public class WriteRelationInstanceFiles
{
	private static String entityextractedfilename;
	//private static String contexts;
	private static boolean training = false;
	
	private static String featuretype;
	
	
	private static DecimalFormat formatter = new DecimalFormat(".0000");
	
	
	public static void main(String[] args)
	{
		readArgs(args);
		
		//It is kind of dumb to initialize all the printwriters at once right here, but it made sense in an old version of this program.
		HashMap<Integer,HashMap<String,PrintWriter>> relationtypeTocontextToprintwriter = initializePrintWriters(featuretype);
		
		HashMap<Integer, ArrayList<InstanceID>> relationtypeToinstanceidorder = InstanceID.readRelationTypeToInstanceIDOrder(entityextractedfilename, training);
		
		buildAndWriteTrainingInstances(featuretype, relationtypeTocontextToprintwriter, relationtypeToinstanceidorder);
		
		//Close the file streams.
		for(HashMap<String,PrintWriter> contextToprintwriter : relationtypeTocontextToprintwriter.values())
			for(PrintWriter pw : contextToprintwriter.values())
				pw.close();
	}
	
	//Arguments: 
	//1. extractedfilename (This is the name of the file written by PrintPreprocessedDocuments.  
	//Valid known values for this argument are "original", "entityreplaced", and "aliasreplaced")
	//2. featuretype (This code (1 character in length) encodes which feature type to build the
	//file from.  To see available feature type character codes and their
	//meanings, see FeatureMap.)
	//3. training (optional).  If you include the word "training" in your command line arguments
	//after the first two (required) feature types, training files (based on the .ser.gz contents
	//of Training DataFiles directory) will be written.  Else testing files (based on the .ser.gz
	//contents of Testing DataFiles directory) will be written.
	private static void readArgs(String[] args)
	{
		entityextractedfilename = args[0];
		featuretype = args[1];

		for(int i = 2; i < args.length; i++)
		{
			if("training".equals(args[i]))
				training = true;
		}
	}
	

	private static HashMap<Integer,HashMap<String,PrintWriter>> initializePrintWriters(String feature)
	{
		HashMap<Integer,HashMap<String,PrintWriter>> relationtypeTocontextToprintwriter = new HashMap<Integer,HashMap<String,PrintWriter>>();
		
		try
		{
				for(Integer i : GenericCyberEntityTextRelationship.getAllRelationshipTypesSet())
				{
					File f = ProducedFileGetter.getRelationshipSVMInstancesFile(entityextractedfilename, feature, i, training);
					
					HashMap<String,PrintWriter> contextToprintwriter = relationtypeTocontextToprintwriter.get(i);
					if(contextToprintwriter == null)
					{
						contextToprintwriter = new HashMap<String,PrintWriter>();
						relationtypeTocontextToprintwriter.put(i, contextToprintwriter);
					}
					contextToprintwriter.put(feature, new PrintWriter(new FileWriter(f)));
				}
		}catch(IOException e)
		{
			System.out.println(e);
			e.printStackTrace();
			System.exit(3);
		}
		
		return relationtypeTocontextToprintwriter;
	}

	
	//Direct the flow to the method for writing the appropriate feature type.
	private static void buildAndWriteTrainingInstances(String featuretype, HashMap<Integer,HashMap<String,PrintWriter>> relationtypeTocontextToprintwriter, HashMap<Integer,ArrayList<InstanceID>> relationtypeToinstanceidorder)
	{
		if(featuretype.equals(FeatureMap.WORDEMBEDDINGBEFORECONTEXT))
			writeContextFile(featuretype, relationtypeTocontextToprintwriter, relationtypeToinstanceidorder);
		if(featuretype.equals(FeatureMap.WORDEMBEDDINGBETWEENCONTEXT))
			writeContextFile(featuretype, relationtypeTocontextToprintwriter, relationtypeToinstanceidorder);
		if(featuretype.equals(FeatureMap.WORDEMBEDDINGAFTERCONTEXT))
			writeContextFile(featuretype, relationtypeTocontextToprintwriter, relationtypeToinstanceidorder);
	}
	
	private static void writeContextFile(String featuretype, HashMap<Integer,HashMap<String,PrintWriter>> relationtypeTocontextToprintwriter, HashMap<Integer,ArrayList<InstanceID>> relationtypeToinstanceidorder)
	{
		//Read in the saved word vectors
		WordToVectorMap wvm = WordToVectorMap.getWordToVectorMap(entityextractedfilename);
		
		try
		{
			//We are switching to using zip files for these because they could potentially be very big.
			ZipFile zipfile = new ZipFile(ProducedFileGetter.getEntityExtractedText(entityextractedfilename, training));
			ZipEntry entry = zipfile.entries().nextElement();
			BufferedReader in = new BufferedReader(new InputStreamReader(zipfile.getInputStream(entry)));

			//We are switching to using zip files for these because they could potentially be very big.
			ZipFile zipfile3 = new ZipFile(ProducedFileGetter.getEntityExtractedText("unlemmatized", training));
			ZipEntry entry3 = zipfile3.entries().nextElement();
			BufferedReader unlemmatizedalignedin = new BufferedReader(new InputStreamReader(zipfile3.getInputStream(entry3)));
		
		
			for(Integer relationtype : GenericCyberEntityTextRelationship.getAllRelationshipTypesSet())
			{
				PrintWriter out = relationtypeTocontextToprintwriter.get(relationtype).get(featuretype);
			
				ArrayList<InstanceID> instanceidorder = new ArrayList<InstanceID>(relationtypeToinstanceidorder.get(relationtype));
				while(instanceidorder.size() > 0)
				{
					String desiredfilename = instanceidorder.get(0).getFileName();
				
					ArrayList<String[]> filelines = getLinesAssociatedWithOneFile(desiredfilename, in, zipfile, entry);
					ArrayList<String[]> unlemmatizedfilelines = getLinesAssociatedWithOneFile(desiredfilename, unlemmatizedalignedin, zipfile3, entry3);
				
					//These are the indices of the tokens in the original document (before we 
					//replaced entities with representative tokens).
					ArrayList<int[]> originalindices = new ArrayList<int[]>();
					for(String[] sentence : unlemmatizedfilelines)
						originalindices.add(PrintPreprocessedDocuments.getOriginalTokenIndexesFromPreprocessedLine(sentence));
				
					while(instanceidorder.size() > 0 && instanceidorder.get(0).getFileName().equals(desiredfilename))
					{
						InstanceID instanceid = instanceidorder.remove(0);
					
						int firsttokensentencenum = instanceid.getFirstTokenSentenceNum();
						int replacedfirsttokenindex = instanceid.getReplacedFirstTokenIndex();
						int secondtokensentencenum = instanceid.getSecondTokenSentenceNum();
						int replacedsecondtokenindex = instanceid.getReplacedSecondTokenIndex();
					
						String[] firstsentence = filelines.get(firsttokensentencenum);
						String[] firsttokenunlemmatizedsentence = unlemmatizedfilelines.get(firsttokensentencenum);
						String[] secondsentence = filelines.get(secondtokensentencenum);
						String[] secondtokenunlemmatizedsentence = unlemmatizedfilelines.get(secondtokensentencenum);
					
						ArrayList<String> context = new ArrayList<String>();
						if(featuretype.equals(FeatureMap.WORDEMBEDDINGBEFORECONTEXT))
						{
							for(int i = 0; i < replacedfirsttokenindex; i++)
								context.add(firstsentence[i]);
						}
						if(featuretype.equals(FeatureMap.WORDEMBEDDINGAFTERCONTEXT))
						{
							for(int i = replacedsecondtokenindex+1; i < secondsentence.length; i++)
								context.add(secondsentence[i]);
						}
						if(featuretype.equals(FeatureMap.WORDEMBEDDINGBETWEENCONTEXT))
						{
							if(firsttokensentencenum == secondtokensentencenum)
							{
								for(int i = replacedfirsttokenindex+1; i < replacedsecondtokenindex; i++)
									context.add(firstsentence[i]);
							}
							else
							{
								for(int i = replacedfirsttokenindex+1; i < firstsentence.length; i++)
									context.add(firstsentence[i]);
								for(int i = 0; i < replacedsecondtokenindex; i++)
									context.add(secondsentence[i]);
								for(int sentencenum = firsttokensentencenum + 1; sentencenum < secondtokensentencenum; sentencenum++)
								{
									String[] sentence = filelines.get(sentencenum);
									for(String word : sentence)
										context.add(word);
								}
							}
						}
					
						//Now, actually build the SVM_light style string representation of the instance.
						String instanceline = buildOutputLineFromVector(instanceid.getHeuristicLabel(), wvm.getContextVector(context));
				
				
						//SVM_light format allows us to add comments to the end of lines.  So to make the line more human-interpretable,
						//add the entity names to the end of the line.
						if(training)
						instanceline += " # " + instanceid;
						else
						{
							String unlemmatizedtokens = "";
							for(int i = firsttokensentencenum; i <= secondtokensentencenum; i++)
							{
								String[] unlemmatizedline = unlemmatizedfilelines.get(i);
								for(String unlemmatizedword : unlemmatizedline)
									unlemmatizedtokens += " " + unlemmatizedword;
							}
							
							instanceline += " # " + instanceid + " " + firsttokensentencenum + " " + firsttokenunlemmatizedsentence[replacedfirsttokenindex] + " " + firstsentence[replacedfirsttokenindex] + " " + secondtokensentencenum + " " + secondtokenunlemmatizedsentence[replacedsecondtokenindex] + " " + secondsentence[replacedsecondtokenindex] + " " + unlemmatizedtokens.trim();
						}
				
						//And finally, print it to the appropriate file using the PrintWriter we 
						//made earlier.
						out.println(instanceline);
					//}
					}
				}
			}
		}catch(IOException e)
		{
			System.out.println(e);
			e.printStackTrace();
			System.exit(3);
		}
	}
	
	
 	public static String buildOutputLineFromVector(Boolean isknownrelationship, double[] contextvectors)
	{
		String result = "";
		if(isknownrelationship == null)
			result += "0";
		else if(isknownrelationship)
			result += "+1";
		else
			result += "-1";
		
		for(int i = 0; i < contextvectors.length; i++)
			result += " " + (i+1) + ":" + formatter.format(contextvectors[i]);
		
		return result;
	}
 	
 	public static String buildOutputLineFromVector(int label, double[] contextvectors)
 	{
 		if(label == -1)
 			return buildOutputLineFromVector(false, contextvectors);
 		else if(label == 1)
 			return buildOutputLineFromVector(true, contextvectors);
 		else if(label == 0)
 			return buildOutputLineFromVector(null, contextvectors);

		return null;
 	}
	
 	
 	public static ArrayList<String[]> getLinesAssociatedWithOneFile(String desiredfilename, BufferedReader in, ZipFile zipfile, ZipEntry entry)
 	{
 		ArrayList<String[]> resultlines = new ArrayList<String[]>();
 		
 		
 		try
 		{
 			try{in.ready();}
 			catch(IOException e){in = new BufferedReader(new InputStreamReader(zipfile.getInputStream(entry)));}
 			
 			
 			String line;
 			while((line = in.readLine()) != null)
 			{
				if(line.startsWith("###") && line.endsWith("###"))
				{
					String currentfilename = line.substring(3, line.length()-3);
					if(currentfilename.equals(desiredfilename))
					{
						while(!(line = in.readLine()).equals(""))
							resultlines.add(line.split(" "));
						break;
					}
				}
 			}
 			
 			if(resultlines.size() == 0)
 			{
 				in.close();
	    		in = new BufferedReader(new InputStreamReader(zipfile.getInputStream(entry)));
	 			while((line = in.readLine()) != null)
	 			{
					if(line.startsWith("###") && line.endsWith("###"))
					{
						String currentfilename = line.substring(3, line.length()-3);
						if(currentfilename.equals(desiredfilename))
						{
							while(!(line = in.readLine()).equals(""))
								resultlines.add(line.split(" "));
							break;
						}
					}
	 			}
 			}
 		}catch(IOException e)
 		{
 			System.out.println(e);
 			e.printStackTrace();
 			System.exit(3);
 		}
 		
 		return resultlines;
 	}
	
	
}
