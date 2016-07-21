package gov.ornl.stucco.relationprediction;

import java.util.*;
import java.io.*;

public class FeatureMap extends HashMap<String,Integer>
{
	//1.
	//Feature Type Codes.  These don't really belong here, but they don't really belong anywhere else either.
	public static final String WORDEMBEDDINGBEFORECONTEXT = "a";	//Context before the first entity.
	public static final String WORDEMBEDDINGBETWEENCONTEXT = "b";	//Context between entities.
	public static final String WORDEMBEDDINGAFTERCONTEXT = "c";	//Context after second entity.
	
	
	
	
	public static final String FEATUREMAPPREFIX = "FeatureMap";
	
	
	private int featurecounter;
	private HashMap<Integer,String> index2feature;
	private HashMap<Integer,String> index2type;
	
	FeatureMap()
	{
		featurecounter = 1;
		index2feature = new HashMap<Integer,String>();
		index2type = new HashMap<Integer,String>();
	}
	
	/*
	FeatureMap(File featuremapfile) throws IOException
	{
		index2feature = new HashMap<Integer,String>();
		index2type = new HashMap<Integer,String>();
		
		BufferedReader in = new BufferedReader(new FileReader(featuremapfile));
		String line;
		while((line = in.readLine()) != null)
		{
			String[] typecolonnameandindex = line.split("	");
			String typecolonname = typecolonnameandindex[0];
			
			int index = Integer.parseInt(typecolonnameandindex[1]);
			String beforecolon = typecolonname.substring(0, typecolonname.indexOf(':'));
			String type;
			if(beforecolon.contains("."))
				type = beforecolon.substring(0, beforecolon.indexOf('.'));
			else
				type = beforecolon;
			String name = typecolonname;
			
			index2feature.put(index, name);
			index2type.put(index, type);
		}
		in.close();
	}
	*/
	
	public int getIndex(String featurename, String featuretype)
	{
		Integer result = get(featurename);
		if(result == null)
		{
			result = featurecounter++;
			put(featurename, result);
			index2feature.put(result, featurename);
			index2type.put(result, featuretype);
		}
		
		return result;
	}
	
	public String getFeature(int index)
	{
		return index2feature.get(index);
	}

	public String getCode(int index)
	{
		return index2type.get(index);
	}

	public HashMap<Integer,String> getIndex2Type()
	{
		return index2type;
	}
	
	public String getType(Integer index)
	{
		return index2type.get(index);
	}

	public static String getOrderedFeatureTypes(String featuretypes)
	{
		HashSet<String> featuretypeset = new HashSet<String>();
		for(int i = 0; i < featuretypes.length(); i++)
			featuretypeset.add(featuretypes.charAt(i) + "");
		ArrayList<String> featuretypelist = new ArrayList<String>(featuretypeset);
		Collections.sort(featuretypelist);
		
		String result = "";
		for(String featuretype : featuretypelist)
			result += featuretype;
		
		return result;
	}
	public static String getCommaSeparatedOrderedFeatureTypes(String featuretypes)
	{
		ArrayList<String> featuretypelist = new ArrayList<String>();
		String[] featuretypesarray = featuretypes.split(",");
		for(String featuretype : featuretypesarray)
			featuretypelist.add(featuretype);
		Collections.sort(featuretypelist);
		
		String result = "";
		for(String featuretype : featuretypelist)
			result += " " + featuretype;
		result = result.trim();
		result = result.replaceAll(" ", ",");
		
		return result;
	}


}