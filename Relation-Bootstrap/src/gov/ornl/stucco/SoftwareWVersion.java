package gov.ornl.stucco;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gov.ornl.stucco.entity.heuristics.CyberHeuristicAnnotator;
import gov.ornl.stucco.entity.models.CyberEntityType;
import gov.ornl.stucco.heurstics.utils.FreebaseEntry;
import gov.ornl.stucco.heurstics.utils.FreebaseList;
import gov.ornl.stucco.heurstics.utils.ListLoader;

import java.util.ArrayList;
import java.util.Collection;


public class SoftwareWVersion 
{
	private static String softwarevendorsfilelocation = "src/main/resources/dictionaries/software_developers.json";
	private static String softwareinfofilelocation = "src/main/resources/dictionaries/software_info.json";
	private static String operatingsystemsfilelocation = "src/main/resources/dictionaries/operating_systems.json";
	
	
	
	private String softwareid;
	private String vendor;
	private String name;
	private String version;
	
	private ArrayList<VulnerabilityToSoftwareWVersionRelationship> relationships;
	
	private static HashMap<String,ArrayList<String>> softwarealiasToaliases;
	private static HashMap<String,ArrayList<String>> vendoraliasToaliases;
	private static HashSet<String> allknownversions = new HashSet<String>();

	private static HashMap<String,SoftwareWVersion> softwareidTosoftwarewversion = new HashMap<String,SoftwareWVersion>();
	
	private static HashMap<String,HashSet<String>> softwarealiasTosoftwarewversionids = new HashMap<String,HashSet<String>>();
	
	
	private SoftwareWVersion(String softwareid, String vendor, String name, String version)
	{
		this.softwareid = softwareid;
		this.vendor = vendor;
		this.name = name;
		this.version = version;
	}
	
	
	public String getSoftwareID()
	{
		return softwareid;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getVendor()
	{
		return vendor;
	}
	
	public String getVersion()
	{
		return version;
	}
	
	public ArrayList<String> getSoftwareAliases()
	{
		return softwarealiasToaliases.get(name);
	}
	
	public ArrayList<String> getVendorAliases()
	{
		return vendoraliasToaliases.get(vendor);
	}
	
	public void addRelationship(VulnerabilityToSoftwareWVersionRelationship relationship)
	{
		if(relationships == null)
			relationships = new ArrayList<VulnerabilityToSoftwareWVersionRelationship>();
		
		relationships.add(relationship);
	}
	
	public ArrayList<VulnerabilityToSoftwareWVersionRelationship> getRelationships()
	{
		return relationships;
	}
	
	
	//If we have already created a SoftwareWVersion instance having this software id, get it from the softwareidTosoftwarewversion map and return it.  Else create it, add it to the map, then return it.
	public static SoftwareWVersion getSoftwareFromSoftwareID(String softwareid)
	{
		SoftwareWVersion result = softwareidTosoftwarewversion.get(softwareid);
		
		if(result == null)
		{
			String[] splitsoftwareid = softwareid.split(":");
			String vendor = null;
			String name = null;
			String version = null;

			if(splitsoftwareid.length >= 3)
				vendor = splitsoftwareid[2].replaceAll("_", " ");
			if(splitsoftwareid.length >= 4)
				name = splitsoftwareid[3].replaceAll("_", " ");
			if(splitsoftwareid.length >= 5)
				version = splitsoftwareid[4];
			
			result = new SoftwareWVersion(softwareid, vendor, name, version);
			
			softwareidTosoftwarewversion.put(softwareid, result);
			
			if(version != null)
				allknownversions.add(version);
		}
		
		return result;
	}

	public static Collection<SoftwareWVersion> getAllSoftwareWVersions()
	{
		return softwareidTosoftwarewversion.values();
	}


	public static void setAllAliases()
	{	
		FreebaseList software_developerslist = ListLoader.loadFreebaseList(softwarevendorsfilelocation, CyberHeuristicAnnotator.SW_VENDOR.toString());
		vendoraliasToaliases = getAliasToAllAliases(software_developerslist);
		
		for(SoftwareWVersion swv : SoftwareWVersion.getAllSoftwareWVersions())
		{
			ArrayList<String> vendoraliases = swv.getVendorAliases();
			if(swv.getVendorAliases() == null)
			{
				vendoraliases = new ArrayList<String>();
				vendoraliases.add(swv.getVendor());
				vendoraliasToaliases.put(swv.getVendor(), vendoraliases);
			}
		}
		
			
		softwarealiasToaliases = readSoftwaresAliases();
		
		for(SoftwareWVersion swv : SoftwareWVersion.getAllSoftwareWVersions())
		{
			ArrayList<String> softwarealiases = swv.getSoftwareAliases();
			if(swv.getSoftwareAliases() == null)
			{
				softwarealiases = new ArrayList<String>();
				softwarealiases.add(swv.getName());
				softwarealiasToaliases.put(swv.getName(), softwarealiases);
			}
			
			for(String alias : softwarealiases)
			{
				HashSet<String> softwarewversionids = softwarealiasTosoftwarewversionids.get(alias);
				if(softwarewversionids == null)
				{
					softwarewversionids = new HashSet<String>();
					softwarealiasTosoftwarewversionids.put(alias, softwarewversionids);
				}
				softwarewversionids.add(swv.getSoftwareID());
			}
		}
	}
	
	private static HashMap<String,ArrayList<String>> readSoftwaresAliases()
	{
        FreebaseList softwarelist = ListLoader.loadFreebaseList(softwareinfofilelocation, (new CyberEntityType("sw", "info")).toString());
        HashMap<String,ArrayList<String>> softwarealiasesToallaliases = getAliasToAllAliases(softwarelist);
        
        FreebaseList operatingsystemslist = ListLoader.loadFreebaseList(operatingsystemsfilelocation, CyberHeuristicAnnotator.SW_PRODUCT.toString());
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
        
        return allsoftwaresaliasesToaliases;
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
        	
        	//Put the freebase name first.
        	if(allnames2.remove(name))
        		allnames2.add(0, name);
        	////Put the longest alias first.
        	//if(allnames2.size() > 0)
        	//{
        	//	int longestlength = -1;
        	//	int longestlengthindex = -1;
        	//	for(int i = 0; i < allnames2.size(); i++)
        	//	{
        	//		int ilength = allnames2.get(i).length();
        	//		if(ilength > longestlength)
        	//		{
        	//			longestlength = ilength;
        	//			longestlengthindex = i;
        	//		}
        	//	}
        	//	String longestalias = allnames2.remove(longestlengthindex);
        	//	allnames2.add(0, longestalias);
        	//}
        }
        
        return aliasToallaliases;
	}

	public static ArrayList<SoftwareWVersion> getAllSoftwaresWithAlias(String alias)
	{
		ArrayList<SoftwareWVersion> result = new ArrayList<SoftwareWVersion>();
		
		HashSet<String> versionids = softwarealiasTosoftwarewversionids.get(alias);
		if(versionids != null)
		{
			for(String versionid : versionids)
			{
				SoftwareWVersion swv = softwareidTosoftwarewversion.get(versionid);
				if(swv != null)
					result.add(swv);
			}
		}
		
		return result;
	}
	
	public static String getCanonicalVendorAlias(String alias)
	{
		ArrayList<String> vendoraliases = vendoraliasToaliases.get(alias);
		if(vendoraliases != null && vendoraliases.size() > 0)
			return vendoraliases.get(0);
		
		return null;
	}
	
	public static String getCanonicalSoftwareAlias(String alias)
	{
		ArrayList<String> softwarealiases = softwarealiasToaliases.get(alias);
		if(softwarealiases != null && softwarealiases.size() > 0)
			return softwarealiases.get(0);
		
		return null;
	}

	public static Set<String> getAllVendorAliases()
	{
		return vendoraliasToaliases.keySet();
	}
	
	public static Set<String> getAllProductAliases()
	{
		return softwarealiasToaliases.keySet();
	}
	
	public static Set<String> getAllVersions()
	{
		return allknownversions;
	}
}
