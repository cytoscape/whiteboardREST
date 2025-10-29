package org.cytoscape.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public class NewColumnParameterModel {
	@Schema(description = "New Column Name", requiredMode=Schema.RequiredMode.REQUIRED)
	public String name;
	@Schema(description = "New Column Data Type", requiredMode=Schema.RequiredMode.REQUIRED)
	public ModelConstants.ColumnTypePrimitive type; //"data type, Double, String, Boolean, Long, Integer",
	@Schema(description="If true, make this column immutable.", requiredMode=Schema.RequiredMode.NOT_REQUIRED)
	public Boolean immutable; //": "Optional: boolean description to specify immutable or not",
	@Schema(description="If true, make this a List column for the given type.", requiredMode=Schema.RequiredMode.NOT_REQUIRED)
	public Boolean list; //": "Optional.  If true, return create List column for the given type."
	@Schema(description="If true, make this a local column.", requiredMode=Schema.RequiredMode.NOT_REQUIRED)
	public Boolean local;// "local": "Optional.  If true, it will be a local column"
}
