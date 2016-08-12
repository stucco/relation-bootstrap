//Instances of this class store information about a particular software product version.  So it includes the product's
//name, its vendor, and its version number.
package gov.ornl.stucco.relationprediction;

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
	private static HashSet<String> allknownversions;

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
		return getsoftwarealiasToaliases().get(name);
	}
	
	public ArrayList<String> getVendorAliases()
	{
		return getvendoraliasToaliases().get(vendor);
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
		if(softwareidTosoftwarewversion == null)
			softwareidTosoftwarewversion = new HashMap<String,SoftwareWVersion>();
		
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
			{
				if(allknownversions == null)
					allknownversions = new HashSet<String>();
				
				allknownversions.add(version);
			}
		}
		
		return result;
	}

	public static Collection<SoftwareWVersion> getAllSoftwareWVersions()
	{
		if(softwareidTosoftwarewversion == null)
			GenericCyberEntityTextRelationship.loadAllKnownRelationships();
		
		return softwareidTosoftwarewversion.values();
	}


	public static void setAllAliases()
	{	
		if(vendoraliasToaliases == null)
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
		}
		

		if(softwarealiasTosoftwarewversionids == null)
			softwarealiasTosoftwarewversionids = new HashMap<String,HashSet<String>>();
		if(softwarealiasToaliases == null)
		{
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
        }
        
        return aliasToallaliases;
	}

	public static ArrayList<SoftwareWVersion> getAllSoftwaresWithAlias(String alias)
	{
		if(softwarealiasTosoftwarewversionids == null)
			setAllAliases();
		if(softwareidTosoftwarewversion == null)
			GenericCyberEntityTextRelationship.loadAllKnownRelationships();
		
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
		ArrayList<String> vendoraliases = getvendoraliasToaliases().get(alias);
		if(vendoraliases != null && vendoraliases.size() > 0)
			return vendoraliases.get(0);
		
		return null;
	}
	
	public static String getCanonicalSoftwareAlias(String alias)
	{
		ArrayList<String> softwarealiases = getsoftwarealiasToaliases().get(alias);
		if(softwarealiases != null && softwarealiases.size() > 0)
			return softwarealiases.get(0);
		
		return null;
	}

	public static Set<String> getAllVendorAliases()
	{
		return getvendoraliasToaliases().keySet();
	}
	
	public static Set<String> getAllProductAliases()
	{
		return getsoftwarealiasToaliases().keySet();
	}
	
	public static Set<String> getAllVersions()
	{
		if(allknownversions == null)
			GenericCyberEntityTextRelationship.loadAllKnownRelationships();
		
		return allknownversions;
	}
	

	private static HashMap<String,ArrayList<String>> getsoftwarealiasToaliases()
	{
		if(softwarealiasToaliases == null)
			setAllAliases();
		
		return softwarealiasToaliases;
	}
	
	private static HashMap<String,ArrayList<String>> getvendoraliasToaliases()
	{
		if(vendoraliasToaliases == null)
			setAllAliases();
		
		return vendoraliasToaliases;
	}
	
}
