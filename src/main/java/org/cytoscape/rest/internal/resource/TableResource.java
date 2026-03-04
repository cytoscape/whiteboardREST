package org.cytoscape.rest.internal.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import jakarta.enterprise.context.ApplicationScoped;
import org.osgi.service.component.annotations.Reference;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.rest.internal.datamapper.TableMapper;
import org.cytoscape.rest.internal.datamapper.TableMapper.ColumnNotFoundException;
import org.cytoscape.rest.internal.model.CyColumnModel;
import org.cytoscape.rest.internal.model.CyColumnValuesModel;
import org.cytoscape.rest.internal.model.CyRowModel;
import org.cytoscape.rest.internal.model.CyTableWithRowsModel;
import org.cytoscape.rest.internal.model.ModelConstants;
import org.cytoscape.rest.internal.model.NewColumnParameterModel;
import org.cytoscape.rest.internal.model.RenameModel;
import org.cytoscape.rest.internal.model.SUIDKeyValueModel;
import org.cytoscape.rest.internal.model.UpdateTableModel;

import org.cytoscape.rest.internal.serializer.CyTableSerializer;
import org.cytoscape.rest.internal.serializer.TableModule;
import org.cytoscape.rest.internal.task.ResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
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

/**
 * REST API for CyTable objects. This is for assigned table only.
 * 
 */
@ApplicationScoped
@Tag(name = CyRESTSwagger.CyRESTSwaggerConfig.TABLES_TAG)
@Path("/v1/networks/{networkId}/tables")
public class TableResource extends AbstractResource {

	static final int NETWORK_NOT_FOUND_ERROR= 1;
	static final int TABLE_NOT_FOUND_ERROR = 2;
	static final int COLUMN_NOT_FOUND_ERROR = 3;
	static final int ROW_NOT_FOUND_ERROR = 4;
	
	static final int INVALID_PARAMETER_ERROR = 5;
	static final int SERIALIZATION_ERROR = 6;
	static final int INTERNAL_METHOD_ERROR = 7;
	
	static final String RESOURCE_URN = "networks:tables";

	@Override
	public String getResourceURI() {
		return RESOURCE_URN;
	}

	private final static Logger logger = LoggerFactory.getLogger(TableResource.class);

	@Override
	public Logger getResourceLogger() {
		return logger;
	}

	public final static String ROW_EXAMPLE="A row contains one or more entries of column names to values.\n\n"
			+ "```json\n{\n" 
			+ "  \"SUID\": 101,\n"  
			+ "  \"gene_name\": \"brca1\",\n" 
			+ "  \"exp\": 0.1\n" 
			+ "}\n```";


	public final static String ROW_ARRAY_EXAMPLE="```\n[\n" 
			+ "  {\n" 
			+ "    \"SUID\": 101,\n"  
			+ "    \"gene_name\": \"brca1\",\n" 
			+ "    \"exp\": 0.1\n" 
			+ "  },\n"
			+ "  {\n" 
			+ "    \"SUID\": 102,\n"  
			+ "    \"gene_name\": \"brca2\",\n" 
			+ "    \"exp\": 0.2\n" 
			+ "  }\n"
			+ "]\n"
			+ "```";


	private static enum TableType {
		DEFAULT_NODE("defaultnode"), DEFAULT_EDGE("defaultedge"), DEFAULT_NETWORK("defaultnetwork");

		private final String type;

		private TableType(final String type) {
			this.type = type;
		}

		private String getType() {
			return this.type;
		}
	}

	private TableMapper tableMapper;
	private ObjectMapper tableObjectMapper;
	private CyTableSerializer tableSerializer;

	@Reference
	private CyTableManager tableManager;

	public TableResource() {
		super();
		this.tableMapper = new TableMapper();
		this.tableObjectMapper = new ObjectMapper();
		this.tableObjectMapper.registerModule(new TableModule());
		this.tableSerializer = new CyTableSerializer();
	}

	@Reference
	protected void init(ResourceManager manager) {
		super.init(manager);
	}

	@POST
	@Path("/{tableType}/columns")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary="Create new column(s) in the table", description="Creates a new, empty column in the table specified by the `tableType` parameter, in the network specified by the `networkId` parameter.\n\n"
			+ "This resource can also accept an array of new columns to create multiple columns.")
	@RequestBody( description = "New Column Info", content=
	              @Content(schema=@Schema(implementation=NewColumnParameterModel.class)), required=true)
	@ApiResponses ( value = {
			@ApiResponse(responseCode="201", description="Column(s) createed"),
			@ApiResponse(responseCode="412", description="Could not process column JSON")
	})
	public Response createColumn(@Parameter(description="SUID of the Network") @PathParam("networkId") Long networkId, 
			@Parameter(description="Table Type", schema=@Schema(allowableValues={"defaultnode","defaultedge","defaultnetwork"})) @PathParam("tableType") String tableType,
			@Parameter(hidden=true) final InputStream is) {
		final CyNetwork network = getCyNetwork(NETWORK_NOT_FOUND_ERROR, networkId);
		final CyTable table = getTableByType(network, tableType, null);
		final CyTable localTable = getTableByType(
				network, tableType, JsonTags.COLUMN_IS_LOCAL);

		final ObjectMapper objMapper = new ObjectMapper();
		JsonNode rootNode = null;
		try {
			rootNode = objMapper.readValue(is, JsonNode.class);
		} catch (Exception e) {
			//throw getError("Could not process column JSON.", 
			//		e, Response.Status.PRECONDITION_FAILED);
			throw this.getCIWebApplicationException(Status.INTERNAL_SERVER_ERROR.getStatusCode(), 
					RESOURCE_URN, 
					INVALID_PARAMETER_ERROR, 
					"Could not process column JSON.", 
					logger, e);
		}

		if(rootNode.isArray()) {
			for(JsonNode node: rootNode) {
				tableMapper.createNewColumn(node, table, localTable);
			}
		} else {
			tableMapper.createNewColumn(rootNode, table, localTable);
		}

		// Use 201 for created resource
		return Response.status(Response.Status.CREATED).build();


	}

	@DELETE
	@Path("/{tableType}/columns/{columnName}")
	@Operation(summary="Delete a column in a table", description="Deletes the column specified by the `columnName` parameter from the table speficied by the `tableType` and `networkId` parameters.")
	public Response deleteColumn(
			@Parameter(description="SUID of the network containing the table from which to delete the column") @PathParam("networkId") Long networkId, 
			@Parameter(description="Table Type from which to delete the column", schema=@Schema(allowableValues={"defaultnode","defaultedge","defaultnetwork"})) @PathParam("tableType") String tableType,
			@Parameter(description="Name of the column to delete") @PathParam("columnName") String columnName) {
		final CyNetwork network = getCyNetwork(NETWORK_NOT_FOUND_ERROR, networkId);
		final CyTable table = getTableByType(network, tableType, null);
		if (table != null) {
			table.deleteColumn(columnName);
			return Response.ok().build();
		} else {
			logger.error("Failed to delete a column. (Missing table?)");

			//throw new NotFoundException("Could not find the table.  (This should not happen!)");
			throw this.getCIWebApplicationException(Status.NOT_FOUND.getStatusCode(), 
					RESOURCE_URN, 
					TABLE_NOT_FOUND_ERROR, 
					"Could not find the table.  (This should not happen!)", 
					logger, null);
		}
	}

	@PUT
	@Path("/{tableType}/columns")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary="Update a column name", description="Renames an existing column in the table specified by the `tableType` and `networkId` parameters.")
	@RequestBody(description="Old and new column name", required=true, content = 
							 @Content(schema=@Schema(implementation=RenameModel.class)))
	public Response updateColumnName(
			@Parameter(description="SUID of the network containing the table") @PathParam("networkId") Long networkId,
			@Parameter(description="Table Type", schema=@Schema(allowableValues={"defaultnode","defaultedge","defaultnetwork"})) @PathParam("tableType") String tableType,
			@Parameter(hidden=true) final InputStream is) {
		final CyNetwork network = getCyNetwork(NETWORK_NOT_FOUND_ERROR, networkId);
		final CyTable table = getTableByType(network, tableType, null);
		final ObjectMapper objMapper = new ObjectMapper();
		JsonNode rootNode = null;
		try {
			rootNode = objMapper.readValue(is, JsonNode.class);
		} catch (IOException e) {
			//throw getError("Could not parse the input JSON for updating "
			//		+ "column name.", e, Response.Status.INTERNAL_SERVER_ERROR);
			throw this.getCIWebApplicationException(Status.INTERNAL_SERVER_ERROR.getStatusCode(), 
					RESOURCE_URN, 
					INVALID_PARAMETER_ERROR, 
					"Could not parse the input JSON for updating column name.", 
					logger, e);
		}
		try {
			tableMapper.updateColumnName(rootNode, table);
		} catch (IllegalArgumentException e) {
			throw this.getCIWebApplicationException(Status.INTERNAL_SERVER_ERROR.getStatusCode(), 
					RESOURCE_URN, 
					INVALID_PARAMETER_ERROR, 
					e.getMessage(), 
					logger, e);
		} catch (TableMapper.ColumnNotFoundException e) {
			throw this.getCIWebApplicationException(Status.NOT_FOUND.getStatusCode(), 
					RESOURCE_URN, 
					COLUMN_NOT_FOUND_ERROR, 
					e.getMessage(), 
					logger, e);
		}
		
		return Response.ok().build();
	}

	@PUT
	@Path("/{tableType}/columns/{columnName}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary="Update values in a column", description="Sets the values for cells in the table specified by the `tableType` and `networkId` parameters.\n\nIf the 'default` parameter is not specified, the message body should consist of key-value pairs with which to set "
			+ "values.\n\nIf the `default` parameter is specified, its value will be used for every cell in the column. This is useful to "
			+ "set columns like \"selected.\"")
	@RequestBody(description="Array of SUID Keyed values", required=true, content = 
							 @Content(schema=@Schema(implementation=SUIDKeyValueModel.class)))
	public Response updateColumnValues(
			@Parameter(description="SUID of the network containing the table") @PathParam("networkId") Long networkId,
			@Parameter(description="The type of table", schema=@Schema(allowableValues={"defaultnode","defaultedge","defaultnetwork"})) @PathParam("tableType") String tableType,
			@Parameter(description="Name of the column in which to set values") @PathParam("columnName") String columnName,
			@Parameter(description="Default Value. If this value is provided, all cells will be set to this.", required=false) @QueryParam("default") String defaultValue, 
			@Parameter(hidden=true) final InputStream is) {
		final CyNetwork network = getCyNetwork(NETWORK_NOT_FOUND_ERROR, networkId);
		final CyTable table = getTableByType(network, tableType, null);

		if (defaultValue != null) {
			tableMapper.updateAllColumnValues(defaultValue, table, columnName);
		} else {
			final ObjectMapper objMapper = new ObjectMapper();
			JsonNode rootNode = null;
			try {
				rootNode = objMapper.readValue(is, JsonNode.class);
				
			} catch (IOException e) {
				//throw getError(
				//		"Could not parse the input JSON for updating column values.",
				//		e, Response.Status.INTERNAL_SERVER_ERROR);
				throw this.getCIWebApplicationException(Status.INTERNAL_SERVER_ERROR.getStatusCode(), 
						RESOURCE_URN, 
						INVALID_PARAMETER_ERROR, 
						"Could not parse the input JSON for updating column values.", 
						logger, e);
			}
			tableMapper.updateColumnValues(rootNode, table, columnName);
		}
		return Response.ok().build();
	}

	@PUT
	@Path("/{tableType}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Operation(summary="Update default node/edge/network data table",
	description="Updates the table specified by the `tableType` and `networkId` parameters.  New columns will be created if they "
			+ "do not exist in the target table.\n"
			+ "\n"
			+ "Current limitations:\n"
			+ "* Numbers are handled as Double\n"
			+ "* List columns must be valid, and of non-zero length, with no nulls or heterogenous content.\n"
			)
	@RequestBody(description="The data with which to update the table.", required=true, content = 
							 @Content(schema=@Schema(implementation=UpdateTableModel.class)))
	public Response updateTable(
			@Parameter(description="SUID containing the table") @PathParam("networkId") Long networkId, 
			@Parameter(description="Type of table", schema=@Schema(allowableValues={"defaultnode","defaultedge","defaultnetwork"})) @PathParam("tableType") String tableType,
			@Parameter(schema=@Schema(allowableValues={"local"}), required=false) @QueryParam("class") String tableClass,
			@Parameter(hidden=true) final InputStream is
			) {

		final CyNetwork network = getCyNetwork(NETWORK_NOT_FOUND_ERROR, networkId);
		final CyTable table = getTableByType(network, tableType, tableClass);
		final ObjectMapper objMapper = new ObjectMapper();
		JsonNode rootNode = null;
		try {
			// This should be an JSON array.
			rootNode = objMapper.readValue(is, JsonNode.class);
			
		} catch (Exception e) {
			e.printStackTrace();
			//throw getError("Could not parse the input JSON for updating table because: " + e.getMessage(), e, Response.Status.INTERNAL_SERVER_ERROR);
			throw this.getCIWebApplicationException(Status.INTERNAL_SERVER_ERROR.getStatusCode(), 
					RESOURCE_URN, 
					INVALID_PARAMETER_ERROR, 
					"Could not parse the input JSON for updating table because: " + e.getMessage(), 
					logger, e);
		}
		try {
			tableMapper.updateTableValues(rootNode, table);
		} catch (IllegalArgumentException e) {
			throw this.getCIWebApplicationException(Status.INTERNAL_SERVER_ERROR.getStatusCode(), 
					RESOURCE_URN, 
					INVALID_PARAMETER_ERROR, 
					e.getMessage(), 
					logger, e);
		} catch (ColumnNotFoundException e) {
			throw this.getCIWebApplicationException(Status.NOT_FOUND.getStatusCode(), 
					RESOURCE_URN, 
					COLUMN_NOT_FOUND_ERROR, 
					e.getMessage(), 
					logger, e);
		}
		return Response.ok().build();
	}

	@GET
	@Path("/{tableType}/rows/{primaryKey}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary="Get a row in the table", 
	           description="Gets a row matching the value specified by the `primaryKey` parameter from the table specified by the `tableType` and `networkId` parameters.\n\n" + ModelConstants.ROW_DESCRIPTION,
	           responses= {
							 @ApiResponse(responseCode = "200", description = "A table row", content = { @Content(schema=@Schema(implementation=CyRowModel.class)) })
						 }
	)
	public String getRow(
			@Parameter(description="SUID of the network containing the table") @PathParam("networkId") Long networkId,
			@Parameter(description="Table type", schema=@Schema(allowableValues={"defaultnode","defaultedge","defaultnetwork"})) @PathParam("tableType") String tableType,
			@Parameter(description="Primary key of the row Object, normally an SUID") @PathParam("primaryKey") Long primaryKey) {
		final CyNetwork network = getCyNetwork(NETWORK_NOT_FOUND_ERROR, networkId);
		final CyTable table = getTableByType(network, tableType, null);
		if (!table.rowExists(primaryKey)) {
			//throw new NotFoundException("Could not find the row "
			//		+ "with primary key: " + primaryKey);
			throw this.getCIWebApplicationException(Status.NOT_FOUND.getStatusCode(), 
					RESOURCE_URN, 
					ROW_NOT_FOUND_ERROR, 
					"Could not find the row with primary key: " + primaryKey, 
					logger, null);
		}

		final CyRow row = table.getRow(primaryKey);

		try {
			return this.serializer.serializeRow(row);
		} catch (IOException e) {
			//throw getError("Could not serialize a row with primary key: " + primaryKey, e,
			//		Response.Status.INTERNAL_SERVER_ERROR);
			throw this.getCIWebApplicationException(Status.INTERNAL_SERVER_ERROR.getStatusCode(), 
					RESOURCE_URN, 
					SERIALIZATION_ERROR, 
					"Could not serialize a row with primary key: " + primaryKey, 
					logger, e);
		}
	}

	@GET
	@Path("/{tableType}/rows/{primaryKey}/{columnName}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary="Get a value from a cell", description="Return the value of a cell specified by the `primaryKey` and `columnName` parameters in the table specified by the `tableType` and `networkId` parameters.\n\nReturns a JSON representation of a String, Boolean, Number, or List.")
	public Object getCell(
			@Parameter(description="SUID of the network containing the table") @PathParam("networkId") Long networkId,
			@Parameter(description="Table type", schema=@Schema(allowableValues={"defaultnode","defaultedge","defaultnetwork"})) @PathParam("tableType") String tableType,
			@Parameter(description="Primary key of the row Object, normally an SUID") @PathParam("primaryKey") Long primaryKey,
			@Parameter(description="Name of the Column") @PathParam("columnName") String columnName) {
		final CyNetwork network = getCyNetwork(NETWORK_NOT_FOUND_ERROR, networkId);

		final CyTable table = getTableByType(network, tableType, null);
		if (!table.rowExists(primaryKey)) {
			//throw new NotFoundException("Could not find the row with promary key: " + primaryKey);
			throw this.getCIWebApplicationException(Status.NOT_FOUND.getStatusCode(), 
					RESOURCE_URN, 
					ROW_NOT_FOUND_ERROR, 
					"Could not find the row with primary key: " + primaryKey, 
					logger, null);
		}

		final CyColumn column = table.getColumn(columnName);
		if (column == null) {
			//throw new NotFoundException("Could not find the column: " + columnName);
			throw this.getCIWebApplicationException(Status.NOT_FOUND.getStatusCode(), 
					RESOURCE_URN, 
					COLUMN_NOT_FOUND_ERROR, 
					"Could not find the column: " + columnName, 
					logger, null);
		}

		final CyRow row = table.getRow(primaryKey);

		if (column.getType() == List.class) {
			List<?> listCell = row.getList(columnName, column.getListElementType());
			if (listCell == null) {
				//throw new NotFoundException("Could not find list value.");
				throw this.getCIWebApplicationException(Status.NOT_FOUND.getStatusCode(), 
						RESOURCE_URN, 
						ROW_NOT_FOUND_ERROR, 
						"Could not find list value for row with primary key: " + primaryKey, 
						logger, null);
			} else {
				return listCell;
			}
		} else {
			final Object cell = row.get(columnName, column.getType());
			if (cell == null) {
				//throw new NotFoundException("Could not find value.");
				throw this.getCIWebApplicationException(Status.NOT_FOUND.getStatusCode(), 
						RESOURCE_URN, 
						ROW_NOT_FOUND_ERROR, 
						"Could not find value for row with primary key: " + primaryKey, 
						logger, null);
			}

			if (column.getType() == String.class) {
				return cell.toString();
			} else {
				return cell;
			}
		}
	}

	@GET
	@Path("/{tableType}/rows")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary="Get all rows in a table", 
	           description="Returns all rows from the table specified by `networkId` and `tableType` parameters. Returns a JSON representation of an array of rows.\n\n" + ROW_ARRAY_EXAMPLE, 
		         responses = {
								@ApiResponse(responseCode = "200", description = "Array of rows", 
			    		               content = { @Content(array = @ArraySchema(schema = @Schema(implementation=CyRowModel.class)))})
	})
	public String getRows(
			@Parameter(description="SUID of the network containing the table") @PathParam("networkId") Long networkId,
			@Parameter(description="Table Type", schema=@Schema(allowableValues={"defaultnode","defaultedge","defaultnetwork"})) @PathParam("tableType") String tableType) {
		final CyNetwork network = getCyNetwork(NETWORK_NOT_FOUND_ERROR, networkId);
		final CyTable table = getTableByType(network, tableType, null);
		try {
			return this.serializer.serializeAllRows(table.getAllRows());
		} catch (IOException e) {
			//throw getError("Could not serialize rows.", e, Response.Status.INTERNAL_SERVER_ERROR);
			throw this.getCIWebApplicationException(Status.INTERNAL_SERVER_ERROR.getStatusCode(), 
					RESOURCE_URN, 
					SERIALIZATION_ERROR, 
					"Could not serialize rows.", 
					logger, e);
		}
	}

	@GET
	@Path("/{tableType}/columns")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary="Get all columns in a table", description="Returns all the columns in the table specified by the `networkId` and `tableType` parameters.",
		         responses = {
								@ApiResponse(responseCode = "200", description = "List of columns", 
			    		               content = { @Content(array = @ArraySchema(schema = @Schema(implementation=CyColumnModel.class)))})
	})
	public String getColumnNames(
			@Parameter(description="SUID of the network containing the table") @PathParam("networkId") Long networkId,
			@Parameter(description="Table Type", schema=@Schema(allowableValues={"defaultnode","defaultedge","defaultnetwork"})) @PathParam("tableType") String tableType) {
		final CyNetwork network = getCyNetwork(NETWORK_NOT_FOUND_ERROR, networkId);
		final CyTable table = getTableByType(network, tableType, null);
		try {
			return this.serializer.serializeColumns(table.getColumns());
		} catch (IOException e) {
			//throw getError("Could not serialize column names.", e, Response.Status.INTERNAL_SERVER_ERROR);
			throw this.getCIWebApplicationException(Status.INTERNAL_SERVER_ERROR.getStatusCode(), 
					RESOURCE_URN, 
					SERIALIZATION_ERROR, 
					"Could not serialize column names.", 
					logger, e);
		}
	}

	@GET
	@Path("/{tableType}/columns/{columnName}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary="Get all values in a column", description="Returns all the values for the column specified by the `columnType` parameter, in the table specified by the `networkId` and `tableType` parameters.", 
	           responses= {
							 @ApiResponse(responseCode = "200", description = "Column values", content = { @Content(schema=@Schema(implementation=CyColumnValuesModel.class)) })
						 }
	)
	public String getColumnValues(
			@Parameter(description="SUID of the Network") @PathParam("networkId") Long networkId,
			@Parameter(description="Type of Table", schema=@Schema(allowableValues={"defaultnode","defaultedge","defaultnetwork"})) @PathParam("tableType") String tableType,
			@Parameter(description="Name of the Column") @PathParam("columnName") String columnName) {
		final CyNetwork network = getCyNetwork(NETWORK_NOT_FOUND_ERROR, networkId);
		final CyTable table = getTableByType(network, tableType, null);
		final CyColumn column = table.getColumn(columnName);
		final List<Object> values = column.getValues(column.getType());

		try {
			return this.serializer.serializeColumnValues(column, values);
		} catch (IOException e) {
			//throw getError("Could not serialize column values.", e, Response.Status.INTERNAL_SERVER_ERROR);
			throw this.getCIWebApplicationException(Status.INTERNAL_SERVER_ERROR.getStatusCode(), 
					RESOURCE_URN, 
					SERIALIZATION_ERROR, 
					"Could not serialize column names.", 
					logger, e);
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary="Get all Tables assigned to the Network", description="Returns every table in the network specified by the `networkId` parameter.", 
		         responses = {
								@ApiResponse(responseCode = "200", description = "List of tables with rows", 
			    		               content = { @Content(array = @ArraySchema(schema = @Schema(implementation=CyTableWithRowsModel.class)))})
	})
	public String getTables(
			@Parameter(description="SUID of the Network") @PathParam("networkId") Long networkId) {
		final Set<CyTable> tables = this.tableManager.getAllTables(true);
		try {
			return tableObjectMapper.writeValueAsString(tables);
		} catch (IOException e) {
			//throw getError("Could not serialize tables.", e, Response.Status.INTERNAL_SERVER_ERROR);
			throw this.getCIWebApplicationException(Status.INTERNAL_SERVER_ERROR.getStatusCode(), 
					RESOURCE_URN, 
					SERIALIZATION_ERROR, 
					"Could not serialize tables.", 
					logger, e);
		}
	}

	private final CyTable getTableByType(final CyNetwork network, 
			final String tableType, final String tableClass) {

		// Check local or not
		final Boolean isLocal;
		if(tableClass == null || tableClass.isEmpty()) {
			isLocal = false;
		} else if(tableClass.equals("local")) {
			isLocal = true;
		} else {
			isLocal = false;
		}

		CyTable table;
		if (tableType.equals(TableType.DEFAULT_NODE.getType())) {
			if(isLocal) {
				table = network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
			} else {
				table = network.getDefaultNodeTable();
			}
		} else if (tableType.equals(TableType.DEFAULT_EDGE.getType())) {
			if(isLocal) {
				table = network.getTable(CyEdge.class, CyNetwork.LOCAL_ATTRS);
			} else {
				table = network.getDefaultEdgeTable();
			}
		} else if (tableType.equals(TableType.DEFAULT_NETWORK.getType())) {
			if(isLocal) {
				table = network.getTable(CyNetwork.class, CyNetwork.LOCAL_ATTRS);
			} else {
				table = network.getDefaultNetworkTable();
			}
		} else {
			// No such table.
			//throw new NotFoundException("No such table type: " + tableType);
			throw this.getCIWebApplicationException(Status.NOT_FOUND.getStatusCode(), 
					RESOURCE_URN, 
					TABLE_NOT_FOUND_ERROR, 
					"No such table type: " + tableType, 
					logger, null);
		}
		return table;
	}

	@GET
	@Path("/{tableType}")
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary="Get a default table", description="Returns the table specified by the `networkId` and 'tableType' parameters.", 
			responses = {
					@ApiResponse(responseCode = "200", description = "Table with rows", 
			             		 content = { @Content(schema = @Schema(implementation=CyTableWithRowsModel.class))})
	})
	public String getTable(
			@Parameter(description="SUID of the network containing the table") @PathParam("networkId") Long networkId,
			@Parameter(description="Table type", schema=@Schema(allowableValues={"defaultnode","defaultedge","defaultnetwork"})) @PathParam("tableType") String tableType) {
		final CyNetwork network = getCyNetwork(NETWORK_NOT_FOUND_ERROR, networkId);
		final CyTable table = getTableByType(network, tableType, null);

		try {
			return this.tableObjectMapper.writeValueAsString(table);
		} catch (JsonProcessingException e) {
			//throw getError("Could not serialize table.", e, Response.Status.INTERNAL_SERVER_ERROR);
			throw this.getCIWebApplicationException(Status.INTERNAL_SERVER_ERROR.getStatusCode(), 
					RESOURCE_URN, 
					SERIALIZATION_ERROR, 
					"Could not serialize table.", 
					logger, e);
		}
	}

	@GET
	@Path("/{tableType}.csv")
	@Produces(MediaType.TEXT_PLAIN)
	@Operation(summary="Get a table as CSV", description="Returns a CSV representation of the table specified by the `networkId` and `tableType` parameters. All column names are included in the first row.")
	public String getTableAsCsv(
			@Parameter(description="SUID of the network containing the table") @PathParam("networkId") Long networkId, 
			@Parameter(description="Table type", schema=@Schema(allowableValues={"defaultnode","defaultedge","defaultnetwork"})) @PathParam("tableType") String tableType) {
		return getTableString(networkId, tableType, ",");
	}

	@GET
	@Path("/{tableType}.tsv")
	@Produces(MediaType.TEXT_PLAIN)
	@Operation(summary="Get a table as TSV (tab delimited text)", description="Returns a TSV (tab delimited text) representation of the table specified by the `networkId` and `tableType` parameters. All column names are included in the first row.")
	public String getTableAsTsv(
			@Parameter(description="SUID of the network containing the table") @PathParam("networkId") Long networkId, 
			@Parameter(description="Table type", schema=@Schema(allowableValues={"defaultnode","defaultedge","defaultnetwork"})) @PathParam("tableType") String tableType) {
		return getTableString(networkId, tableType, "\t");
	}


	/**
	 * Actual function to generate CSV/TSV
	 * 
	 * @param networkId
	 * @param tableType
	 * @param separator
	 * @return
	 */
	private final String getTableString(final Long networkId, 
			final String tableType, final String separator) {

		final CyNetwork network = getCyNetwork(NETWORK_NOT_FOUND_ERROR, networkId);
		final CyTable table = getTableByType(network, tableType, null);
		try {
			final String result = tableSerializer.toCSV(table, separator);
			return result;
		} catch (Exception e) {
			//throw getError("Could not serialize table into CSV.", e, Response.Status.INTERNAL_SERVER_ERROR);
			throw this.getCIWebApplicationException(Status.INTERNAL_SERVER_ERROR.getStatusCode(), 
					RESOURCE_URN, 
					SERIALIZATION_ERROR, 
					"Could not serialize table into CSV.", 
					logger, e);
		}
	}
}
