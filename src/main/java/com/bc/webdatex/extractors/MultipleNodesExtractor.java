package com.bc.webdatex.extractors;

import com.bc.webdatex.extractors.node.NodeExtractor;
import java.util.Set;

public abstract interface MultipleNodesExtractor extends PageExtractor {
    
  public abstract NodeExtractor createExtractor(String paramString);
  
  public abstract NodeExtractor getExtractor(String paramString);
  
  public abstract Set<String> getFailedNodeExtractors();
  
  public abstract Set<String> getNodeExtractorIds();
  
  public abstract Set<String> getSuccessfulNodeExtractors();
  
  public abstract boolean isSuccessfulCompletion(Set<String> columns);
  
  public abstract void reset();
}
