package org.cytoscape.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name="Visual Property Value")
public class VisualPropertyValueModel {
	@Schema(description="Unique internal name of the Visual Property.", example="VISUAL_PROPERTY_NAME")
	public String visualProperty;
	@Schema(description="Serialized value of the Visual Property, or null.")
	public Object value;
}
