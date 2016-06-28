package gov.ornl.stucco;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;


public class AllKnownDatabaseRelationships
{
	private static File nvdxmldir = new File("src/nvdxml/");	//Location where xml dumps from NVD can be found.  Downloaded from https://nvd.nist.gov/download.cfm
	
	public static final int RT_SWVENDOR_SWPRODUCT = 0;	//sw.vendor & sw.product à same software
	public static final int RT_SWVERSION_SWPRODUCT = 1;	//sw.version & sw.product à same software
	public static final int RT_VUDESCRIPTION_VUNAME = 2;	//vuln.description & vuln.name à same vulnerability
	public static final int RT_VUMS_VUNAME = 3;	//vuln.ms & vuln.name à same vulnerability
	public static final int RT_VUCVE_VUNAME = 4;	//vuln.cve & vuln.name à same vulnerability
	public static final int RT_VUDESCRIPTION_VUMS = 5;	//vuln.description & vuln.ms à same vulnerability
	public static final int RT_VUDESCRIPTION_VUCVE = 6;	//vuln.description & vuln.cve à same vulnerability
	public static final int RT_VUCVE_VUMS = 7;	//vuln.cve & vuln.ms à same vulnerability
	
	//The commented out relationship types are relationships we are interested in, but cannot be found directly in the text.
	//public static final int RT_VU_SW = 8;	//vuln.* & sw.* à ExploitTargetRelatedObservable
	//public static final int RT_SW_FILENAME = 9;	//sw.* & file.name à Sub-Observable
	//public static final int RT_SW_FUNCTIONNAME= 10;	//sw.* & function.name à Sub-Observable
	//public static final int RT_VU_FILENAME = 11;	//vuln.* & file.name à ExploitTargetRelatedObservable
	//public static final int RT_VU_FUNCTIONNAME = 12;	//vuln.* & function.name à ExploitTargetRelatedObservable
	
	//The following relationship types are not directly ones we are interested in, but are necessary for finding the ones above that cannot be found directly in the text.
	//The next group are needed for vuln.* & sw.* à ExploitTargetRelatedObservable
	public static final int RT_SWPRODUCT_VUNAME = 13;	//sw.product & vuln.name
	public static final int RT_SWPRODUCT_VUMS = 14;	//sw.product & vuln.ms
	public static final int RT_SWPRODUCT_VUCVE = 15;	//sw.product & vuln.cve
	public static final int RT_SWVERSION_VUNAME = 16;	//sw.version & vuln.name
	public static final int RT_SWVERSION_VUMS = 17;	//sw.version & vuln.ms
	public static final int RT_SWVERSION_VUCVE = 18;	//sw.version & vuln.cve
	
	//The next group are needed for sw.* & file.name à Sub-Observable
	public static final int RT_SWPRODUCT_FINAME = 19;	//sw.product & file.name
	public static final int RT_SWVERSION_FINAME = 20;	//sw.version & file.name
	
	//The next group are needed for sw.* & function.name à Sub-Observable
	public static final int RT_SWPRODUCT_FUNAME = 21;	//sw.product & function.name
	public static final int RT_SWVERSION_FUNAME = 22;	//sw.version & function.name
	
	//The next group are needed for vuln.* & file.name à ExploitTargetRelatedObservable
	public static final int RT_VUNAME_FINAME = 23;	//vuln.name & file.name
	public static final int RT_VUCVE_FINAME = 24;	//vuln.cve & file.name
	public static final int RT_VUMS_FINAME = 25;	//vuln.ms & file.name
	
	//The next group are needed for vuln.* & function.name à ExploitTargetRelatedObservable
	public static final int RT_VUNAME_FUNAME = 26;	//vuln.name & function.name
	public static final int RT_VUCVE_FUNAME = 27;	//vuln.cve & function.name
	public static final int RT_VUMS_FUNAME = 28;	//vuln.ms & function.name
	
	
	/*
	private static final HashMap<Integer,int[]> relationtypeToorderedentitytypes = new HashMap<Integer,int[]>();
	static
	{
		int[] ints;
		
		ints = {CyberEntityText., CyberEntityText.};
		relationtypeToorderedentitytypes.put(RT_SWVENDOR_SWPRODUCT, ints);
		
		ints = {CyberEntityText., CyberEntityText.};
		relationtypeToorderedentitytypes.put(RT_SWVERSION_SWPRODUCT, ints);
		
		ints = {CyberEntityText., CyberEntityText.};
		relationtypeToorderedentitytypes.put(RT_VUDESCRIPTION_VUNAME, ints);
		
		ints = {CyberEntityText., CyberEntityText.};
		relationtypeToorderedentitytypes.put(RT_VUMS_VUNAME, ints);
		
		ints = {CyberEntityText., CyberEntityText.};
		relationtypeToorderedentitytypes.put(RT_VUCVE_VUNAME, ints);
		
		ints = {CyberEntityText., CyberEntityText.};
		relationtypeToorderedentitytypes.put(RT_VUDESCRIPTION_VUMS, ints);
		
		ints = {CyberEntityText., CyberEntityText.};
		relationtypeToorderedentitytypes.put(RT_VUDESCRIPTION_VUCVE, ints);
		
		ints = {CyberEntityText., CyberEntityText.};
		relationtypeToorderedentitytypes.put(RT_VUCVE_VUMS, ints);
		
		ints = {CyberEntityText., CyberEntityText.};
		relationtypeToorderedentitytypes.put(RT_SWPRODUCT_VUNAME, ints);
		
		ints = {CyberEntityText., CyberEntityText.};
		relationtypeToorderedentitytypes.put(RT_SWPRODUCT_VUMS, ints);
		
		ints = {CyberEntityText., CyberEntityText.};
		relationtypeToorderedentitytypes.put(RT_SWPRODUCT_VUCVE, ints);
		
		ints = {CyberEntityText., CyberEntityText.};
		relationtypeToorderedentitytypes.put(RT_SWVERSION_VUNAME, ints);
		
		ints = {CyberEntityText., CyberEntityText.};
		relationtypeToorderedentitytypes.put(RT_SWVERSION_VUMS, ints);
		
		ints = {CyberEntityText., CyberEntityText.};
		relationtypeToorderedentitytypes.put(RT_SWVERSION_VUCVE, ints);
		
		ints = {CyberEntityText., CyberEntityText.};
		relationtypeToorderedentitytypes.put(RT_SWPRODUCT_FINAME, ints);
		
		ints = {v, CyberEntityText.};
		relationtypeToorderedentitytypes.put(RT_SWVERSION_FINAME, ints);
		
		ints = {CyberEntityText., CyberEntityText.};
		relationtypeToorderedentitytypes.put(RT_SWPRODUCT_FUNAME, ints);
		
		ints = {CyberEntityText., CyberEntityText.};
		relationtypeToorderedentitytypes.put(RT_SWVERSION_FUNAME, ints);
		
		ints = {CyberEntityText., CyberEntityText.};
		relationtypeToorderedentitytypes.put(RT_VUNAME_FINAME, ints);
		
		ints = {CyberEntityText., CyberEntityText.};
		relationtypeToorderedentitytypes.put(RT_VUCVE_FINAME, ints);
		
		ints = {CyberEntityText., CyberEntityText.};
		relationtypeToorderedentitytypes.put(RT_VUMS_FINAME, ints);
		
		ints = {CyberEntityText., CyberEntityText.};
		relationtypeToorderedentitytypes.put(RT_VUNAME_FUNAME, ints);
		
		ints = {CyberEntityText., CyberEntityText.};
		relationtypeToorderedentitytypes.put(RT_VUCVE_FUNAME, ints);
		
		ints = {CyberEntityText., CyberEntityText.};
		relationtypeToorderedentitytypes.put(RT_VUMS_FUNAME, ints);
	}
	*/
	
	
	private static AllKnownDatabaseRelationships theakdr = null;
	
	
	private AllKnownDatabaseRelationships()
	{
		load();
	}
	
	
	
	public static AllKnownDatabaseRelationships getAllKnownDatabaseRelationships()
	{
		if(theakdr == null)
			theakdr = new AllKnownDatabaseRelationships();
		
		return theakdr;
	}
	
	
	//Reads all known entities in from Database
	public void load()
	{
		Vulnerability.setAllDescriptions();
		
		loadRelationshipsFromNVD();
		
		SoftwareWVersion.setAllAliases();
	}
	
	//Loads entities from NVD xml data.  The code is ugly because I have never done anything with xml files before.
	private void loadRelationshipsFromNVD()
	{
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
		
			//NVD does not offer one dump of all its data as one file.  It stores them in separate files by year.  So we have to iterate through them all.
			for(File xmlfile : nvdxmldir.listFiles())
			{
				if(xmlfile.isDirectory())
					continue;
			
				org.w3c.dom.Document document = db.parse(xmlfile);
				org.w3c.dom.Element root = document.getDocumentElement();
				org.w3c.dom.NodeList nodelist = root.getChildNodes();
				for(int i = 0; i < nodelist.getLength(); i++)
				{
					org.w3c.dom.Node node = nodelist.item(i);
					
					//Skip any nodes that are not vulnerability entries.
					if(!node.getNodeName().equals("entry"))
						continue;
					
					
					String cveid = null;
					String msid = null;
					ArrayList<SoftwareWVersion> vulnerablesoftwares = new ArrayList<SoftwareWVersion>();
					ArrayList<String> description = null;
					String name = null;
					HashSet<String> functionnames = new HashSet<String>();
					HashSet<String> filenames = new HashSet<String>();
					
					
					org.w3c.dom.NodeList nodelist2 = node.getChildNodes();
					for(int j = 0; j < nodelist2.getLength(); j++)
					{
						org.w3c.dom.Node node2 = nodelist2.item(j);
						
						if(node2.getNodeName().equals("vuln:cve-id"))
							cveid = node2.getTextContent();
						else if(node2.getNodeName().equals("vuln:references"))
						{
							org.w3c.dom.NodeList nodelist3 = node2.getChildNodes();
							for(int k = 0; k < nodelist3.getLength(); k++)
							{
								org.w3c.dom.Node node3 = nodelist3.item(k);
								if(node3.getNodeName().equals("vuln:reference"))
								{
									String possiblemsid = node3.getTextContent();
									if(possiblemsid.startsWith("MS") && possiblemsid.length() == 8 && possiblemsid.charAt(4) == '-')
										msid = possiblemsid;
								}
							}
						}
						else if(node2.getNodeName().equals("vuln:vulnerable-software-list"))
						{
							org.w3c.dom.NodeList nodelist3 = node2.getChildNodes();
							for(int k = 0; k < nodelist3.getLength(); k++)
							{
								org.w3c.dom.Node node3 = nodelist3.item(k);
								if(node3.getNodeName().equals("vuln:product"))
								{
									String vulnerablesoftwareid = node3.getTextContent();
					
									//All the stuff we want to know about a software is encoded in the software id, so we can build the Software object when we extract the id.
									SoftwareWVersion vulnerablesoftware = SoftwareWVersion.getSoftwareFromSoftwareID(vulnerablesoftwareid);
								
									vulnerablesoftwares.add(vulnerablesoftware);
								}
							}
						}
						else if(node2.getNodeName().equals("vuln:summary"))
						{
							String summary = node2.getTextContent().toLowerCase();
							
							
							//A relevant term counts as a description if it appears in the list of relevant terms at https://github.com/stucco/entity-extractor/blob/master/src/main/resources/dictionaries/relevant_terms.txt
							description = Vulnerability.getRelevantDescriptionsFromText(summary);
							
							
							//We say the vulnerability's name starts after ", aka " and ends at whichever occurs first: 1) the next period, 2) the next comma, or 3) the end of the summary.
							int startposition = summary.lastIndexOf(", aka ");
							if(startposition > -1)
							{
								startposition = startposition + 6;
								
								int nextperiodposition = summary.indexOf('.', startposition);
								if(nextperiodposition == -1)
									nextperiodposition = Integer.MAX_VALUE;
								int nextcommaposition = summary.indexOf(',', startposition);
								if(nextcommaposition == -1)
									nextcommaposition = Integer.MAX_VALUE;
								int endofstring = summary.length()-1;
								
								int endposition = Math.min(nextperiodposition, Math.min(nextcommaposition, endofstring));
								
								name = summary.substring(startposition, endposition).replaceAll("\"", "");
							}
							
							
							//Summaries contain function and file names sometimes
							String[] splitdescription = summary.split(" ");
							for(int k = 0; k < splitdescription.length; k++)
							{
								if(splitdescription[k].startsWith("function"))
								{
									String possiblefunctionthing = splitdescription[k];
									if(possiblefunctionthing.charAt(possiblefunctionthing.length()-1) == ',' || possiblefunctionthing.charAt(possiblefunctionthing.length()-1) == ';' || possiblefunctionthing.charAt(possiblefunctionthing.length()-1) == ')')
										possiblefunctionthing = possiblefunctionthing.substring(0, possiblefunctionthing.length()-1);
									if((possiblefunctionthing.equals("function") || possiblefunctionthing.equals("functions")) && k > 0)
										functionnames.add(splitdescription[k-1]);
								}
								
								RelevantFile f = RelevantFile.getRelevantFileOfCommonType(splitdescription[k]);
								if(f != null)
									filenames.add(f.getFileName());
							}
						}
					}
					
					
					//We wait to create the vulnerability object until here because Vulnerability's fields need to be populated from information from different places in the xml entity (so we need to finish the last loop before constructing a vulnerability instance).
					Vulnerability vulnerability = Vulnerability.getVulnerabilityFromCVEID(cveid, msid, name, description);
						
					
					//We want to associate functions and file names with the vulnerability, software, and 
					//relationship, but we do not have a really good way for figuring out when they are 
					//associated with the right software if there are multiple softwares for this vulnerability.
					//So we associate a file name or vulnerability with these things only if 
					//all the vulnerable softwares are versions of the same software.  So, for example, we
					//would not associate any functions or filenames with anything if the vulnerable
					//softwares included Flash and Firefox, but we would create the association if 
					//the vulnerability is associated with Firefox version 1.3 and Firefox version 1.4.
					boolean founddifferentvulnerablesoftwares = false;
					if(vulnerablesoftwares.size() > 0)
					{
						String thesoftwarename = vulnerablesoftwares.get(0).getName();
						for(int j = 1; j < vulnerablesoftwares.size(); j++)
							if(!thesoftwarename.equals(vulnerablesoftwares.get(j).getName()))
								founddifferentvulnerablesoftwares = true;
					}
					
					
					//Relate all softwares mentioned with the vulnerability.
					for(SoftwareWVersion software : vulnerablesoftwares)
					{
						VulnerabilityToSoftwareWVersionRelationship r = VulnerabilityToSoftwareWVersionRelationship.getRelationship(software, vulnerability);
						
						if(!founddifferentvulnerablesoftwares)
						{
							r.setFunctionNames(functionnames);
							r.setFileNames(filenames);
						}
					}
					
					
				}
			}
		}catch(ParserConfigurationException | IOException | SAXException e)
		{
			System.out.println(e);
			e.printStackTrace();
			System.exit(3);
		}
	}
	
	
	
	public static void main(String[] args)
	{
		AllKnownDatabaseRelationships akdr = AllKnownDatabaseRelationships.getAllKnownDatabaseRelationships();
		
		printRelationStatistics();
	}
	
	private static void printRelationStatistics()
	{
		Collection<SoftwareWVersion> softwares = SoftwareWVersion.getAllSoftwareWVersions();
		
		
		//·         sw.vendor & sw.product à same software
		HashSet<String> relationholder = new HashSet<String>();
		for(SoftwareWVersion software : softwares)
			if(software.getVendor() != null && software.getName() != null)
				relationholder.add(software.getVendor() + "\t" + software.getName());
		System.out.println("sw.vendor & sw.product:\t" + relationholder.size());
		
		//·         sw.version & sw.product à same software
		relationholder = new HashSet<String>();
		for(SoftwareWVersion software : softwares)
			if(software.getName() != null && software.getVersion() != null)
				relationholder.add(software.getName() + "\t" + software.getVersion());
		System.out.println("sw.version & sw.product:\t" + relationholder.size());
		
		//·         vuln.description & vuln.name à same vulnerability
		relationholder = new HashSet<String>();
		HashMap<String,Vulnerability> vulnerabilitynameTovulnerability = Vulnerability.getCveIdToVulnerability();
		for(Vulnerability vulnerability : vulnerabilitynameTovulnerability.values())
			if(vulnerability.getName() != null && vulnerability.getDescription() != null)
				relationholder.add(vulnerability.getName() + "\t" + vulnerability.getDescription());
		System.out.println("vuln.description & vuln.name:\t" + relationholder.size());
		
		//·         vuln.ms & vuln.name à same vulnerability
		relationholder = new HashSet<String>();
		for(Vulnerability vulnerability : vulnerabilitynameTovulnerability.values())
			if(vulnerability.getMSID() != null && vulnerability.getName() != null)
				relationholder.add(vulnerability.getMSID() + "\t" + vulnerability.getName());
		System.out.println("vuln.ms & vuln.name:\t" + relationholder.size());
		
		//·         vuln.cve & vuln.name à same vulnerability
		relationholder = new HashSet<String>();
		for(Vulnerability vulnerability : vulnerabilitynameTovulnerability.values())
			if(vulnerability.getName() != null && vulnerability.getCVEID() != null)
				relationholder.add(vulnerability.getCVEID() + "\t" + vulnerability.getName());
		System.out.println("vuln.cve & vuln.name:\t" + relationholder.size());
		
		//·         vuln.description & vuln.ms à same vulnerability
		relationholder = new HashSet<String>();
		for(Vulnerability vulnerability : vulnerabilitynameTovulnerability.values())
			if(vulnerability.getDescription() != null && vulnerability.getMSID() != null)
				relationholder.add(vulnerability.getDescription() + "\t" + vulnerability.getMSID());
		System.out.println("vuln.description & vuln.ms:\t" + relationholder.size());
		
		//·         vuln.description & vuln.cve à same vulnerability		
		relationholder = new HashSet<String>();
		for(Vulnerability vulnerability : vulnerabilitynameTovulnerability.values())
			if(vulnerability.getDescription() != null && vulnerability.getCVEID() != null)
				relationholder.add(vulnerability.getDescription() + "\t" + vulnerability.getCVEID());
		System.out.println("vuln.description & vuln.cve:\t" + relationholder.size());
		
		//·         vuln.cve & vuln.ms à same vulnerability
		relationholder = new HashSet<String>();
		for(Vulnerability vulnerability : vulnerabilitynameTovulnerability.values())
			if(vulnerability.getCVEID() != null && vulnerability.getMSID() != null)
				relationholder.add(vulnerability.getCVEID() + "\t" + vulnerability.getMSID());
		System.out.println("vuln.cve & vuln.ms:\t" + relationholder.size());
		
		//·         vuln.* & sw.* à ExploitTargetRelatedObservable
		relationholder = new HashSet<String>();
		for(VulnerabilityToSoftwareWVersionRelationship relationship : VulnerabilityToSoftwareWVersionRelationship.getPrimaryKeyToRelationship().values())
			if(relationship.getVulnerability().getCVEID() != null && relationship.getSoftwareWVersion().getName() != null)
				relationholder.add(relationship.getVulnerability().getCVEID() + "\t" + relationship.getSoftwareWVersion().getName());
		System.out.println("vuln.* & sw.*:\t" + relationholder.size() + "\t(note that different versions do not count as different sw.*s here or below)");
		
		//·         sw.* & file.name à Sub-Observable
		relationholder = new HashSet<String>();
		for(VulnerabilityToSoftwareWVersionRelationship relationship : VulnerabilityToSoftwareWVersionRelationship.getPrimaryKeyToRelationship().values())
		{
			HashSet<String> filenames = relationship.getFileNames();
			if(filenames != null)
			{
				for(String filename : filenames)
					if(relationship.getSoftwareWVersion().getName() != null && filename != null)
						relationholder.add(relationship.getSoftwareWVersion().getName() + "\t" + filename);
			}
		}
		System.out.println("sw.* & file.name:\t" + relationholder.size());
		
		//·         sw.* & function.name à Sub-Observable
		relationholder = new HashSet<String>();
		for(VulnerabilityToSoftwareWVersionRelationship relationship : VulnerabilityToSoftwareWVersionRelationship.getPrimaryKeyToRelationship().values())
		{
			HashSet<String> functionnames = relationship.getFunctionNames();
			if(functionnames != null)
			{
				for(String functionname : functionnames)
					if(relationship.getSoftwareWVersion().getName() != null && functionname != null)
						relationholder.add(relationship.getSoftwareWVersion().getName() + "\t" + functionname);
			}
		}
		System.out.println("sw.* & function.name:\t" + relationholder.size());
		
		//·         vuln.* & file.name à ExploitTargetRelatedObservable
		relationholder = new HashSet<String>();
		for(VulnerabilityToSoftwareWVersionRelationship relationship : VulnerabilityToSoftwareWVersionRelationship.getPrimaryKeyToRelationship().values())
		{
			HashSet<String> filenames = relationship.getFileNames();
			if(filenames != null)
			{
				for(String filename : filenames)
					if(relationship.getVulnerability().getCVEID() != null && filename != null)
						relationholder.add(relationship.getVulnerability().getCVEID() + "\t" + filename);
			}
		}
		System.out.println("vuln.* & file.name:\t" + relationholder.size());
		
		//·         vuln.* & function.name à ExploitTargetRelatedObservable
		relationholder = new HashSet<String>();
		for(VulnerabilityToSoftwareWVersionRelationship relationship : VulnerabilityToSoftwareWVersionRelationship.getPrimaryKeyToRelationship().values())
		{
			HashSet<String> functionnames = relationship.getFunctionNames();
			if(functionnames != null)
			{
				for(String functionname : functionnames)
					if(relationship.getVulnerability().getCVEID() != null && functionname != null)
						relationholder.add(relationship.getVulnerability().getCVEID() + "\t" + functionname);
			}
		}
		System.out.println("vuln.* & function.name:\t" + relationholder.size());
	}
	
	
	//Return true if the entities are in a known relationship.  Return false if both entities are known, but they are not in a known relationship.  Return null if either entity is not known.
	public static Boolean isKnownRelationship(String entity1, String entity2, int relationshiptype)
	{
		entity1 = entity1.toLowerCase();
		entity2 = entity2.toLowerCase();
		
		switch (relationshiptype) 
		{
        	case RT_SWVENDOR_SWPRODUCT:  return isKnownRelationship_SWVendor_SWProduct(entity1, entity2);
        	case RT_SWVERSION_SWPRODUCT:  return isKnownRelationship_SWVersion_SWProduct(entity1,entity2);
        	case RT_VUDESCRIPTION_VUNAME:  return isKnownRelationship_VUDescription_VUName(entity1,entity2);
        	case RT_VUMS_VUNAME:  return isKnownRelationship_VUMS_VUName(entity1,entity2);
        	case RT_VUCVE_VUNAME:  return isKnownRelationship_VUCVE_VUName(entity1,entity2);
        	case RT_VUDESCRIPTION_VUMS:  return isKnownRelationship_VUDescription_VUMS(entity1,entity2);
        	case RT_VUDESCRIPTION_VUCVE:  return isKnownRelationship_VUDescription_VUCVE(entity1,entity2);
        	case RT_VUCVE_VUMS:  return isKnownRelationship_VUCVE_VUMS(entity1,entity2);
        	case RT_SWPRODUCT_VUNAME:	return isKnownRelationship_SWProduct_VUName(entity1, entity2);
        	case RT_SWPRODUCT_VUMS:	return isKnownRelationship_SWProduct_VUMS(entity1, entity2);
        	case RT_SWPRODUCT_VUCVE:	return isKnownRelationship_SWProduct_VUCVE(entity1, entity2);
        	case RT_SWVERSION_VUNAME:	return isKnownRelationship_SWVersion_VUName(entity1, entity2);
        	case RT_SWVERSION_VUMS:	return isKnownRelationship_SWVersion_VUMS(entity1, entity2);
        	case RT_SWVERSION_VUCVE:	return isKnownRelationship_SWVersion_VUCVE(entity1, entity2);
        	case RT_SWPRODUCT_FINAME:	return isKnownRelationship_SWProduct_FIName(entity1, entity2);
        	case RT_SWVERSION_FINAME:	return isKnownRelationship_SWVersion_FIName(entity1, entity2);
        	case RT_SWPRODUCT_FUNAME:	return isKnownRelationship_SWProduct_FUName(entity1, entity2);
        	case RT_SWVERSION_FUNAME:	return isKnownRelationship_SWVersion_FUName(entity1, entity2);
        	case RT_VUNAME_FINAME:	return isKnownRelationship_VUName_FIName(entity1, entity2);
        	case RT_VUCVE_FINAME:	return isKnownRelationship_VUCVE_FIName(entity1, entity2);
        	case RT_VUMS_FINAME:	return isKnownRelationship_VUMS_FIName(entity1, entity2);
        	case RT_VUNAME_FUNAME:	return isKnownRelationship_VUName_FUName(entity1, entity2);
        	case RT_VUCVE_FUNAME:	return isKnownRelationship_VUCVE_FUName(entity1, entity2);
        	case RT_VUMS_FUNAME:	return isKnownRelationship_VUMS_FUName(entity1, entity2);
        	default: return null;
		}
	}
	
	
	public static Boolean isKnownRelationship_SWVendor_SWProduct(String vendoralias, String productalias)
	{
		ArrayList<SoftwareWVersion> softwares = SoftwareWVersion.getAllSoftwaresWithAlias(productalias);
		for(SoftwareWVersion swv : softwares)
		{
			for(String swvvendoralias : swv.getVendorAliases())
				if(vendoralias.equalsIgnoreCase(swvvendoralias))
					return true;
		}
		
		if(SoftwareWVersion.getAllProductAliases().contains(productalias) && SoftwareWVersion.getAllVendorAliases().contains(vendoralias))
			return false;
		
		return null;
	}
	
	public static Boolean isKnownRelationship_SWVersion_SWProduct(String version, String productalias)
	{
		ArrayList<SoftwareWVersion> softwares = SoftwareWVersion.getAllSoftwaresWithAlias(productalias);
		for(SoftwareWVersion swv : softwares)
		{
			if(version.equals(swv.getVersion()))
				return true;
		}
		
		if(SoftwareWVersion.getAllProductAliases().contains(productalias) && SoftwareWVersion.getAllVersions().contains(version))
			return false;
		
		return null;
	}
	
	public static Boolean isKnownRelationship_VUDescription_VUName(String description, String vulnerabilityname)
	{
		for(Vulnerability vulnerability : Vulnerability.getVulnerabilitiesWithName(vulnerabilityname))
		{
			if(vulnerability.getDescription() != null && vulnerability.getDescription().contains(description))
				return true;
		}
		
		if(Vulnerability.getAllRelevantTerms().contains(description) && Vulnerability.getAllNames().contains(vulnerabilityname))
			return false;
			
		return null;
	}
	
	public static Boolean isKnownRelationship_VUMS_VUName(String ms, String vulnerabilityname)
	{
		for(Vulnerability vulnerability : Vulnerability.getVulnerabilitiesWithName(vulnerabilityname))
		{
			if(vulnerability.getMSID().equals(ms))
				return true;
		}
		
		if(Vulnerability.getAllMSs().contains(ms) && Vulnerability.getAllNames().contains(vulnerabilityname))
			return false;
		
		return null;
	}
	
	public static Boolean isKnownRelationship_VUCVE_VUName(String cve, String vulnerabilityname)
	{
		Vulnerability vulnerability = Vulnerability.getVulnerabilityFromCVEID(cve);
		if(vulnerability != null && vulnerability.getName().equals(vulnerabilityname))
			return true;
		
		if(Vulnerability.getAllCVEs().contains(cve) && Vulnerability.getAllNames().contains(vulnerabilityname))
			return false;
		
		return null;
	}
	
	public static Boolean isKnownRelationship_VUDescription_VUMS(String description, String ms)
	{
		for(Vulnerability vulnerability : Vulnerability.getVulnerabilitiesWithMSid(ms))
		{
			if(vulnerability.getDescription() != null && vulnerability.getDescription().contains(description))
				return true;
		}
		
		if(Vulnerability.getAllRelevantTerms().contains(description) && Vulnerability.getAllMSs().contains(ms))
			return false;
		
		return null;
	}
	
	public static Boolean isKnownRelationship_VUDescription_VUCVE(String description, String cve)
	{
		Vulnerability v = Vulnerability.getVulnerabilityFromCVEID(cve);
		if(v != null && v.getDescription() != null && v.getDescription().contains(description))
			return true;
		
		if(Vulnerability.getAllRelevantTerms().contains(description) && Vulnerability.getAllCVEs().contains(cve))
			return false;
		
		return null;
	}

	public static Boolean isKnownRelationship_VUCVE_VUMS(String cve, String ms)
	{
		Vulnerability v = Vulnerability.getVulnerabilityFromCVEID(cve);
		if(v != null && v.getMSID().equals(ms))
			return true;
		
		if(Vulnerability.getAllMSs().contains(ms) && Vulnerability.getAllCVEs().contains(cve))
			return false;
		
		return null;
	}

	public static Boolean isKnownRelationship_SWProduct_VUName(String swproduct, String vuname)
	{
		for(Vulnerability vulnerability : Vulnerability.getVulnerabilitiesWithName(vuname))
		{
			for(VulnerabilityToSoftwareWVersionRelationship relationship : vulnerability.getRelationships())
			{
				if(relationship.getSoftwareWVersion().getSoftwareAliases().contains(swproduct))
					return true;
			}
		}
		
		if(SoftwareWVersion.getAllProductAliases().contains(swproduct) && Vulnerability.getAllNames().contains(vuname))
			return false;
		
		return null;
	}
	
	public static Boolean isKnownRelationship_SWProduct_VUMS(String swproduct, String ms)
	{
		for(Vulnerability vulnerability : Vulnerability.getVulnerabilitiesWithMSid(ms))
		{
			for(VulnerabilityToSoftwareWVersionRelationship relationship : vulnerability.getRelationships())
			{
				if(relationship.getSoftwareWVersion().getSoftwareAliases().contains(swproduct))
					return true;
			}
		}
		
		if(SoftwareWVersion.getAllProductAliases().contains(swproduct) && Vulnerability.getAllMSs().contains(ms))
			return false;
		
		return null;
	}
	
	public static Boolean isKnownRelationship_SWProduct_VUCVE(String swproduct, String cve)
	{
		Vulnerability vulnerability = Vulnerability.getVulnerabilityFromCVEID(cve);
		if(vulnerability != null)
		{
			for(VulnerabilityToSoftwareWVersionRelationship relationship : vulnerability.getRelationships())
			{
				if(relationship.getSoftwareWVersion().getSoftwareAliases().contains(swproduct))
					return true;
			}
		}
		
		if(SoftwareWVersion.getAllProductAliases().contains(swproduct) && Vulnerability.getAllCVEs().contains(cve))
			return false;
		
		return null;
	}

	public static Boolean isKnownRelationship_SWVersion_VUName(String swversion, String vuname)
	{
		for(Vulnerability vulnerability : Vulnerability.getVulnerabilitiesWithName(vuname))
		{
			for(VulnerabilityToSoftwareWVersionRelationship relationship : vulnerability.getRelationships())
			{
				if(relationship.getSoftwareWVersion().getVersion().equals(swversion))
					return true;
			}
		}
		
		if(SoftwareWVersion.getAllVersions().contains(swversion) && Vulnerability.getAllNames().contains(vuname))
			return false;
		
		return null;
	}
	
	public static Boolean isKnownRelationship_SWVersion_VUMS(String swversion, String ms)
	{
		for(Vulnerability vulnerability : Vulnerability.getVulnerabilitiesWithMSid(ms))
		{
			for(VulnerabilityToSoftwareWVersionRelationship relationship : vulnerability.getRelationships())
			{
				if(relationship.getSoftwareWVersion().getVersion().equals(swversion))
					return true;
			}
		}
		
		if(SoftwareWVersion.getAllVersions().contains(swversion) && Vulnerability.getAllMSs().contains(ms))
			return false;
		
		return null;
	}

	public static Boolean isKnownRelationship_SWVersion_VUCVE(String swversion, String cve)
	{
		Vulnerability vulnerability = Vulnerability.getVulnerabilityFromCVEID(cve);
		if(vulnerability != null)
		{
			for(VulnerabilityToSoftwareWVersionRelationship relationship : vulnerability.getRelationships())
			{
				if(relationship.getSoftwareWVersion().getVersion().equals(swversion))
					return true;
			}
		}
		
		if(SoftwareWVersion.getAllVersions().contains(swversion) && Vulnerability.getAllCVEs().contains(cve))
			return false;
		
		return null;
	}

	public static Boolean isKnownRelationship_SWProduct_FIName(String swproduct, String filename)
	{
		ArrayList<VulnerabilityToSoftwareWVersionRelationship> relationships = VulnerabilityToSoftwareWVersionRelationship.getRelationshipsWithFileName(filename);
		if(relationships != null)
		{
			for(VulnerabilityToSoftwareWVersionRelationship relationship : relationships)
			{
				if(relationship.getSoftwareWVersion().getSoftwareAliases().contains(swproduct))
					return true;
			}
		}
		
		if(SoftwareWVersion.getAllProductAliases().contains(swproduct) && VulnerabilityToSoftwareWVersionRelationship.getAllFileNames().contains(filename))
			return false;
		
		return null;
	}

	public static Boolean isKnownRelationship_SWVersion_FIName(String swversion, String filename)
	{
		ArrayList<VulnerabilityToSoftwareWVersionRelationship> relationships = VulnerabilityToSoftwareWVersionRelationship.getRelationshipsWithFileName(filename);
		if(relationships != null)
		{
			for(VulnerabilityToSoftwareWVersionRelationship relationship : relationships)
			{
				if(relationship.getSoftwareWVersion().getVersion().equals(swversion))
					return true;
			}
		}
		
		if(SoftwareWVersion.getAllVersions().contains(swversion) && VulnerabilityToSoftwareWVersionRelationship.getAllFileNames().contains(filename))
			return false;
		
		return null;
	}

	public static Boolean isKnownRelationship_SWProduct_FUName(String swproduct, String functionname)
	{
		ArrayList<VulnerabilityToSoftwareWVersionRelationship> relationships = VulnerabilityToSoftwareWVersionRelationship.getRelationshipsWithFunctionName(functionname);
		if(relationships != null)
		{
			for(VulnerabilityToSoftwareWVersionRelationship relationship : relationships)
			{
				if(relationship.getSoftwareWVersion().getSoftwareAliases().contains(swproduct))
					return true;
			}
		}
		
		if(SoftwareWVersion.getAllProductAliases().contains(swproduct) && VulnerabilityToSoftwareWVersionRelationship.getAllFunctionNames().contains(functionname))
			return false;
		
		return null;
	}

	public static Boolean isKnownRelationship_SWVersion_FUName(String swversion, String functionname)
	{
		ArrayList<VulnerabilityToSoftwareWVersionRelationship> relationships = VulnerabilityToSoftwareWVersionRelationship.getRelationshipsWithFunctionName(functionname);
		if(relationships != null)
		{
			for(VulnerabilityToSoftwareWVersionRelationship relationship : relationships)
			{
				if(relationship.getSoftwareWVersion().getVersion().equals(swversion))
					return true;
			}
		}
		
		if(SoftwareWVersion.getAllVersions().contains(swversion) && VulnerabilityToSoftwareWVersionRelationship.getAllFunctionNames().contains(functionname))
			return false;
		
		return null;
	}

	public static Boolean isKnownRelationship_VUName_FIName(String vulnerabilityname, String filename)
	{
		ArrayList<VulnerabilityToSoftwareWVersionRelationship> relationships = VulnerabilityToSoftwareWVersionRelationship.getRelationshipsWithFileName(filename);
		if(relationships != null)
		{
			for(VulnerabilityToSoftwareWVersionRelationship relationship : relationships)
			{
				if(relationship.getVulnerability().getName().equals(vulnerabilityname))
					return true;
			}
		}
		
		if(Vulnerability.getAllNames().contains(vulnerabilityname) && VulnerabilityToSoftwareWVersionRelationship.getAllFileNames().contains(filename))
			return false;
		
		return null;
	}

	public static Boolean isKnownRelationship_VUCVE_FIName(String cve, String filename)
	{
		ArrayList<VulnerabilityToSoftwareWVersionRelationship> relationships = VulnerabilityToSoftwareWVersionRelationship.getRelationshipsWithFileName(filename);
		if(relationships != null)
		{
			for(VulnerabilityToSoftwareWVersionRelationship relationship : relationships)
			{
				if(relationship.getVulnerability().getCVEID().equals(cve))
					return true;
			}
		}
		
		if(Vulnerability.getAllCVEs().contains(cve) && VulnerabilityToSoftwareWVersionRelationship.getAllFileNames().contains(filename))
			return false;
		
		return null;
	}

	public static Boolean isKnownRelationship_VUMS_FIName(String ms, String filename)
	{
		ArrayList<VulnerabilityToSoftwareWVersionRelationship> relationships = VulnerabilityToSoftwareWVersionRelationship.getRelationshipsWithFileName(filename);
		if(relationships != null)
		{
			for(VulnerabilityToSoftwareWVersionRelationship relationship : relationships)
			{
				if(relationship.getVulnerability().getMSID().equals(ms))
					return true;
			}
		}
		
		if(Vulnerability.getAllMSs().contains(ms) && VulnerabilityToSoftwareWVersionRelationship.getAllFileNames().contains(filename))
			return false;
		
		return null;
	}

	public static Boolean isKnownRelationship_VUName_FUName(String vulnerabilityname, String functionname)
	{
		ArrayList<VulnerabilityToSoftwareWVersionRelationship> relationships = VulnerabilityToSoftwareWVersionRelationship.getRelationshipsWithFunctionName(functionname);
		if(relationships != null)
		{
			for(VulnerabilityToSoftwareWVersionRelationship relationship : relationships)
			{
				if(relationship.getVulnerability().getName().equals(vulnerabilityname))
					return true;
			}
		}
		
		if(Vulnerability.getAllNames().contains(vulnerabilityname) && VulnerabilityToSoftwareWVersionRelationship.getAllFunctionNames().contains(functionname))
			return false;
		
		return null;
	}

	public static Boolean isKnownRelationship_VUCVE_FUName(String cve, String functionname)
	{
		ArrayList<VulnerabilityToSoftwareWVersionRelationship> relationships = VulnerabilityToSoftwareWVersionRelationship.getRelationshipsWithFunctionName(functionname);
		if(relationships != null)
		{
			for(VulnerabilityToSoftwareWVersionRelationship relationship : relationships)
			{
				if(relationship.getVulnerability().getCVEID().equals(cve))
					return true;
			}
		}
		
		if(Vulnerability.getAllCVEs().contains(cve) && VulnerabilityToSoftwareWVersionRelationship.getAllFunctionNames().contains(functionname))
			return false;
		
		return null;
	}

	public static Boolean isKnownRelationship_VUMS_FUName(String ms, String functionname)
	{
		ArrayList<VulnerabilityToSoftwareWVersionRelationship> relationships = VulnerabilityToSoftwareWVersionRelationship.getRelationshipsWithFunctionName(functionname);
		if(relationships != null)
		{
			for(VulnerabilityToSoftwareWVersionRelationship relationship : relationships)
			{
				if(relationship.getVulnerability().getMSID().equals(ms))
					return true;
			}
		}
		
		if(Vulnerability.getAllMSs().contains(ms) && VulnerabilityToSoftwareWVersionRelationship.getAllFunctionNames().contains(functionname))
			return false;
		
		return null;
	}
	
	

	
	
}


