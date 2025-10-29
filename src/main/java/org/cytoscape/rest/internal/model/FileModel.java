package org.cytoscape.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;


@Schema
public class FileModel 
{
	@Schema(description="Full name of the file.", requiredMode=Schema.RequiredMode.REQUIRED)
	public String file;
	
	public FileModel(String file)
	{
		this.file = file;
	}
}
