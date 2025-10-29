package org.cytoscape.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 
 * @author David Otasek (dotasek.dev@gmail.com)
 *
 */
@Schema
public class CountModel 
{
	@Schema(description="Count description.", example="1")
	public Long count;
	
	public CountModel(Long count) {
		this.count=count;
	}
}
