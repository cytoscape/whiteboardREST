package org.cytoscape.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public class SUIDNameModel {
	@Schema(description="SUID of the Object")
	public long SUID;
	@Schema(description="Name of the Object. This is the content of the Objects `name` column in its default table.")
	public String name;
}
