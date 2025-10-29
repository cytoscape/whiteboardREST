package org.cytoscape.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public class NetworkViewVisualPropertyModel {
	
	@Schema(description = "Visual Property Name", example="NODE_FILL_COLOR", requiredMode=Schema.RequiredMode.REQUIRED)
	public String visualProperty;
	
	@Schema(description = "Serialized form of value, or null", example="red", requiredMode=Schema.RequiredMode.REQUIRED)
	public Object description;
}
