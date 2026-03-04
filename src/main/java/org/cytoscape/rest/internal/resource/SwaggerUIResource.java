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
@Component(service = SwaggerUIResource.class, property = { 
	"osgi.jaxrs.resource=true",
	"osgi.http.whiteboard.resource.pattern=/v1/swaggerUI/swaggger-ui/*",
	"osgi.http.whiteboard.resource.prefix=/v1/swaggerUI",
	"osgi.http.whiteboard.context.select=(osgi.http.whiteboard.context.name=default)"
})
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
	@Path("{fullPath:.*}")
	public InputStream serveUI(@Parameter(description="The path to the file") @PathParam("fullPath") String path) throws IOException {
		try {
			InputStream stream = manager.getBundleResourceProvider().getResourceInputStream(path);
			return (stream);
		} catch (Exception e) {
			System.out.println("Exception getting the stream: "+e.toString());
		}
		return null;
	}
}
