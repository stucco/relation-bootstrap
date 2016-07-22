package gov.ornl.stucco.relationprediction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;


public class CyberEntityText 
{
	public static final int SWPRODUCT = 0;
	public static final int VUDESCRIPTION = 1;
	public static final int O = 2;
	public static final int SWVERSION = 3;
	public static final int FUNAME = 4;
	public static final int SWVENDOR = 5;
	public static final int VUNAME = 6;
	public static final int VUCVE = 7;
	public static final int VUMS = 8;
	public static final int FINAME = 9;
	public static final int ENTITYTYPECOUNT = 10;
	
	public static HashMap<String,Integer> entitytypenameToentitytypeindex = new HashMap<String,Integer>();
	static
	{
		entitytypenameToentitytypeindex.put("sw.product", SWPRODUCT);
		entitytypenameToentitytypeindex.put("sw.version", SWVERSION);
		entitytypenameToentitytypeindex.put("vuln.description", VUDESCRIPTION);
		entitytypenameToentitytypeindex.put("function.name", FUNAME);
		entitytypenameToentitytypeindex.put("sw.vendor", SWVENDOR);
		entitytypenameToentitytypeindex.put("vu.name", VUNAME);
		entitytypenameToentitytypeindex.put("vu.cve", VUCVE);
		entitytypenameToentitytypeindex.put("vu.ms", VUMS);
		entitytypenameToentitytypeindex.put("file.name", FINAME);
		entitytypenameToentitytypeindex.put("O", O);
	}
	
	public static HashMap<Integer,String> entitytypeindexToentitytypename = new HashMap<Integer,String>();
	static
	{
		entitytypeindexToentitytypename.put(SWPRODUCT, "sw.product");
		entitytypeindexToentitytypename.put(SWVERSION, "sw.version");
		entitytypeindexToentitytypename.put(VUDESCRIPTION, "vuln.description");
		entitytypeindexToentitytypename.put(FUNAME, "function.name");
		entitytypeindexToentitytypename.put(SWVENDOR, "sw.vendor");
		entitytypeindexToentitytypename.put(VUNAME, "vuln.name");
		entitytypeindexToentitytypename.put(VUCVE, "vuln.cve");
		entitytypeindexToentitytypename.put(VUMS, "vuln.ms");
		entitytypeindexToentitytypename.put(FINAME, "file.name");
		entitytypeindexToentitytypename.put(O, "O");
	}
	
	
	private String entitytext;
	private int entitytype;
	
	
	
	//The entity learner outputs predictions for each token belonging to each entity type as a probability array.  
	//These are the entity types the indices of the probability array correspond to (discovered experimentally).
	public static HashMap<Integer,String> probabilityindexToentitytypestring = new HashMap<Integer,String>();
	static
	{
		probabilityindexToentitytypestring.put(0,"sw.product");
		probabilityindexToentitytypestring.put(1,"vuln.description");
		probabilityindexToentitytypestring.put(2,"O");
		probabilityindexToentitytypestring.put(3,"sw.version");
		probabilityindexToentitytypestring.put(4,"function.name");
		probabilityindexToentitytypestring.put(5,"sw.vendor");
	}
	
	public static HashMap<String,Integer> entitytypestringToprobabilityindex = new HashMap<String,Integer>();
	static
	{
		entitytypestringToprobabilityindex.put("sw.product", 0);
		entitytypestringToprobabilityindex.put("vuln.description", 1);
		entitytypestringToprobabilityindex.put("O", 2);
		entitytypestringToprobabilityindex.put("sw.version", 3);
		entitytypestringToprobabilityindex.put("function.name", 4);
		entitytypestringToprobabilityindex.put("sw.vendor", 5);
	}
	
	public static HashMap<Integer,Integer> probabilityindexToentitytypeindex = new HashMap<Integer,Integer>();
	static
	{
		probabilityindexToentitytypeindex.put(0, SWPRODUCT);
		probabilityindexToentitytypeindex.put(1, VUDESCRIPTION);
		probabilityindexToentitytypeindex.put(2, O);
		probabilityindexToentitytypeindex.put(3, SWVERSION);
		probabilityindexToentitytypeindex.put(4, FUNAME);
		probabilityindexToentitytypeindex.put(5, SWVENDOR);
	}
	
	
	public CyberEntityText(String entitytext, int entitytype)
	{
		this.entitytext = entitytext;
		this.entitytype = entitytype;
	}
	
	public int getEntityType()
	{
		return entitytype;
	}
	
	public String getEntityText()
	{
		return entitytext;
	}

	public String getEntitySpacedText()
	{
		return getEntitySpacedText(entitytext);
	}
	
	public static String getEntitySpacedText(String entitytext)
	{
		if(entitytext.startsWith("["))
		{
			String result = entitytext.toLowerCase();
			if(result.contains("_"))
			{
				result = result.substring(result.indexOf('_')+1, result.length()-1);
				result = result.replaceAll("_", " ");
				result = result.trim();
				
				return result;
			}
		}

		
		return entitytext;
	}
	
	//Return the best label according to the probability array unless the best label is O.
	//If the best label is O, return the second best label if its value exceeds Othreshold.
	//If the second best label's value does not exceed Othreshold, return O.
	public static int getTypeOfHighestProbabilityIndex(double[] probabilities, double Othreshold)
	{
		ArrayList<ObjectRank> ors = new ArrayList<ObjectRank>();
		for(int i = 0; i < probabilities.length; i++)
			ors.add(new ObjectRank(i, probabilities[i]));
		Collections.sort(ors);
		
		ObjectRank bestrankedprob = ors.get(ors.size()-1);
		int bestentitytype = probabilityindexToentitytypeindex.get((Integer)bestrankedprob.obj);
		if(bestentitytype != O)
			return bestentitytype;
		else
		{
			ObjectRank secondbestrankedprob = ors.get(ors.size()-2);
			if(secondbestrankedprob.value >= Othreshold)
			{
				int secondbestentitytype = probabilityindexToentitytypeindex.get((Integer)secondbestrankedprob.obj);
				return secondbestentitytype;
			}
			else
				return O;
		}
	}

	
	//If the token looks like a cyber entity token, return a CyberEntityText.  Otherwise, return null.
	public static CyberEntityText getCyberEntityTextFromToken(String token)
	{
		int entitytype = getEntityTypeFromToken(token);
		
		if(entitytype == O)
			return null;
		else
			return new CyberEntityText(token, entitytype);
	}
	
	//Tokens from preprocessed documents are formatted in a certain way.  Particularly, if the token 
	//is a cyber entity (as detected by the cyber entity detector), it will start with 
	//[entity.type_word1_word2...].  If the token is not a cyber entity, there is no special formatting.
	//This method returns the constant integer associated with the given entity's type, provided it is
	//a cyber entity.  Otherwise, if it is not formatted like a cyber entity, it returns type O (the 
	//non-entity type).
	public static int getEntityTypeFromToken(String token)
	{
		if(token.charAt(0) != '[')
			return O;
		
		int indexoffirstspace = token.indexOf('_');
		if(indexoffirstspace == -1 && token.charAt(token.length()-1) == ']')
			indexoffirstspace = token.length()-1;
		
		if(indexoffirstspace <= 0)
			return O;
		
		String tokenlabel = token.substring(1, indexoffirstspace);
		Integer result = entitytypenameToentitytypeindex.get(tokenlabel);
		
		if(result == null)
			return O;
		
		return result;
	}
	
	
	public static String getCanonicalName(String name, int entitytype)
	{
		ArrayList<Vulnerability> vulnerabilities;
		String result = name;
		String holder;
		
		  switch (entitytype) 
		  {
		  	case O:  
		  		break;
		  	case FUNAME:
		  		break;
		  	case FINAME:
		  		break;
		  	case VUCVE:
		  		break;
		  	case VUMS:
		  		vulnerabilities = Vulnerability.getVulnerabilitiesWithMSid(name);
		  		if(vulnerabilities.size() == 1)
		  			result = vulnerabilities.get(0).getCanonicalName();
		  		break;
		  	case VUNAME:
		  		vulnerabilities = Vulnerability.getVulnerabilitiesWithName(name);
		  		if(vulnerabilities.size() == 1)
		  			result = vulnerabilities.get(0).getCanonicalName();
	  			break;
		  	case VUDESCRIPTION:
		  		break;
		  	case SWPRODUCT:  
		  		holder = SoftwareWVersion.getCanonicalSoftwareAlias(name);
		  		if(holder != null)
		  			result = holder;
		  		break;
		  	case SWVENDOR:
		  		holder = SoftwareWVersion.getCanonicalVendorAlias(name);
		  		if(holder != null)
		  			result = holder;
		  		break;
		  	case SWVERSION:
		  		break;
		  	default: 
		  		System.err.println("Invalid Entity Type: " + entitytype);
		  		break;
		  }
		  
		 return result;
	}
	
	public String toString()
	{
		return entitytext;
	}

}
