package org.cytoscape.rest.internal.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;

import org.osgi.service.component.annotations.Component;

import org.cytoscape.rest.internal.datamapper.MapperUtil;
import org.cytoscape.rest.internal.model.SUIDNameModel;
import org.cytoscape.rest.internal.task.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import static org.cytoscape.rest.internal.resource.NetworkErrorConstants.*;

@Tag(name="networks")
@Component(service = NetworkNameResource.class, property = { "osgi.jaxrs.resource=true" })
@Path("/v1")
public class NetworkNameResource extends AbstractResource {

	private static final String RESOURCE_URN = "networks";

	private CyNetworkManager networkManager;

	@Override
  public String getResourceURI() {
    return RESOURCE_URN;
  }
	
	private final static Logger logger = LoggerFactory.getLogger(NetworkNameResource.class);
  
	@Override
  public Logger getResourceLogger() {
    return logger;
	}
	
	public NetworkNameResource(final ResourceManager manager) {
		super(manager);
	}
	
	public NetworkNameResource() {
		super();
		// this.networkManager = getService(CyNetworkManager.class);
	}

	public void init(final ResourceManager manager) {
		super.init(manager);
		this.networkManager = getService(CyNetworkManager.class);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
	@Operation(summary = "Returns a list of network names with corresponding SUIDs",
		description="Returns a list of all networks as names and their corresponding SUIDs.\n\n" + NETWORK_QUERY_DESCRIPTION)
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Success", 
		             content = { 
										@Content( array = @ArraySchema(
										    schema = @Schema(implementation = SUIDNameModel.class)))
								 }),
	})
  @Path("/network.names")
	public List<Map<String,Object>> getNetworksNames(
			@Parameter(description=COLUMN_DESCRIPTION) @QueryParam("column") String column,
			@Parameter(description=QUERY_STRING_DESCRIPTION) @QueryParam("query") String query) {
		Set<CyNetwork> networks;

		if (column == null && query == null) {
			networks = networkManager.getNetworkSet();
		} else {
			try {
				networks = getNetworksByQuery(INVALID_PARAMETER_ERROR, query, column);
			} catch(WebApplicationException e) {
				throw(e);
			} catch (Exception e) {
				throw new RuntimeException("Could not get networks."+e.toString());
			}
		}

		return getNetworksAsSimpleList(networks);
	}

	@SuppressWarnings("unchecked")
	private final List<Map<String, Object>> getNetworksAsSimpleList(final Set<CyNetwork> networks) {
		if (networks.isEmpty()) {
			return Collections.EMPTY_LIST;
		}
		
		final List<Map<String, Object>> networksList = new ArrayList<>();
		for(final CyNetwork network: networks) {
			final Map<String, Object> values = new HashMap<>();
			values.put("SUID", network.getSUID());
			values.put("name", network.getRow(network).get(CyNetwork.NAME, String.class));
			networksList.add(values);
		}

		return networksList;
	}

}
