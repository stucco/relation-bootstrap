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
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;

public class WriteRelationInstanceFiles
{
	public static String[] validcontexts = {"001", "010", "011", "100", "101", "110", "111"};
	
	private static String entityextractedfilename;
	//private static String contexts;
	private static boolean training = false;
	
	private static HashMap<Integer,HashMap<String,PrintWriter>> relationtypeTocontextToprintwriter;
	
	
	public static void main(String[] args)
	{
		readArgs(args);
		
		//Read in the saved word vectors
		WordToVectorMap wvm = WordToVectorMap.getWordToVectorMap(entityextractedfilename);
		
		initializePrintWriters();
		
		buildAndWriteTrainingInstances(wvm);
		
		//Close the file streams.
		for(HashMap<String,PrintWriter> contextToprintwriter : relationtypeTocontextToprintwriter.values())
			for(PrintWriter pw : contextToprintwriter.values())
				pw.close();
	}
	
	//Arguments: 
	//1. extractedfilename (This is the name of the file written by PrintPreprocessedDocuments.  
	//Valid known values for this argument are "original", "entityreplaced", and "aliasreplaced")
	//2. contexts (This argument tells us whether or not we want to use the context
	//preceding the first entity (first digit), whether or not we want to use the context between entities (second digit),
	//and whether or not we want to use the context after the second entity (third digit).  Valid
	//values for it are 000, 001, 010, 011, 100, 101, 110, or 111).
	private static void readArgs(String[] args)
	{
		entityextractedfilename = args[0];

		for(int i = 1; i < args.length; i++)
		{
			if("training".equals(args[i]))
				training = true;
		}
	}
	
	private static void initializePrintWriters()
	{
		relationtypeTocontextToprintwriter = new HashMap<Integer,HashMap<String,PrintWriter>>();
		
		try
		{
			for(String context : validcontexts)
			{
				for(Integer i : GenericCyberEntityTextRelationship.getAllRelationshipTypesSet())
				{
					File f = ProducedFileGetter.getRelationshipSVMInstancesFile(entityextractedfilename, context, i, training);
					
					HashMap<String,PrintWriter> contextToprintwriter = relationtypeTocontextToprintwriter.get(i);
					if(contextToprintwriter == null)
					{
						contextToprintwriter = new HashMap<String,PrintWriter>();
						relationtypeTocontextToprintwriter.put(i, contextToprintwriter);
					}
					contextToprintwriter.put(context, new PrintWriter(new FileWriter(f)));
				}
			}
		}catch(IOException e)
		{
			System.out.println(e);
			e.printStackTrace();
			System.exit(3);
		}
	}
	
	private static void buildAndWriteTrainingInstances(WordToVectorMap wvm)
	{
		try
		{
			//Read the appropriate text file chosen as a command line argument.
			//BufferedReader in = new BufferedReader(new FileReader(ProducedFileGetter.getEntityExtractedText(entityextractedfilename)));

			//We are switching to using zip files for these because they could potentially be very big.
			ZipFile zipfile = new ZipFile(ProducedFileGetter.getEntityExtractedText(entityextractedfilename, training));
			ZipEntry entry = zipfile.entries().nextElement();
		    BufferedReader in = new BufferedReader(new InputStreamReader(zipfile.getInputStream(entry)));
	
		
			//The original text should align exactly, token-for-token, with either of the other two text types.
			//BufferedReader aliasalignedin = new BufferedReader(new FileReader(ProducedFileGetter.getEntityExtractedText("original")));
			
		    //We are switching to using zip files for these because they could potentially be very big.
			ZipFile zipfile2 = new ZipFile(ProducedFileGetter.getEntityExtractedText("aliasreplaced", training));
			ZipEntry entry2 = zipfile2.entries().nextElement();
		    BufferedReader aliasreplacedalignedin = new BufferedReader(new InputStreamReader(zipfile2.getInputStream(entry2)));
			
		    //We are switching to using zip files for these because they could potentially be very big.
			ZipFile zipfile3 = new ZipFile(ProducedFileGetter.getEntityExtractedText("unlemmatized", training));
			ZipEntry entry3 = zipfile3.entries().nextElement();
			BufferedReader unlemmatizedalignedin = new BufferedReader(new InputStreamReader(zipfile3.getInputStream(entry3)));
			
			
			int linecounter = 0;	//Keep track of the number of the line we are on in the file.  We'll make a note of what line our instances come from in this way.
			
			//Each line in this file is a single sentence.  Relationships can only be extracted if both 
			//participating entities appear in the same sentence.  We do not do coreference resolution,
			//so each entity must be mentioned by name (it cannot be replaced with a pronoun).
			String line;
			while((line = in.readLine()) != null)
			{
				//Read the corresponding line from the alias replaced text file.
				String aliasedline = aliasreplacedalignedin.readLine();
				String unlemmatizedline = unlemmatizedalignedin.readLine();
				
				
				linecounter++;
				
				
				//The text files contain a blank line between documents.  Just ignore them.
				if(line.length() == 0)
					continue;
				
	
				//Recall that, in the input file, a cyber entity is compressed into one token,
				//even though in the source text, it may have been several tokens long.
				String[] tokens = line.split(" ");
				
				
				//And keep track of the alias replaced tokens the normal tokens correspond to.
				String[] aliasreplacedtokens = aliasedline.split(" ");
				String[] unlemmatizedtokens = unlemmatizedline.split(" ");
				
				
				//The text file we are reading is already annotated with cyber entities.
				//Here, we are constructing an array of CyberEntityTexts parallel to 
				//the tokens array that tells us the token's Cyber entity label (if it has one).
				CyberEntityText[] cyberentitytexts = new CyberEntityText[tokens.length];
				CyberEntityText[] aliasedcyberentitytexts = new CyberEntityText[tokens.length];
				CyberEntityText[] unlemmatizedcyberentitytexts = new CyberEntityText[tokens.length];
				for(int i = 0; i < tokens.length; i++)
				{
					cyberentitytexts[i] = CyberEntityText.getCyberEntityTextFromToken(tokens[i]);
					aliasedcyberentitytexts[i] = CyberEntityText.getCyberEntityTextFromToken(aliasreplacedtokens[i]);
					unlemmatizedcyberentitytexts[i] = CyberEntityText.getCyberEntityTextFromToken(unlemmatizedtokens[i]);
				}
				
				//Now, we scan through each pair of tokens.  If both are cyber entities...
				for(int i = 0; i < tokens.length; i++)
				{
					if(cyberentitytexts[i] != null)
					{
						for(int j = i+1; j < tokens.length; j++)
						{
							if(cyberentitytexts[j] != null)
							{
								//...we construct a relationship instance from them.
								GenericCyberEntityTextRelationship relationship = new GenericCyberEntityTextRelationship(cyberentitytexts[i], cyberentitytexts[j]);
								GenericCyberEntityTextRelationship aliasedrelationship = new GenericCyberEntityTextRelationship(aliasedcyberentitytexts[i], aliasedcyberentitytexts[j]);
								GenericCyberEntityTextRelationship unlemmatizedrelationship = new GenericCyberEntityTextRelationship(unlemmatizedcyberentitytexts[i], unlemmatizedcyberentitytexts[j]);
								
								
								//Any pair of entities of any types can be used to construct a relationship instance.
								//But we only care about a subset of all possible relationship types.  In particular, 
								//we care about the relationships ennumerated at the top of GenericCyberEntityTextRelationship
								//(or their reverses, where the entities appear in the reverse of the normal order
								//in the text).  So check if this entity pair's relationship type is one of the 
								//types we care about.  Since we only built print writers for the types we cared about 
								//earlier in the program, we check for this condition by checking if we constructed a 
								//PrintWriter for this relationship.
								Integer relationtype = aliasedrelationship.getRelationType();
								
								
								HashMap<String,PrintWriter> contextToprintwriter = relationtypeTocontextToprintwriter.get(relationtype);
								if(contextToprintwriter != null)
								{
									//If we do care about this relationship type, check whether
									//we can label this relationship confidently enough to
									//use it as a training instance.
									//(There is a comment above GenericCyberEntityTextRelationship.isKnownRelationship()
									//explaining our rules for making this determination.)
									//Use the aliased version of the relationship to check for this because
									//its contents are easiest to align with known entities.
									//Boolean isknownrelationship = relationship.isKnownRelationship();
									Boolean isknownrelationship = aliasedrelationship.isKnownRelationship();
									if(!(training && isknownrelationship == null))	//Do not bother to write instances with 0 labels.  isknownrelationship == null if the label would be 0 (we don't know the label).
									{	
											//Make a list of tokens in this context, then
											//take the average of their corresponding vectors.
											//Add the resulting vector to the concatenatedvector we are using 
											//for our feature representation.
											String[] context1 = Arrays.copyOfRange(tokens, 0, i);
											double[] context1vector = wvm.getContextVector(context1);
										
											//Context between the entities.
											String[] context2 = Arrays.copyOfRange(tokens, i+1, j);
											double[] context2vector = wvm.getContextVector(context2);
									
											//Context after the entities.
											String[] context3 = Arrays.copyOfRange(tokens, j, tokens.length);
											double[] context3vector = wvm.getContextVector(context3);
									
									
											for(String context : validcontexts)
											{
												PrintWriter pw = contextToprintwriter.get(context);
											

												//Construct the context
												//vectors using the tokens in the appropriate context windows and
												//the word vectors.  Concatenate all the chosen vectors into one
												//feature vecture which we will use to represent the instance.
												ArrayList<Double> concatenatedvectors = new ArrayList<Double>();
												if(context.charAt(0) == '1')
													for(double value : context1vector)
														concatenatedvectors.add(value);
												if(context.charAt(1) == '1')
													for(double value : context2vector)
														concatenatedvectors.add(value);
												if(context.charAt(2) == '1')
													for(double value : context3vector)
														concatenatedvectors.add(value);
											
											
												//Now, actually build the SVM_light style string representation of the instance.
												String instanceline = buildSVMLightLine(isknownrelationship, concatenatedvectors);
										
										
												//SVM_light format allows us to add comments to the end of lines.  So to make the line more human-interpretable,
												//add the entity names to the end of the line.
												if(training)
													instanceline += " # " + linecounter;
												else
													instanceline += " # " + linecounter + " " + i + " " + unlemmatizedrelationship.getFirstEntity().getEntityText() + " " + relationship.getFirstEntity().getEntityText() + " " + j + " " + unlemmatizedrelationship.getSecondEntity().getEntityText() + " " + relationship.getSecondEntity().getEntityText() + " " + unlemmatizedline;
										
										
												//And finally, print it to the appropriate file using the PrintWriter we 
												//made earlier.
												pw.println(instanceline);
											}
										//}
									}
								}
							}
						}
					}
				}
			}
			in.close();
			aliasreplacedalignedin.close();
			unlemmatizedalignedin.close();
			zipfile.close();
			zipfile2.close();
			zipfile3.close();
		}catch(IOException e)
		{
			System.out.println(e);
			e.printStackTrace();
			System.exit(3);
		}
	}
	
	
	//This method takes a proposed relationship and a feature vector representation.  It checks 
	public static String buildSVMLightLine(Boolean isknownrelationship, ArrayList<Double> contextvectors)
	{
		String result = "";
		if(isknownrelationship == null)
			result += "0";
		else if(isknownrelationship)
			result += "+1";
		else
			result += "-1";
		
		for(int i = 0; i < contextvectors.size(); i++)
			result += " " + (i+1) + ":" + contextvectors.get(i);
		
		return result;
	}
	
	
	
}
