package org.cytoscape.rest.internal.model;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name="A Group of Cytoscape Nodes and Edges")
public class GroupModel {
	
	@Schema(description="SUID of the Node representing the group.")
	public long SUID;
	
	@Schema(description="The collapsed value of this group. If this is `true`, only "
			+ "the Node representing this group will be visible, and all the nodes and edges "
			+ "contained by it will not.")
	public boolean collapsed;
	
	@Schema(description="The Nodes contained by this Group represented by SUIDs")
	public List<Long> nodes;
	
	@Schema(description="The Edges contained by this Group represented by SUIDs")
	public List<Long> internal_edges;

	@Schema(description="Edges from outside this Group that connect to its nodes, represented by SUIDs")
	public List<Long> external_edges;
}
