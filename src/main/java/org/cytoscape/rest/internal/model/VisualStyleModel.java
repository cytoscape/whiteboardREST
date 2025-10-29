package org.cytoscape.rest.internal.model;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public class VisualStyleModel {
	@Schema(description="Name of the Visual Style", example="default")
	public String title;
	@Schema(description="List of Visual Properties and their default descriptions")
	public List<VisualPropertyValueModel> defaults;
	@Schema(description="List of Mappings")
	public List<VisualStyleMappingModel> mappings;
}
