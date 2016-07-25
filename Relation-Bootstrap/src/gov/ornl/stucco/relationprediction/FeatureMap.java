package gov.ornl.stucco.relationprediction;

import java.util.*;
import java.io.*;

public class FeatureMap extends HashMap<String,Integer>
{
	//Warning: word embeddings only generate valid features for instances where both entities occur in the same sentence.
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

	//This method reads all the training instance files associated with a particular entityextracted type and each feature type
	//and maps each feature to a unique integer in a new FeatureMap.
	//Warning: It is very important that the same training instances are written to entityextracted when the feature map
	//is first constructed (in Run RelationSVMs
	public static FeatureMap constructFeatureMap(String entityextractedfilename, String featuretypes, int relationtype)
	{
		FeatureMap featuremap = new FeatureMap();
		
		for(int i = 0; i < featuretypes.length(); i++)
		{
			String featuretype = featuretypes.charAt(i) + "";
			File relationinstancesfile = ProducedFileGetter.getRelationshipSVMInstancesFile(entityextractedfilename, featuretype, relationtype, true);
			
			try
			{
				BufferedReader in = new BufferedReader(new FileReader(relationinstancesfile));
				String instanceline;
				while((instanceline = in.readLine()) != null)
				{
					if(PrintPreprocessedDocuments.isLineBetweenDocuments(instanceline) || PrintPreprocessedDocuments.isFileNameLine(instanceline))
						continue;
					
					
					String[] splitline = instanceline.split("#");
					String[] classAndfeatures = splitline[0].trim().split(" ");
					for(int j = 1; j < classAndfeatures.length; j++)
					{
						String featurenameAndvalue = classAndfeatures[j];
						String featurename = featurenameAndvalue.substring(0, featurenameAndvalue.lastIndexOf(':'));
						//double value = Double.parseDouble(featurenameAndvalue.substring(featurenameAndvalue.lastIndexOf(':')+1));
						featurename = featuretype + ":" + featurename;
						
						featuremap.getIndex(featurename, featuretype);	//Just by getting the index, we add the feature to the map.  So don't need to do anything with it.
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
		
		return featuremap;
	}

}