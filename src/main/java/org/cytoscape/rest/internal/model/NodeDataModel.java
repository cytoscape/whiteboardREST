package org.cytoscape.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name="Node Data", description=ModelConstants.ROW_DESCRIPTION)
public class NodeDataModel {

	private String id;

	/**
	 * @return the id
	 */
	@Schema(description="Primary Key value of the Node. This is normally the SUID.")
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
}
