package org.cytoscape.rest.internal.resource;

import java.util.HashMap;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.cytoscape.ci.CISwaggerConstants;
import org.cytoscape.rest.internal.task.AutomationAppTracker;
import org.cytoscape.rest.internal.task.ResourceManager;
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.integration.api.OpenApiReader;
import io.swagger.v3.jaxrs2.integration.JaxrsAnnotationScanner;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.jaxrs2.ReaderListener;
// import io.swagger.v3.oas.models.HttpMethod; -- use method attribute in @Operation

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.PathItem.HttpMethod;
import io.swagger.v3.oas.models.Paths;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
/*
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.ObjectProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.util.Json;
*/

@Component(service = CyRESTSwagger.class, property = { "osgi.jaxrs.resource=true" })
@Path("/v1/swagger.json")
public class CyRESTSwagger extends AbstractResource
{
	AutomationAppTracker appTracker;
	
	private static final String RESOURCE_URN = "swagger";


	@Override
	public String getResourceURI() {
		return RESOURCE_URN;
	}
	
	private final static Logger logger = LoggerFactory.getLogger(CyRESTSwagger.class);
	
	@Override
	public Logger getResourceLogger() {
		return logger;
	}
	
	private String swaggerDefinition;

	final Set<Class<?>> classes = new HashSet<Class<?>>();

	public void addResource(Class<?> clazz)
	{
		classes.add(clazz);
		updateSwagger();
	}

	public void removeResource(Class<?> clazz)
	{
		classes.remove(clazz);
		updateSwagger();
	}

	public CyRESTSwagger(){
		super();
	}

	@Override
	public void init(ResourceManager manager) {
		super.init(manager);
		appTracker = manager.getAutomationAppTracker();
		updateSwagger();
		buildSwagger();

	}

	protected void updateSwagger()
	{
		swaggerDefinition = null;
	}

	public boolean isSwaggerDefinitionNull()
	{
		return (swaggerDefinition == null);
	}

	protected void buildSwagger()
	{
		/*
		final Set<Class<?>> classes = new HashSet<Class<?>>(this.classes);
		BeanConfig beanConfig = new BeanConfig(){
			public Set<Class<?>> classes()
			{
				//Set<Class<?>> classes = new HashSet<Class<?>>();
				//classes.addAll();
				classes.add(CyRESTSwaggerConfig.class);
				return classes;
			}
		};

		//FIXME This needs to get set from the ResourceManager
		beanConfig.setHost(ResourceManager.HOST + ":" + ResourceManager.cyRESTPort);
		beanConfig.setScan(true);
		beanConfig.setPrettyPrint(true);

		Swagger swagger = beanConfig.getSwagger();
		*/

		String automationAppReport = appTracker.getMarkdownReport(); 

		OpenAPI openAPI = new OpenAPI()
				.info(new Info()
						.title("CyREST API")
						.description(SWAGGER_INFO_DESCRIPTION + automationAppReport))
				.servers(Collections.singletonList(new Server().url(ResourceManager.HOST + ":" + ResourceManager.cyRESTPort)))
				.addTagsItem(new Tag().name(CyRESTSwaggerConfig.COLLECTIONS_TAG))
				.addTagsItem(new Tag().name(CyRESTSwaggerConfig.COMMANDS_TAG))
				.addTagsItem(new Tag().name(CyRESTSwaggerConfig.CYTOSCAPE_SYSTEM_TAG))
				.addTagsItem(new Tag().name(CyRESTSwaggerConfig.GROUPS_TAG))
				.addTagsItem(new Tag().name(CyRESTSwaggerConfig.LAYOUTS_TAG))
				.addTagsItem(new Tag().name(CyRESTSwaggerConfig.NETWORKS_TAG))
				.addTagsItem(new Tag().name(CyRESTSwaggerConfig.NETWORK_VIEWS_TAG))
				.addTagsItem(new Tag().name(CyRESTSwaggerConfig.PROPERTIES_TAG))
				.addTagsItem(new Tag().name(CyRESTSwaggerConfig.REST_SERVICE_TAG))
				.addTagsItem(new Tag().name(CyRESTSwaggerConfig.SESSION_TAG))
				.addTagsItem(new Tag().name(CyRESTSwaggerConfig.TABLES_TAG))
				.addTagsItem(new Tag().name(CyRESTSwaggerConfig.USER_INTERFACE_TAG))
				.addTagsItem(new Tag().name(CyRESTSwaggerConfig.VISUAL_PROPERTIES_TAG))
				.addTagsItem(new Tag().name(CyRESTSwaggerConfig.VISUAL_STYLES_TAG));
		
		wrapCIResponses(openAPI);
		addCommandLinks(openAPI);

		// serialization of the Swagger definition
		try 
		{
			Json.mapper().enable(SerializationFeature.INDENT_OUTPUT);
			this.swaggerDefinition = Json.mapper().writeValueAsString(openAPI);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	private void wrapCIResponses(OpenAPI openAPI) {
		Paths paths = openAPI.getPaths();
		if (paths != null)
			for (Map.Entry<String, PathItem> pathEntry : paths.entrySet()) {
				try {
					Operation operation = pathEntry.getValue().getGet();
					if (operation != null)
						wrapOperation(pathEntry, operation);

					operation = pathEntry.getValue().getPost();
					if (operation != null)
						wrapOperation(pathEntry, operation);

					operation = pathEntry.getValue().getPut();
					if (operation != null)
						wrapOperation(pathEntry, operation);

					operation = pathEntry.getValue().getDelete();
					if (operation != null)
						wrapOperation(pathEntry, operation);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
	}

	private void wrapOperation(Map.Entry<String, PathItem> pathEntry, Operation operation) {

		Object ciExtension = operation.getExtensions().get(CISwaggerConstants.X_CI_EXTENSION);

		if (ciExtension != null && ciExtension instanceof Map) {
				Map<?,?> map = (Map<?, ?>) ciExtension;
				if (CISwaggerConstants.TRUE.equals(map.get(CISwaggerConstants.CI_EXTENSION_CI_WRAPPING))) {

					for (Map.Entry<String, ApiResponse> responseEntry : operation.getResponses().entrySet()) {

						System.out.println("Wrapping " + responseEntry.getKey() + " response for path " + pathEntry.getKey() + " data model:" + responseEntry.getValue().getDescription());

						Content content = responseEntry.getValue().getContent();
						if (content != null) {
							for (String mediaTypeName:content.keySet()) {
								Schema schemaToWrap = content.get(mediaTypeName).getSchema();
								Schema wrappedSchema = new Schema<>().addProperty("data",schemaToWrap).addProperty("errors", new ArraySchema().$ref("#/definitions/CIError"));
								content.get(mediaTypeName).setSchema(wrappedSchema);
							}
						}
					}
				}
			}
	}
					

	public static final String COMMAND_LINK_PREFIX = "\n\nFor a list of all available commands and their documentation, see the [CyREST Command API](";
	
	public static final String COMMAND_LINK_POSTFIX = ")";
	
	private String getCommandLink() {
		String url;
		try {
			url = "http://localhost:"+ResourceManager.cyRESTPort+"/v1/swaggerUI/swagger-ui/index.html"
					+ "?url=" + URLEncoder.encode("http://" + ResourceManager.HOST + ":" + ResourceManager.cyRESTPort + "/v1/commands/swagger.json", "UTF-8");
		
			//TODO this should be done with a string formatting utility.
		return COMMAND_LINK_PREFIX +url + COMMAND_LINK_POSTFIX;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "\n\nUnable to make a hyperlink to the CyREST Command API";
		}
	}
	
	private void addCommandLinks(OpenAPI openAPI) {
		Paths paths = openAPI.getPaths();
		
		if (paths != null) {
			for (Map.Entry<String, PathItem> pathEntry : paths.entrySet()) {
				try {
					Operation operation = pathEntry.getValue().getGet();
					if (operation != null)
						wrapCommand(pathEntry, operation);

					operation = pathEntry.getValue().getPost();
					if (operation != null)
						wrapCommand(pathEntry, operation);

					operation = pathEntry.getValue().getPut();
					if (operation != null)
						wrapCommand(pathEntry, operation);

					operation = pathEntry.getValue().getDelete();
					if (operation != null)
						wrapCommand(pathEntry, operation);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}

	private void wrapCommand(Map.Entry<String, PathItem> pathEntry, Operation operation) {

		if (operation.getTags() != null && operation.getTags().contains(CyRESTSwagger.CyRESTSwaggerConfig.COMMANDS_TAG))
		{
			String description = operation.getDescription();
			if (description == null) {
				description = "";
			}
			description += getCommandLink();
			operation.setDescription(description);
		}

		//Should be want to scan descriptions, parameter details, etc. and automatically generate links,
		//this is how it could happen. Note that 
		//operationEntry.getValue().setDescription("Test afterScan replacement.");
	}

	@Produces(MediaType.APPLICATION_JSON)
	@GET
	@Path("/")
	public String get()
	{
		if (swaggerDefinition == null)
		{
			buildSwagger();
		}
		return swaggerDefinition;
	}

	private static final String SWAGGER_INFO_DESCRIPTION =  "A RESTful service for accessing Cytoscape 3.\n\n";
	
	
	/*
	@OpenAPIDefinition(
			info = @Info(
					description = "A RESTful service for accessing Cytoscape 3.",
					version = "V2.0.0",
					title = "CyREST API"
					//termsOfService = "http://theweatherapi.io/terms.html",
					// contact = @Contact(
					//   name = "Rain Moore", 
					//    email = "rain.moore@theweatherapi.io", 
					//    url = "http://theweatherapi.io"
					// ),
					// license = @License(
					//    name = "Apache 2.0", 
					//    url = "http://www.apache.org/licenses/LICENSE-2.0"
					// )
					),
			tags = 
		{
				@Tag(name = CyRESTSwaggerConfig.COLLECTIONS_TAG),
				@Tag(name = CyRESTSwaggerConfig.COMMANDS_TAG),
				@Tag(name = CyRESTSwaggerConfig.CYTOSCAPE_SYSTEM_TAG),
				@Tag(name = CyRESTSwaggerConfig.GROUPS_TAG),
				@Tag(name = CyRESTSwaggerConfig.LAYOUTS_TAG),
				@Tag(name = CyRESTSwaggerConfig.NETWORKS_TAG),
				@Tag(name = CyRESTSwaggerConfig.NETWORK_VIEWS_TAG),
				@Tag(name = CyRESTSwaggerConfig.PROPERTIES_TAG),
				@Tag(name = CyRESTSwaggerConfig.REST_SERVICE_TAG),
				@Tag(name = CyRESTSwaggerConfig.SESSION_TAG),
				@Tag(name = CyRESTSwaggerConfig.TABLES_TAG),	
				@Tag(name = CyRESTSwaggerConfig.USER_INTERFACE_TAG),			
				@Tag(name = CyRESTSwaggerConfig.VISUAL_PROPERTIES_TAG),
				@Tag(name = CyRESTSwaggerConfig.VISUAL_STYLES_TAG)
		}, 
		externalDocs = @ExternalDocumentation(description = "Cytoscape", url = "http://cytoscape.org/")
			)
	*/
	public static class CyRESTSwaggerConfig implements ReaderListener
	{

		public static final String SESSION_TAG = "Session";
		public static final String APPS_TAG = "Apps";
		public static final String USER_INTERFACE_TAG = "User Interface";
		public static final String NETWORKS_TAG = "Networks";
		public static final String TABLES_TAG = "Tables";
		public static final String COMMANDS_TAG = "Commands";
		public static final String REST_SERVICE_TAG = "REST Service";
		public static final String LAYOUTS_TAG = "Layouts";
		public static final String NETWORK_VIEWS_TAG = "Network Views";
		public static final String PROPERTIES_TAG = "Properties";
		public static final String VISUAL_PROPERTIES_TAG = "Visual Properties";
		public static final String VISUAL_STYLES_TAG = "Visual Styles";
		public static final String GROUPS_TAG = "Groups";
		public static final String COLLECTIONS_TAG = "Collections";
		public static final String CYTOSCAPE_SYSTEM_TAG = "Cytoscape System";

		@Override
		public void beforeScan(OpenApiReader reader, OpenAPI openAPI) 
		{

		}

		public void afterScan(OpenApiReader reader, OpenAPI openAPI)
		{
			
		}
	}

	/*
	 * This may need to be changed should we switch from Swagger UI 2.x to 3.x. The 3.x id tags are in the following 
	 * format: operations,get-/v1/networks/{networkId}/views,Network Views
	 */

	public final static String NETWORK_GET_LINK = "[/v1/networks](#!/Networks/getNetworksAsSUID)";
	public final static String NETWORK_VIEWS_LINK = "[/v1/networks/{networkId}/views](#!/Network32Views/getAllNetworkViews)";
}
