package org.cytoscape.rest.internal.model;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public class NewGroupParameterModel {
	@Schema(description="Name of the Group.")
	public String name;
	@Schema(description="Nodes contained in the Group, represented as SUIDs")
	public List<Long> nodes;
}
