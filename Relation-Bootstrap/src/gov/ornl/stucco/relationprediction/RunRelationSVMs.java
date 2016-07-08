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




public class RunRelationSVMs
{
	private static String pid;	//In the even that we have multiple instances of this program running, we will use each process's pid to ensure that their temporary files do not interfere with eachother.
	static
	{
		pid = ManagementFactory.getRuntimeMXBean().getName();
		//pid = pid.replaceAll("@", "");
		//pid = pid.substring(0, pid.indexOf('.'));
	}
	
	
	//We may have previously run this program and thus have already trained some model files.  Set this to false if we do not want to train a new model in this case.
	private static boolean alwaysretrain = true;
	
	//This program is very expensive to run, so if this variable gets turned on, we will run it on only a subset of the available data to save time.  This should be good enough to let us know if the program is working alright.
	private static boolean testingprogram = true;
	
	
	private static double[] cs = { 100., 1000., 10000., 100000};
	private static double[] gammas = { .001, .01, .1, 1., 10., 100. };
	private static String[] kerneltypes = {"Linear", "RBF"};
	
	
	private static String entityextractedfilename;
	private static String contexts;
	
	
	//private static int folds = 5;
	//This is the number of folds we use for cross validation.  The null at the end handles the real world case where we want to use all the training data possible.  This is kind of an ugly solution, but not as ugly as some of the alternatives for solving this problem.
	private static Integer[] folds = {0, 1, 2, 3, 4, null};
	
	
	public static void main(String[] args) throws IOException, InterruptedException
	{
		readArgs(args);
		
		
		//We need a bunch of temporary files to hold different stuff repeatedly.  Build them, and give them names with this process's id to avoid collisions with other instances of this program that might be running.
		File trainingfile = ProducedFileGetter.getTemporaryFile("TR." + pid);
		File trainingfilecomments = ProducedFileGetter.getTemporaryFile("TRC." + pid);
		File testfile1 = ProducedFileGetter.getTemporaryFile("TE1." + pid);
		File testfile1comments = ProducedFileGetter.getTemporaryFile("TEC1." + pid);
		File testpredictionsfile1 = ProducedFileGetter.getTemporaryFile("PR1." + pid);
		File testfile2 = ProducedFileGetter.getTemporaryFile("TE2." + pid);
		File testfile2comments = ProducedFileGetter.getTemporaryFile("TEC2." + pid);
		File testpredictionsfile2 = ProducedFileGetter.getTemporaryFile("PR2." + pid);
		
		
		File libsvmjar = ProducedFileGetter.getLibSVMJarFile();
		
		
		//Train a classifier for each relationship type.
		for(int relationtype : GenericCyberEntityTextRelationship.getAllRelationshipTypesSet())
		{
			//For testing, we will only pay attention to one relationship type, as this program is about 50 times as expensive to run for all relationship types.
			if(testingprogram && relationtype != 1)
				continue;
			
			
			PrintWriter testresultsout = new PrintWriter(new FileWriter(ProducedFileGetter.getResultsFile(entityextractedfilename, contexts, relationtype)));
			
			//In order to do cross-validation, we need to set aside two folds for tuning parameters and testing.  So testfold1 and testfold2 will be those folds.  All the other folds can be used for training.
			for(int t1index = 0; t1index < folds.length; t1index++)
			//for(int testfold1 = 0; testfold1 < folds; testfold1++)
			{
				Integer testfold1 = folds[t1index];
				
				for(int t2index = t1index; t2index < folds.length; t2index++)
				//for(int testfold2 = testfold1; testfold2 < folds; testfold2++)
				{
					Integer testfold2 = folds[t2index];
					
					
					//As mentioned above, the null value is included because of the case where we want to use all data for training and testing. But this is only one case.  We do not need to pair null with any other test fold.  So if either of the two testfolds == null, just skip this iteration.  But if both of them are null, do this special training and testing.
					if( ( testfold1 == null || testfold2 == null ) && !(testfold1 == null && testfold2 == null) )
						continue;
					
					
					//Write the training data to a temporary file.  Since testing is, by comparison, super fast, run testing too.
					writeSVMFiles(relationtype, testfold1, testfold2, entityextractedfilename, contexts, trainingfile, trainingfilecomments, testfile1, testfile1comments, testfile2, testfile2comments);
						
					
					//Our SVMs have two parameters, c and gamma.  Iterate over all combinations of them so that we can do a grid search. (Gamma is not needed for linear kernels, so we set the gammas array to have only one value in readArgs() if the kernel type chosen was Linear).
					for(String kerneltype : kerneltypes)
					{
						for(double c : cs)
						{
							for(double gamma : gammas)
							{
								File modelfile = ProducedFileGetter.getSVMModelFile(kerneltype, entityextractedfilename, contexts, relationtype, testfold1, testfold2, c, gamma);
							
								String line;
								if(!modelfile.exists() || alwaysretrain)
								{
									String[] trainarray = null;
									if(kerneltype.equals("Linear"))
										trainarray = new String[]{"java", "-cp", libsvmjar.getAbsolutePath(), "svm_train", "-s", "0", "-t", "0", "-c", "" + c, trainingfile.getAbsolutePath(), modelfile.getAbsolutePath()};
									else if(kerneltype.equals("RBF"))
										trainarray = new String[]{"java", "-cp", libsvmjar.getAbsolutePath(), "svm_train", "-s", "0", "-t", "2", "-c", "" + c, "-g", "" + gamma, trainingfile.getAbsolutePath(), modelfile.getAbsolutePath()};
								

									Process process = (new ProcessBuilder(trainarray)).start();
									BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));
									while ((line = br.readLine()) != null)
										System.out.println(line);
									process.waitFor();
								}
							
							
								//Apply the model to test instances
								String[] t1array = {"java", "-cp",  libsvmjar.getAbsolutePath(), "svm_predict", testfile1.getAbsolutePath(), modelfile.getAbsolutePath(), testpredictionsfile1.getAbsolutePath()};
								Process t1process = (new ProcessBuilder(t1array)).start();
								BufferedReader br = new BufferedReader(new InputStreamReader(t1process.getErrorStream()));
								while ((line = br.readLine()) != null)
									System.out.println(line);
								t1process.waitFor();
							
								//And print the resulting classified instances to a result file.
								printResultsFile(testfold1 + "-" + getFoldSplitString(testfold1, testfold2) + " " + "kerneltype=" + kerneltype + " " + "c=" + c + " " + "gamma=" + gamma + " " + "context=" + contexts, testresultsout, testfile1, testfile1comments, testpredictionsfile1);
							
							
								//Recall that we left two folds out of the training set.  If those two folds are not the same, also apply the model to and print the results from the other fold.
								if(testfold1 != testfold2 && (testfold1 != null & testfold2 != null))
								{
									String[] t2array = {"java", "-cp", libsvmjar.getAbsolutePath(), "svm_predict", testfile2.getAbsolutePath(), modelfile.getAbsolutePath(), testpredictionsfile2.getAbsolutePath()};
									Process t2process = (new ProcessBuilder(t2array)).start();
									br = new BufferedReader(new InputStreamReader(t2process.getErrorStream()));
									while ((line = br.readLine()) != null)
										System.out.println(line);
									t2process.waitFor();
								
									printResultsFile(testfold2 + "-" + getFoldSplitString(testfold1, testfold2) + " " + "kerneltype=" + kerneltype + " " + "c=" + c + " " + "gamma=" + gamma + " " + "context=" + contexts, testresultsout, testfile2, testfile2comments, testpredictionsfile2);
								}
							
								//We only care about the predictions in most cases.  We need to save the model only when we are training on the entire dataset because that model may be used to make real predictions for stucco.
								if(!(testfold1 == null && testfold2 == null))
									modelfile.delete();
							}
						}
					}
				}
			}
			testresultsout.close();
		}
		
		
		//Delete all our temporary files.
		trainingfile.delete();
		testfile1.delete();
		testpredictionsfile1.delete();
		testfile2.delete();
		testpredictionsfile2.delete();
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
		
		//kerneltype = args[2];
		//if( !(kerneltype.equals("RBF") || kerneltype.equals("Linear")) )
		//{
		//	System.err.println("Error, invalid kernel type.  Kerneltype must be RBF or Linear.");
		//	System.exit(3);
		//}
		//else if(kerneltype.equals("Linear"))	//Linear kernels do not have a gamma parameter, so reset the gammas array to have only one value to reduce redundant processing.
		//{
		//	double[] holder = { 0. };
		//	gammas = holder;
		//}
	}
	
	
	private static void writeSVMFiles(int relationtype, Integer excludedfold1, Integer excludedfold2, String entityextractedfilename, String contexts, File trainingfile, File trainingfilecomments, File testfile1, File testfile1comments, File testfile2, File testfile2comments) 
	{
		File relationinstancesfile = ProducedFileGetter.getRelationshipSVMInstancesFile(entityextractedfilename, contexts, relationtype);
	
		try
		{
			boolean foundatleastonetrainingexample = false;	//We crash if there are 0 training instances, so keep track of whether we find at least one.  If we don't, we'll make an artificial one.
			
			BufferedReader in = new BufferedReader(new FileReader(relationinstancesfile));
			PrintWriter trainingout =  new PrintWriter(new FileWriter(trainingfile));
			PrintWriter trainingoutcomments = new PrintWriter(new FileWriter(trainingfilecomments));	//It is dumb that we have to save comments separately.  LibSVM documents that it is okay to include comments after a '#' in the training and test files, but it is wrong.  It fails if it finds a '#'.
			PrintWriter testout1 = new PrintWriter(new FileWriter(testfile1));
			PrintWriter testout1comments = new PrintWriter(new FileWriter(testfile1comments));
			PrintWriter testout2 = new PrintWriter(new FileWriter(testfile2));
			PrintWriter testout2comments = new PrintWriter(new FileWriter(testfile2comments));
			String line;
			while((line = in.readLine()) != null)
			{
				Integer whichfold = isInWhichFold(line, excludedfold1, excludedfold2);
				String[] instanceAndcomments = line.split("#");
				
				if(whichfold == 0 || whichfold == 3)	//The instance is in neither test fold, and can therefore be used for training, or we are in the special case where we are processing all folds as training and test data.
				{
					if(!line.startsWith("0"))	//Use this line for training only if we can confidently heuristically classify it (and thus the line would start with "+1" or "-1").
					{
						trainingout.println(instanceAndcomments[0]);
						trainingoutcomments.println(instanceAndcomments[1]);
						
						foundatleastonetrainingexample = true;
					}
				}
				if(whichfold == 1 || whichfold == 3)	//The instance is in test fold 1, or we are in the special case where we are processing all folds as training and test data.
				{
					testout1.println(instanceAndcomments[0]);
					testout1comments.println(instanceAndcomments[1]);
				}
				if(whichfold == 2 || whichfold == 3)	//The instance is in test fold 2, or we are in the special case where we are processing all folds as training and test data.
				{
					testout2.println(instanceAndcomments[0]);
					testout2comments.println(instanceAndcomments[1]);
				}
			}
			
			//If we did not find at least one training example, just make a fake one so we do not crash.  Every test instance for this training set will fall into the one instance's class.
			if(!foundatleastonetrainingexample)
			{
				trainingout.println("-1");
				trainingoutcomments.println("0 0 x 0 x");
			}
			
			in.close();
			trainingout.close();
			trainingoutcomments.close();
			testout1.close();
			testout1comments.close();
			testout2.close();
			testout2comments.close();
		}catch(IOException e)
		{
			System.out.println(e);
			e.printStackTrace();
			System.exit(3);
		}
	}
	
	//Returns 0 if the instance is in one of the training folds, 1 if it is in excludedfold1, or 2 if it is in excludedfold2.  Returns 3 in the special case where both excluded folds are null (the case where we want to train and test on all available instances).
	public static int isInWhichFold(String instanceline, Integer excludedfold1, Integer excludedfold2)
	{
		String[] splitline = instanceline.split("#");
		String linecomment = splitline[1];
		String sentencenumstring = linecomment.substring(1, linecomment.indexOf(' ', 1));
		int sentencenum = Integer.parseInt(sentencenumstring);
		
		int modnum = sentencenum % (folds.length - 1);
		
		if(excludedfold1 == null && excludedfold2 == null)
			return 3;
		if(modnum == excludedfold1)
			return 1;
		else if(modnum == excludedfold2)
			return 2;
		else
			return 0;
	}
	

	//This is a little helper function for constructing the code that explains which test folds were not used in training the model that produced these results.
	public static String getFoldSplitString(Integer testfold1, Integer testfold2)
	{
		if(testfold1 == null)
			return testfold2 + "," + testfold1;
		else if(testfold2 == null)
			return testfold1 + "," + testfold2;
		else if(testfold1 < testfold2)
			return testfold1 + "," + testfold2;
		else
			return testfold2 + "," + testfold1;
	}
	
	

	//We used the SVM to classify test instances in the main method, but we have not stored the resulting predictions in a useful way yet.  So do that.
	private static void printResultsFile(String firstline, PrintWriter resultsout, File testdatafile, File testcommentsfile, File testpredictionsfile) throws IOException
	{
		//Since we are writing all results for one relationship type to the same file, we need some way to distinguish what parameters are used to generate a particular result shown in the results file.  This first line tells what parameters were used to generate all instances that follow it.
		resultsout.println(firstline);
		
		
		BufferedReader testdatain = new BufferedReader(new FileReader(testdatafile));
		BufferedReader testcommentsin = new BufferedReader(new FileReader(testcommentsfile));
		BufferedReader testpredictionsin = new BufferedReader(new FileReader(testpredictionsfile));
	
		
		String dataline = testdatain.readLine();
		String commentline = testcommentsin.readLine();
		String predictionline = testpredictionsin.readLine();
		while(dataline != null)
		{
			String truelabel = dataline.substring(0, dataline.indexOf(' '));
			//String instanceid = dataline.split("#")[1];
			
			long predictedlabel = Math.round(Double.parseDouble(predictionline));

			resultsout.println(truelabel + "/" + predictedlabel + " #" + commentline);
			
			dataline = testdatain.readLine();
			predictionline = testpredictionsin.readLine();
			commentline = testcommentsin.readLine();
		}
		testdatain.close();
		testcommentsin.close();
		testpredictionsin.close();
	}
}
