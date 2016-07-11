package gov.ornl.stucco.relationprediction.yml;

import java.util.Iterator;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


public class YmlFileEntry 
{
	private static HashMap<String,String> sourcetypeToymltype = new HashMap<String,String>();
	static
	{
		sourcetypeToymltype.put("RSS", "RSS");
		sourcetypeToymltype.put("rss", "RSS");
		sourcetypeToymltype.put("Atom", "RSS");
		sourcetypeToymltype.put("atom", "RSS");
		sourcetypeToymltype.put("web", "WEB");
		sourcetypeToymltype.put("file", "FILEBYLINE");
	}
	
	private String name;
	private String url;
	private String sourcetype;
	private String category;
	
	
	YmlFileEntry(JSONObject obj)
	{
		name = (String)obj.get("name");
		
		url = (String)obj.get("url");
		
		//obj.get("location");
		
		sourcetype = (String)obj.get("type");
		
		//obj.get("description");
		
		//JSONArray tags = (JSONArray)obj.get("tags");
		
		//obj.get("investigating");
		
		//JSONArray nodes = (JSONArray)obj.get("nodes");
		
		category = (String)obj.get("category");
		
		//obj.get("priority");
	}
	
	
	public boolean isWorkingType()
	{
		if(!category.equals("news") && !category.equals("articles") && !category.equals("vendor alerts"))
			return false;
		
		return true;
	}
	
	public String getYmlType()
	{
		return sourcetypeToymltype.get(sourcetype);
	}
	
	/*
	public String getStructuredOrUnstructured()
	{
		return "structured";
	}
	*/
	
	public String getContentType()
	{
		if(url.contains(".pdf"))
			return "text/pdf";
		else if(url.contains(".txt"))
			return "text/plain";
		else if(url.contains(".csv"))
			return "text/csv";
		
		return "text/html";
	}
	
	public boolean isZipped()
	{
		if(url.endsWith(".gz") || url.endsWith(".zip"))
			return true;
		return false;
	}
	
	public String toYmlString()
	{
		String result = "";
        
		result += "          type: " + getYmlType() + "\n";
        //result += "          data-type: " + getStructuredOrUnstructured() + "\n";
		result += "          data-type: unstructured\n";	//Kelly says we only want unstructured documents.  Also there is no extractor for structured documents running on the virtual machine.
        result += "          source-name: " + name + "\n";
        if(isZipped())
        	result += "          post-process: unzip\n";
        result += "          source-URI: " + url + "\n";
        result += "          content-type : " + getContentType() + "\n";
        result += "          now-collect: all\n";
        result += "          cron: 0 40 * * * ?";
        
        return result;
	}
	
	
	
}