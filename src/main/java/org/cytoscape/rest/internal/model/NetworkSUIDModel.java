package org.cytoscape.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public class NetworkSUIDModel 
{
	@Schema(description="SUID of the Network")
	public Long networkSUID;
	
	public NetworkSUIDModel(Long networkSUID) {
		this.networkSUID=networkSUID;
	}
}
