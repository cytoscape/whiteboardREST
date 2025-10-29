package org.cytoscape.rest.internal.model;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name="Cytoscape Column Values")
public class CyColumnValuesModel {
	@Schema(description = "Column Name", requiredMode=Schema.RequiredMode.REQUIRED, example="Weight")
	public String name;
	@Schema(description = "Column Values. These are formatted as JSON primitives.", requiredMode=Schema.RequiredMode.REQUIRED, example="9")
	public List<?> descriptions;
}
