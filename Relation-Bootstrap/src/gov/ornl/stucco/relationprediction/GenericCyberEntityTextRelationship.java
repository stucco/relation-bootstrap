package gov.ornl.stucco.relationprediction;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class GenericCyberEntityTextRelationship 
{
	//This is the directory containing the nvd xml dumps.
	//private static File nvdxmldir = new File("src/nvdxml/");	//Location where xml dumps from NVD can be found.  Downloaded from https://nvd.nist.gov/download.cfm
	
	
	//Each of the 24 relationshp types is associated with an int constant here.
	public static final int RT_SWVENDOR_SWPRODUCT = 1;	//sw.vendor & sw.product à same software
	public static final int RT_SWVERSION_SWPRODUCT = 2;	//sw.version & sw.product à same software
	public static final int RT_VUDESCRIPTION_VUNAME = 3;	//vuln.description & vuln.name à same vulnerability
	public static final int RT_VUMS_VUNAME = 4;	//vuln.ms & vuln.name à same vulnerability
	public static final int RT_VUCVE_VUNAME = 5;	//vuln.cve & vuln.name à same vulnerability
	public static final int RT_VUDESCRIPTION_VUMS = 6;	//vuln.description & vuln.ms à same vulnerability
	public static final int RT_VUDESCRIPTION_VUCVE = 7;	//vuln.description & vuln.cve à same vulnerability
	public static final int RT_VUCVE_VUMS = 8;	//vuln.cve & vuln.ms à same vulnerability
	
	//The commented out relationship types are relationships we are interested in, but cannot be found directly in the text.
	//public static final int RT_VU_SW = 9;	//vuln.* & sw.* à ExploitTargetRelatedObservable
	//public static final int RT_SW_FILENAME = 10;	//sw.* & file.name à Sub-Observable
	//public static final int RT_SW_FUNCTIONNAME= 11;	//sw.* & function.name à Sub-Observable
	//public static final int RT_VU_FILENAME = 12;	//vuln.* & file.name à ExploitTargetRelatedObservable
	//public static final int RT_VU_FUNCTIONNAME = 13;	//vuln.* & function.name à ExploitTargetRelatedObservable
	
	//The following relationship types are not directly ones we are interested in, but are necessary for finding the ones above that cannot be found directly in the text.
	//The next group are needed for vuln.* & sw.* à ExploitTargetRelatedObservable
	public static final int RT_SWPRODUCT_VUNAME = 14;	//sw.product & vuln.name
	public static final int RT_SWPRODUCT_VUMS = 15;	//sw.product & vuln.ms
	public static final int RT_SWPRODUCT_VUCVE = 16;	//sw.product & vuln.cve
	public static final int RT_SWVERSION_VUNAME = 17;	//sw.version & vuln.name
	public static final int RT_SWVERSION_VUMS = 18;	//sw.version & vuln.ms
	public static final int RT_SWVERSION_VUCVE = 19;	//sw.version & vuln.cve
	
	//The next group are needed for sw.* & file.name à Sub-Observable
	public static final int RT_SWPRODUCT_FINAME = 20;	//sw.product & file.name
	public static final int RT_SWVERSION_FINAME = 21;	//sw.version & file.name
	
	//The next group are needed for sw.* & function.name à Sub-Observable
	public static final int RT_SWPRODUCT_FUNAME = 22;	//sw.product & function.name
	public static final int RT_SWVERSION_FUNAME = 23;	//sw.version & function.name
	
	//The next group are needed for vuln.* & file.name à ExploitTargetRelatedObservable
	public static final int RT_VUNAME_FINAME = 24;	//vuln.name & file.name
	public static final int RT_VUCVE_FINAME = 25;	//vuln.cve & file.name
	public static final int RT_VUMS_FINAME = 26;	//vuln.ms & file.name
	
	//The next group are needed for vuln.* & function.name à ExploitTargetRelatedObservable
	public static final int RT_VUNAME_FUNAME = 27;	//vuln.name & function.name
	public static final int RT_VUCVE_FUNAME = 28;	//vuln.cve & function.name
	public static final int RT_VUMS_FUNAME = 29;	//vuln.ms & function.name

	
	//Each entity type is associated with an int constant in CyberEntityText, and each relationship type
	//is associated with an int constant above.  Given the ints associated with a pair of entities, this
	//array stores the int associated with their relationship type.  Entries should be null if there
	//is no relationship type that fits with the two entities.  Order matters, and an entry
	//in this array is positive if the entities appear in the standard order, and negative
	//if the order is reversed.  The "standard" order is arbitrary, and is defined only in this array.
	public static final Integer[][] entity1typeToentity2typeTorelationshiptype = new Integer[CyberEntityText.ENTITYTYPECOUNT][CyberEntityText.ENTITYTYPECOUNT];
	public static final HashSet<Integer> allrelationshiptypesset = new HashSet<Integer>();
	public static final HashSet<Integer> allpositiverelationshiptypesset = new HashSet<Integer>();
	static
	{
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.SWVENDOR][CyberEntityText.SWPRODUCT] = RT_SWVENDOR_SWPRODUCT;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.SWVERSION][CyberEntityText.SWPRODUCT] = RT_SWVERSION_SWPRODUCT;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.VUDESCRIPTION][CyberEntityText.VUNAME] = RT_VUDESCRIPTION_VUNAME;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.VUMS][CyberEntityText.VUNAME] = RT_VUMS_VUNAME;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.VUCVE][CyberEntityText.VUNAME] = RT_VUCVE_VUNAME;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.VUDESCRIPTION][CyberEntityText.VUMS] = RT_VUDESCRIPTION_VUMS;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.VUDESCRIPTION][CyberEntityText.VUCVE] = RT_VUDESCRIPTION_VUCVE;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.VUCVE][CyberEntityText.VUMS] = RT_VUCVE_VUMS;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.SWPRODUCT][CyberEntityText.VUNAME] = RT_SWPRODUCT_VUNAME;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.SWPRODUCT][CyberEntityText.VUMS] = RT_SWPRODUCT_VUMS;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.SWPRODUCT][CyberEntityText.VUCVE] = RT_SWPRODUCT_VUCVE;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.SWVERSION][CyberEntityText.VUNAME] = RT_SWVERSION_VUNAME;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.SWVERSION][CyberEntityText.VUMS] = RT_SWVERSION_VUMS;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.SWVERSION][CyberEntityText.VUCVE] = RT_SWVERSION_VUCVE;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.SWPRODUCT][CyberEntityText.FINAME] = RT_SWPRODUCT_FINAME;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.SWVERSION][CyberEntityText.FINAME] = RT_SWVERSION_FINAME;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.SWPRODUCT][CyberEntityText.FUNAME] = RT_SWPRODUCT_FUNAME;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.SWVERSION][CyberEntityText.FUNAME] = RT_SWVERSION_FUNAME;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.VUNAME][CyberEntityText.FINAME] = RT_VUNAME_FINAME;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.VUCVE][CyberEntityText.FINAME] = RT_VUCVE_FINAME;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.VUMS][CyberEntityText.FINAME] = RT_VUMS_FINAME;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.VUNAME][CyberEntityText.FUNAME] = RT_VUNAME_FUNAME;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.VUCVE][CyberEntityText.FUNAME] = RT_VUCVE_FUNAME;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.VUMS][CyberEntityText.FUNAME] = RT_VUMS_FUNAME;

		for(int i = 0; i < CyberEntityText.ENTITYTYPECOUNT; i++)
		{
			for(int j = 0; j < CyberEntityText.ENTITYTYPECOUNT; j++)
			{
				if(entity1typeToentity2typeTorelationshiptype[i][j] != null)
				{
					entity1typeToentity2typeTorelationshiptype[j][i] = -entity1typeToentity2typeTorelationshiptype[i][j];
					
					allrelationshiptypesset.add(entity1typeToentity2typeTorelationshiptype[i][j]);
					allrelationshiptypesset.add(-entity1typeToentity2typeTorelationshiptype[i][j]);
					
					allpositiverelationshiptypesset.add(Math.abs(entity1typeToentity2typeTorelationshiptype[i][j]));
				}
			}
		}
	}
	
	
	//This class contains methods for determining if a relationship candidate is a known relationship or not.
	//In order to determine this, it needs to load a bunch of relationships into memory.  But it doesn't make
	//sense to load the relationships into memory unless they're needed.  So this variable keeps track
	//of whether the relationships have been loaded into memory.
	private static boolean loadedrelationships = false;
	
	
	private CyberEntityText[] entities = new CyberEntityText[2];
	
	
	
	
	public GenericCyberEntityTextRelationship(CyberEntityText entity1, CyberEntityText entity2)
	{
		entities[0] = entity1;
		entities[1] = entity2;
	}
	
	public CyberEntityText getEntityOfType(int type)
	{
		for(CyberEntityText cet : entities)
		{
			if(cet.getEntityType() == type)
				return cet;
		}
		
		return null;
	}
	
	public CyberEntityText getFirstEntity()
	{
		return entities[0];
	}
	
	public CyberEntityText getSecondEntity()
	{
		return entities[1];
	}
	
	public Integer getRelationType()
	{
		return entity1typeToentity2typeTorelationshiptype[entities[0].getEntityType()][entities[1].getEntityType()];
	}
	
	

	//Reads all known entities in from Database.  The information gets stored in various different places.
	public static void loadAllKnownRelationships()
	{
		Vulnerability.setAllRelevantTerms();
		
		loadRelationshipsFromNVD();
		
		SoftwareWVersion.setAllAliases();
		
		loadedrelationships = true;
	}
	
	//Loads entities from NVD xml data.  The code is ugly because I have never done anything with xml files before.
	private static void loadRelationshipsFromNVD()
	{
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
		
			for(File file : ProducedFileGetter.getNVDXMLDir().listFiles())
			{
				if(!file.getName().endsWith(".zip"))
					continue;
				
				ZipFile zipfile = new ZipFile(file);
			    Enumeration<? extends ZipEntry> entries = zipfile.entries();
			    while(entries.hasMoreElements())
			    {
			        ZipEntry entry = entries.nextElement();
			        InputStream stream = zipfile.getInputStream(entry);
				
			        org.w3c.dom.Document document = db.parse(stream);
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
			    zipfile.close();
			}
		}catch(ParserConfigurationException | IOException | SAXException e)
		{
			System.out.println(e);
			e.printStackTrace();
			System.exit(3);
		}
	}
	
	

	
	//Return true if the entities are in a known relationship.  Return false if both entities are known, but they are not in a known relationship.  Return null if either entity is not known.
	public Boolean isKnownRelationship()
	{
		//We can't tell if a relationship is known unless we have loaded our known relationships into memory.  So check
		//if we've done that.  And if not, do it.
		if(!loadedrelationships)
			loadAllKnownRelationships();
		
		
		switch (getRelationType()) 
		{
			//Choose a function depending on the types of the entities involved.
        	case RT_SWVENDOR_SWPRODUCT:  return isKnownRelationship_SWVendor_SWProduct();
        	case RT_SWVERSION_SWPRODUCT:  return isKnownRelationship_SWVersion_SWProduct();
        	case RT_VUDESCRIPTION_VUNAME:  return isKnownRelationship_VUDescription_VUName();
        	case RT_VUMS_VUNAME:  return isKnownRelationship_VUMS_VUName();
        	case RT_VUCVE_VUNAME:  return isKnownRelationship_VUCVE_VUName();
        	case RT_VUDESCRIPTION_VUMS:  return isKnownRelationship_VUDescription_VUMS();
        	case RT_VUDESCRIPTION_VUCVE:  return isKnownRelationship_VUDescription_VUCVE();
        	case RT_VUCVE_VUMS:  return isKnownRelationship_VUCVE_VUMS();
        	case RT_SWPRODUCT_VUNAME:	return isKnownRelationship_SWProduct_VUName();
        	case RT_SWPRODUCT_VUMS:	return isKnownRelationship_SWProduct_VUMS();
        	case RT_SWPRODUCT_VUCVE:	return isKnownRelationship_SWProduct_VUCVE();
        	case RT_SWVERSION_VUNAME:	return isKnownRelationship_SWVersion_VUName();
        	case RT_SWVERSION_VUMS:	return isKnownRelationship_SWVersion_VUMS();
        	case RT_SWVERSION_VUCVE:	return isKnownRelationship_SWVersion_VUCVE();
        	case RT_SWPRODUCT_FINAME:	return isKnownRelationship_SWProduct_FIName();
        	case RT_SWVERSION_FINAME:	return isKnownRelationship_SWVersion_FIName();
        	case RT_SWPRODUCT_FUNAME:	return isKnownRelationship_SWProduct_FUName();
        	case RT_SWVERSION_FUNAME:	return isKnownRelationship_SWVersion_FUName();
        	case RT_VUNAME_FINAME:	return isKnownRelationship_VUName_FIName();
        	case RT_VUCVE_FINAME:	return isKnownRelationship_VUCVE_FIName();
        	case RT_VUMS_FINAME:	return isKnownRelationship_VUMS_FIName();
        	case RT_VUNAME_FUNAME:	return isKnownRelationship_VUName_FUName();
        	case RT_VUCVE_FUNAME:	return isKnownRelationship_VUCVE_FUName();
        	case RT_VUMS_FUNAME:	return isKnownRelationship_VUMS_FUName();

        	//Same as above, but for when the order of entities is reversed (and thus the relationship type number is negated)
        	case -RT_SWVENDOR_SWPRODUCT:  return isKnownRelationship_SWVendor_SWProduct();
        	case -RT_SWVERSION_SWPRODUCT:  return isKnownRelationship_SWVersion_SWProduct();
        	case -RT_VUDESCRIPTION_VUNAME:  return isKnownRelationship_VUDescription_VUName();
        	case -RT_VUMS_VUNAME:  return isKnownRelationship_VUMS_VUName();
        	case -RT_VUCVE_VUNAME:  return isKnownRelationship_VUCVE_VUName();
        	case -RT_VUDESCRIPTION_VUMS:  return isKnownRelationship_VUDescription_VUMS();
        	case -RT_VUDESCRIPTION_VUCVE:  return isKnownRelationship_VUDescription_VUCVE();
        	case -RT_VUCVE_VUMS:  return isKnownRelationship_VUCVE_VUMS();
        	case -RT_SWPRODUCT_VUNAME:	return isKnownRelationship_SWProduct_VUName();
        	case -RT_SWPRODUCT_VUMS:	return isKnownRelationship_SWProduct_VUMS();
        	case -RT_SWPRODUCT_VUCVE:	return isKnownRelationship_SWProduct_VUCVE();
        	case -RT_SWVERSION_VUNAME:	return isKnownRelationship_SWVersion_VUName();
        	case -RT_SWVERSION_VUMS:	return isKnownRelationship_SWVersion_VUMS();
        	case -RT_SWVERSION_VUCVE:	return isKnownRelationship_SWVersion_VUCVE();
        	case -RT_SWPRODUCT_FINAME:	return isKnownRelationship_SWProduct_FIName();
        	case -RT_SWVERSION_FINAME:	return isKnownRelationship_SWVersion_FIName();
        	case -RT_SWPRODUCT_FUNAME:	return isKnownRelationship_SWProduct_FUName();
        	case -RT_SWVERSION_FUNAME:	return isKnownRelationship_SWVersion_FUName();
        	case -RT_VUNAME_FINAME:	return isKnownRelationship_VUName_FIName();
        	case -RT_VUCVE_FINAME:	return isKnownRelationship_VUCVE_FIName();
        	case -RT_VUMS_FINAME:	return isKnownRelationship_VUMS_FIName();
        	case -RT_VUNAME_FUNAME:	return isKnownRelationship_VUName_FUName();
        	case -RT_VUCVE_FUNAME:	return isKnownRelationship_VUCVE_FUName();
        	case -RT_VUMS_FUNAME:	return isKnownRelationship_VUMS_FUName();
        	
        	default: return null;
		}
	}
	
	
	public Boolean isKnownRelationship_SWVendor_SWProduct()
	{
		String vendoralias = getEntityTextGivenType(CyberEntityText.SWVENDOR);
		String productalias = getEntityTextGivenType(CyberEntityText.SWPRODUCT);
		
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
	
	public Boolean isKnownRelationship_SWVersion_SWProduct()
	{
		String version = getEntityTextGivenType(CyberEntityText.SWVERSION);
		String productalias = getEntityTextGivenType(CyberEntityText.SWPRODUCT);
		 
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
	
	public Boolean isKnownRelationship_VUDescription_VUName()
	{
		String description = getEntityTextGivenType(CyberEntityText.VUDESCRIPTION);
		String vulnerabilityname = getEntityTextGivenType(CyberEntityText.VUNAME);
		
		for(Vulnerability vulnerability : Vulnerability.getVulnerabilitiesWithName(vulnerabilityname))
		{
			if(vulnerability.getDescription() != null && vulnerability.getDescription().contains(description))
				return true;
		}
		
		if(Vulnerability.getAllRelevantTerms().contains(description) && Vulnerability.getAllNames().contains(vulnerabilityname))
			return false;
			
		return null;
	}
	
	public Boolean isKnownRelationship_VUMS_VUName()
	{
		String ms = getEntityTextGivenType(CyberEntityText.VUMS);
		String vulnerabilityname = getEntityTextGivenType(CyberEntityText.VUNAME);
		
		for(Vulnerability vulnerability : Vulnerability.getVulnerabilitiesWithName(vulnerabilityname))
		{
			if(vulnerability.getMSID().equals(ms))
				return true;
		}
		
		if(Vulnerability.getAllMSs().contains(ms) && Vulnerability.getAllNames().contains(vulnerabilityname))
			return false;
		
		return null;
	}
	
	public Boolean isKnownRelationship_VUCVE_VUName()
	{
		String cve = getEntityTextGivenType(CyberEntityText.VUCVE);
		String vulnerabilityname = getEntityTextGivenType(CyberEntityText.VUNAME);
		
		Vulnerability vulnerability = Vulnerability.getVulnerabilityFromCVEID(cve);
		if(vulnerability != null && vulnerability.getName().equals(vulnerabilityname))
			return true;
		
		if(Vulnerability.getAllCVEs().contains(cve) && Vulnerability.getAllNames().contains(vulnerabilityname))
			return false;
		
		return null;
	}
	
	public Boolean isKnownRelationship_VUDescription_VUMS()
	{
		String description = getEntityTextGivenType(CyberEntityText.VUDESCRIPTION);
		String ms = getEntityTextGivenType(CyberEntityText.VUMS);
		
		for(Vulnerability vulnerability : Vulnerability.getVulnerabilitiesWithMSid(ms))
		{
			if(vulnerability.getDescription() != null && vulnerability.getDescription().contains(description))
				return true;
		}
		
		if(Vulnerability.getAllRelevantTerms().contains(description) && Vulnerability.getAllMSs().contains(ms))
			return false;
		
		return null;
	}
	
	public Boolean isKnownRelationship_VUDescription_VUCVE()
	{
		String description = getEntityTextGivenType(CyberEntityText.VUDESCRIPTION);
		String cve = getEntityTextGivenType(CyberEntityText.VUCVE);
		
		Vulnerability v = Vulnerability.getVulnerabilityFromCVEID(cve);
		if(v != null && v.getDescription() != null && v.getDescription().contains(description))
			return true;
		
		if(Vulnerability.getAllRelevantTerms().contains(description) && Vulnerability.getAllCVEs().contains(cve))
			return false;
		
		return null;
	}

	public Boolean isKnownRelationship_VUCVE_VUMS()
	{
		String cve = getEntityTextGivenType(CyberEntityText.VUCVE);
		String ms = getEntityTextGivenType(CyberEntityText.VUMS);
		
		Vulnerability v = Vulnerability.getVulnerabilityFromCVEID(cve);
		if(v != null && v.getMSID().equals(ms))
			return true;
		
		if(Vulnerability.getAllMSs().contains(ms) && Vulnerability.getAllCVEs().contains(cve))
			return false;
		
		return null;
	}

	public Boolean isKnownRelationship_SWProduct_VUName()
	{
		String swproduct = getEntityTextGivenType(CyberEntityText.SWPRODUCT);
		String vuname = getEntityTextGivenType(CyberEntityText.VUNAME);
		
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
	
	public Boolean isKnownRelationship_SWProduct_VUMS()
	{
		String swproduct = getEntityTextGivenType(CyberEntityText.SWPRODUCT);
		String ms = getEntityTextGivenType(CyberEntityText.VUMS);
		
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
	
	public Boolean isKnownRelationship_SWProduct_VUCVE()
	{
		String swproduct = getEntityTextGivenType(CyberEntityText.SWPRODUCT);
		String cve = getEntityTextGivenType(CyberEntityText.VUCVE);
		
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

	public Boolean isKnownRelationship_SWVersion_VUName()
	{
		String swversion = getEntityTextGivenType(CyberEntityText.SWVERSION);
		String vuname = getEntityTextGivenType(CyberEntityText.VUNAME);
		
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
	
	public Boolean isKnownRelationship_SWVersion_VUMS()
	{
		String swversion = getEntityTextGivenType(CyberEntityText.SWVERSION);
		String ms = getEntityTextGivenType(CyberEntityText.VUMS);
		
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

	public Boolean isKnownRelationship_SWVersion_VUCVE()
	{
		String swversion = getEntityTextGivenType(CyberEntityText.SWVERSION);
		String cve = getEntityTextGivenType(CyberEntityText.VUCVE);
		
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

	public Boolean isKnownRelationship_SWProduct_FIName()
	{
		String swproduct = getEntityTextGivenType(CyberEntityText.SWPRODUCT);
		String filename = getEntityTextGivenType(CyberEntityText.FINAME);
		
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

	public Boolean isKnownRelationship_SWVersion_FIName()
	{
		String swversion = getEntityTextGivenType(CyberEntityText.SWVERSION);
		String filename = getEntityTextGivenType(CyberEntityText.FINAME);
		
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

	public Boolean isKnownRelationship_SWProduct_FUName()
	{
		String swproduct = getEntityTextGivenType(CyberEntityText.SWPRODUCT);
		String functionname = getEntityTextGivenType(CyberEntityText.FUNAME);
		
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

	public Boolean isKnownRelationship_SWVersion_FUName()
	{
		String swversion = getEntityTextGivenType(CyberEntityText.SWVERSION);
		String functionname = getEntityTextGivenType(CyberEntityText.FUNAME);
		
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

	public Boolean isKnownRelationship_VUName_FIName()
	{
		String vulnerabilityname = getEntityTextGivenType(CyberEntityText.VUNAME);
		String filename = getEntityTextGivenType(CyberEntityText.FINAME);
		
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

	public Boolean isKnownRelationship_VUCVE_FIName()
	{
		String cve = getEntityTextGivenType(CyberEntityText.VUCVE);
		String filename = getEntityTextGivenType(CyberEntityText.FINAME);
		
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

	public Boolean isKnownRelationship_VUMS_FIName()
	{
		String ms = getEntityTextGivenType(CyberEntityText.VUMS);
		String filename = getEntityTextGivenType(CyberEntityText.FINAME);
		
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

	public Boolean isKnownRelationship_VUName_FUName()
	{
		String vulnerabilityname = getEntityTextGivenType(CyberEntityText.VUNAME);
		String functionname = getEntityTextGivenType(CyberEntityText.FUNAME);
		
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

	public Boolean isKnownRelationship_VUCVE_FUName()
	{
		String cve = getEntityTextGivenType(CyberEntityText.VUCVE);
		String functionname = getEntityTextGivenType(CyberEntityText.FUNAME);
		
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

	public Boolean isKnownRelationship_VUMS_FUName()
	{
		String ms = getEntityTextGivenType(CyberEntityText.VUMS);
		String functionname = getEntityTextGivenType(CyberEntityText.FUNAME);
		
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
	

	public String getEntityTextGivenType(int entitytype)
	{
		if(entities[0].getEntityType() == entitytype)
			return entities[0].getEntitySpacedText();
		else if(entities[1].getEntityType() == entitytype)
			return entities[1].getEntitySpacedText();
		
		return null;
	}
	
	
	//Just for testing, loads all known relationships and aliaes from xml and json files and prints statistics about them.
	public static void main(String[] args)
	{
		loadAllKnownRelationships();
		
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
	
	public static HashSet<Integer> getAllRelationshipTypesSet()
	{
		return allrelationshiptypesset;
	}

	//A reverse relationship just has entities coming in the opposite order in the text.  We just gave these
	//reverse relationships IDs that are -1 times the original relationship's id.
	public static int getReverseRelationshipType(int relationshiptype)
	{
		return -relationshiptype;
	}
	
	
	public String toString()
	{
		return entities[0] + "\t" + entities[1] + "\t" + getRelationType();
	}
}
