package org.cytoscape.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public class SUIDKeyValueModel {
	@Schema(description="SUID of the object.")
	public Long SUID;
	@Schema(description="Value, represented as a JSON primitive.")
	public Object description;
}
