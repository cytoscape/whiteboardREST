package org.cytoscape.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name="Cytoscape Column", description="A column definition in a Cytoscape table")
public class CyColumnModel {
	@Schema(description = "Column Name", requiredMode=Schema.RequiredMode.REQUIRED, example="Weight")
	public String name;
	@Schema(description = "Column Data Type", requiredMode=Schema.RequiredMode.REQUIRED, example="Double")
	public ModelConstants.ColumnTypeAll type; //"data type, Double, String, Boolean, Long, Integer",
	@Schema(description="If the type of this column is list, this specifies the type of data in the list.", requiredMode=Schema.RequiredMode.NOT_REQUIRED, example="String")
	public ModelConstants.ColumnTypePrimitive listType;
	@Schema(description="If true, this column is immutable.", requiredMode=Schema.RequiredMode.REQUIRED, example="false")
	public Boolean immutable; //": "Optional: boolean name to specify immutable or not",
	@Schema(description="If true, this column acts as the primary key for this table.", requiredMode=Schema.RequiredMode.REQUIRED, example="false")
	public Boolean primaryKey;// "local": "Optional.  If true, it will be a local column"
	
}
