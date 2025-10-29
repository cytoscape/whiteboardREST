package org.cytoscape.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="Row data associated with the Edge" + ModelConstants.ROW_DESCRIPTION)
public class EdgeDataModel {

	private String source;
	private String target;

	/**
	 * @return the source
	 */
	@Schema(description="SUID of the Edge's Source Node", requiredMode=Schema.RequiredMode.REQUIRED)
	public String getSource() {
		return source;
	}

	/**
	 * @param source
	 *            the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/**
	 * @return the target
	 */
	@Schema(description="SUID of the Edge's Target Node", requiredMode=Schema.RequiredMode.REQUIRED)
	public String getTarget() {
		return target;
	}

	/**
	 * @param target
	 *            the target to set
	 */
	public void setTarget(String target) {
		this.target = target;
	}
}
