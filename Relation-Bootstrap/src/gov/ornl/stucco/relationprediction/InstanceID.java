package gov.ornl.stucco.relationprediction;

public class InstanceID 
{

	private String filename;
	private int sentencenum;
	private int firsttokenstartindex;
	private int firsttokenendindex;
	private int secondtokenstartindex;
	private int secondtokenendindex;
	
	InstanceID(String filename, int sentencenum, int firsttokenstartindex, int firsttokenendindex, int secondtokenstartindex, int secondtokenendindex)
	{
		this.filename = filename;
		this.sentencenum = sentencenum;
		this.firsttokenstartindex = firsttokenstartindex;
		this.firsttokenendindex = firsttokenendindex;
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
		sentencenum = Integer.parseInt(indicesarray[0]);
		firsttokenstartindex = Integer.parseInt(indicesarray[1]);
		firsttokenendindex = Integer.parseInt(indicesarray[2]);
		secondtokenstartindex = Integer.parseInt(indicesarray[3]);
		secondtokenendindex = Integer.parseInt(indicesarray[4]);
	}
	
	public String toString()
	{
		return filename + "-" + sentencenum + "(" + firsttokenstartindex + "," + firsttokenendindex + ")" + 
				"(" + secondtokenstartindex + "," + secondtokenendindex + ")";
	}
}
