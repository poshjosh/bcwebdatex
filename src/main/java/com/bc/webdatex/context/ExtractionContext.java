package com.bc.webdatex.context;

import com.bc.json.config.JsonConfig;
import com.bc.webdatex.filters.Filter;
import com.bc.webdatex.extractors.DataExtractor;
import java.util.Map;
import org.htmlparser.NodeFilter;
import com.bc.webdatex.extractors.PageExtractor;
import com.bc.webdatex.extractors.node.AttributesExtractor;
import java.util.function.UnaryOperator;

public interface ExtractionContext {
    
  AttributesExtractor getAttributesExtractor(Object id);
        
  ExtractionConfig getExtractionConfig();
  
  JsonConfig getConfig();
  
  PageExtractor getExtractor();
  
  PageExtractor getExtractor(float tolerance, boolean greedy);
  
  NodeFilter getFilter();
  
  Filter<String> getCaptureUrlFilter();
  
  Filter<String> getScrappUrlFilter();
  
  UnaryOperator<Map<String, Object>> getFormatter();
  
  UnaryOperator<String> getUrlFormatter();
  
  DataExtractor<String> getUrlDataExtractor();
}
