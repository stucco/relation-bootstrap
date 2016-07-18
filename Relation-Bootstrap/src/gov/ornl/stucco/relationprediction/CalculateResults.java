package gov.ornl.stucco.relationprediction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class CalculateResults 
{
	//This program is very expensive to run, so if this variable gets turned on, we will run it on only a subset of the available data to save time.  This should be good enough to let us know if the program is working alright.
	private static boolean testingprogram = true;
	
	
	private static NumberFormat formatter = new DecimalFormat(".000");
	private static boolean printperfoldresults = false;
	

	private static HashSet<Integer> relationshiptypes = GenericCyberEntityTextRelationship.allpositiverelationshiptypesset;
	static
	{
		if(testingprogram)
		{
			relationshiptypes = new HashSet<Integer>();
			relationshiptypes.add(1);
		}
	}
	
	private static ParametersLine commandlineargumentconstraints = null;	//Not using this yet, but we may in the future want to make it possible to put constraints on what parameters are allowable.
	
	//private static boolean training = false;
	
	private static String entityextractedfilename;
	
	
	

	public static void main(String[] args) 
	{
		readArgs(args);

		for(int relationshiptype : relationshiptypes)
			printResults(relationshiptype);
	}
	
	private static void readArgs(String[] args)
	{
		entityextractedfilename = args[0];
		
		//for(int i = 1; i < args.length; i++)
		//{
		//	if("training".equals(args[i]))
		//		training = true;
		//}
	}
	
	
	
	private static void printResults(int relationshiptype)
	{
		ArrayList<RelationPrediction> testpredictions = new ArrayList<RelationPrediction>();
		
		ArrayList<ParametersLine> normalbestparameterslist = new ArrayList<ParametersLine>();
		ArrayList<ParametersLine> reversebestparameterslist = new ArrayList<ParametersLine>();
		ArrayList<ObjectRank> normalbestparameterslistranked = new ArrayList<ObjectRank>();
		ArrayList<ObjectRank> reversebestparameterslistranked = new ArrayList<ObjectRank>();
		
		//Collect all test set instances and do a bunch of bookkeeping along the way.  Simultaneously select the best set of parameters for predicting them via cross validation on the trainings set.
		for(Integer testfold : RunRelationSVMs.folds)
		{
			if(testfold != null)
			{
				//Read the predictions for when the entities come in the normal order.
				ParametersLine normalbestparameters = findBestParameters(commandlineargumentconstraints, relationshiptype, testfold);
				normalbestparameterslist.add(normalbestparameters);
				ArrayList<RelationPrediction> normalpredictionstoadd = readPredictions(normalbestparameters, testfold, relationshiptype, true, false);
				testpredictions.addAll(normalpredictionstoadd);
				{
					double[] fpr = getFPRForTestInstances(normalpredictionstoadd);
					double fscore = fpr[0];
					
					normalbestparameterslistranked.add(new ObjectRank(normalbestparameters, fscore));
				}
				
				
				//Add the predictions for when the entities come in the reverse order.
				ParametersLine reversebestparameters = findBestParameters(commandlineargumentconstraints, GenericCyberEntityTextRelationship.getReverseRelationshipType(relationshiptype), testfold);
				reversebestparameterslist.add(reversebestparameters);
				ArrayList<RelationPrediction> reversepredictionstoadd = readPredictions(reversebestparameters, testfold, relationshiptype, true, false);
				testpredictions.addAll(reversepredictionstoadd);
				testpredictions.addAll(normalpredictionstoadd);
				{
					double[] fpr = getFPRForTestInstances(reversepredictionstoadd);
					double fscore = fpr[0];
					
					reversebestparameterslistranked.add(new ObjectRank(reversebestparameters, fscore));
				}
				
				
				//If we want to print results for each fold, do it.
				if(printperfoldresults)
				{
					ArrayList<RelationPrediction> onefoldresults = new ArrayList<RelationPrediction>(normalpredictionstoadd);
					onefoldresults.addAll(reversepredictionstoadd);
					
					double[] fpr = getFPRForTestInstances(onefoldresults);
					double fscore = fpr[0];
					double precision = fpr[1];
					double recall = fpr[2];
					
					System.out.println(relationshiptype + "\t" + formatter.format(fscore) + "\t" + formatter.format(precision) + "\t" + formatter.format(recall) + "\t" + normalbestparameters+ "\t" + reversebestparameters);
				}
			}
		}
		
		
		//Calculate the results for the entire test set.
		double[] fpr = getFPRForTestInstances(testpredictions);
		double fscore = fpr[0];
		double precision = fpr[1];
		double recall = fpr[2];
		
		
		//Choose the set of parameters that got the highest f-score.
		Collections.sort(normalbestparameterslistranked);
		ParametersLine bestnormalparametersline = (ParametersLine)normalbestparameterslistranked.get(normalbestparameterslistranked.size()-1).obj;
		Collections.sort(reversebestparameterslistranked);
		ParametersLine bestreverseparametersline = (ParametersLine)reversebestparameterslistranked.get(reversebestparameterslistranked.size()-1).obj;
		
		
		String outputline = relationshiptype + "\t" + formatter.format(fscore) + "\t" + formatter.format(precision) + "\t" + formatter.format(recall) + "\t" + bestnormalparametersline + "\t" + bestreverseparametersline;
		System.out.println(outputline);
	
	
		//Also print it to a file because we'll want to retrieve the best parameters for making predictions on unknown relationships.
		try
		{
			File resultsfile = ProducedFileGetter.getResultsFile(entityextractedfilename, relationshiptype);
			PrintWriter out = new PrintWriter(new FileWriter(resultsfile));
			out.println(outputline);
			out.close();
		}catch(IOException e)
		{
			System.out.println(e);
			e.printStackTrace();
			System.exit(3);
		}
	}

	private static ParametersLine findBestParameters(ParametersLine cmdlineargconstraints, int relationshiptype, Integer testfold)
	{
		ArrayList<ParametersLine> allparameters = getAllParameters(cmdlineargconstraints, relationshiptype);

		double bestfscore = Double.NEGATIVE_INFINITY;
		ParametersLine bestfscoreparams = null;
		
		for(ParametersLine currentparameters : allparameters)
		{
			ArrayList<RelationPrediction> developmentpredictions = readPredictions(currentparameters, testfold, relationshiptype, true, true);
		
			double TPs = 0.;
			double FPs = 0.;
			double FNs = 0.;
			for(RelationPrediction instance : developmentpredictions)
			{
				int tpfptnfncode = instance.getTPorFPorTNorFN();
				if(tpfptnfncode == RelationPrediction.TPindex)
					TPs++;
				else if(tpfptnfncode == RelationPrediction.FPindex)
					FPs++;
				else if(tpfptnfncode == RelationPrediction.FNindex)
					FNs++;
			}
			double fscore = getFscore(TPs, FPs, FNs);
			//double precision = getPrecision(TPs, FPs);
			//double recall = getRecall(TPs, FNs);
			
			if(fscore > bestfscore)
			{
				bestfscore = fscore;
				bestfscoreparams = currentparameters;
			}
		}
		
		return bestfscoreparams;
	}
	
	private static ArrayList<ParametersLine> getAllParameters(ParametersLine cmdlineargconstraints, int relationshiptype)
	{
		ArrayList<ParametersLine> allparameters = new ArrayList<ParametersLine>();
		
		try
		{
				File resultsfile = ProducedFileGetter.getPredictionsFile(entityextractedfilename, relationshiptype, true);
				
				BufferedReader in = new BufferedReader(new FileReader(resultsfile));
				String line;
				while((line = in.readLine()) != null)
				{
					if(ParametersLine.isParametersLine(line))
					{
						ParametersLine current = new ParametersLine(line, false);
					
						if(cmdlineargconstraints == null || current.matchesConstraints(cmdlineargconstraints))
						{
							boolean foundmatch = false;
							for(ParametersLine pl : allparameters)
							{
								if(current.exactlyMatches(pl))
									foundmatch = true;
							}
							if(!foundmatch)
								allparameters.add(current);
						}
					}
				}
				in.close();
		}catch(IOException e)
		{
			System.out.println(e);
			e.printStackTrace();
			System.exit(3);
		}
		
		return allparameters;
	}

	//development should be sent true if we want development predictions, and false if we want test predictions.
	private static ArrayList<RelationPrediction> readPredictions(ParametersLine currentparameters, Integer testfold, int relationshiptype, boolean training, boolean development)
	{
		ArrayList<RelationPrediction> results = new ArrayList<RelationPrediction>();
		
		try
		{
				File resultsfile = ProducedFileGetter.getPredictionsFile(entityextractedfilename, relationshiptype, training);
				
				boolean developmentstate = false;
				boolean teststate = false;
			
				BufferedReader in = new BufferedReader(new FileReader(resultsfile));
				String line;
				while((line = in.readLine()) != null)
				{
					if(ParametersLine.isParametersLine(line))
					{
						developmentstate = false;	//by default, states are false, so reset it.
						teststate = false;
						
						ParametersLine pl = new ParametersLine(line, false);
						if(pl.exactlyMatches(currentparameters))
						{
							Integer resultfold = pl.getResultsFold();
							Integer excludedfold1 = pl.getExcludedFold1();
							Integer excludedfold2 = pl.getExcludedFold2();
							
							if(resultfold != testfold)	//Every fold other than the test fold is used for development.
							{
								if(excludedfold1 == testfold || excludedfold2 == testfold)	//A fold's results should be included in the development set only if the test fold was not used in training.
									developmentstate = true;
							}
							if(resultfold == testfold)	//We want test fold results.
							{
								if(excludedfold1 == testfold && excludedfold2 == testfold)
									teststate = true;
							}
						}
					}
					else
					{
						if(developmentstate && development)
							results.add(new RelationPrediction(line));
						if(teststate && !development)
							results.add(new RelationPrediction(line));
					}
				}
				in.close();
		}catch(IOException e)
		{
			System.out.println(e);
			e.printStackTrace();
			System.exit(3);
		}
		
		return results;
	}
	
	
	
	public static double getFscore(double tp, double fp, double fn)
	{
		double numerator = 2 * tp;
		double denominator = (2 * tp) + fp + fn;
		
		if(denominator == 0.)
			return 0.;
		
		return numerator / denominator;
	}

	public static double getPrecision(double tp, double fp)
	{
		if(tp + fp == 0.)
			return 0.;
		
		return tp / (tp + fp);
	}

	public static double getRecall(double tp, double fn)
	{
		if(tp + fn == 0.)
			return 0.;
		
		return tp / (tp + fn);
	}

	public static double[] getFPRForTestInstances(ArrayList<RelationPrediction> predictions)
	{
		double TPs = 0.;
		double FPs = 0.;
		double FNs = 0.;
		for(int i = 0; i < predictions.size(); i++)
		{
			RelationPrediction instance = predictions.get(i);
			int tpfptnfncode = instance.getTPorFPorTNorFN();
			if(tpfptnfncode == RelationPrediction.TPindex)
				TPs++;
			else if(tpfptnfncode == RelationPrediction.FPindex)
				FPs++;
			else if(tpfptnfncode == RelationPrediction.FNindex)
				FNs++;
		}
		double fscore = getFscore(TPs, FPs, FNs);
		double precision = getPrecision(TPs, FPs);
		double recall = getRecall(TPs, FNs);
		
		
		double[] result = {fscore, precision, recall};
		return result;
	}
	

}
