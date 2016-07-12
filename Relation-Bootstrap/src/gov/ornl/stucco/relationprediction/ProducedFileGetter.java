/*
 * The purpose of this class is to be a central place for getting the path to a desired file given
 * information about the file.  I want a central class for this purpose because this Java project
 * needs to share some files with a related Python project, and I thought it would be easiest to
 * keep the file names consistent between the two projects if the Java portion retrieved all file
 * locations from the same class.
 * 
 * It is very important to note that these methods assume that ProducedFileGetter's class file
 * must be located somewhere within a directory called "relation-bootstrap".  It can be nested
 * arbitrarily deeply, or even packaged in a jar file as long as it is there.  See the static 
 * block near the top where I set the project directories for how I do this.
 */

package gov.ornl.stucco.relationprediction;

import java.io.File;
import java.net.URISyntaxException;


public class ProducedFileGetter 
{
	private static File producedfilesdirectory;
	private static File gitprojectdirectory;
	private static File datafilesdirectory;
	static
	{
		//String currentdirectorypath = System.getProperty("user.dir");
		//String currentdirectorypath = System.getProperty("user.home");	//Get the user's home directory instead of their current working directory.
		//File currentdirectory = new File(currentdirectorypath);
		//shareddirectory = new File(currentdirectory.getParent(), "RelationExtractionProducedFiles/");
		
		try
		{
			gitprojectdirectory = new File(ProducedFileGetter.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
		
			if(!gitprojectdirectory.getName().equals("relation-bootstrap"))
			{
				while(!gitprojectdirectory.getParentFile().getName().equals("relation-bootstrap"))
					gitprojectdirectory = gitprojectdirectory.getParentFile();
				gitprojectdirectory = gitprojectdirectory.getParentFile();
			}
			producedfilesdirectory = new File(gitprojectdirectory, "ProducedFiles/");
			datafilesdirectory = new File(gitprojectdirectory, "DataFiles/");
		}catch(URISyntaxException e) 
		{
			System.out.println(e);
			e.printStackTrace();
			System.exit(3);
		}
	}
	
	
	
	
	//The file containing a vector for each word in our vocabulary.  This file is written by the Python program TrainModel.py.
	//trainingtype is one of the three entity extracted text types mentioned above getEntityExtractedText(String filename).
	public static File getWordVectorsFile(String trainingtype)
	{
		File dir = new File(producedfilesdirectory, "Models/");
		dir.mkdirs();
		
		return new File(dir, "wordvectors." + trainingtype);
	}
	
	//Known possible filenames are "original", "entityreplaced", and "aliasreplaced"
	public static File getEntityExtractedText(String filename)
	{
		File dir = new File(producedfilesdirectory, "EntityExtractedText/");
		dir.mkdirs();
		
		return new File(dir, filename + ".zip");
	}
	
	//SVM training files
	//entityextractedfilename is the name of the text file that has entities replaced with tokens.  
	//getEntityExtractedText(String filename) will previously have been used to get this file for writing.
	//contexts is a three digit code composed of 0s and 1s indicating which context windows are/were used to 
	//generate the contents of the file.
	public static File getRelationshipSVMInstancesFile(String entityextractedfilename, String contexts, int relationshiptype)
	{
		File dir = new File(producedfilesdirectory, "InstanceFiles/");
		dir.mkdirs();
		
		String filename = "RelationInstances." + entityextractedfilename + "." + contexts + "." + relationshiptype;
		
		return new File(dir, filename);
	}
	
	
	//This file contains predictions made by an SVM.
	public static File getResultsFile(String entityextractedfilename, String contexts, int relationshiptype)
	{
		File dir = new File(producedfilesdirectory, "ResultFiles/");
		dir.mkdirs();
		
		String filename = "Results." + entityextractedfilename + "." + contexts + "." + relationshiptype;
		
		return new File(dir, filename);
	}

	
	
	
	public static File getSVMModelFile(String kerneltype, String entityextractedfilename, String contexts, int relationshiptype, Integer excludedfold1, Integer excludedfold2, double c, double gamma)
	{
		File dir = new File(producedfilesdirectory, "SVMModelFiles/");
		dir.mkdirs();
		
		//Replace the decimals in c and gamma with commas because periods are used as separators in the file name.
		String cstring = ("" + c).replaceAll("\\.", ",");
		String gammastring = ("" + gamma).replaceAll("\\.", ",");
		
		String filename = "SVMModel." + kerneltype + "." + entityextractedfilename + "." + contexts + "." + relationshiptype + "." + excludedfold1 + "." + excludedfold2 + "." + cstring + "." + gammastring;
		
		return new File(dir, filename);
	}
	
	
	public static File getEntityExtractedSerializedDirectory()
	{
		File dir = new File(datafilesdirectory, "EntityExtractedSerialized/");
		dir.mkdirs();
		
		return dir;
	}
	
	
	public static File getLibSVMJarFile()
	{
		return new File(gitprojectdirectory, "lib/libsvm.jar");
	}
	
	
	public static File getTemporaryFile(String tempfilename)
	{
		File result = new File(producedfilesdirectory, "temp/" + tempfilename);
		result.getParentFile().mkdirs();
		
		return result;
	}
	
	
	public static File getLemmatizedWikipediaFile(int filenum)
	{
		File result = new File(producedfilesdirectory, "WikipediaLemmatized/wikipedialemmatized" + filenum + ".zip");
		result.getParentFile().mkdirs();
		
		return result;
	}
	
	
	public static File getNVDXMLDir()
	{
		File result = new File(datafilesdirectory, "nvdxml/");
		
		return result;
	}
	
	
	public static File getSourcesJSONFile()
	{
		File result = new File(datafilesdirectory, "Sources/sources.json");
		
		return result;
	}
	
	
	//Purely for testing
	public static void main(String[] args)
	{
		System.out.println(getEntityExtractedText("original").getAbsolutePath());
	}
}
