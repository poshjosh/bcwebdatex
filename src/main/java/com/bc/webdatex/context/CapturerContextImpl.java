package com.bc.webdatex.context;

import com.bc.webdatex.extractors.node.NodeExtractorConfig;
import com.bc.json.config.JsonConfig;
import com.bc.util.Log;
import com.bc.webdatex.extractors.node.AttributesExtractor;
import com.bc.webdatex.extractors.node.AttributesExtractorImpl;
import com.bc.webdatex.filters.Filter;
import com.bc.webdatex.formatters.Formatter;
import com.bc.webdatex.config.Config;
import com.bc.webdatex.extractors.DataExtractor;
import com.bc.webdatex.extractors.MappingsExtractor;
import com.bc.webdatex.extractors.MultipleNodesExtractorImpl;
import com.bc.webdatex.filters.CaptureUrlFilter;
import com.bc.webdatex.filters.DefaultBoundsFilter;
import com.bc.webdatex.filters.ScrappUrlFilter;
import com.bc.webdatex.formatters.DefaultFormatter;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.htmlparser.NodeFilter;
import com.bc.webdatex.extractors.MultipleNodesExtractor;

public class CapturerContextImpl implements CapturerContext, Serializable
{
  private JsonConfig config;
  private NodeExtractorConfig _settings;
  
  public CapturerContextImpl()
  {
    this(null);
  }
  
  public CapturerContextImpl(JsonConfig config) {
    this.config = config;
  }
  
  @Override
  public NodeExtractorConfig getNodeExtractorConfig()
  {
    if (this._settings == null) {
      this._settings = new NodeExtractorConfigImpl(getConfig());
    }
    return this._settings;
  }
  
  @Override
  public AttributesExtractor getAttributesExtractor(String propertyKey)
  {
    return getAttributesExtractor(getConfig(), propertyKey);
  }
  
  public AttributesExtractor getAttributesExtractor(JsonConfig config, String propertyKey)
  {
    String[] toExtract = getArray(getConfig(), propertyKey, Config.Extractor.attributesToExtract);
    
    if (toExtract == null) {
      return null;
    }
    
    AttributesExtractor attributesExtractor = (AttributesExtractor)loadInstance(config, "attributesExtractor");
    

    if (attributesExtractor == null)
    {
      attributesExtractor = new AttributesExtractorImpl(propertyKey);
    }
    
    return attributesExtractor;
  }
  
  @Override
  public JsonConfig getConfig()
  {
    return this.config;
  }
  
  @Override
  public DataExtractor<String> getUrlDataExtractor()
  {
    return getUrlDataExtractor(getConfig());
  }
  
  public DataExtractor<String> getUrlDataExtractor(JsonConfig config)
  {
    DataExtractor<String> urlDataExtractor = (DataExtractor)loadInstance(config, "urlDataExtractor");
    

    if (urlDataExtractor == null)
    {

      urlDataExtractor = MappingsExtractor.getInstance(MappingsExtractor.Type.url.name(), config);
    }
    
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
  public MultipleNodesExtractor getExtractor()
  {
    return getExtractor(getConfig());
  }
  
  public MultipleNodesExtractor getExtractor(JsonConfig config) {
      
    MultipleNodesExtractor extractor = (MultipleNodesExtractor)loadInstance(config, "extractor");
    
    if (extractor == null)
    {
      extractor = new MultipleNodesExtractorImpl(this);
    }
    return extractor;
  }
  
  @Override
  public NodeFilter getFilter()
  {
    return getFilter(getConfig());
  }
  


  public NodeFilter getFilter(JsonConfig config)
  {
    String key = config.getString(new Object[] { "parentNode", "value" });
    
    if (key == null) {
      return null;
    }
    
    NodeFilter filter = (NodeFilter)loadInstance(getConfig(), "filter");
    
    if (filter == null)
    {
      filter = new DefaultBoundsFilter(getConfig());
    }
    return filter;
  }
  
  @Override
  public Formatter<Map<String, Object>> getFormatter()
  {
    return getFormatter(this);
  }
  
  public Formatter<Map<String, Object>> getFormatter(CapturerContext context) {
    Formatter<Map<String, Object>> formatter = (Formatter)loadInstance(context.getConfig(), "formatter");
    

    if (formatter == null)
    {
      formatter = new DefaultFormatter(context);
    }
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
  public Formatter<String> getUrlFormatter()
  {
    return getUrlFormatter(getConfig());
  }
  
  public Formatter<String> getUrlFormatter(JsonConfig config) {
    Formatter<String> urlFormatter = (Formatter)loadInstance(config, "urlFormatter");
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
      Log.getInstance().log(Level.WARNING, "{0}", getClass(), e.toString());
    }
    return output;
  }
  


  private String[] getArray(JsonConfig config, String prefix, Object key)
  {
    List val = config.getList(new Object[] { prefix, key });
    
    if (val == null)
    {

      val = config.getList(new Object[] { key });
    }
    
    return val == null ? null : (String[])val.toArray(new String[0]);
  }
}
