package org.cytoscape.rest.internal.resource;

import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import jakarta.enterprise.context.ApplicationScoped;
import org.osgi.service.component.annotations.Reference;

import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.rest.internal.model.CountModel;
import org.cytoscape.rest.internal.serializer.TableModule;
import org.cytoscape.rest.internal.task.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@ApplicationScoped
@Path("/v1/tables")
@Tag(name = CyRESTSwagger.CyRESTSwaggerConfig.TABLES_TAG)
public class GlobalTableResource extends AbstractResource {

	private static final String RESOURCE_URN = "tables";

	@Override
	public String getResourceURI() {
		return RESOURCE_URN;
	}
	
	private final static Logger logger = LoggerFactory.getLogger(GlobalTableResource.class);
	
	@Override
	public Logger getResourceLogger() {
		return logger;
	}
	
	@Reference
	private CyTableFactory tableFactory;

	@Reference
	private CyTableManager tableManager;

	private ObjectMapper tableObjectMapper;

	public GlobalTableResource() {
		super();
		this.tableObjectMapper = new ObjectMapper();
		this.tableObjectMapper.registerModule(new TableModule());
	}

	@Reference
	protected void init(ResourceManager manager) {
		super.init(manager);
	}

	/*
	public void init(ResourceManager manager) {
		super.init(manager);
		tableFactory = manager.getService(CyTableFactory.class);
		tableManager = manager.getService(CyTableManager.class);
		this.tableObjectMapper = new ObjectMapper();
		this.tableObjectMapper.registerModule(new TableModule());
	}
	*/

	@GET
	@Path("/count")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary="Get number of global tables", 
			       description="Returns the number of global tables.")
	public CountModel getTableCount() {
		final Set<CyTable> globals = tableManager.getGlobalTables();
		return new CountModel(Integer.valueOf(globals.size()).longValue());
	}
}
