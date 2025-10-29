package org.cytoscape.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema()
public class CyPropertyModel {
	@Schema(description="Value of the CyProperty")
	public String description;
	
	@Schema(description="Key of the CyProperty")
	public String key;
	
}
