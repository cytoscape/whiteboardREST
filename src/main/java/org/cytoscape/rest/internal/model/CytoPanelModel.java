package org.cytoscape.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="Cytoscape's GUI Panel Element")
public class CytoPanelModel {
	@Schema(description="Name of the Panel", allowableValues="SOUTH,EAST,WEST,SOUTH_WEST", requiredMode=Schema.RequiredMode.REQUIRED)
	public String name;
	@Schema(description="State of the Panel", allowableValues="FLOAT,DOCK,HIDE", requiredMode=Schema.RequiredMode.REQUIRED)
	public String state;
}
