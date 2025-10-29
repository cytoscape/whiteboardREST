package org.cytoscape.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Message model for returning messages from REST calls.
 * 
 * @author David Otasek (dotasek.dev@gmail.com)
 *
 */
@Schema
public class MessageModel 
{
	@Schema(description="Message text")
	public String message;
	
	public MessageModel(String string) {
		message = string;
	}
}
