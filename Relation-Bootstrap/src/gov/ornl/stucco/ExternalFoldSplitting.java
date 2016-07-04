package gov.ornl.stucco;

import java.io.*;
import java.util.*;


public class ExternalFoldSplitting 
{
	private static File defaultsplittingdir = new File("/shared/mlrdir3/disk1/persingq/ICLE/PromptAgreement/FoldSplit/regular/");
	public static File defaultpromptsplittingdir = new File("/shared/mlrdir3/disk1/persingq/ICLE/PromptAgreement/FoldSplit/prompt/");
	public static File defaultpromptmissingsplittingdir = new File("/shared/mlrdir3/disk1/persingq/ICLE/PromptAgreement/FoldSplit/promptmissing/");
	private static HashMap<String,File> typetosplittingdir = new HashMap<String,File>();
	static
	{
		typetosplittingdir.put(FoldSplitFileGetter.REGULARSPLITTING, defaultsplittingdir);
		typetosplittingdir.put(FoldSplitFileGetter.PROMPTSPLITTING, defaultpromptsplittingdir);
		typetosplittingdir.put(FoldSplitFileGetter.PROMPTMISSINGSPLITTING, defaultpromptmissingsplittingdir);
	}
	
	
	private HashSet<String> allEssayIDs;
	private HashMap<String,HashMap<Integer,HashSet<String>>> setTofoldsplitToessayids;
	private int foldscount = 0;
	
	private String whichsplitting;

	
	
	ExternalFoldSplitting()
	{
		this(FoldSplitFileGetter.REGULARSPLITTING);
	}
	
	ExternalFoldSplitting(String splittingtype)
	{
		allEssayIDs = new HashSet<String>();
		
		setTofoldsplitToessayids = new HashMap<String,HashMap<Integer,HashSet<String>>>();
		
		whichsplitting = splittingtype;
		
		//File[] defaultfiles = defaultsplittingdir.listFiles();
		File[] defaultfiles = typetosplittingdir.get(splittingtype).listFiles();
		try
		{
			for(File f : defaultfiles)
			{
				String filename = f.getName();
				String[] prefixandsetandfoldsplit = filename.split("\\.");
				if(prefixandsetandfoldsplit.length == 3)
				{
					String set = prefixandsetandfoldsplit[1];
					int foldsplitting = Integer.parseInt(prefixandsetandfoldsplit[2]);
					foldscount = Math.max(foldscount, foldsplitting+1);	//+1 because this is a fold count, and the first fold is numbered 0.
					
					
					HashMap<Integer,HashSet<String>> foldsplitToessayids = setTofoldsplitToessayids.get(set);
					if(foldsplitToessayids == null)
					{
						foldsplitToessayids = new HashMap<Integer,HashSet<String>>();
						setTofoldsplitToessayids.put(set, foldsplitToessayids);
					}
					HashSet<String> essayids = new HashSet<String>();
					foldsplitToessayids.put(foldsplitting, essayids);
					
					
					BufferedReader in = new BufferedReader(new FileReader(f));
					in.readLine();	//The first line is some heading stuff.  If I wanted to make this code more generic, I could read it in to find what column the essayid is supposed to be in, but for now I think it's safe to assume that the first column will always contain the essay id.
					String line;
					while((line = in.readLine()) != null)
					{
						String[] splitline = line.split("\t");
						essayids.add(splitline[0]);
						allEssayIDs.add(splitline[0]);
					}
					in.close();
				}
			}
			
		}catch(IOException e)
		{
			System.out.println(e);
			e.printStackTrace();
			System.exit(3);
		}
	}
	
	public boolean isInTrainingFold(String essayid, int testfold1, int testfold2)
	{
		HashSet<String> testfold = setTofoldsplitToessayids.get(FoldSplitFileGetter.TEPREFIX).get(testfold2);
		HashSet<String> devfold = setTofoldsplitToessayids.get(FoldSplitFileGetter.TEPREFIX).get(testfold1);
		return allEssayIDs.contains(essayid) && !testfold.contains(essayid) && !devfold.contains(essayid);
	}
	/*
	public boolean isInDevelopmentFold(String essayid, int devfoldid, int testfoldid)
	{
		return setTofoldsplitToessayids.get(FoldSplitFileGetter.TEPREFIX).get(devfoldid).contains(essayid);
	}
	public boolean isInValidationFold(String essayid, int devfoldid, int testfoldid)	//method named for convenience.
	{
		return isInDevelopmentFold(essayid, devfoldid, testfoldid);
	}
	public boolean isInTestFold(String essayid, int devfoldid, int testfoldid)
	{
		return setTofoldsplitToessayids.get(FoldSplitFileGetter.TEPREFIX).get(testfoldid).contains(essayid);
	}
	*/
	public boolean isInTestFold(String essayid, int testfoldid)
	{
		return setTofoldsplitToessayids.get(FoldSplitFileGetter.TEPREFIX).get(testfoldid).contains(essayid);
	}
	
	public HashMap<String,HashMap<Integer,HashSet<String>>> getSetToFoldSplitToEssayIDs()
	{
		return setTofoldsplitToessayids;
	}
	
	public HashSet<String> getAllEssayIDs()
	{
		return allEssayIDs;
	}
	
	public int getFoldsCount()
	{
		return foldscount;
	}
	
	public String getWhichSplitting()
	{
		return whichsplitting;
	}
	
	public static void main(String[] args) 
	{
		ExternalFoldSplitting efs = new ExternalFoldSplitting();
		
		System.out.println(efs.getSetToFoldSplitToEssayIDs());
	}

}
