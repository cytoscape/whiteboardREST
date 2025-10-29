package org.cytoscape.rest.internal.model;

import java.util.List;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description="Table Update Data")
public class UpdateTableModel {
	@Schema(description = "The column in the target table to use as a key. If not specified, SUID will be used.", example="SUID")
	public String key;
	@Schema(description = "The field in the row data to use as a key. If not specified, SUID will be used.", example="id")
	public String dataKey;
	@Schema(description = "The row data with which to update the table.\n\nEach row entry should consist of pairs of keys and values, including one that supplies a value for the `dataKey` key. \n"
			+ "```\n"
			+ "[\n"
			+ "  {\n"
			+ "    \"id\": 12345,\n"
			+ "    \"gene_name\": \"brca1\",\n"  
			+ "    \"exp1\": 0.11,\n"  
			+ "    \"exp2\": 0.2\n"
			+ "  },...\n"
			+ "]\n"
			+ "```\n", requiredMode=Schema.RequiredMode.REQUIRED)
	public List<CyRowModel> data;
}
