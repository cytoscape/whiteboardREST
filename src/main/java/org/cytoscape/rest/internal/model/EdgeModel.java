package org.cytoscape.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="Edge Data")
public class EdgeModel {
	private EdgeDataModel data;

	/**
	 * @return the data
	 */
	@Schema(description="Associated Data from the edge table. " + ModelConstants.ROW_DESCRIPTION)
	public EdgeDataModel getData() {
		return data;
	}

	/**
	 * @param data
	 *            the data to set
	 */
	public void setData(EdgeDataModel data) {
		this.data = data;
	}
}
