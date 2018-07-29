package com.bc.webdatex.context;

import com.bc.json.config.JsonConfig;
import com.bc.nodelocator.ConfigName;
import com.bc.webdatex.extractors.node.AttributesExtractor;
import com.bc.webdatex.extractors.node.AttributesExtractorImpl;
import com.bc.webdatex.filters.Filter;
import com.bc.webdatex.extractors.DataExtractor;
import com.bc.webdatex.extractors.PageExtractor;
import com.bc.webdatex.extractors.PageExtractorImpl;
import com.bc.webdatex.filters.CaptureUrlFilter;
import com.bc.webdatex.filters.ScrappUrlFilter;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.logging.Level;
import org.htmlparser.NodeFilter;
import java.util.logging.Logger;

public class CapturerContextImpl implements ExtractionContext, Serializable
{

  private transient static final Logger LOG = Logger.getLogger(CapturerContextImpl.class.getName());
  
  private JsonConfig config;
  private ExtractionConfig _settings;
  
  public CapturerContextImpl() {
    this(null);
  }
  
  public CapturerContextImpl(JsonConfig config) {
    this.config = config;
  }
  
  @Override
  public ExtractionConfig getExtractionConfig() {
    if (this._settings == null) {
      this._settings = new NodeExtractorConfigImpl(getConfig());
    }
    return this._settings;
  }
  
  @Override
  public AttributesExtractor getAttributesExtractor(Object id)
  {
    return getAttributesExtractor(getConfig(), id);
  }
  
  public AttributesExtractor getAttributesExtractor(JsonConfig config, Object id)
  {
    String[] toExtract = getArray(getConfig(), id, ConfigName.attributesToExtract);
    
    if (toExtract == null) {
      return (tag, attrs) -> new String[0];
    }
    
    AttributesExtractor attributesExtractor = (AttributesExtractor)loadInstance(config, "attributesExtractor");
    

    if (attributesExtractor == null)
    {
      attributesExtractor = new AttributesExtractorImpl(id);
    }
    
    return attributesExtractor;
  }
  
  @Override
  public JsonConfig getConfig()
  {
    return this.config;
  }
  
  @Override
  public DataExtractor<String> getUrlDataExtractor() {
    return getUrlDataExtractor(getConfig());
  }
  
  public DataExtractor<String> getUrlDataExtractor(JsonConfig config) {
    DataExtractor<String> urlDataExtractor = (DataExtractor)loadInstance(config, "urlDataExtractor");
    return urlDataExtractor;
  }
  
  @Override
  public Filter<String> getCaptureUrlFilter()
  {
    return getCaptureUrlFilter(getConfig());
  }
  
  public Filter<String> getCaptureUrlFilter(JsonConfig config) {
    Filter<String> captureUrlFilter = (Filter)loadInstance(config, "captureUrlFilter");
    
    if (captureUrlFilter == null)
    {

      captureUrlFilter = new CaptureUrlFilter(getConfig());
    }
    return captureUrlFilter;
  }
  
  @Override
  public PageExtractor getExtractor() {
    return getExtractor(0.0f, false);
  }
  
  @Override
  public PageExtractor getExtractor(float tolerance, boolean greedy) {
    return this.getExtractor(this, tolerance, greedy);
  }
  
  public PageExtractor getExtractor(ExtractionContext context, float tolerance, boolean greedy) {
      
    PageExtractor extractor = (PageExtractor)loadInstance(context.getConfig(), "extractor");
    
    if (extractor == null)
    {
      extractor = new PageExtractorImpl(context, tolerance, greedy);
    }
    return extractor;
  }
  
  @Override
  public NodeFilter getFilter()
  {
    return getFilter(getConfig());
  }
  


  public NodeFilter getFilter(JsonConfig config){
    NodeFilter filter = (NodeFilter)loadInstance(getConfig(), "filter");
    return filter;
  }
  
  @Override
  public UnaryOperator<Map<String, Object>> getFormatter(){
    return getFormatter(this);
  }
  
  public UnaryOperator<Map<String, Object>> getFormatter(ExtractionContext context) {
    final UnaryOperator<Map<String, Object>> formatter = (UnaryOperator)loadInstance(context.getConfig(), "formatter");
    return formatter;
  }
  
  @Override
  public Filter<String> getScrappUrlFilter()
  {
    return getScrappUrlFilter(getConfig());
  }
  
  public Filter<String> getScrappUrlFilter(JsonConfig config) {
    Filter<String> scrappUrlFilter = (Filter)loadInstance(config, "scrappUrlFilter");
    
    if (scrappUrlFilter == null)
    {

      scrappUrlFilter = new ScrappUrlFilter(getConfig());
    }
    return scrappUrlFilter;
  }
  
  @Override
  public UnaryOperator<String> getUrlFormatter()
  {
    return getUrlFormatter(getConfig());
  }
  
  public UnaryOperator<String> getUrlFormatter(JsonConfig config) {
    UnaryOperator<String> urlFormatter = (UnaryOperator)loadInstance(config, "urlFormatter");
    if(urlFormatter == null) {
        urlFormatter = (url) -> url;
    }

    return urlFormatter;
  }
  
  private Object loadInstance(JsonConfig config, String propertyName)
  {
    if ((config == null) || (propertyName == null)) {
      throw new NullPointerException();
    }
    
    String className = config.getString(new Object[] { propertyName });
    
    if (className == null) {
      return null;
    }
    
    Object output = newInstance(className, null, null);
    if (output == null) {
      output = newInstance(className, JsonConfig.class, config);
      if (output == null) {
        output = newInstance(className, getClass(), this);
      }
    }
    return output;
  }
  
  private Object newInstance(String className, Class<?> parameterType, Object initarg)
  {
    if (className == null) {
      return null;
    }
    
    Object output = null;
    try {
      if (parameterType == null) {
        output = Class.forName(className).getConstructor(new Class[0]).newInstance(new Object[0]);
      } else {
        output = Class.forName(className).getConstructor(new Class[] { parameterType }).newInstance(new Object[] { initarg });
      }
    } catch (ClassNotFoundException|InstantiationException|IllegalAccessException|NoSuchMethodException|InvocationTargetException e) {
      LOG.log(Level.WARNING, "{0}", e.toString());
    }
    return output;
  }
  


  private String[] getArray(JsonConfig config, Object prefix, Object key)
  {
    List val = config.getList(new Object[] { prefix, key });
    
    if (val == null)
    {

      val = config.getList(new Object[] { key });
    }
    
    return val == null ? null : (String[])val.toArray(new String[0]);
  }
}
