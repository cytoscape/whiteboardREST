package org.cytoscape.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="Cytoscape Desktop Availability")
public class DesktopAvailableModel {
	@Schema(description="This is `true` if Cytoscape Desktop is available and `false` if Cytoscape is running in headless mode (not available yet).")
	public boolean isDesktopAvailable;
}
