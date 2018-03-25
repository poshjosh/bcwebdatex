package com.bc.webdatex.extractors;

import com.bc.json.config.JsonConfig;
import com.bc.util.Log;
import com.bc.webdatex.context.CapturerContext;
import com.bc.webdatex.functions.FindValueWithMatchingKey;
import java.util.Map;
import java.util.logging.Level;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import com.bc.webdatex.extractors.node.NodeExtractorConfig;
import java.util.Objects;
import java.util.function.BiFunction;

public class PageExtractorImpl extends NodeListExtractorImpl implements PageExtractor {
    
  private boolean done;
  private String pageTitle;
  private Tag titleTag;
  private boolean titleExtracted;
  private final BiFunction<Map, String, String> findValueWithMatchingKey;
  private final CapturerContext context;
  
  public PageExtractorImpl(CapturerContext context) {
    this.context = Objects.requireNonNull(context);
    this.findValueWithMatchingKey = new FindValueWithMatchingKey();
  }
  
  @Override
  public void reset() {
      
    super.reset();
    
    this.done = false;
    this.pageTitle = null;
    this.titleTag = null;
    this.titleExtracted = false;
  }
  
  @Override
  public void visitTag(Tag tag) {
      
    if (this.done) { 
      return;
    }
    Log.getInstance().log(Level.FINER, "visitTag: {0}", getClass(), tag);
    
    super.visitTag(tag);
    
    if (tag.getTagName().equals("TITLE")) {
      this.titleTag = tag;
    }
  }
  
  @Override
  public void visitEndTag(Tag tag) {
      
    if (this.done) { 
      return;
    }
    Log.getInstance().log(Level.FINER, "visitEndTag: {0}", getClass(), tag);
    
    super.visitEndTag(tag);
    
    if (tag.getTagName().equals("TITLE")) {
      this.titleTag = null;
    }
  }
  

  @Override
  public void visitStringNode(Text node) {
      
    if (this.done) { 
      return;
    }
    Log.getInstance().log(Level.FINER, "#visitStringNode: {0}", getClass(), node);
    
    super.visitStringNode(node);
    
    extractTitle(node);
  }
  
  @Override
  public void visitRemarkNode(Remark remark) {
      
    if (this.done) {
      return;
    }
    super.visitRemarkNode(remark);
  }
  
  private boolean extractTitle(Text node) {
      
    if (!this.titleExtracted && withinTitleTag()) {

      this.titleExtracted = true;
      
      doExtractTitle(node);
      
      this.titleTag = null;
      
      return true;
    }
    
    return false;
  }
  
  protected String add(String key, Object val, boolean append, boolean guessColumnNameFromKey) {
      
    Log.getInstance().log(Level.FINER, "#add. Append: {0}, Key: {1}, Val: {2}", 
            getClass(), append, key, val);
    
    if ((key == null) || (val == null)) { 
      return null;
    }
    if ((key.trim().isEmpty()) || (val.toString().trim().isEmpty())) { 
      return null;
    }
    String col = key;
    
    if (guessColumnNameFromKey) {
        
      Map keys = getCapturerConfig().getMap(new Object[] { "keys" });
      
      col = findValueWithMatchingKey.apply(keys, key);
      
      Log.getInstance().log(Level.FINER, "#add. Key: {0}, Matching col: {1}", getClass(), key, col);
    }
    
    if (col == null) {
      return null;
    }
    
    doAdd(col, val, append);
    
    return col;
  }
  
  private String doAdd(String col, Object val, boolean append) {
      
    Object oldVal = getExtractedData().get(col);
    
    if (oldVal == null) {

      getExtractedData().put(col, val);
      
      Log.getInstance().log(Level.FINE, "#doAdd. Added: [{0}={1}]", getClass(), col, val);
      
    } else if (append) {
        
      if (!oldVal.equals(val)) {
          
        String lineSep = getCapturerSettings().getLineSeparator();
        String partSep = getCapturerSettings().getPartSeparator();
        if (lineSep != null) {
          val = val.toString().replace("\n", lineSep);
        }
        
        String s = partSep != null ? partSep : "";
        
        String newVal = oldVal + s + val;
        
        getExtractedData().put(col, newVal);
        
        Log.getInstance().log(Level.FINE, "#doAdd. Appended: [{0}={1}]", getClass(), col, val);
      }
    }
    
    return col;
  }
  
  protected boolean withinTitleTag() {
    return this.titleTag != null;
  }
  
  protected void doExtractTitle(Text node) {
      
    String title = getTitle(node);
    
    setPageTitle(title);
    
    node.setText(title);
  }
  
  protected String getTitle(Text node) {
      
    String val = node.getText();
    
    String defaultTitle = getCapturerSettings().getDefaultTitle();
    
    if ((val == null) || (val.isEmpty())) { 
      return defaultTitle;
    }
    if ((defaultTitle == null) || (defaultTitle.isEmpty())) { 
      return val;
    }
    return defaultTitle + " | " + val;
  }
  
  @Override
  public boolean isDone() {
    return this.done;
  }
  
  @Override
  public boolean isTitleExtracted() {
    return this.titleExtracted;
  }
  
  @Override
  public Tag getTitleTag() {
    return this.titleTag;
  }
  
  @Override
  public CapturerContext getCapturerContext() {
    return this.context;
  }
  
  @Override
  public NodeExtractorConfig getCapturerSettings() {
    return this.context.getNodeExtractorConfig();
  }
  
  @Override
  public JsonConfig getCapturerConfig() {
    return this.context.getConfig();
  }
  
  @Override
  public void setPageTitle(String pageTitle) {
    this.pageTitle = pageTitle;
  }
  
  @Override
  public String getPageTitle() {
    return this.pageTitle;
  }
  
  @Override
  public String getTaskName() {
    return getClass().getName();
  }
}