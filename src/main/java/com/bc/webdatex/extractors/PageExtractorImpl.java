package com.bc.webdatex.extractors;

import com.bc.json.config.JsonConfig;
import com.bc.nodelocator.ConfigName;
import com.bc.nodelocator.Path;
import com.bc.webdatex.extractors.node.NodeExtractor;
import com.bc.webdatex.extractors.node.NodeExtractorImpl;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import com.bc.webdatex.extractors.node.AttributesExtractor;
import com.bc.webdatex.functions.FindValueWithMatchingKey;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.logging.Logger;
import com.bc.webdatex.context.ExtractionContext;
import com.bc.webdatex.context.ExtractionConfig;

public class PageExtractorImpl extends NodeListExtractorImpl implements PageExtractor {

  private transient static final Logger LOG = Logger.getLogger(PageExtractorImpl.class.getName());

  private static final class HashMapNoNulls extends HashMap {
    public NodeExtractor put(String key, NodeExtractor value) {
      if ((key == null) || (value == null)) throw new NullPointerException();
      return (NodeExtractor)super.put(key, value);
    }
  }
  
  private final ExtractionContext context;
  private final float tolerance;
  private final boolean greedy;
  private final Map<Object, NodeExtractor> nodeExtractors;
  private final Map<Object, Object[]> columns;
  private final BiFunction<Map, String, String> findValueWithMatchingKey;

  private boolean withinTitleTag;
  private Tag titleTag;
  
  public PageExtractorImpl(ExtractionContext context){
      this(context, 0.0f, false);
  }
  
  public PageExtractorImpl(ExtractionContext context, float tolerance, boolean greedy){
    this.context = Objects.requireNonNull(context);
    this.findValueWithMatchingKey = new FindValueWithMatchingKey();
    this.tolerance = tolerance;
    this.greedy = greedy;
    
    final JsonConfig config = context.getConfig();
    
    this.nodeExtractors = new HashMapNoNulls();
    
    this.columns = new HashMapNoNulls();
    
    final List<Map> selectorCfgList = config.getList(ConfigName.selectorConfigList);
    
    final int limit = selectorCfgList.size();
    
    for (int i = 0; i < limit; i++) {
        
      addExtractor(i);
    }
  }
  
  @Override
  public void reset() {
      
    super.reset();
    
    this.withinTitleTag = false;
    this.titleTag = null;
    
    for(NodeExtractor extractor : this.nodeExtractors.values()) {
      extractor.reset();
    }
  }
  
  @Override
  public boolean isSuccessfulCompletion(Set<String> columns) {
    boolean output;
    if (columns != null) {
      output = getExtractedData().size() >= columns.size();
    } else {
      output = getFailedNodeExtractorIds().isEmpty();
    }
    return output;
  }
  
  @Override
  public void finishedParsing() {

    for (Object name : this.nodeExtractors.keySet()) {
        
      NodeExtractor extractor = (NodeExtractor)this.nodeExtractors.get(name);
      
      extractor.finishedParsing();
      
      Object[] cols = (Object[])this.columns.get(name);
      
      boolean append = extractor.isConcatenateMultipleExtracts();
      
      if (cols != null) {
          
        for (Object column : cols) {
            
          LOG.log(Level.FINEST, "Extractor: {0}", name);
          
          add(column.toString(), extractor.getExtract(), append, false);
        }
      }
    }
    LOG.finer(() -> MessageFormat.format("Extractors: {0}, Extracted data: {1}", 
            this.nodeExtractors.size(), getExtractedData().size()));
  }
  
  @Override
  public Set getFailedNodeExtractorIds() {
    final HashSet failed = new HashSet();
    final Set keys = getExtractedData().keySet();
    for (Object key : this.nodeExtractors.keySet()) {
      NodeExtractor extractor = (NodeExtractor)this.nodeExtractors.get(key);
      Object[] cols = (Object[])this.columns.get(key);
      if (cols != null) {
        for (Object col : cols) {
          if (!keys.contains(col)) {
            failed.add(extractor.getId());
            break;
          }
        }
      }
    }
    return failed;
  }
  
  @Override
  public Set getSuccessfulNodeExtractorIds() {
    Set failed = getFailedNodeExtractorIds();
    Set all = getNodeExtractorIds();
    all.removeAll(failed);
    return all;
  }
  
  @Override
  public Set getNodeExtractorIds() {
    return new HashSet(this.nodeExtractors.keySet());
  }
  
  @Override
  public void visitTag(Tag tag) {
      
    if(this.isStopRequested()) {
        return;
    }
      
    LOG.log(Level.FINER, "visitTag: {0}", tag);
    
    if (tag.getTagName().equalsIgnoreCase("TITLE")) {
      this.withinTitleTag = true;
      this.titleTag = tag;
    }
    
    LOG.log(Level.FINER, "Extracting with: {0}", this.nodeExtractors.keySet());

    for (Object key : this.nodeExtractors.keySet()) {
      NodeExtractor extractor = (NodeExtractor)this.nodeExtractors.get(key);
      extractor.visitTag(tag);
    }
  }
  
  @Override
  public void visitEndTag(Tag tag) {
      
//    if(this.isStopRequested()) {
//      return;
//    }
    
    LOG.log(Level.FINER, "visitEndTag: {0}", tag);
    
    if (tag.getTagName().equalsIgnoreCase("TITLE")) {
      this.withinTitleTag = false;
    }
    
    for (Object key : this.nodeExtractors.keySet()) {
      NodeExtractor extractor = (NodeExtractor)this.nodeExtractors.get(key);
      extractor.visitEndTag(tag);
    }
  }
  
  @Override
  public void visitStringNode(Text node) {
    
    if(this.isStopRequested()) {
      return;
    }
    
    LOG.log(Level.FINER, "visitStringNode: {0}", node);
    
    for (Object key : this.nodeExtractors.keySet()) {
      NodeExtractor extractor = (NodeExtractor)this.nodeExtractors.get(key);
      extractor.visitStringNode(node);
    }
  }
  
  @Override
  public void visitRemarkNode(Remark node) {
      
    if(this.isStopRequested()) {
      return;    
    }
    
    LOG.log(Level.FINER, "visitRemarkNode: {0}", node);
  }
  
  private void addExtractor(Object id) {
      
    ExtractionConfig cs = context.getExtractionConfig();
    
    Object[] cols = cs.getColumns(id);
    
    final Path<String> path = cs.getPathFlattened(id);
    
    if (cols == null || cols.length == 0) {
      LOG.finer(() -> MessageFormat.format("{0}.{1} == null", id, ConfigName.ids));
    }else if(path == null || path.isEmpty()) {  
      LOG.finer(() -> MessageFormat.format("{0}.{1} == null", id, ConfigName.transverse));  
    } else {
      NodeExtractor extractor = createNodeExtractor(id);
      if(extractor != null) {
        this.columns.put(id, cols);
        this.nodeExtractors.put(id, extractor);
      }
    }
    
    LOG.log(Level.FINER, "Added Extractor for property key: {0}", id);
  }
  
  @Override
  public NodeExtractor createNodeExtractor(Object id) {
      
    final ExtractionConfig config = context.getExtractionConfig();
    
    final Path<String> path = config.getPathFlattened(id);
    
    final NodeExtractorImpl extractor;
    
    if(path == null || path.isEmpty()) {
        extractor = null;
    }else{
        final AttributesExtractor ae = context.getAttributesExtractor(id);
        extractor = new NodeExtractorImpl(id, config, ae, tolerance, greedy);
    }
    
    return extractor;
  }

  protected String add(String key, Object val, boolean append, boolean guessColumnNameFromKey) {
      
    LOG.finer(() -> MessageFormat.format(
            "#add. Append: {0}, Key: {1}, Val: {2}", 
            append, key, val));
    
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
      
      if(LOG.isLoggable(Level.FINER)) {
        LOG.log(Level.FINER, "#add. Key: {0}, Matching col: {1}", new Object[]{key, col});
      }
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
      
      if(LOG.isLoggable(Level.FINE)) {
        LOG.log(Level.FINE, "#doAdd. Added: [{0}={1}]", new Object[]{col, val});
      }
      
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

        if(LOG.isLoggable(Level.FINE)) {
          LOG.log(Level.FINE, "#doAdd. Appended: [{0}={1}]", new Object[]{col, val});
        }
      }
    }
    
    return col;
  }

  @Override
  public NodeExtractor getNodeExtractor(Object id) {
      
    return (NodeExtractor)this.nodeExtractors.get(id);
  }
  
  @Override
  public boolean isWithinTitleTag() {
    return this.withinTitleTag;
  }
  
  @Override
  public Tag getTitleTag() {
    return this.titleTag;
  }

  @Override
  public ExtractionContext getCapturerContext() {
    return this.context;
  }
  
  @Override
  public ExtractionConfig getCapturerSettings() {
    return this.context.getExtractionConfig();
  }
  
  @Override
  public JsonConfig getCapturerConfig() {
    return this.context.getConfig();
  }
}
/**
 * 
  protected String getTitle(Text node) {
      
    String val = node.getText();
    
    String defaultTitle = getCapturerSettings().getDefaultTitle();
    
    if ((val == null) || (val.isEmpty())) { 
      return defaultTitle;
    }else
    if ((defaultTitle == null) || (defaultTitle.isEmpty())) { 
      return val;
    }
    return defaultTitle + " | " + val;
  }
  
 * 
 */