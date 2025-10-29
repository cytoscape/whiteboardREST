package org.cytoscape.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="Created Edge")
public class CreatedCyEdgeModel {
	@Schema(description="SUID of the new Edge", example="203")
	public long SUID;
	@Schema(description="SUID of the Edge's Source Node", example="101")
	public long source;
	@Schema(description="SUID of the Edge's Target Node", example="102")
	public long target;
}
