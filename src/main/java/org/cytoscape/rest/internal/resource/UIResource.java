package org.cytoscape.rest.internal.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import jakarta.enterprise.context.ApplicationScoped;
import org.osgi.service.component.annotations.Reference;

import org.osgi.service.component.annotations.Component;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.rest.internal.task.LevelOfDetails;
import org.cytoscape.rest.internal.model.CytoPanelModel;
import org.cytoscape.rest.internal.model.DesktopAvailableModel;
import org.cytoscape.rest.internal.model.MessageModel;
import org.cytoscape.rest.internal.task.HeadlessTaskMonitor;
import org.cytoscape.rest.internal.task.ResourceManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

@ApplicationScoped
@Component(service = UIResource.class, property = { "osgi.jaxrs.resource=true" })
@Tag(name = CyRESTSwagger.CyRESTSwaggerConfig.USER_INTERFACE_TAG)
@Path("/v1/ui")
public class UIResource extends AbstractResource {

	@Reference
	protected CySwingApplication desktop;

	// @Reference
	protected LevelOfDetails detailsTF;

	static final String RESOURCE_URN = "ui";

	@Override
	public String getResourceURI() {
		return RESOURCE_URN;
	}
	
	private final static Logger logger = LoggerFactory.getLogger(UIResource.class);
	
	@Override
	public Logger getResourceLogger() {
		return logger;
	}
	
	public static final int INTERNAL_METHOD_ERROR = 1;

	public UIResource() {
		super();
	}

	@Reference
	@Override
	protected void init(ResourceManager manager) {
		super.init(manager);

		// This needs to be initialized explicitly
		detailsTF = manager.getService(LevelOfDetails.class);
		System.out.println("detailsTF = "+detailsTF);
	}

	/*
	public void init(final ResourceManager manager) {
		super.init(manager);
		desktop = manager.getService(CySwingApplication.class);
		detailsTF = manager.getService(LevelOfDetails.class);
	}
	*/
	
	@GET
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Operation(summary="Get status of Desktop", description="Returns the status of the Desktop",
		responses = {
			@ApiResponse(responseCode = "200", description = "Desktop status", 
			             content = { @Content(schema = @Schema(implementation=DesktopAvailableModel.class))})
	})
	public Map<String, Boolean> getDesktop() {
		final Map<String, Boolean> status = new HashMap<>();
		boolean desktopAvailable = false;
		if(desktop != null) {
			desktopAvailable = true;
		}
		status.put("isDesktopAvailable", desktopAvailable);
		return status;
	}

	@PUT
	@Path("/lod")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Operation(summary="Toggle level of graphics details (LoD)", 
		description="Switch between full graphics details <---> fast rendering mode.\n\n"
				+ "Returns a success message.")
	public MessageModel updateLodState() {
		
		CyNetworkView view = manager.getService(CyApplicationManager.class).getCurrentNetworkView();
		
		final TaskIterator lod = detailsTF.getLodTF().createTaskIterator(view);
		
		try {
			lod.next().run(new HeadlessTaskMonitor());
		} catch (Exception e) {
			//throw getError("Could not toggle LOD.", e,
			//		Response.Status.INTERNAL_SERVER_ERROR);
			throw this.getCIWebApplicationException(Status.INTERNAL_SERVER_ERROR.getStatusCode(), 
					RESOURCE_URN, 
					INTERNAL_METHOD_ERROR, 
					"Could not toggle LOD.", 
					logger, e);
		}

		return new MessageModel("Toggled Graphics level of details.");
	}

	@GET
	@Path("/panels")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Operation(summary="Get status of all CytoPanels", 
		description="Returns all CytoPanels and their statuses.",
		responses = {
					@ApiResponse(responseCode = "200", description = "CytoPanels and their status", 
			    		         content = { @Content(array = @ArraySchema(schema = @Schema(implementation=CytoPanelModel.class)))})
	})
	public List<Map<String, String>> getAllPanelStatus() {
		try {
		return Arrays.asList(CytoPanelName.values()).stream()
			.map(panelName->desktop.getCytoPanel(panelName))
			.map(panel->getMap(panel))
			.collect(Collectors.toList());
		} catch(Exception ex) {
			//throw getError("Could not getpanel status", ex, Status.INTERNAL_SERVER_ERROR);
			throw this.getCIWebApplicationException(Status.INTERNAL_SERVER_ERROR.getStatusCode(), 
					RESOURCE_URN, 
					INTERNAL_METHOD_ERROR, 
					"Could not get panel status.", 
					logger, ex);
		}
	}


	private final Map<String, String> getMap(final CytoPanel panel) {
		final Map<String, String> values = new HashMap<>();
		values.put("name", panel.getCytoPanelName().name());
		values.put("state", panel.getState().name());
		return values;
	}
	
	@GET
	@Path("/panels/{panelName}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@Operation(summary="Get status of a CytoPanel", 
		description="Returns the status of the CytoPanel specified by the `panelName` parameter.",
		responses = {
			@ApiResponse(responseCode = "200", description = "CytoPanel status", 
			             content = { @Content(schema = @Schema(implementation=CytoPanelModel.class))})
	})
	public Response getPanelStatus(
			@Parameter(description="Name of the CytoPanel") @PathParam("panelName") String panelName) {
		final CytoPanelName panel = CytoPanelName.valueOf(panelName);
		if(panel == null) {
			return Response.status(Response.Status.NOT_FOUND).build();
		}
		final CytoPanel panelObject = desktop.getCytoPanel(panel);
		return Response.ok(getMap(panelObject)).build();
	}

	@PUT
	@Path("/panels")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary="Update CytoPanel states", 
		description="Updates the status(es) of available CytoPanels.")
	@RequestBody(description="A list of CytoPanels with states.", required=true, content = 
							 @Content(schema=@Schema(implementation=CytoPanelModel.class)))
	public Response updatePanelStatus(
			@Parameter(hidden=true) final InputStream is) {
		
		final ObjectMapper objMapper = new ObjectMapper();

		JsonNode rootNode = null;
		try {
			rootNode = objMapper.readValue(is, JsonNode.class);
		} catch (IOException e) {
			//throw new InternalServerErrorException("Could not parse input JSON.", e);
			throw this.getCIWebApplicationException(Status.INTERNAL_SERVER_ERROR.getStatusCode(), 
					RESOURCE_URN, 
					INTERNAL_METHOD_ERROR, 
					"Could not parse input JSON.", 
					logger, null);
		}
		
		for (final JsonNode entry : rootNode) {
			final JsonNode panelName = entry.get("name");
			final JsonNode panelStatus = entry.get("state");
			
			if(panelName == null || panelStatus == null) {
				throw new IllegalArgumentException("Imput parameters are missing."); 
			}
			
			final CytoPanelName panel = CytoPanelName.valueOf(panelName.asText());
			if(panel == null) {
				throw new IllegalArgumentException("Could not find panel: " + panelName.asText()); 
			}
			
			final CytoPanelState state = CytoPanelState.valueOf(panelStatus.asText());
			if(state == null) {
				throw new IllegalArgumentException("Invalid Panel State: " + panelStatus.asText()); 
			}
			
			final CytoPanel panelObject = desktop.getCytoPanel(panel);
			
			if (state == CytoPanelState.HIDE && panelObject.getState() == CytoPanelState.FLOAT)
				panelObject.setState(CytoPanelState.DOCK);
			panelObject.setState(state);
		}
		
		return Response.ok().build();
	}
}
