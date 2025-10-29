package org.cytoscape.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema
public class GroupSUIDModel 
{
	@Schema(description="SUID of the Node representing the group.")
	public Long groupSUID;
	
	public GroupSUIDModel(Long groupSUID) {
		this.groupSUID=groupSUID;
	}
}
