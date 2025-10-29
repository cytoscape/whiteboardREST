package org.cytoscape.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="Node Data")
public class NodeModel {
	private NodeDataModel data;

	/**
	 * @return the data
	 */
	@Schema(description="Associated Data from the Node Table. " + ModelConstants.ROW_DESCRIPTION)
	public NodeDataModel getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(NodeDataModel data) {
		this.data = data;
	}
}
