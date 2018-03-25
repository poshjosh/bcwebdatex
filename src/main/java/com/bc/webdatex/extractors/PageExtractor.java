package com.bc.webdatex.extractors;

import com.bc.json.config.JsonConfig;
import com.bc.webdatex.context.CapturerContext;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import com.bc.webdatex.extractors.node.NodeExtractorConfig;

public abstract interface PageExtractor extends NodeListExtractor {
    
  public abstract JsonConfig getCapturerConfig();
  
  public abstract CapturerContext getCapturerContext();
  
  public abstract NodeExtractorConfig getCapturerSettings();
  
  public abstract String getPageTitle();
  
  public abstract String getTaskName();
  
  public abstract Tag getTitleTag();
  
  public abstract boolean isDone();
  
  public abstract boolean isTitleExtracted();
  
  public abstract void reset();
  
  public abstract void setPageTitle(String paramString);
  
  public abstract void visitEndTag(Tag paramTag);
  
  public abstract void visitRemarkNode(Remark paramRemark);
  
  public abstract void visitStringNode(Text paramText);
  
  public abstract void visitTag(Tag paramTag);
}
