package org.cytoscape.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 
 * @author David Otasek (dotasek.dev@gmail.com)
 *
 */
@Schema
public class NetworkViewSUIDModel 
{
	@Schema(description="SUID of the Network View")
	public Long networkViewSUID;
	
	public NetworkViewSUIDModel(Long networkViewSUID) {
		this.networkViewSUID=networkViewSUID;
	}
}
