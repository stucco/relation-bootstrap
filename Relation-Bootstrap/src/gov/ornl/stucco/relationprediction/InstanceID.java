package gov.ornl.stucco.relationprediction;

/*
 * The purpose of this class is to store information about the entities from which an instance was generated.
 * It includes the name of the file from which it came, the 
 */


public class InstanceID 
{

	private String filename;
	private int firsttokensentencenum;
	private int firsttokenstartindex;
	private int firsttokenendindex;
	private int secondtokensentencenum;
	private int secondtokenstartindex;
	private int secondtokenendindex;
	
	InstanceID(String filename, int firsttokensentencenum, int firsttokenstartindex, int firsttokenendindex, int secondtokensentencenum, int secondtokenstartindex, int secondtokenendindex)
	{
		this.filename = filename;
		this.firsttokensentencenum = firsttokensentencenum;
		this.firsttokenstartindex = firsttokenstartindex;
		this.firsttokenendindex = firsttokenendindex;
		this.secondtokensentencenum = secondtokensentencenum;
		this.secondtokenstartindex = secondtokenstartindex;
		this.secondtokenendindex = secondtokenendindex;
	}
	
	InstanceID(String instanceidasstring)
	{
		filename = instanceidasstring.substring(0, instanceidasstring.lastIndexOf('-'));
		
		String indices = instanceidasstring.substring(instanceidasstring.lastIndexOf('-')+1);
		
		indices  = indices .replaceAll(")(", " ");
		indices  = indices .replaceAll("(", " ");
		indices  = indices .replaceAll(")", " ");
		indices  = indices .replaceAll(",", " ");
		
		String[] indicesarray = indices.split(" ");
		firsttokensentencenum = Integer.parseInt(indicesarray[0]);
		firsttokenstartindex = Integer.parseInt(indicesarray[1]);
		firsttokenendindex = Integer.parseInt(indicesarray[2]);
		secondtokensentencenum = Integer.parseInt(indicesarray[3]);
		secondtokenstartindex = Integer.parseInt(indicesarray[4]);
		secondtokenendindex = Integer.parseInt(indicesarray[5]);
	}

	public int getFirstTokenSentenceNum()
	{
		return firsttokensentencenum;
	}
	
	public String toString()
	{
		return filename + "-(" + firsttokensentencenum + "," + firsttokenstartindex + "," + firsttokenendindex + ")" + 
				"(" + secondtokensentencenum + "," + secondtokenstartindex + "," + secondtokenendindex + ")";
	}
}
