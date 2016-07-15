/*
 * This program looks at all the .ser.gz files in the directory ProducedFileGetter.getEntityExtractedSerializedDirectory()
 * and produces several text files from them with one sentence per line.  The text files use the .ser.gz file supplied
 * tokenization, lemmatization, and entity annotations to produce three text files.  
 * One contains a tokenized, lemmatized
 * version of the text wherein the entities have been replaced with a token representing their predicted entity types.  
 * Another contains a tokenized, lemmatized version wherein the entities have been replaced with a token that includes information about
 * their predicted type and their original text. 
 * The third contains a tokenized, lemmatized version wherein we try to match the entity-labeled text segments with their canonical,
 * Freebase names by matching the entity text against Freebase aliases.  The entities are replaced with a token indicating 
 * their predicted entity type and canonical name.
 */

package gov.ornl.stucco.relationprediction;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Sentence;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetBeginAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.CharacterOffsetEndAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import gov.ornl.stucco.entity.EntityLabeler;
import gov.ornl.stucco.entity.CyberEntityAnnotator.CyberConfidenceAnnotation;
import gov.ornl.stucco.entity.heuristics.CyberHeuristicAnnotator.CyberHeuristicAnnotation;
import gov.ornl.stucco.entity.heuristics.CyberHeuristicAnnotator.CyberHeuristicMethodAnnotation;

public class PrintPreprocessedDocuments 
{
	//Any token having this high a probability of belonging to any non-O entity type will be assumed to belong to that entity type,
	//unless some other non-O entity type has a higher probability.  Otherwise, the token will be assigned to the type
	//having the highest predicted probability.
	public static double arbitraryprobabilitythreshold = 0.3; 
	
	//We read files from and write files to a different place if our goal is to train a new model.  In production, our goal will be only to make predictions, so we will not bother with any training.
	private static boolean training = false;
	
	
	public static void main(String[] args) throws IOException
	{
		readArgs(args);
		
		
		//Check whether there are any available files to work with.
		boolean foundsergz = false;
		for(File f : ProducedFileGetter.getEntityExtractedSerializedDirectory(training).listFiles())
		{
			if(f.getName().endsWith(".ser.gz"))
				foundsergz = true;
		}
		if(!foundsergz)
		{
			System.out.println("Error: No .ser.gz files found in " + ProducedFileGetter.getEntityExtractedSerializedDirectory(training).getAbsolutePath() + " .  Please place .ser.gz files that have been processed by the entity extractor here before running this program.");
			System.exit(3);
		}
		
		
		//Open printwriters for each of the three text file types mentioned at the beginning of this .java file.  We get all
		//files from ProducedFileGetter to help maintain consistency between locations used by different programs.
		//PrintWriter aliassubstitutednamesout = new PrintWriter(new FileWriter(ProducedFileGetter.getEntityExtractedText("aliasreplaced")));
		//PrintWriter completelyreplacednamesout = new PrintWriter(new FileWriter(ProducedFileGetter.getEntityExtractedText("entityreplaced")));
		//PrintWriter originalnamesout = new PrintWriter(new FileWriter(ProducedFileGetter.getEntityExtractedText("original")));
		
		//Since there may be issues with sharing files as big as these might turn out to be through github, we are zipping them instead of just writing them in plain text format.
		ZipOutputStream aliassubstitutednamesout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(ProducedFileGetter.getEntityExtractedText("aliasreplaced", training))));
		aliassubstitutednamesout.putNextEntry(new ZipEntry("data"));

		ZipOutputStream completelyreplacednamesout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(ProducedFileGetter.getEntityExtractedText("entityreplaced", training))));
		completelyreplacednamesout.putNextEntry(new ZipEntry("data"));

		ZipOutputStream originalnamesout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(ProducedFileGetter.getEntityExtractedText("original", training))));
		originalnamesout.putNextEntry(new ZipEntry("data"));

		ZipOutputStream unlemmatizednamesout = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(ProducedFileGetter.getEntityExtractedText("unlemmatized", training))));
		unlemmatizednamesout.putNextEntry(new ZipEntry("data"));
		
		
		//Process each serialized file.
		for(File f : ProducedFileGetter.getEntityExtractedSerializedDirectory(training).listFiles())
		{
			if(!f.getName().endsWith(".ser.gz"))
				continue;
			
			
			Annotation deserDoc = EntityLabeler.deserializeAnnotatedDoc(f.getAbsolutePath());
			List<CoreMap> sentences = deserDoc.get(SentencesAnnotation.class);
			for ( CoreMap sentence : sentences) 
			{
				List<CoreLabel> labels = sentence.get(TokensAnnotation.class);
				if(labels.size() == 0)
					continue;
				
				int currententitystate = CyberEntityText.O;
				int indexoffirsttokenhavingcurrentstate = 0;
			 	for (int i = 0; i < labels.size(); i++) 
			 	{
			 		CoreLabel token = labels.get(i);
			 		Integer entityfinaltype = null;
			 		
			 		
			 		//If the token was labeled by the heuristic annotator or was labeled with a high enough confidence
			 		//(see the note above arbitraryprobabilitythreshold for details), assign it the appropriate label.
			 		if (token.containsKey(CyberHeuristicMethodAnnotation.class))
			 		{
			 			entityfinaltype = CyberEntityText.entitytypenameToentitytypeindex.get(token.get(CyberHeuristicAnnotation.class).toString());
			 		}
			 		else if(token.containsKey(CyberConfidenceAnnotation.class)) 
			 		{
			 			double[] probabilities = token.get(CyberConfidenceAnnotation.class);
			 			entityfinaltype = CyberEntityText.getTypeOfHighestProbabilityIndex(probabilities, arbitraryprobabilitythreshold);
			 		}
			 		
			 		
			 		//Sometimes, due to the probability threshold I set, the automatic entity labeler makes dumb, correctable decisions. 
			 		//This method heuristically corrects some of these errors.
			 		entityfinaltype = resetEntityFinalTypeHeuristically(entityfinaltype, token);
			 		
			 		
			 		//If the current token has a different label than the previous token, write the last entity out.
			 		if(!entityfinaltype.equals(currententitystate))
			 		{
				 		if(currententitystate != CyberEntityText.O)
				 		{
				 			String entitytypestring = CyberEntityText.entitytypeindexToentitytypename.get(currententitystate);
				 			
				 			//Below are the places where we write the three different versions of the text file.
				 			//Print the "original" version of the text (described above)
				 			String originalentitystring = "";
				 			for(int j = indexoffirsttokenhavingcurrentstate; j < i; j++)
				 			{
				 				String word = labels.get(j).get(TextAnnotation.class);
				 				word = replaceWordSpecialCharacters(word);
				 				originalentitystring += " " + word;
				 			}
				 			originalentitystring = originalentitystring.trim();
				 			originalentitystring = "[" + entitytypestring + "_" + originalentitystring.replaceAll(" ", "_") + "]";
				 			originalnamesout.write((originalentitystring + " ").getBytes());
				 			
				 			unlemmatizednamesout.write((originalentitystring + " ").getBytes());
				 			
				 			//Print the alias replaced version of the text as described above.
				 			String aliasreplacedentitystring = "";
				 			for(int j = indexoffirsttokenhavingcurrentstate; j < i; j++)
				 			{
				 				String lemma = labels.get(j).get(LemmaAnnotation.class);
				 				lemma = replaceWordSpecialCharacters(lemma).toLowerCase();
				 				aliasreplacedentitystring += " " + lemma;
				 			}
				 			aliasreplacedentitystring = aliasreplacedentitystring.trim();
				 			aliasreplacedentitystring = CyberEntityText.getCanonicalName(aliasreplacedentitystring, currententitystate);
				 			aliasreplacedentitystring = "[" + entitytypestring + "_" + aliasreplacedentitystring.replaceAll(" ", "_") + "]";
				 			aliassubstitutednamesout.write((aliasreplacedentitystring + " ").getBytes());
				 			
				 			//Replace the entity's text with its type.
				 			completelyreplacednamesout.write(("[" + entitytypestring + "] ").getBytes());
				 		}
			 			
			 			currententitystate = entityfinaltype;
			 			indexoffirsttokenhavingcurrentstate = i;
			 		}
			 		
			 		//If the current token was not labeled as any kind of cyber entity, just print it ('s lemma) out.
			 		if(entityfinaltype == CyberEntityText.O)
			 		{
			 			aliassubstitutednamesout.write( (((String)labels.get(i).get(LemmaAnnotation.class)).toLowerCase() + " ").getBytes());
			 			completelyreplacednamesout.write( (((String)labels.get(i).get(LemmaAnnotation.class)).toLowerCase() + " ").getBytes());
			 			originalnamesout.write( (((String)labels.get(i).get(LemmaAnnotation.class)).toLowerCase() + " ").getBytes());
			 			unlemmatizednamesout.write( (((String)labels.get(i).get(TextAnnotation.class)) + " ").getBytes());
			 		}
			 	}
			 	
			 	//If the sentence ends on an entity, print the entity.  This code does the same things as in the above loop,
			 	//only the loop does not process an entity if it comes at the end of a sentence.  So handle that situation.
		 		if(currententitystate != CyberEntityText.O)
		 		{
		 			String entitytypestring = CyberEntityText.entitytypeindexToentitytypename.get(currententitystate);
		 			
		 			//Only adjust the entity's formatting.
		 			String originalentitystring = "";
		 			for(int j = indexoffirsttokenhavingcurrentstate; j < labels.size(); j++)
		 			{
		 				String word = labels.get(j).get(TextAnnotation.class);
		 				word = replaceWordSpecialCharacters(word);
		 				originalentitystring += " " + word;
		 			}
		 			originalentitystring = originalentitystring.trim();
		 			originalentitystring = "[" + entitytypestring + "_" + originalentitystring.replaceAll(" ", "_") + "]";
		 			originalnamesout.write((originalentitystring + " ").getBytes());

		 			unlemmatizednamesout.write((originalentitystring + " ").getBytes());
		 			
		 			//Replace the entity with its main alias if available.
		 			String aliasreplacedentitystring = "";
		 			for(int j = indexoffirsttokenhavingcurrentstate; j < labels.size(); j++)
		 			{
		 				String lemma = labels.get(j).get(LemmaAnnotation.class);
		 				lemma = replaceWordSpecialCharacters(lemma).toLowerCase();
		 				aliasreplacedentitystring += " " + lemma;
		 			}
		 			aliasreplacedentitystring = aliasreplacedentitystring.trim();
		 			aliasreplacedentitystring = CyberEntityText.getCanonicalName(aliasreplacedentitystring, currententitystate);
		 			aliasreplacedentitystring = "[" + entitytypestring + "_" + aliasreplacedentitystring.replaceAll(" ", "_") + "]";
		 			aliassubstitutednamesout.write((aliasreplacedentitystring + " ").getBytes());
		 			
		 			//Completely replace the entity with the name of its type.
		 			completelyreplacednamesout.write(("[" + entitytypestring + "] ").getBytes());
		 		}
		 		
		 		aliassubstitutednamesout.write("\n".getBytes());
		 		completelyreplacednamesout.write("\n".getBytes());
		 		originalnamesout.write("\n".getBytes());
		 		
		 		unlemmatizednamesout.write("\n".getBytes());
			}
			
			aliassubstitutednamesout.write("\n".getBytes());
			completelyreplacednamesout.write("\n".getBytes());
			originalnamesout.write("\n".getBytes());
			
			unlemmatizednamesout.write("\n".getBytes());
		}

		aliassubstitutednamesout.closeEntry();
		aliassubstitutednamesout.close();
		completelyreplacednamesout.closeEntry();
		completelyreplacednamesout.close();
		originalnamesout.closeEntry();
		originalnamesout.close();
		unlemmatizednamesout.closeEntry();
		unlemmatizednamesout.close();
	}
	
	private static void readArgs(String[] args)
	{
		for(int i = 0; i < args.length; i++)
		{
			if("training".equals(args[i]))
				training = true;
		}
	}
	
	private static int resetEntityFinalTypeHeuristically(int predictedtype, CoreLabel token)
	{
		int result = predictedtype;
		
		if(predictedtype == CyberEntityText.SWVERSION)
		{
			String lemma = token.get(LemmaAnnotation.class);
			if(lemma.equals(".") || lemma.equals("version"))
				result = CyberEntityText.O;
		}
		
		return result;
	}
	
	private static String replaceWordSpecialCharacters(String word)
	{
		word = word.replaceAll("_", "~");
		word = word.replaceAll("\\[", "(");
		word = word.replaceAll("\\]", ")");
		
		return word;
	}

}
