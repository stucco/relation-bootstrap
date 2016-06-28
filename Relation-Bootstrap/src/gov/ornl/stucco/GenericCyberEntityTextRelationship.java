package gov.ornl.stucco;

public class GenericCyberEntityTextRelationship 
{
	public static final Integer[][] entity1typeToentity2typeTorelationshiptype = new Integer[CyberEntityText.ENTITYTYPECOUNT][CyberEntityText.ENTITYTYPECOUNT];
	static
	{
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.SWVENDOR][CyberEntityText.SWPRODUCT] = AllKnownDatabaseRelationships.RT_SWVENDOR_SWPRODUCT;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.SWVERSION][CyberEntityText.SWPRODUCT] = AllKnownDatabaseRelationships.RT_SWVERSION_SWPRODUCT;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.VUDESCRIPTION][CyberEntityText.VUNAME] = AllKnownDatabaseRelationships.RT_VUDESCRIPTION_VUNAME;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.VUMS][CyberEntityText.VUNAME] = AllKnownDatabaseRelationships.RT_VUMS_VUNAME;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.VUCVE][CyberEntityText.VUNAME] = AllKnownDatabaseRelationships.RT_VUCVE_VUNAME;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.VUDESCRIPTION][CyberEntityText.VUMS] = AllKnownDatabaseRelationships.RT_VUDESCRIPTION_VUMS;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.VUDESCRIPTION][CyberEntityText.VUCVE] = AllKnownDatabaseRelationships.RT_VUDESCRIPTION_VUCVE;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.VUCVE][CyberEntityText.VUMS] = AllKnownDatabaseRelationships.RT_VUCVE_VUMS;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.SWPRODUCT][CyberEntityText.VUNAME] = AllKnownDatabaseRelationships.RT_SWPRODUCT_VUNAME;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.SWPRODUCT][CyberEntityText.VUMS] = AllKnownDatabaseRelationships.RT_SWPRODUCT_VUMS;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.SWPRODUCT][CyberEntityText.VUCVE] = AllKnownDatabaseRelationships.RT_SWPRODUCT_VUCVE;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.SWVERSION][CyberEntityText.VUNAME] = AllKnownDatabaseRelationships.RT_SWVERSION_VUNAME;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.SWVERSION][CyberEntityText.VUMS] = AllKnownDatabaseRelationships.RT_SWVERSION_VUMS;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.SWVERSION][CyberEntityText.VUCVE] = AllKnownDatabaseRelationships.RT_SWVERSION_VUCVE;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.SWPRODUCT][CyberEntityText.FINAME] = AllKnownDatabaseRelationships.RT_SWPRODUCT_FINAME;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.SWVERSION][CyberEntityText.FINAME] = AllKnownDatabaseRelationships.RT_SWVERSION_FINAME;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.SWPRODUCT][CyberEntityText.FUNAME] = AllKnownDatabaseRelationships.RT_SWPRODUCT_FUNAME;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.SWVERSION][CyberEntityText.FUNAME] = AllKnownDatabaseRelationships.RT_SWVERSION_FUNAME;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.VUNAME][CyberEntityText.FINAME] = AllKnownDatabaseRelationships.RT_VUNAME_FINAME;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.VUCVE][CyberEntityText.FINAME] = AllKnownDatabaseRelationships.RT_VUCVE_FINAME;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.VUMS][CyberEntityText.FINAME] = AllKnownDatabaseRelationships.RT_VUMS_FINAME;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.VUNAME][CyberEntityText.FUNAME] = AllKnownDatabaseRelationships.RT_VUNAME_FUNAME;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.VUCVE][CyberEntityText.FUNAME] = AllKnownDatabaseRelationships.RT_VUCVE_FUNAME;
		entity1typeToentity2typeTorelationshiptype[CyberEntityText.VUMS][CyberEntityText.FUNAME] = AllKnownDatabaseRelationships.RT_VUMS_FUNAME;

		for(int i = 0; i < CyberEntityText.ENTITYTYPECOUNT; i++)
		{
			for(int j = 0; j < CyberEntityText.ENTITYTYPECOUNT; j++)
			{
				if(entity1typeToentity2typeTorelationshiptype[i][j] != null)
					entity1typeToentity2typeTorelationshiptype[j][i] = entity1typeToentity2typeTorelationshiptype[i][j];
			}
		}
	}
	
	private CyberEntityText[] entities = new CyberEntityText[2];
	
	
	GenericCyberEntityTextRelationship(CyberEntityText entity1, CyberEntityText entity2)
	{
		entities[0] = entity1;
		entities[1] = entity2;
	}
	
	public CyberEntityText getEntityOfType(int type)
	{
		for(CyberEntityText cet : entities)
		{
			if(cet.getEntityType() == type)
				return cet;
		}
		
		return null;
	}
}
