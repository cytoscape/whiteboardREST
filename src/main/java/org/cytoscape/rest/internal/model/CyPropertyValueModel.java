package org.cytoscape.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public class CyPropertyValueModel {
	
	@Schema(description="Value of the CyProperty")
	public String description;
	
}
