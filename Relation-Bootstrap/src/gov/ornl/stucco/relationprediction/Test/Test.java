package gov.ornl.stucco.relationprediction.Test;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
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
import gov.ornl.stucco.relationprediction.CyberEntityText;
import gov.ornl.stucco.relationprediction.GenericCyberEntityTextRelationship;
import gov.ornl.stucco.relationprediction.ObjectRank;
import gov.ornl.stucco.relationprediction.ProducedFileGetter;
import gov.ornl.stucco.relationprediction.Vulnerability;
import gov.ornl.stucco.relationprediction.WordToVectorMap;
import gov.ornl.stucco.entity.CyberEntityAnnotator.CyberAnnotation;
import gov.ornl.stucco.entity.CyberEntityAnnotator.CyberConfidenceAnnotation;
import gov.ornl.stucco.entity.CyberEntityAnnotator.CyberEntityMentionsAnnotation;

//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;


public class Test 
{
	
	public static void main(String[] args) throws Exception
	{
		writeLemmatizedExample();
		
		//printVulnerabilityNames();
		
		//writeSerGzToText();
		
		//findExamples();
		
		//countInstancesInDifferentClasses();
		
		//testZipReadingAndWriting();
		
		//testCurrentWorkingDirectory();
		
		//findNearestWords();
		
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
		
		
		for(String testword : wvm.keySet())
		{
			if(!testword.startsWith("["))
				continue;
			//if(testword.equals("hamburger") || testword.equals("house") || testword.equals("cat") || testword.equals("boat") || testword.equals("malware"))
			
			ArrayList<ObjectRank> rankedwords = wvm.findNearestWords(testword);
		
			System.out.print(testword);
			for(int i = 1; i < rankedwords.size() && i < 20; i++)
				System.out.print("\t" + rankedwords.get(i).obj + "(" + rankedwords.get(i).value + ")");
			System.out.println();
			
		}
	}
	
	private static void testCurrentWorkingDirectory() throws Exception
	{
		String userdir = System.getProperty("user.dir");
		System.out.println("user.dir\t" + userdir);

		String userhome = System.getProperty("user.home");
		System.out.println("user.home\t" + userhome);
		
		File thing = new File(Test.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		System.out.println("thing\t" + thing.getAbsolutePath());
		
		while(!thing.getParentFile().getName().equals("relation-bootstrap"))
			thing = thing.getParentFile();
		thing = thing.getParentFile();
		
		System.out.println("thing2\t" + thing.getAbsolutePath());
	}

	private static void testZipReadingAndWriting() throws FileNotFoundException, IOException
	{
		String document = "This is a\ntest document to see\nif we can read\nzip files.\n";
		
		
		
		//File outputfile = ProducedFileGetter.getTemporaryFile("tmp");
		File outputfile = new File("tmp.zip");
		
		
		
		FileOutputStream dest = new FileOutputStream(outputfile);
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
		
		out.putNextEntry(new ZipEntry("somefilename"));
		out.write(document.getBytes());
		out.closeEntry();
		
		out.close();
		dest.close();
		
		
		
		ZipFile zipfile = new ZipFile(outputfile);
	    Enumeration<? extends ZipEntry> entries = zipfile.entries();
	    while(entries.hasMoreElements())
	    {
	        ZipEntry entry = entries.nextElement();
	        BufferedReader in = new BufferedReader(new InputStreamReader(zipfile.getInputStream(entry)));
	        
	        String line;
	        while((line = in.readLine()) != null)
	        	System.out.println(line);
	        in.close();
	    }
	    zipfile.close();
	}
	
	private static void countInstancesInDifferentClasses() throws IOException
	{
		DecimalFormat formatter = new DecimalFormat(".0");
		
		
		File testdirectory = ProducedFileGetter.getEntityExtractedSerializedDirectory(false);
		int totaldocuments = testdirectory.listFiles().length;
		System.out.println("Total Documents: " + totaldocuments);
		
		
		//System.out.println("Relationship Name\tInstance Frequency Per Document\tFrequency of Labeled Instances that are Positive\tFrequency of Unknown Instances");
		System.out.println("Relation Name\tPositive Count\tNegative Count\tUnknown Count");
		
		
		for(int positiverelationtype : GenericCyberEntityTextRelationship.allpositiverelationshiptypesset)
		{
			int positivecount = 0;
			int negativecount = 0;
			int unknowncount = 0;
			
			
			File relationinstancesfile = ProducedFileGetter.getRelationshipSVMInstancesFile("aliasreplaced", "a", positiverelationtype, false);
			
			BufferedReader in = new BufferedReader(new FileReader(relationinstancesfile));
			String line;
			while((line = in.readLine()) != null)
			{
				String[] splitline = line.split(" ");
				
				int label = Integer.parseInt(splitline[0]);
				if(label == 1)
					positivecount++;
				else if(label == -1)
					negativecount++;
				else if(label == 0)
					unknowncount++;
				else
					System.out.println("Error: invalid label: " + label);
			}
			in.close();
			
			
			relationinstancesfile = ProducedFileGetter.getRelationshipSVMInstancesFile("aliasreplaced", "a", -positiverelationtype, false);
			
			in = new BufferedReader(new FileReader(relationinstancesfile));
			while((line = in.readLine()) != null)
			{
				String[] splitline = line.split(" ");
				
				int label = Integer.parseInt(splitline[0]);
				if(label == 1)
					positivecount++;
				else if(label == -1)
					negativecount++;
				else if(label == 0)
					unknowncount++;
				else
					System.out.println("Error: invalid label: " + label);
			}
			in.close();
			
			
			int totalcount = positivecount + negativecount + unknowncount;
			int labeledcount = positivecount + negativecount;
			System.out.print(GenericCyberEntityTextRelationship.relationshipidTorelationshipname.get(positiverelationtype));
			
			
			System.out.println("\t" + positivecount + "\t" + negativecount + "\t" + unknowncount);
			
			
			/*
			if(totaldocuments > 0)	//Frequency per document of all instances.
				System.out.print("\t" + formatter.format(totalcount * 1. / totaldocuments));
			else
				System.out.print("\t0.0");
			
			if(labeledcount > 0)	//Frequency of positive instances / labeled instances.
				System.out.print("\t" + formatter.format(positivecount * 100. / labeledcount) + "%");
			else
				System.out.print("\t?");
			
			if(totalcount > 0)	//Frequency of unknown instances / all instances.
				System.out.print("\t" + formatter.format(unknowncount * 100. / totalcount) + "%");
			else
				System.out.print("\t?");
			System.out.println();
			*/
		}
	}
	
	private static void findExamples() throws IOException
	{
		File f = ProducedFileGetter.getRelationshipSVMInstancesFile("original", "111", 1, false);
		
		BufferedReader in = new BufferedReader(new FileReader(f));
		String line;
		while((line = in.readLine()) != null)
		{
			if(line.startsWith("0"))
				continue;
			if(line.contains("Trump"))
				continue;
			if(line.contains("Twitter"))
				continue;
			
			System.out.println(line.substring(0, 2) + line.substring(line.indexOf('#')));
		}
		in.close();
	}
	
	private static void writeSerGzToText() throws IOException
	{
		File directory = new File("/Users/p5r/git/relation-bootstrap/DataFiles/Training/EntityExtractedSerialized/");
		File outputdirectory = new File("/Users/p5r/Downloads/sergztexts/");
		outputdirectory.mkdirs();
		
		
		for(File f : directory.listFiles())
		{
			if(!f.getName().endsWith(".ser.gz"))
				continue;
			
			
			String exampleText = getExampleTextFromSerGz(f);
			
			
			File g = new File(outputdirectory, f.getName());
			PrintWriter out = new PrintWriter(new FileWriter(g));
			out.println(exampleText);
			out.close();
		}
	}
	
	private static String getExampleTextFromSerGz(File f)
	{
		String result = "";
		
			Annotation deserDoc = EntityLabeler.deserializeAnnotatedDoc(f.getAbsolutePath());
			List<CoreMap> sentences = deserDoc.get(SentencesAnnotation.class);
			for (int sentencenum = 0; sentencenum < sentences.size(); sentencenum++) 
			{
				CoreMap sentence = sentences.get(sentencenum);
				
				List<CoreLabel> labels = sentence.get(TokensAnnotation.class);
				
			 	for (int i = 0; i < labels.size(); i++) 
			 	{
			 		CoreLabel token = labels.get(i);
			 		String tokenstring = token.get(TextAnnotation.class);
			 		result += " " + tokenstring;
			 	}
			 	result = result.trim() + "\n";
			}
			
		return result;
	}
	
	private static void printVulnerabilityNames()
	{
		GenericCyberEntityTextRelationship.loadAllKnownRelationships();
		
		for(String name : Vulnerability.getAllNames())
			System.out.println(name);
	}
	
	private static void writeLemmatizedExample()
	{
		//	String exampleText = "The software developer who inserted a major security flaw into OpenSSL 1.2.4.8, using the file foo/bar/blah.php has said the error was \"quite trivial\" despite the severity of its impact, according to a new report.  The Sydney Morning Herald published an interview today with Robin Seggelmann, who added the flawed code to OpenSSL, the world's most popular library for implementing HTTPS encryption in websites, e-mail servers, and applications. The flaw can expose user passwords and potentially the private key used in a website's cryptographic certificate (whether private keys are at risk is still being determined). This is a new paragraph about Apache Tomcat's latest update 7.0.1.";
		String exampleText = "I walked its dog.";
		//	String exampleText = "Oracle DBRM has vulnerability in ABCD plug-in via abcd.1234 (found on abcd.com).";

		
		Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit, pos, lemma");
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		
	    
	    Annotation document = new Annotation(exampleText);

	    
	    // run all Annotators on this text
	    pipeline.annotate(document);
		
		
		
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	
		for ( CoreMap sentence : sentences) 
		{
			for ( CoreLabel token : sentence.get(TokensAnnotation.class)) 
			{
				//System.out.println(token.get(TextAnnotation.class) + "\t" + token.get(CyberAnnotation.class));
				
				System.out.print(token.get(LemmaAnnotation.class) + " ");
			}
			
			//System.out.println("Entities:\n" + sentence.get(CyberEntityMentionsAnnotation.class));
		
			//System.out.println("Parse Tree:\n" + sentence.get(TreeAnnotation.class));		
		}
	}
}