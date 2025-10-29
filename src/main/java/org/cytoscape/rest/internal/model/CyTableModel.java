package org.cytoscape.rest.internal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name="Cytoscape Table", description="A Cytoscape table definition", subTypes= {CyTableWithRowsModel.class})
public class CyTableModel {
	@Schema(description = "SUID of the Table", requiredMode=Schema.RequiredMode.REQUIRED)
	public Long SUID;
	
	@Schema(description = "Title", requiredMode=Schema.RequiredMode.REQUIRED)
	public String title;
	
	@Schema(description = "Public. This is true if this table is visible by default in the Cytoscape GUI", requiredMode=Schema.RequiredMode.REQUIRED)
	@JsonProperty("public")
	public boolean _public;
	
	@Schema(description = "Mutable", requiredMode=Schema.RequiredMode.REQUIRED)
	public String mutable;
	
	@Schema(description = "Primary Key", requiredMode=Schema.RequiredMode.REQUIRED)
	public String primaryKey;
}
