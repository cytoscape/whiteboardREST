package org.cytoscape.rest.internal.resource;

import java.io.IOException;
import java.io.InputStream;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import jakarta.enterprise.context.ApplicationScoped;
import org.osgi.service.component.annotations.Reference;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.cytoscape.rest.internal.BundleResourceProvider;
import org.cytoscape.rest.internal.task.ResourceManager;

import io.swagger.v3.oas.annotations.Parameter;

@ApplicationScoped
@Component(service = SwaggerUIResource.class, property = { "osgi.jaxrs.resource=true" })
@Path("/v1/swaggerUI")
public class SwaggerUIResource extends AbstractResource {
	
	public SwaggerUIResource() {
		super();
	}

	@Reference
	protected void init(ResourceManager manager) {
		super.init(manager);
	}

	/*
	public void init(ResourceManager manager) {
		super.init(manager);
	}
	*/

	private static final String RESOURCE_URN = "swaggerUI";

	@Override
	public String getResourceURI() {
		return RESOURCE_URN;
	}
	
	private final static Logger logger = LoggerFactory.getLogger(SwaggerUIResource.class);
	
	@Override
	public Logger getResourceLogger() {
		return logger;
	}
	
	@GET
	@Path("{path:.*}")
	public InputStream serveUI(@Parameter(description="path") String path) throws IOException {
		return manager.getBundleResourceProvider().getResourceInputStream(path);
	}
}
