package gov.nasa.arc.dert.raster.geotiff;

import java.util.HashMap;

public class GdalMetadata {
	
	private HashMap<String,String> metadataMap;
	
	public GdalMetadata(String str) {
		metadataMap = new HashMap<String,String>();
		
		String[] token = str.split("\n");
		for (int i=0; i<token.length; ++i) {
			token[i] = token[i].trim();
			if (token[i].startsWith("<Item")) {
				int p = token[i].indexOf(" name=\"");
				if (p < 0)
					continue;
				String name = token[i].substring(p+7);
				p = name.indexOf("\"");
				if (p < 0)
					continue;
				name = name.substring(0, p);
				p = token[i].indexOf(">");
				if (p < 0)
					continue;
				int q = token[i].indexOf("</");
				if (q < 0)
					continue;
				String value = token[i].substring(p+1, q);
				metadataMap.put(name, value);
			}
		}
	}
	
	public String getValue(String name) {
		return(metadataMap.get(name));
	}

}
