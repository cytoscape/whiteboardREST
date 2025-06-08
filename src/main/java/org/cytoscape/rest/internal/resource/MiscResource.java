package org.cytoscape.rest.internal.resource;

import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.cytoscape.property.CyProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.service.component.annotations.Component;


import org.cytoscape.rest.internal.model.CytoscapeVersionModel;
import org.cytoscape.rest.internal.model.ServerStatusModel;
import org.cytoscape.rest.internal.task.ResourceManager;

/**
 * Resource to provide general status of the Cytoscape REST server. 
 * 
 * 
 * @servicetag Server status
 * 
 */
@Component(service = MiscResource.class, property = { "osgi.jaxrs.resource=true" })
@Path("/v1")
public class MiscResource extends AbstractResource {
	
	private static final String RESOURCE_URN = "";

	static CyProperty<Properties> props;

	@Override
  public String getResourceURI() {
    return RESOURCE_URN;
  }
  
	private final static Logger logger = LoggerFactory.getLogger(MiscResource.class);
  
	@Override
  public Logger getResourceLogger() {
    return logger;
	}

	public MiscResource(final ResourceManager manager) {
		super(manager);
		this.props = getService(CyProperty.class, "(cyPropertyName=cytoscape3.props)");
	}

	public MiscResource() {
	}

  @GET
  @Path("/")
  @Produces("application/json")
  public ServerStatusModel getStatus() {
    ServerStatusModel output = new ServerStatusModel();
    // output.setAllAppsStarted(allAppsStartedListener.getAllAppsStarted());
    return output;
  }

	@GET
  @Path("/gc")
  @Produces(MediaType.APPLICATION_JSON)
  public Response runGarbageCollection() {
    Runtime.getRuntime().gc();
    return Response.noContent().build();
  }



	@Path("/version")
	@Produces("application/json")
	@GET
	public CytoscapeVersionModel getCytoscapeVersion() {

		if (props == null) {
			throw new InternalServerErrorException("Could not find CyProperty object.");
		}
		
		final Properties properties = (Properties) props.getProperties();
		final Object versionNumber = properties.get("cytoscape.version.number");
		if (versionNumber != null) {
			return new CytoscapeVersionModel("v1", versionNumber.toString());
			// return "Hello, world";
		} else {
			throw new NotFoundException("Could not find Cytoscape version number property.");
		}
	}

}
