package com.bc.webdatex.extractors;

import com.bc.json.config.JsonConfig;
import com.bc.webdatex.context.CapturerContext;
import com.bc.webdatex.extractors.node.NodeExtractor;
import com.bc.webdatex.context.NodeExtractorConfig;
import java.util.Set;
import org.htmlparser.Tag;

public interface PageExtractor extends NodeListExtractor {
    
  JsonConfig getCapturerConfig();
  
  CapturerContext getCapturerContext();
  
  NodeExtractorConfig getCapturerSettings();
  
  Tag getTitleTag();

  boolean isWithinTitleTag();

  NodeExtractor createNodeExtractor(Object id);
  
  NodeExtractor getNodeExtractor(Object id);
  
  Set getFailedNodeExtractorIds();
  
  Set getNodeExtractorIds();
  
  Set getSuccessfulNodeExtractorIds();
  
  boolean isSuccessfulCompletion(Set<String> columns);
}
