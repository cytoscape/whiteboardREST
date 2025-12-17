package org.cytoscape.rest.internal.resource;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import jakarta.enterprise.context.ApplicationScoped;
import org.osgi.service.component.annotations.Reference;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.cytoscape.rest.internal.task.ResourceManager;

/**
 * Root of the REST API server.
 * 
 */
@ApplicationScoped
@Component(service = RootResource.class, property = { "osgi.jaxrs.resource=true" })
@Path("/")
@Tag(name = CyRESTSwagger.CyRESTSwaggerConfig.REST_SERVICE_TAG)
public class RootResource extends AbstractResource {

	static final String RESOURCE_URN = "";

	public RootResource() {
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

	@Override
	public String getResourceURI() {
		return RESOURCE_URN;
	}
	
	private final static Logger logger = LoggerFactory.getLogger(RootResource.class);
		
	@Override
	public Logger getResourceLogger() {
		return logger;
	}
	
	private static final String[] VERSION_LIST = { API_VERSION };
	private static final Map<String, String[]> VERSION_MAP = new HashMap<String, String[]>();
	static {
		VERSION_MAP.put("availableApiVersions", VERSION_LIST);
	}

	/**
	 * @summary Get available REST API versions
	 * 
	 * @return List of available REST API versions. Currently, v1 is the only
	 *         available version.
	 * 
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary="Get available REST API versions", description="Returns a list of available REST API versions. Currently, v1 is the only available version")
	public Map<String, String[]> getVersions() {
		return VERSION_MAP;
	}

}
