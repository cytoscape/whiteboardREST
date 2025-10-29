package org.cytoscape.rest.internal.model;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public class VisualPropertyValuesModel {
	@Schema(description="Unique internal name of the Visual Property.", example="NODE_SHAPE")
	public String visualProperty;
	@Schema(description="Values available for the Visual Property")
	public List<Object> descriptions;
}
