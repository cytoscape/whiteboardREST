package org.cytoscape.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name="Title Model", description="The new name", subTypes= {CyTableWithRowsModel.class})
public class TitleModel {
	
	@Schema(description = "Title", requiredMode=Schema.RequiredMode.REQUIRED)
	public String title;
}
