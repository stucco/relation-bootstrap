package gov.ornl.stucco.relationprediction.yml;

/*
 * This program's purpose is to write the configure .yml file needed for the collector to collect
 * unlabeled documents.  It takes as input the json file of sources available at 
 * https://github.com/stucco/data/blob/master/data/sources.json
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import gov.ornl.stucco.relationprediction.ProducedFileGetter;

import java.io.FileWriter;


public class WriteYmlFileFromSources 
{
	private static File outputfile = new File("/Users/p5r/stuccovm/collectors.yml");
	
	
	public static void main(String[] args) throws IOException
	{
		PrintWriter out = new PrintWriter(new FileWriter(outputfile));
		
		printYmlFileBeginning(out);
		
		ArrayList<YmlFileEntry> ymlfileentries = constructYmlFileEntriesFromSource();
		
		writeSources(out, ymlfileentries);
		
		//printYmlFileEnding(out);
		
		out.close();
	}

	
	private static void printYmlFileBeginning(PrintWriter out) throws IOException
	{
		out.println("default:"
				+ "\n  rabbitmq:"
				+ "\n    host: localhost"
				+ "\n    port: 5672"
				+ "\n    vhost: /"
				+ "\n    exchange: stucco"
				+ "\n    queue: stucco"
				+ "\n    login: stucco"
				+ "\n    password: stucco"
				+ "\n    message_size_limit: 10485760"
				+ "\n  stucco:"
				+ "\n    document-service:"
				+ "\n      host: localhost"
				+ "\n      port: 8118"
				+ "\n"
				+ "\n  demo:"
				+ "\n    collectors:");
	}
	
	private static void printYmlFileEnding(PrintWriter out) throws IOException
	{
		out.println("vagrant:"
			+ "\n  rabbitmq:"
			+ "\n  host: localhost");
	}
	
	
	private static ArrayList<YmlFileEntry> constructYmlFileEntriesFromSource()
	{
		ArrayList<YmlFileEntry> result = new ArrayList<YmlFileEntry>();
		
		//HashSet<String> typesset = new HashSet<String>();
		//HashSet<String> tagsset = new HashSet<String>();
		//HashSet<String> nodesset = new HashSet<String>();
		//HashSet<String> categoriesset = new HashSet<String>();
		
		JSONParser parser = new JSONParser();
		try 
		{
			JSONArray array = (JSONArray)parser.parse(new FileReader(ProducedFileGetter.getSourcesJSONFile()));

			Iterator<JSONObject> iterator = array.iterator();
			while(iterator.hasNext())
			{
				JSONObject obj = iterator.next();
				
				result.add(new YmlFileEntry(obj));
				
				/*
				String name = (String)obj.get("name");
				
				String url = (String)obj.get("url");
				
				//obj.get("location");
				
				String type = (String)obj.get("type");
				typesset.add(type);
				
				//obj.get("description");
				
				JSONArray tags = (JSONArray)obj.get("tags");
				Iterator tagiterator = tags.iterator();
				while(tagiterator.hasNext())
					tagsset.add((String)tagiterator.next());
				
				//obj.get("investigating");
				
				JSONArray nodes = (JSONArray)obj.get("nodes");
				Iterator nodeiterator = nodes.iterator();
				while(nodeiterator.hasNext())
					nodesset.add((String)nodeiterator.next());
				
				String category = (String)obj.get("category");
				categoriesset.add(category);
				
				//obj.get("priority");
				 */
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		//System.out.println("types:\t" + typesset);
		//System.out.println("tags:\t" + tagsset);
		//System.out.println("nodes:\t" + nodesset);
		//System.out.println("categories:\t" + categoriesset);
		
		return result;
	}
	
	
	private static void writeSources(PrintWriter out, ArrayList<YmlFileEntry> entries)
	{
		int counter = 0;
		
		for(YmlFileEntry entry : entries)
		{
			if(entry.isWorkingType())
			{
				out.println("        -");
				out.println(entry.toYmlString(counter));
				counter++;
			}
		}
		
		System.out.println("Working Sources: " + counter);
	}
	
}