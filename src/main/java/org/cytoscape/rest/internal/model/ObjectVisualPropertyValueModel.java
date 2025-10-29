package org.cytoscape.rest.internal.model;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public class ObjectVisualPropertyValueModel {
	
	@Schema(description="SUID of the Object")
	public Long SUID;
	
	@Schema(description="List of the Objects Visual Properties")
	public List<VisualPropertyValueModel> view;
}
