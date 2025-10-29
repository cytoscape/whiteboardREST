package org.cytoscape.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema()
public class LayoutParameterValueModel {
	@Schema(description="The name of the Parameter")
	public String name;
	@Schema(description="The value of this Parameter as a JSON primitive")
	public Object description;
}
