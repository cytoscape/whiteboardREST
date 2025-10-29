package org.cytoscape.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="New Edge Parameter")
public class CreateCyEdgeParameterModel {
	@Schema(requiredMode=Schema.RequiredMode.REQUIRED, description="The SUID of the source node for the new edge", example="101")
	public long source;
	@Schema(requiredMode=Schema.RequiredMode.REQUIRED, description="The SUID of the target node for the new edge", example="102")
	public long target;
	@Schema(requiredMode=Schema.RequiredMode.NOT_REQUIRED, description="The `directed` property of the edge. This is `true` if the edge is directed.", example="true")
	public boolean directed;
	@Schema(requiredMode=Schema.RequiredMode.NOT_REQUIRED, description="The description to include in the new edge's `interaction` column.", example="pp")
	public String interaction;
}
