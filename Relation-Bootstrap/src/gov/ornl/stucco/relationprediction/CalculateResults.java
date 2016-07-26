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
	private static boolean printperfoldresults = true;
	
	
	private static boolean predictallpositive = false;
	

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
	private static String featuretypes;
	
	
	

	public static void main(String[] args) 
	{
		readArgs(args);

		for(int relationshiptype : relationshiptypes)
			printResults(entityextractedfilename, featuretypes, relationshiptype);
	}
	
	private static void readArgs(String[] args)
	{
		entityextractedfilename = args[0];

		featuretypes = FeatureMap.getOrderedFeatureTypes(args[1]);
		
		for(int i = 2; i < args.length; i++)
		{
			if("allpositive".equals(args[i]))
				predictallpositive = true;
		}
	}
	
	
	private static void printResults(String entityextractedfilename, String featuretypes, int relationshiptype)
	{
		ArrayList<RelationPrediction> testpredictions = new ArrayList<RelationPrediction>();
		
		ArrayList<ParametersLine> normalbestparameterslist = new ArrayList<ParametersLine>();
		ArrayList<ParametersLine> reversebestparameterslist = new ArrayList<ParametersLine>();
		ArrayList<ObjectRank> normalbestparameterslistranked = new ArrayList<ObjectRank>();
		ArrayList<ObjectRank> reversebestparameterslistranked = new ArrayList<ObjectRank>();
		
		try
		{
			File resultsfile = ProducedFileGetter.getResultsFile(entityextractedfilename, featuretypes, relationshiptype);
			if(predictallpositive)
				resultsfile = ProducedFileGetter.getResultsFile(entityextractedfilename, FeatureMap.ALWAYSPREDICTPOSITIVECODE, relationshiptype);
			PrintWriter out = new PrintWriter(new FileWriter(resultsfile));
		
			//Collect all test set instances and do a bunch of bookkeeping along the way.  Simultaneously select the best set of parameters for predicting them via cross validation on the trainings set.
			for(Integer testfold : RunRelationSVMs.folds)
			{
				if(testfold != null)
				{
					//Read the predictions for when the entities come in the normal order.
					ParametersLine normalbestparameters = findBestParameters(commandlineargumentconstraints, entityextractedfilename, featuretypes, relationshiptype, testfold);
					normalbestparameterslist.add(normalbestparameters);
					ArrayList<RelationPrediction> normalpredictionstoadd = readPredictions(normalbestparameters, entityextractedfilename, featuretypes, testfold, relationshiptype, true, false);
					testpredictions.addAll(normalpredictionstoadd);
					{
						double[] fpr = getFPRForTestInstances(normalpredictionstoadd);
						double fscore = fpr[0];
					
						normalbestparameterslistranked.add(new ObjectRank(normalbestparameters, fscore));
					}
				
					
					//Add the predictions for when the entities come in the reverse order.
					ParametersLine reversebestparameters = findBestParameters(commandlineargumentconstraints, entityextractedfilename, featuretypes, GenericCyberEntityTextRelationship.getReverseRelationshipType(relationshiptype), testfold);
					reversebestparameterslist.add(reversebestparameters);
					ArrayList<RelationPrediction> reversepredictionstoadd = readPredictions(reversebestparameters, entityextractedfilename, featuretypes, testfold, relationshiptype, true, false);
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
						if(predictallpositive)
							fpr = getFPRForPredictAllPositive(onefoldresults);
						double fscore = fpr[0];
						double precision = fpr[1];
						double recall = fpr[2];
					
						String onefoldresultline = relationshiptype + "\t" + formatter.format(fscore) + "\t" + formatter.format(precision) + "\t" + formatter.format(recall) + "\t" + normalbestparameters+ "\t" + reversebestparameters;
						System.out.println(onefoldresultline);
						out.println(onefoldresultline);
					}
				}
			}
		
		
			//Calculate the results for the entire test set.
			double[] fpr = getFPRForTestInstances(testpredictions);
			if(predictallpositive)
				fpr = getFPRForPredictAllPositive(testpredictions);
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
			out.println(outputline);
			out.close();
		}catch(IOException e)
		{
			System.out.println(e);
			e.printStackTrace();
			System.exit(3);
		}
	}

	private static ParametersLine findBestParameters(ParametersLine cmdlineargconstraints, String entityextractedfilename, String featuretypes, int relationshiptype, Integer testfold)
	{
		ArrayList<ParametersLine> allparameters = getAllParameters(cmdlineargconstraints, entityextractedfilename, featuretypes, relationshiptype);

		double bestfscore = Double.NEGATIVE_INFINITY;
		ParametersLine bestfscoreparams = null;
		
		for(ParametersLine currentparameters : allparameters)
		{
			ArrayList<RelationPrediction> developmentpredictions = readPredictions(currentparameters, entityextractedfilename, featuretypes, testfold, relationshiptype, true, true);
		
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
	
	private static ArrayList<ParametersLine> getAllParameters(ParametersLine cmdlineargconstraints, String entityextractedfilename, String featuretypes, int relationshiptype)
	{
		ArrayList<ParametersLine> allparameters = new ArrayList<ParametersLine>();
		
		try
		{
				File resultsfile = ProducedFileGetter.getPredictionsFile(entityextractedfilename, featuretypes, relationshiptype, true);
				
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
	private static ArrayList<RelationPrediction> readPredictions(ParametersLine currentparameters, String entityextractedfilename, String featuretypes, Integer testfold, int relationshiptype, boolean training, boolean development)
	{
		ArrayList<RelationPrediction> results = new ArrayList<RelationPrediction>();
		
		try
		{
				File resultsfile = ProducedFileGetter.getPredictionsFile(entityextractedfilename, featuretypes, relationshiptype, training);
				
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
	
	public static double[] getFPRForPredictAllPositive(ArrayList<RelationPrediction> predictions)
	{
		double TPs = 0.;
		double FPs = 0.;
		double FNs = 0.;
		double TNs = 0.;
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
			else if(tpfptnfncode == RelationPrediction.TNindex)
				TNs++;
		}
		
		
		double allpositiveTPs = TPs + FNs;
		double allpositiveFPs = FPs + TNs;
		double allpositiveFNs = 0.;

		
		double fscore = getFscore(allpositiveTPs, allpositiveFPs, allpositiveFNs);
		double precision = getPrecision(allpositiveTPs, allpositiveFPs);
		double recall = getRecall(allpositiveTPs, allpositiveFNs);
		
		
		double[] result = {fscore, precision, recall};
		return result;
	}

}
