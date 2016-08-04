/*
 * The purpose of this program is to write lemmatized versions of the wikipedia articles with spaces 
 * between tokens and one sentence per line given the files written by the Wikipedia Extractor.  
 * The resulting files should be similar in format to the files that get written by PrintPreprocessedDocuments
 * except that there will be no annotated cyber entities.  
 * The files will be used for training a word2vec model.
 */

package gov.ornl.stucco.relationprediction;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;


public class WriteLemmatizedWikipediaArticlesFile 
{
	//Directory on processing virtual machine containing wikipedia articles that have been extracted using Wikipedia Extractor (https://github.com/bwbaugh/wikipedia-extractor)
	//Changed this file from a global constant to a command line argument.  So now the directory containing the wikipedia articles should be the first command line argument.
	//private static File wikiarticlesdirectory = new File("/data/p5r/extracted/AA");

	private static long articlesperfile = 100000;
	
	
	public static void main(String[] args) throws IOException
	{	
		File wikiarticlesdirectory = new File(args[0]);
		
		//Properties for the automatic annotation process.
		Properties props = new Properties();
		//props.setProperty("ssplit.eolonly", "true");
		//props.setProperty("tokenize.whitespace", "true");
		//props.setProperty("ssplit.newlineIsSentenceBreak", "always");
		props.put("annotators", "tokenize, ssplit, pos, lemma");  
		edu.stanford.nlp.pipeline.StanfordCoreNLP pipeline = new edu.stanford.nlp.pipeline.StanfordCoreNLP(props);
	
		
		long articlecounter = 0;
		int outputfilescounter = 0;
		long sentencecounter = 0;
		File outputfile;
		FileOutputStream dest;
		ZipOutputStream out = null;
		
		
		
		File[] wikiarticles = wikiarticlesdirectory.listFiles();
		for(int i = 0; i < wikiarticles.length; i++)
		{
			File f = wikiarticles[i];
		
			BufferedReader in = new BufferedReader(new FileReader(f));
			
			String documenttext;
			while((documenttext = readNextDocumentText(in)) != null)
			{
				if(articlecounter++ % articlesperfile == 0)
				{
					if(out != null)
					{
						out.closeEntry();
						out.close();
					}
					
					outputfile = ProducedFileGetter.getLemmatizedWikipediaFile(++outputfilescounter);
					dest = new FileOutputStream(outputfile);
					out = new ZipOutputStream(new BufferedOutputStream(dest));
					out.putNextEntry(new ZipEntry("somearticles"));
				}
				
				
				// create an empty Annotation just with the given text
		    	Annotation document = new Annotation(documenttext);
		    	
		    	// run all Annotators on this text
		    	pipeline.annotate(document);
		    
		    	// This is Stanford CoreNLP's representation of all the sentences in the article.
		    	List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		    	for(CoreMap sentence: sentences) 
		    	{
		    		List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
		    		
		    		//There is some stuff in our articles having a second character of : (e.g. "file : Elitch Gardens Logo.png")
		    		//that are not sentences, so ignore these.  Also ignore 1 word sentences while we're at it because they
		    		//cannot affect the model.
		    		if(tokens.size() < 2 || tokens.get(1).get(LemmaAnnotation.class).equals(":"))
						continue;
		    		
		    		sentencecounter++;
						
		    		for (CoreLabel token: tokens) 
		    		{
		    			//This is the token's lemma.
		    			String lemma = token.get(LemmaAnnotation.class);
		    			
		    			out.write((lemma.toLowerCase() + " ").getBytes());
		    		}
		    		out.write("\n".getBytes());	//Get ready to start the next sentence on a new line.
		    	}
		    	out.write("\n".getBytes());	//Print a blank line at the end of the article.
			}
			
			in.close();
		}
		out.closeEntry();
		out.close();
		
		
		System.out.println("Total Articles: " + articlecounter);
		System.out.println("Total Sentences: " + sentencecounter);
	}

	
	private static String readNextDocumentText(BufferedReader in) throws IOException
	{
		String wholearticlestring = "";
		
		String line;
		
		try
		{
			while(!(line = in.readLine()).equals("</doc>"))
			{
				if(!line.startsWith("<doc id="))
					wholearticlestring += line + "\n";
			}
		}catch(NullPointerException e)
		{
			return null;
		}
		
		return wholearticlestring.trim();
	}
}
