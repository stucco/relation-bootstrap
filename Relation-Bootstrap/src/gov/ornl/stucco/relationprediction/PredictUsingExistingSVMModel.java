package gov.ornl.stucco.relationprediction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;




public class PredictUsingExistingSVMModel
{
	//This program is very expensive to run, so if this variable gets turned on, we will run it on only a subset of the available data to save time.  This should be good enough to let us know if the program is working alright.
	private static boolean testingprogram = true;
	
	
	private static String pid;	//In the even that we have multiple instances of this program running, we will use each process's pid to ensure that their temporary files do not interfere with eachother.
	static
	{
		pid = ManagementFactory.getRuntimeMXBean().getName();
		//pid = pid.replaceAll("@", "");
		//pid = pid.substring(0, pid.indexOf('.'));
	}
	
	private static String entityextractedfilename;
	
	
	public static void main(String[] args) throws IOException, InterruptedException
	{
		readArgs(args);
		
		
		//We need a bunch of temporary files to hold different stuff repeatedly.  Build them, and give them names with this process's id to avoid collisions with other instances of this program that might be running.
		File testfile1 = ProducedFileGetter.getTemporaryFile("TE1." + pid);
		File testfile1comments = ProducedFileGetter.getTemporaryFile("TEC1." + pid);
		File testpredictionsfile1 = ProducedFileGetter.getTemporaryFile("PR1." + pid);
		
		
		File libsvmjar = ProducedFileGetter.getLibSVMJarFile();
		
		
		//Train a classifier for each relationship type.
		for(int positiverelationtype : GenericCyberEntityTextRelationship.allpositiverelationshiptypesset)
		{
			//For testing, we will only pay attention to one relationship type, as this program is about 50 times as expensive to run for all relationship types.
			if(testingprogram && positiverelationtype != 1)
				continue;
			
			int[] relationandreverse = {positiverelationtype, -positiverelationtype};
			for(int relationtype : relationandreverse)
			{
				ParametersLine bestparameters = getBestParameters(entityextractedfilename, relationtype);
				
				HashMap<String,String> bestparametersTovalues = bestparameters.getParametersTovalues();
				String context = bestparametersTovalues.get("context");
				String kerneltype = bestparametersTovalues.get("kerneltype");
				double c = Double.parseDouble(bestparametersTovalues.get("c"));
				double gamma = Double.parseDouble(bestparametersTovalues.get("gamma"));
			
				PrintWriter testresultsout = new PrintWriter(new FileWriter(ProducedFileGetter.getPredictionsFile(entityextractedfilename, relationtype, false)));
			
				
				//Write the training data to a temporary file.  Since testing is, by comparison, super fast, run testing too.
				writeSVMFiles(relationtype, entityextractedfilename, context, testfile1, testfile1comments);
						
					
				File modelfile = ProducedFileGetter.getSVMModelFile(kerneltype, entityextractedfilename, context, relationtype, null, null, c, gamma);
							
							
				//Apply the model to test instances
				String[] t1array = {"java", "-cp",  libsvmjar.getAbsolutePath(), "svm_predict", testfile1.getAbsolutePath(), modelfile.getAbsolutePath(), testpredictionsfile1.getAbsolutePath()};
				Process t1process = (new ProcessBuilder(t1array)).start();
				BufferedReader br = new BufferedReader(new InputStreamReader(t1process.getErrorStream()));
				String line;
				while ((line = br.readLine()) != null)
					System.out.println(line);
				t1process.waitFor();
							
				
				//And print the resulting classified instances to a result file.
				RunRelationSVMs.printResultsFile("null-" + RunRelationSVMs.getFoldSplitString(null, null) + " " + "kerneltype=" + kerneltype + " " + "c=" + c + " " + "gamma=" + gamma + " " + "context=" + context, testresultsout, testfile1, testfile1comments, testpredictionsfile1);
					
				
				testresultsout.close();
			}
		}
		

		//Delete all our temporary files.
		testfile1.delete();
		testpredictionsfile1.delete();
	}
	
	//Arguments: 
	//1. extractedfilename (This is the name of the file written by PrintPreprocessedDocuments.  
	//Valid known values for this argument are "original", "entityreplaced", and "aliasreplaced")
	private static void readArgs(String[] args)
	{
		entityextractedfilename = args[0];
		if( !(entityextractedfilename.equals("original") || entityextractedfilename.equals("entityreplaced") || entityextractedfilename.equals("aliasreplaced")) )
		{
			System.err.println("Error, invalid entityextractedfilename.  Entityextractedfilename must be original, entityreplaced, or aliasreplaced.");
			System.exit(3);
		}
	}
	
	
	private static void writeSVMFiles(int relationtype, String entityextractedfilename, String contexts, File testfile1, File testfile1comments) 
	{
		File relationinstancesfile = ProducedFileGetter.getRelationshipSVMInstancesFile(entityextractedfilename, contexts, relationtype, false);
	
		try
		{
			BufferedReader in = new BufferedReader(new FileReader(relationinstancesfile));
			PrintWriter testout1 = new PrintWriter(new FileWriter(testfile1));
			PrintWriter testout1comments = new PrintWriter(new FileWriter(testfile1comments));
			String line;
			while((line = in.readLine()) != null)
			{
				String[] instanceAndcomments = line.split("#");
				
				testout1.println(instanceAndcomments[0]);
				testout1comments.println(instanceAndcomments[1]);
			}
			in.close();
			testout1.close();
			testout1comments.close();
		}catch(IOException e)
		{
			System.out.println(e);
			e.printStackTrace();
			System.exit(3);
		}
	}
	
	
	public static ParametersLine getBestParameters(String entityextractedfilename, int relationshiptype)
	{
		ParametersLine result = null;
		
		try
		{
			File f = ProducedFileGetter.getResultsFile(entityextractedfilename, Math.abs(relationshiptype));
			BufferedReader in = new BufferedReader(new FileReader(f));
			String line = in.readLine();
			in.close();
			
			//relationshiptype + "\t" + formatter.format(fscore) + "\t" + formatter.format(precision) + "\t" + formatter.format(recall) + "\t" + bestnormalparametersline + "\t" + bestreverseparametersline;
			String[] splitline = line.split("\t");
			String normalparametersstring = splitline[splitline.length-2];
			String reverseparametersstring = splitline[splitline.length-1];
			
			if(relationshiptype > 0)
				result = new ParametersLine(normalparametersstring, true);
			else if(relationshiptype < 0)
				result = new ParametersLine(reverseparametersstring, true);
		}catch(IOException e)
		{
			System.out.println(e);
			e.printStackTrace();
			System.exit(3);
		}
		
		return result;
	}
	
	
}
