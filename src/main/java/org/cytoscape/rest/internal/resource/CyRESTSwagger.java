package org.cytoscape.rest.internal.resource;

import java.util.HashMap;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import jakarta.enterprise.context.ApplicationScoped;
import org.osgi.service.component.annotations.Reference;

import org.cytoscape.ci.CISwaggerConstants;
import org.cytoscape.rest.internal.CyRESTConstants;
import org.cytoscape.rest.internal.task.AutomationAppTracker;
import org.cytoscape.rest.internal.task.ResourceManager;
import org.cytoscape.rest.internal.task.SwaggerResourceTracker;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.integration.api.OpenApiReader;
import io.swagger.v3.jaxrs2.integration.JaxrsAnnotationScanner;
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.jaxrs2.Reader;
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
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;

@ApplicationScoped
@Component(service = CyRESTSwagger.class, property = { "osgi.jaxrs.resource=true" })
@Path("/v1/swagger.json")
public class CyRESTSwagger extends AbstractResource
{
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
	final Set<String> classNames = new HashSet<String>();

	public void addResource(Class<?> clazz)
	{
		classes.add(clazz);
		classNames.add(clazz.toString());
		updateSwagger();
	}

	public void removeResource(Class<?> clazz)
	{
		classes.remove(clazz);
		classNames.remove(clazz.toString());
		updateSwagger();
	}

	public CyRESTSwagger(){
		super();
	}

	@Reference
	protected void init(ResourceManager manager) {
		super.init(manager);

		System.out.println("swagger init called");

		BundleContext bundleContext = manager.getBundleContext();
		try {
			SwaggerResourceTracker swaggerResourceTracker = new SwaggerResourceTracker(bundleContext,bundleContext.createFilter(CyRESTConstants.ANY_SERVICE_FILTER), this);
			swaggerResourceTracker.open();
		} catch (Exception e) {
			System.err.println("Unable to initialize resource tracker");
		}

		updateSwagger();
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

		System.out.println("buildSwagger == "+classes.size()+" classes");

		String automationAppReport = manager.getAutomationAppTracker().getMarkdownReport(); 

		OpenAPI openAPI = new OpenAPI()
				.info(new Info()
						.title("CyREST API")
						.description(SWAGGER_INFO_DESCRIPTION + automationAppReport))
				.servers(Collections.singletonList(new Server().url(ResourceManager.HOST + ":" + manager.getCyRESTPort())));

		SwaggerConfiguration oasConfiguration = new SwaggerConfiguration()
				.openAPI(openAPI)
				.resourcePackages(classNames)
				.prettyPrint(true);

		try {
			new JaxrsOpenApiContextBuilder()
				.openApiConfiguration(oasConfiguration)
				.buildContext(true);
		} catch (OpenApiConfigurationException e) {
			System.err.println(e.toString());
		}

		Reader reader = new Reader(openAPI);

		for (Class<?> clazz: classes) {
			System.out.println("Reading class: "+clazz.toString());
			reader.read(clazz, "", null, false, null, null, new LinkedHashSet<String>(), new ArrayList<Parameter>(), new HashSet<Class<?>>());
		}

		// serialization of the Swagger definition
		try 
		{
			ObjectMapper mapper = Json.mapper();
			this.swaggerDefinition = mapper.writeValueAsString(reader.getOpenAPI());
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
			url = "http://localhost:"+manager.getCyRESTPort()+"/v1/swaggerUI/swagger-ui/index.html"
					+ "?url=" + URLEncoder.encode("http://" + ResourceManager.HOST + ":" + manager.getCyRESTPort() + "/v1/commands/swagger.json", "UTF-8");
		
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
	}

	@Produces(MediaType.APPLICATION_JSON)
	@GET
	@Path("/")
	public String get()
	{
		if (swaggerDefinition == null)
		{
			System.out.println("Building swagger");
			buildSwagger();
		}
		System.out.println("Returning definition");
		return swaggerDefinition;
	}



	private static final String SWAGGER_INFO_DESCRIPTION =  "A RESTful service for accessing Cytoscape 3.\n\n";
	
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
