package org.cytoscape.rest.internal.model;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public class LayoutColumnTypesModel {
	
	@Schema(description="Types of Compatible Node Columns")
	public List<ModelConstants.ColumnTypeAll> compatibleNodeColumnDataTypes;
	@Schema(description="Types of Compatible Edge Columns")
	public List<ModelConstants.ColumnTypeAll> compatibleEdgeColumnDataTypes;
}
