package org.cytoscape.rest.internal.model;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name="Cytoscape Table with Rows", description="A definition of a column from a Cytoscape table, and a list of its rows.", allOf={CyTableModel.class})
public class CyTableWithRowsModel extends CyTableModel{
	@Schema(description = "Rows in this Table", requiredMode=Schema.RequiredMode.REQUIRED)
	public List<CyRowModel> rows;
}
