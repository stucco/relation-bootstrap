/*
 * The purpose of this class is to be a central place for getting the path to a desired file given
 * information about the file.  I want a central class for this purpose because this Java project
 * needs to share some files with a related Python project, and I thought it would be easiest to
 * keep the file names consistent between the two projects if the Java portion retreived all file
 * locations from the same class.
 * 
 * I think I am assuming that it is being run from the java project's base directory, but I am 
 * not sure.  This matters because I try to follow a relative path from the project's base 
 * directory to various files we need in the different methods.  See the static block near the top
 * where I set the shared directory for how I do this.
 */

package gov.ornl.stucco;

import java.io.File;

public class ProducedFileGetter 
{
	private static File shareddirectory;
	static
	{
		String currentdirectorypath = System.getProperty("user.dir");
		File currentdirectory = new File(currentdirectorypath);
		shareddirectory = new File(currentdirectory.getParent(), "producedfiles/");
	}
	
	
	
	
	//The file containing a vector for each word in our vocabulary.  This file is written by the Python program TrainModel.py.
	//trainingtype is one of the three entity extracted text types mentioned above getEntityExtractedText(String filename).
	public static File getWordVectorsFile(String trainingtype)
	{
		File dir = new File(shareddirectory, "models/");
		dir.mkdirs();
		
		return new File(dir, "wordvectors." + trainingtype);
	}
	
	//Known possible filenames are "original", "entityreplaced", and "aliasreplaced"
	public static File getEntityExtractedText(String filename)
	{
		File dir = new File(shareddirectory, "entityextractedtext/");
		dir.mkdirs();
		
		return new File(dir, filename);
	}
	
	//SVM training files
	//entityextractedfilename is the name of the text file that has entities replaced with tokens.  
	//getEntityExtractedText(String filename) will previously have been used to get this file for writing.
	//contexts is a three digit code composed of 0s and 1s indicating which context windows are/were used to 
	//generate the contents of the file.
	public static File getRelationshipSVMInstancesFile(String entityextractedfilename, String contexts, int relationshiptype)
	{
		File dir = new File(shareddirectory, "instancefiles/");
		dir.mkdirs();
		
		String filename = "RelationInstances." + entityextractedfilename + "." + contexts + "." + relationshiptype;
		
		return new File(dir, filename);
	}
	
	
	//This file contains predictions made by an SVM.
	public static File getResultsFile(String kerneltype, String entityextractedfilename, String contexts, int relationshiptype)
	{
		File dir = new File(shareddirectory, "resultfiles/");
		dir.mkdirs();
		
		String filename = "Results." + kerneltype + "." + entityextractedfilename + "." + contexts + "." + relationshiptype;
		
		return new File(dir, filename);
	}

	
	
	
	public static File getSVMModelFile(String kerneltype, String entityextractedfilename, String contexts, int relationshiptype, Integer excludedfold1, Integer excludedfold2, double c, double gamma)
	{
		File dir = new File(shareddirectory, "svmmodelfiles/");
		dir.mkdirs();
		
		//Replace the decimals in c and gamma with commas because periods are used as separators in the file name.
		String cstring = ("" + c).replaceAll("\\.", ",");
		String gammastring = ("" + gamma).replaceAll("\\.", ",");
		
		String filename = "SVMModel." + kerneltype + "." + entityextractedfilename + "." + contexts + "." + relationshiptype + "." + excludedfold1 + "." + excludedfold2 + "." + cstring + "." + gammastring;
		
		return new File(dir, filename);
	}
	
	
	public static File getEntityExtractedSerializedDirectory()
	{
		File dir = new File(shareddirectory, "entityextractedserialized/");
		dir.mkdirs();
		
		return dir;
	}
	
	
	public static File getLibSVMJarFile()
	{
		String currentdirectorypath = System.getProperty("user.dir");
		File currentdirectory = new File(currentdirectorypath);
		
		return new File(currentdirectory, "lib/libsvm.jar");
	}
	
	
	public static File getTemporaryFile(String tempfilename)
	{
		File result = new File(shareddirectory, "temp/" + tempfilename);
		result.getParentFile().mkdirs();
		
		return result;
	}
	
	
	public static File getLemmatizedWikipediaFile()
	{
		File result = new File(shareddirectory, "WikipediaLemmatized/wikipedialemmatized.zip");
		result.getParentFile().mkdirs();
		
		return result;
	}
	
	
	//Purely for testing
	public static void main(String[] args)
	{
		System.out.println(getEntityExtractedText("original").getAbsolutePath());
	}
}
