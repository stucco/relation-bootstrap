package gov.ornl.stucco;

import java.io.File;
import java.io.PrintStream;
import java.util.List;
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
	public static File entityextracteddirectory = new File("/Users/p5r/stuccovm/preprocesseddata");
	public static File aliassubstitutedentitynamesoutputfile = new File("/Users/p5r/stuccovm/preprocesseddata/aliassubstitutedentitynamesprocesseddocuments");
	public static File originalentitynamesoutputfile = new File("/Users/p5r/stuccovm/preprocesseddata/originalentitynamesprocesseddocuments");
	public static File completelyreplacedentitynamesoutputfile = new File("/Users/p5r/stuccovm/preprocesseddata/completelyreplacedentitynamesprocesseddocuments");
	
	public static double arbitraryprobabilitythreshold = 0.3; //Any token having this high a probability of belonging to any entity type will be assumed to belong to that entity type.
	
	
	public static void main(String[] args) throws IOException
	{
		GenericCyberEntityTextRelationship.loadAllKnownRelationships();
		
		
		PrintWriter aliassubstitutednamesout = new PrintWriter(new FileWriter(aliassubstitutedentitynamesoutputfile));
		PrintWriter completelyreplacednamesout = new PrintWriter(new FileWriter(completelyreplacedentitynamesoutputfile));
		PrintWriter originalnamesout = new PrintWriter(new FileWriter(originalentitynamesoutputfile));
		
		
		for(File f : entityextracteddirectory.listFiles())
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
			 		
			 		if (token.containsKey(CyberHeuristicMethodAnnotation.class))
			 		{
			 			entityfinaltype = CyberEntityText.entitytypenameToentitytypeindex.get(token.get(CyberHeuristicAnnotation.class).toString());
			 		}
			 		else if(token.containsKey(CyberConfidenceAnnotation.class)) 
			 		{
			 			double[] probabilities = token.get(CyberConfidenceAnnotation.class);
			 			entityfinaltype = CyberEntityText.getTypeOfHighestProbabilityIndex(probabilities, arbitraryprobabilitythreshold);
			 		}
			 		
			 		
			 		//Sometimes, due to the probability threshold I set, the automatic entity labeler makes dumb, correctable decisions.  So correct them.
			 		entityfinaltype = resetEntityFinalTypeHeuristically(entityfinaltype, token);
			 		
			 		
			 		//If the current token has a different label than the previous token, write the last entity out.
			 		if(!entityfinaltype.equals(currententitystate))
			 		{
				 		if(currententitystate != CyberEntityText.O)
				 		{
				 			String entitytypestring = CyberEntityText.entitytypeindexToentitytypename.get(currententitystate);
				 			
				 			//Only adjust the entity's formatting.
				 			String originalentitystring = "";
				 			for(int j = indexoffirsttokenhavingcurrentstate; j < i; j++)
				 			{
				 				String word = labels.get(j).get(TextAnnotation.class);
				 				word = replaceWordSpecialCharacters(word);
				 				originalentitystring += " " + word;
				 			}
				 			originalentitystring = originalentitystring.trim();
				 			originalentitystring = "[" + entitytypestring + "_" + originalentitystring.replaceAll(" ", "_") + "]";
				 			originalnamesout.print(originalentitystring + " ");
				 			
				 			//Replace the entity with its main alias if available.
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
				 			aliassubstitutednamesout.print(aliasreplacedentitystring + " ");
				 			
				 			//Completely replace the entity with the name of its type.
				 			completelyreplacednamesout.print("[" + entitytypestring + "] ");
				 		}
			 			
			 			currententitystate = entityfinaltype;
			 			indexoffirsttokenhavingcurrentstate = i;
			 		}
			 		
			 		if(entityfinaltype == CyberEntityText.O)
			 		{
			 			aliassubstitutednamesout.print( ((String)labels.get(i).get(LemmaAnnotation.class)).toLowerCase() + " ");
			 			completelyreplacednamesout.print( ((String)labels.get(i).get(LemmaAnnotation.class)).toLowerCase() + " ");
			 			originalnamesout.print( ((String)labels.get(i).get(LemmaAnnotation.class)).toLowerCase() + " ");
			 		}
			 	}
			 	
			 	//If the sentence ends on an entity, print the entity.
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
		 			originalnamesout.print(originalentitystring + " ");
		 			
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
		 			aliassubstitutednamesout.print(aliasreplacedentitystring + " ");
		 			
		 			//Completely replace the entity with the name of its type.
		 			completelyreplacednamesout.print("[" + entitytypestring + "] ");
		 		}
		 		
		 		aliassubstitutednamesout.println();
		 		completelyreplacednamesout.println();
		 		originalnamesout.println();
			}
			
			aliassubstitutednamesout.println();
			completelyreplacednamesout.println();
			originalnamesout.println();
		}

		aliassubstitutednamesout.close();
		completelyreplacednamesout.close();
		originalnamesout.close();
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
