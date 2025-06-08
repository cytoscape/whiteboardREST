package org.cytoscape.rest.internal.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Cytoscape Table", description="A Cytoscape table definition", subTypes= {CyTableWithRowsModel.class})
public class CyTableModel {
	@ApiModelProperty(value = "SUID of the Table", required=true)
	public Long SUID;
	
	@ApiModelProperty(value = "Title", required=true)
	public String title;
	
	@ApiModelProperty(value = "Public. This is true if this table is visible by default in the Cytoscape GUI", required=true)
	@JsonProperty("public")
	public boolean _public;
	
	@ApiModelProperty(value = "Mutable", required=true)
	public String mutable;
	
	@ApiModelProperty(value = "Primary Key", required=true)
	public String primaryKey;
}
