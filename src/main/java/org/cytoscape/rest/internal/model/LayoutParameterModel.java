package org.cytoscape.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema()
public class LayoutParameterModel extends LayoutParameterValueModel {
	@Schema(description="Long-form description of Parameter")
	public String description;
	@Schema(description="The type of Parameter, represented as the simple name of its Java class")
	public String type;
}
