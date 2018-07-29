package com.bc.webdatex.extractors;

import com.bc.json.config.JsonConfig;
import com.bc.webdatex.extractors.node.NodeExtractor;
import java.util.Set;
import org.htmlparser.Tag;
import com.bc.webdatex.context.ExtractionContext;
import com.bc.webdatex.context.ExtractionConfig;

public interface PageExtractor extends NodeListExtractor {
    
  JsonConfig getCapturerConfig();
  
  ExtractionContext getCapturerContext();
  
  ExtractionConfig getCapturerSettings();
  
  Tag getTitleTag();

  boolean isWithinTitleTag();

  NodeExtractor createNodeExtractor(Object id);
  
  NodeExtractor getNodeExtractor(Object id);
  
  Set getFailedNodeExtractorIds();
  
  Set getNodeExtractorIds();
  
  Set getSuccessfulNodeExtractorIds();
  
  boolean isSuccessfulCompletion(Set<String> columns);
}
