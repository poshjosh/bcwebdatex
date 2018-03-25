package com.bc.webdatex.context;

import com.bc.webdatex.extractors.node.NodeExtractorConfig;
import com.bc.json.config.JsonConfig;
import com.bc.webdatex.extractors.node.AttributesExtractor;
import com.bc.webdatex.filters.Filter;
import com.bc.webdatex.formatters.Formatter;
import com.bc.webdatex.extractors.DataExtractor;
import java.util.Map;
import org.htmlparser.NodeFilter;
import com.bc.webdatex.extractors.MultipleNodesExtractor;

public abstract interface CapturerContext
{
  public abstract NodeExtractorConfig getNodeExtractorConfig();
  
  public abstract AttributesExtractor getAttributesExtractor(String paramString);
  
  public abstract JsonConfig getConfig();
  
  public abstract MultipleNodesExtractor getExtractor();
  
  public abstract NodeFilter getFilter();
  
  public abstract Filter<String> getCaptureUrlFilter();
  
  public abstract Filter<String> getScrappUrlFilter();
  
  public abstract Formatter<Map<String, Object>> getFormatter();
  
  public abstract Formatter<String> getUrlFormatter();
  
  public abstract DataExtractor<String> getUrlDataExtractor();
}
