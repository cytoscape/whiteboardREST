package org.cytoscape.rest.internal.model;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema()
public class LayoutModel {
	@Schema(description="Unique internal name of the Layout")
	public String name;
	@Schema(description="Human-readable name of the Layout. This can be seen primarily in the Cytoscape GUI.")
	public String longName;
	@Schema(description="Parameters for this layout and their values.")
	public List<LayoutParameterModel> parameters;
	@Schema(description="Column Types that can be used by this layout.")
	public List<LayoutColumnTypesModel> compatibleColumnDataTypes;
	
}
