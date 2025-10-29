package org.cytoscape.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="A single point in the Continuous Mapping Graph")
public class PointModel {
	
	@Schema(description="Value of the Column Cell (x coordinate in the Cytoscape GUI)")
	public Double description;
	
	//The following three descriptions are copied directly from the BoundaryRangeValues API. Not exactly helpful, but the best I can do.
	@Schema(description="Will be used for interpolation upon smaller domain values")
	public String lesser;
	
	@Schema(description="Will be used when the domain value is exactly equal to the associated boundary domain value")
	public String equal;
	
	@Schema(description="Will be used for interpolation upon larger domain values")
	public String greater;
}
