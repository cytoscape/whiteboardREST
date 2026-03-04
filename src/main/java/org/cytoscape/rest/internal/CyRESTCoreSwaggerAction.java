package org.cytoscape.rest.internal;

import org.cytoscape.rest.internal.task.ResourceManager;

public class CyRESTCoreSwaggerAction extends CyRESTSwaggerAction{

	public CyRESTCoreSwaggerAction(ResourceManager resourceManager) {
		super("CyREST API", resourceManager);
	}

	protected String rootURL()	{
		return "http://localhost:"+resourceManager.getCyRESTPort()+"/v1/swaggerUI/swagger-ui/index.html";
	}
	
	protected String swaggerPath() {
		return "v1/swagger.json";
	}
	
}
