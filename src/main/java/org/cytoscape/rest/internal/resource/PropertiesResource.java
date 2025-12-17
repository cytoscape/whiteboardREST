package org.cytoscape.rest.internal.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import jakarta.enterprise.context.ApplicationScoped;
import org.osgi.service.component.annotations.Reference;

import org.cytoscape.ci.model.CIResponse;
import org.cytoscape.rest.internal.model.CyPropertyModel;
import org.cytoscape.rest.internal.model.CyPropertyValueModel;
import org.cytoscape.rest.internal.task.CyPropertyListener;
import org.cytoscape.rest.internal.task.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Resource to provide the status of installed Cytoscape apps. 
 * 
 * @servicetag Server status
 * 
 */
@ApplicationScoped
@Tag(name = CyRESTSwagger.CyRESTSwaggerConfig.PROPERTIES_TAG)
@Path("/v1/properties")
public class PropertiesResource extends AbstractResource {
	
	static final String RESOURCE_URN = "properties";

	private final static Logger logger = LoggerFactory.getLogger(PropertiesResource.class);

	@Override
	public String getResourceURI() {
		return RESOURCE_URN;
	}
	
	@Override
	public Logger getResourceLogger() {
		return logger;
	}
	
	static final int NAMESPACE_NOT_FOUND_ERROR = 1;
	static final int NAMESPACE_IS_EMPTY_ERROR = 2;
	static final int PROPERTY_NOT_FOUND_ERROR = 3;
	
	static final int INVALID_PARAMETER_ERROR = 4;
	
	protected CyPropertyListener cyPropertyListener;

	public PropertiesResource() {
		super();
	}

	@Reference
	protected void init(ResourceManager manager) {
		super.init(manager);
		cyPropertyListener = new CyPropertyListener();
	}

	/*
	public void init(ResourceManager manager) {
		super.init(manager);
		cyPropertyListener = new CyPropertyListener();
	}
	*/
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary="List available Cytoscape Property namespaces",
	           description="Returns a list of available Cytoscape Property namespaces")
	public CIResponse<List<String>> getPropertyNamespaceList() {
		return ciResponseFactory.getCIResponse(cyPropertyListener.getPropertyNames());
	}
	
	private Properties getProperties(String namespace) {
		if (cyPropertyListener.getCyProperty(namespace) == null) {
			throw this.getCIWebApplicationException(Status.NOT_FOUND.getStatusCode(), 
					RESOURCE_URN, 
					NAMESPACE_NOT_FOUND_ERROR, 
					"Could not find property namespace: " + namespace, 
					logger, null);
		}
		Properties properties = (Properties) cyPropertyListener.getCyProperty(namespace).getProperties();
		if (properties == null) {
			throw this.getCIWebApplicationException(Status.NOT_FOUND.getStatusCode(), 
					RESOURCE_URN, 
					NAMESPACE_IS_EMPTY_ERROR, 
					"Property namespace does not contain properties: " + namespace, 
					logger, null);
		}
		return properties;
	}
	
	@GET
	@Path("/{namespace}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary="Gets a list of Cytoscape Properties for a namespace",
	description="Returns the Cytoscape Properties in the namespace specified by the `namespace` parameter.")
	public CIResponse<List<String>> getPropertyList(@Parameter(description="Cytoscape Property namespace") @PathParam("namespace") String namespace)
	{
		Properties properties = getProperties(namespace);
		List<String> output = new ArrayList<String>();
		output.addAll(properties.stringPropertyNames());
		return ciResponseFactory.getCIResponse(output);
	}
	
	private static class CyPropertyModelResponse extends CIResponse<CyPropertyModel> {};
	
	@GET
	@Path("/{namespace}/{propertyKey}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary="Gets a Cytoscape Property",
	           description="Returns the Cytoscape Property specified by the `namespace` and `propertyKey` parameters.",
	           responses = {
			          @ApiResponse(responseCode = "200", description = "CyProperty", 
			                       content = { @Content(schema = @Schema(implementation=CyPropertyModelResponse.class))})
	})
	public Response getProperty(@Parameter(description="Cytoscape Property namespace") @PathParam("namespace") String namespace ,
			                        @Parameter(description="Key of the CyProperty") @PathParam("propertyKey") String propertyKey 
			){
		CyPropertyModel output = null;
		Properties properties = getProperties(namespace);
		output = new CyPropertyModel();
		if (!properties.containsKey(propertyKey)) {
			throw this.getCIWebApplicationException(Status.NOT_FOUND.getStatusCode(), 
					RESOURCE_URN, 
					PROPERTY_NOT_FOUND_ERROR, 
					"Property namespace \"" + namespace + "\" does not contain property: " + propertyKey, 
					logger, null);
		}
		String property = properties.getProperty(propertyKey);
		output.key = propertyKey;
		output.value = property;
			
		return Response.ok(ciResponseFactory.getCIResponse(output)).build();
	}
	
	@PUT
	@Path("/{namespace}/{propertyKey}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary="Sets a Cytoscape Property",
	description="Sets the Cytoscape Property specified by the `namespace` and `propertyKey` parameters.")
	public CIResponse<Object> putProperty(@Parameter(description="Cytoscape Property namespace") @PathParam("namespace") String namespace ,
			                                  @Parameter(description="Key of the CyProperty") @PathParam("propertyKey") String propertyKey,
			                                  @Parameter(description="A CyProperty value") CyPropertyValueModel propertyValue
			){
		Properties properties = getProperties(namespace);
		if (properties.containsKey(propertyKey)) {
			properties.setProperty(propertyKey, propertyValue.value);
		}
		else {
			throw this.getCIWebApplicationException(Status.NOT_FOUND.getStatusCode(), 
					RESOURCE_URN, 
					PROPERTY_NOT_FOUND_ERROR, 
					"Property namespace \"" + namespace + "\" does not contain property: " + propertyKey, 
					logger, null);
		}
		return ciResponseFactory.getCIResponse(new Object());
	}
	
	@DELETE
	@Path("/{namespace}/{propertyKey}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary="Deletes a Cytoscape Property",
	           description="Deletes the Cytoscape Property specified by the `namespace` and `propertyKey` parameters.")
	public CIResponse<Object> deleteProperty(@Parameter(description="Cytoscape Property namespace") @PathParam("namespace") String namespace ,
			                                     @Parameter(description="Key of the CyProperty") @PathParam("propertyKey") String propertyKey
			){
		Properties properties = getProperties(namespace);
		if (properties.containsKey(propertyKey)) {
			properties.remove(propertyKey);
		}
		else {
			throw this.getCIWebApplicationException(Status.NOT_FOUND.getStatusCode(), 
					RESOURCE_URN, 
					PROPERTY_NOT_FOUND_ERROR, 
					"Property namespace \"" + namespace + "\" does not contain property: " + propertyKey, 
					logger, null);
		}
		return ciResponseFactory.getCIResponse(new Object());
	}
	
	@POST
	@Path("/{namespace}/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary="Creates a Cytoscape Property",
	           description="Creates a Cytoscape Property in the namespace specified by the `namespace` parameter.")
	public CIResponse<Object> postProperty(@Parameter(description="Cytoscape Property namespace") @PathParam("namespace") String namespace ,
			                                   @Parameter(description="A CyProperty with a key and value") CyPropertyModel propertyValue
			){
		Properties properties = getProperties(namespace);
		properties.setProperty(propertyValue.key, propertyValue.value);
		return ciResponseFactory.getCIResponse(new Object());
	}
}
