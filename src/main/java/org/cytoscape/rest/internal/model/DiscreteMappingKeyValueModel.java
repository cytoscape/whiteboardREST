package org.cytoscape.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema()
public class DiscreteMappingKeyValueModel {
	@Schema(description="CyTable cell value to match")
	public String key;
	@Schema(description="VisualProperty returned when the CyTable cell value is matched.")
	public String description;
}
