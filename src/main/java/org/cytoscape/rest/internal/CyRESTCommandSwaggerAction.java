package org.cytoscape.rest.internal;

import org.cytoscape.rest.internal.task.ResourceManager;

public class CyRESTCommandSwaggerAction extends CyRESTSwaggerAction{

	public CyRESTCommandSwaggerAction(ResourceManager resourceManager) {
		super("CyREST Command API", resourceManager);
	}

	protected String rootURL()	{
		return "http://localhost:"+resourceManager.getCyRESTPort()+"/v1/swaggerUI/swagger-ui/index.html";
	}
	
	protected String swaggerPath() {
		return "v1/commands/swagger.json";
	}
	
}
