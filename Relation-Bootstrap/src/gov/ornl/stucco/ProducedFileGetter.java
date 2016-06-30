/*
 * The purpose of this class is to be a central place for getting the path to a desired file given
 * information about the file.  I want a central class for this purpose because this Java project
 * needs to share some files with a related Python project, and I thought it would be easiest to
 * keep the file names consistent between the two projects if the Java portion retreived all file
 * locations from the same class.
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
	public static File getWordVectorsFile()
	{
		File dir = new File(shareddirectory, "models/");
		dir.mkdirs();
		
		return new File(dir, "wordvectors");
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
	public static File getRelationshipTrainingFile(String entityextractedfilename, String contexts, int relationshiptype)
	{
		File dir = new File(shareddirectory, "svmfiles/");
		
		String filename = "Training." + entityextractedfilename + "." + contexts + "." + relationshiptype;
		
		return new File(dir, filename);
	}
	
	
	public static File getEntityExtractedSerializedDirectory()
	{
		File dir = new File(shareddirectory, "entityextractedserialized/");
		dir.mkdirs();
		
		return dir;
	}
	
	
	//Purely for testing
	public static void main(String[] args)
	{
		System.out.println(getEntityExtractedText("original").getAbsolutePath());
	}
}
