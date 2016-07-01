package gov.ornl.stucco;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class TrainRelationSVMClassifier 
{
	private static double[] cs = { 1., 10., 100., 1000., 10000.};
	private static double[] gammas = { .001, .01, .1, 1., 10., 100. };
	
	
	private static String entityextractedfilename;
	private static String contexts;
	private static String kerneltype;
	
	private static int folds = 5;
	
	
	public static void main(String[] args)
	{
		readArgs(args);
		
		
		
	}
	
	//Arguments: 
	//1. extractedfilename (This is the name of the file written by PrintPreprocessedDocuments.  
	//Valid known values for this argument are "original", "entityreplaced", and "aliasreplaced")
	//2. contexts (This argument tells us whether or not we want to use the context
	//preceding the first entity (first digit), whether or not we want to use the context between entities (second digit),
	//and whether or not we want to use the context after the second entity (third digit).  Valid
	//values for it are 000, 001, 010, 011, 100, 101, 110, or 111).
	//3. kerneltype (There are two types of kernels we are interested in training our SVM with, Linear, and RBF. 
	//If the difference is important to you, you should probably ask Dr. Bridges about them)
	private static void readArgs(String[] args)
	{
		entityextractedfilename = args[0];
		if( !(entityextractedfilename.equals("original") || entityextractedfilename.equals("entityreplaced") || entityextractedfilename.equals("aliasreplaced")) )
		{
			System.err.println("Error, invalid entityextractedfilename.  Entityextractedfilename must be original, entityreplaced, or aliasreplaced.");
			System.exit(3);
		}
		
		contexts = args[1];
		if(contexts.length() != 3 || 
				!(contexts.charAt(0) == '0' || contexts.charAt(0) == '1') ||
				!(contexts.charAt(1) == '0' || contexts.charAt(1) == '1') ||
				!(contexts.charAt(2) == '0' || contexts.charAt(2) == '1'))
		{
			System.err.println("Error, invalid context.  Context must be 000, 001, 010, 011, 100, 101, 110, or 111.");
			System.exit(3);
		}
		
		kerneltype = args[2];
		if( !(kerneltype.equals("RBF") || kerneltype.equals("Linear")) )
		{
			System.err.println("Error, invalid kernel type.  Kerneltype must be RBF or Linear.");
			System.exit(3);
		}
	}
	
	
	
	
}
