package org.cytoscape.rest.internal.model;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
@Schema
public class VisualStyleDefaultsModel {
	@Schema(description="A list of Visual Properties and their default values.")
	public List<VisualPropertyValueModel> defaults;
}
