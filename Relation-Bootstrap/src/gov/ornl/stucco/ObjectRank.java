package gov.ornl.stucco;

public class ObjectRank implements Comparable<ObjectRank>
{
	public Object obj;
	public Double value;

	
	ObjectRank(Object obj, double value)
	{
		this.obj = obj;
		this.value = value;
	}
	
	
	public int compareTo(ObjectRank o) 
	{
		double order = value - o.value;
		
		if(order > 0)
			return 1;
		else if(order < 0)
			return -1;
		else
			return 0;
	}
	
	public String toString()
	{
		return obj + "\t" + value;
	}
	
}
