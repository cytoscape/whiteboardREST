package org.cytoscape.rest.internal.model;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema
public class VisualStyleMappingModel {
	
	@Schema(description="The type of Mapping", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues={"discreet","continuous","passthrough"})
	public String mappingType;
	
	@Schema(description="Table column this Mapping gets values from", requiredMode = Schema.RequiredMode.REQUIRED)
	public String mappingColumn;
	
	@Schema(description="The type of the `mappingColumn`, represented as the simple name of its Java class.", requiredMode = Schema.RequiredMode.REQUIRED)
	public String mappingColumnType;
	
	@Schema(description="Unique internal name of the Visual Property this mapping is applied to.", requiredMode = Schema.RequiredMode.REQUIRED)
	public String visualProperty;
	
	@Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description="Map for `discreet` Mappings.", example="")
	public List<DiscreteMappingKeyValueModel> map;
	
	@Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description="Points for `continuous` Mappings.", example="")
	public List<PointModel> points;
	
}
