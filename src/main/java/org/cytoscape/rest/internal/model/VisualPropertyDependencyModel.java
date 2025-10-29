package org.cytoscape.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="Visual Property Dependency")
public class VisualPropertyDependencyModel {
	@Schema(description="The name of the Visual Property Dependency", example="arrowColorMatchesEdge")
	public String visualPropertyDependency;
	@Schema(description="```true``` if this dependency is enabled.")
    public boolean enabled;
}
