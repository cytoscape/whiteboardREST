package org.cytoscape.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="Session File name")
public class SessionNameModel 
{
	@Schema(description="Full file name for the Session")
	public String name;
	
	public SessionNameModel(String name)
	{
		this.name = name;
	}
}
