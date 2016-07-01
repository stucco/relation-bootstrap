package gov.ornl.stucco.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import gov.ornl.stucco.entity.EntityLabeler;
import gov.ornl.stucco.entity.heuristics.CyberHeuristicAnnotator;
import gov.ornl.stucco.entity.heuristics.CyberHeuristicAnnotator.CyberHeuristicAnnotation;
import gov.ornl.stucco.entity.heuristics.CyberHeuristicAnnotator.CyberHeuristicMethodAnnotation;
import gov.ornl.stucco.entity.models.CyberEntityType;
import gov.ornl.stucco.heurstics.utils.FreebaseEntry;
import gov.ornl.stucco.heurstics.utils.FreebaseList;
import gov.ornl.stucco.heurstics.utils.ListLoader;
import gov.ornl.stucco.heurstics.utils.TokenCyberLabelMap;
import gov.ornl.stucco.CyberEntityText;
import gov.ornl.stucco.GenericCyberEntityTextRelationship;
import gov.ornl.stucco.ObjectRank;
import gov.ornl.stucco.WordToVectorMap;
import gov.ornl.stucco.entity.CyberEntityAnnotator.CyberAnnotation;
import gov.ornl.stucco.entity.CyberEntityAnnotator.CyberConfidenceAnnotation;
import gov.ornl.stucco.entity.CyberEntityAnnotator.CyberEntityMentionsAnnotation;

//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;


public class Test 
{
	
	public static void main(String[] args)
	{
		findNearestWords();
		
		//checkWorkingDirectories();
		
		//testLoadingFix();
		
		//readEntityExtractedFiles();
		
		//readJsonTest();
		
		//checkSomeFreebaseFacts();
		
		//testIfFileExists();
		
		//gettingEntitiesTest();
		
		//parsintTest();
	}
	
	private static void parsingTest()
	{
		//	String exampleText = "The software developer who inserted a major security flaw into OpenSSL 1.2.4.8, using the file foo/bar/blah.php has said the error was \"quite trivial\" despite the severity of its impact, according to a new report.  The Sydney Morning Herald published an interview today with Robin Seggelmann, who added the flawed code to OpenSSL, the world's most popular library for implementing HTTPS encryption in websites, e-mail servers, and applications. The flaw can expose user passwords and potentially the private key used in a website's cryptographic certificate (whether private keys are at risk is still being determined). This is a new paragraph about Apache Tomcat's latest update 7.0.1.";
		String exampleText = "Microsoft Windows 7 before SP1 has Sun Java cross-site scripting vulnerability Java SE in file.php (refer to CVE-2014-1234).";
		//	String exampleText = "Oracle DBRM has vulnerability in ABCD plug-in via abcd.1234 (found on abcd.com).";
		EntityLabeler labeler = new EntityLabeler();
		Annotation doc = labeler.getAnnotatedDoc("My Doc", exampleText);
		
		List<CoreMap> sentences = doc.get(SentencesAnnotation.class);
	
		for ( CoreMap sentence : sentences) 
		{
			for ( CoreLabel token : sentence.get(TokensAnnotation.class)) 
			{
				System.out.println(token.get(TextAnnotation.class) + "\t" + token.get(CyberAnnotation.class));
			}
			
			System.out.println("Entities:\n" + sentence.get(CyberEntityMentionsAnnotation.class));
		
			System.out.println("Parse Tree:\n" + sentence.get(TreeAnnotation.class));		
		}
	}
	
	//The map this test gets maps all entity names found somewhere to their entity types, if they had only one unique type wherever they were found.
	private static void gettingEntitiesTest()
	{
		TokenCyberLabelMap tokencyberlabelmap = new TokenCyberLabelMap();
		
		tokencyberlabelmap.loadMap("src/main/resources/dictionaries/token_label_map.ser");
		
		System.out.println(tokencyberlabelmap);
	}
	
	private static void testIfFileExists()
	{
		File f = new File("src/main/resources/dictionaries/token_label_map.ser");
		if(f.exists())
			System.out.println("success");
		else
			System.out.println("failure");
		
	}

	
	private static void checkSomeFreebaseFacts()
	{
		File factsfile = new File("/Users/p5r/Downloads/freebase-easy-14-04-14/facts.txt");
		
		//HashSet<String> allrelationships = new HashSet<String>();
		
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(factsfile));
			String line;
			while((line = in.readLine()) != null)
			{
				String[] splitline = line.split("\t");
				if(splitline.length == 3)
				{
					//String relationshipname = splitline[1];
				
					if(splitline[1].contains("Alias"))
					{
						//if(!allrelationships.contains(relationshipname))
						//{
						//	allrelationships.add(relationshipname);
							System.out.println(line);
						//}
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

	private static void readJsonTest()
	{
		FreebaseList software_developerslist = ListLoader.loadFreebaseList("src/main/resources/dictionaries/software_developers.json", CyberHeuristicAnnotator.SW_VENDOR.toString());
		HashMap<String,ArrayList<String>> developersaliasesToallaliases = getAliasToAllAliases(software_developerslist);
        
		
        FreebaseList softwarelist = ListLoader.loadFreebaseList("src/main/resources/dictionaries/software_info.json", (new CyberEntityType("sw", "info")).toString());
        HashMap<String,ArrayList<String>> softwarealiasesToallaliases = getAliasToAllAliases(softwarelist);
        
        
        FreebaseList operatingsystemslist = ListLoader.loadFreebaseList("src/main/resources/dictionaries/operating_systems.json", CyberHeuristicAnnotator.SW_PRODUCT.toString());
        HashMap<String,ArrayList<String>> operatingsystemsaliasesToallaliases = getAliasToAllAliases(operatingsystemslist);
        
        HashMap<String,ArrayList<String>> allsoftwaresaliasesToaliases = new HashMap<String,ArrayList<String>>();
        HashSet<String> allsoftwaresaliases = new HashSet<String>(softwarealiasesToallaliases.keySet());
        allsoftwaresaliases.addAll(operatingsystemsaliasesToallaliases.keySet());
        for(String alias : allsoftwaresaliases)
        {
        	HashSet<String> allaliasesset = new HashSet<String>();
        	ArrayList<String> holder = softwarealiasesToallaliases.get(alias);
        	if(holder != null)
        		allaliasesset.addAll(holder);
        	holder = operatingsystemsaliasesToallaliases.get(alias);
        	if(holder != null)
        		allaliasesset.addAll(holder);
        	
        	allsoftwaresaliasesToaliases.put(alias, new ArrayList<String>(allaliasesset));
        }
	}
	
	private static HashMap<String,ArrayList<String>> getAliasToAllAliases(FreebaseList fl)
	{
		HashMap<String,ArrayList<String>> aliasToallaliases = new HashMap<String,ArrayList<String>>();
		
        List<FreebaseEntry> flentries = fl.getEntries();
        for(FreebaseEntry entry : flentries)
        {
        	ArrayList<String> allnames = new ArrayList<String>();
        	String name = entry.getName();
        	if(name != null)
        		allnames.add(name.toLowerCase());
        	for(String alias : entry.getAliases())
        		allnames.add(alias.toLowerCase());
        	
        	
        	HashSet<String> allnamesappended = new HashSet<String>(allnames);
        	for(String alias : allnames)
        	{
        		ArrayList<String> namesholder = aliasToallaliases.get(alias);
        		if(namesholder != null)
        			allnamesappended.addAll(namesholder);
        	}
        	ArrayList<String> allnames2 = new ArrayList<String>(allnamesappended);
        	for(String alias : allnames2)
        		aliasToallaliases.put(alias, allnames2);
        }
        
        return aliasToallaliases;
	}

	private static void readEntityExtractedFiles()
	{
		File entityextracteddirectory = new File("/Users/p5r/stuccovm/preprocesseddata");
		
		//PrintWriter out = new PrintWriter(new FileWriter(new File(outputfile)));
		PrintStream out = System.out;
		
		double arbitraryprobabilitythreshold = 0.3; //Any token having this high a probability of belonging to any entity type will be assumed to belong to that entity type.
		
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
			 		
			 		
			 		//If the current token has a different label than the previous token, write the last entity out.
			 		if(!entityfinaltype.equals(currententitystate))
			 		{
			 			if(currententitystate != CyberEntityText.O)
			 			{
			 				String entitystring = "";
			 				for(int j = indexoffirsttokenhavingcurrentstate; j < i; j++)
			 				{
			 					String word = labels.get(j).get(LemmaAnnotation.class);
			 					word = word.toLowerCase();
			 					word = word.replaceAll("_", "-");
			 					entitystring += "_" + word;
			 				}
			 				String entitytypestring = CyberEntityText.entitytypeindexToentitytypename.get(currententitystate);
			 				entitystring = "[" + entitytypestring + entitystring + "]";
			 				
			 				out.print(entitystring + " ");
			 			}
			 			
			 			currententitystate = entityfinaltype;
			 			indexoffirsttokenhavingcurrentstate = i;
			 		}
			 		
			 		if(entityfinaltype == CyberEntityText.O)
			 			out.print( ((String)labels.get(i).get(LemmaAnnotation.class)).toLowerCase() + " ");
			 		
			 		
			 		
			 		/*
			 		 	//Some code I borrowed from elsewhere for handling these annotations.
			 		 	 * 
			 			System.out.println(token.get(TextAnnotation.class) + "\t" + token.get(CyberAnnotation.class) + "\t" + token.get(CyberHeuristicAnnotation.class));

			 			if (token.containsKey(CyberHeuristicMethodAnnotation.class))
			 			{
			 				System.out.println("\t" + token.get(CyberHeuristicMethodAnnotation.class));
			 			
			 				if (token.containsKey(CyberConfidenceAnnotation.class)) 
			 				{
			 					double[] probabilities = token.get(CyberConfidenceAnnotation.class);
			 					for (int i=0; i<probabilities.length; i++) 
			 						System.out.print(probabilities[i] + ", ");
			 			
			 					System.out.println();
			 				}
			 			
			 			System.out.println("Entities:\n" + sentence.get(CyberEntityMentionsAnnotation.class));
			 			
			 			System.out.println("Parse Tree:\n" + sentence.get(TreeAnnotation.class));		
			 			String docDir = "/stucco/docs/";
			 			File dir = new File(docDir);
			 			if (!dir.exists()) 
			 				dir.mkdir();
			 			}
			 		*/
			 	}
			 	
			 	//If the sentence ends on an entity, print the entity.
		 		if(currententitystate != CyberEntityText.O)
		 		{
		 			String entitystring = "";
		 			for(int j = indexoffirsttokenhavingcurrentstate; j < labels.size(); j++)
		 			{
		 				String word = labels.get(j).get(LemmaAnnotation.class);
						word = word.toLowerCase();
	 					word = word.replaceAll("_", "-");
	 					entitystring += "_" + word;
	 				}
	 				String entitytypestring = CyberEntityText.entitytypeindexToentitytypename.get(currententitystate);
	 				entitystring = "[" + entitytypestring + entitystring + "]";
		 				
	 				out.print(entitystring + " ");
		 		}
		 		
		 		out.println();
			}
			
			out.println();
		}
	}
	
	private static void testLoadingFix()
	{
		CyberEntityText a = new CyberEntityText("microsoft", CyberEntityText.SWVENDOR);
		CyberEntityText b = new CyberEntityText("windows", CyberEntityText.SWPRODUCT);
		//CyberEntityText b = new CyberEntityText("safari", CyberEntityText.SWPRODUCT);
		//CyberEntityText b = new CyberEntityText("doors", CyberEntityText.SWPRODUCT);
		
		GenericCyberEntityTextRelationship relationship = new GenericCyberEntityTextRelationship(a, b);
		
		System.out.println(relationship.getRelationType());
		
		System.out.println(relationship.isKnownRelationship());
	}

	private static void checkWorkingDirectories()
	{
		System.out.println(System.getProperty("user.dir"));
		
		String currentdirectorypath = System.getProperty("user.dir");
		File currentdirectory = new File(currentdirectorypath);
		File shareddirectory = new File(currentdirectory.getParent(), "producedfiles");
		
		System.out.println(shareddirectory.getAbsoluteFile());
	}
	
	private static void findNearestWords()
	{
		WordToVectorMap wvm = WordToVectorMap.getWordToVectorMap("aliasreplaced");
		
		String testword = "[sw.product_flash_player]";
		ArrayList<ObjectRank> rankedwords = wvm.findNearestWords(testword);
		
		for(ObjectRank or : rankedwords)
			System.out.println(or.obj + "\t" + or.value);
	}
}
