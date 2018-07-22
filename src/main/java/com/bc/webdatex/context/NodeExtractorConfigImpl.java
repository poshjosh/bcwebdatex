package com.bc.webdatex.context;

import com.bc.json.config.JsonConfig;
import com.bc.nodelocator.ConfigName;
import com.bc.nodelocator.impl.ListPath;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.bc.nodelocator.Path;

public class NodeExtractorConfigImpl implements Serializable, NodeExtractorConfig {

  private transient static final Logger LOG = Logger.getLogger(NodeExtractorConfigImpl.class.getName());
  
  private static final String [] EMPTY_STRING_ARRAY = new String[0];
    
  private final JsonConfig config;
  
  public NodeExtractorConfigImpl(JsonConfig config) {
    this.config = config;
  }
  
  public final JsonConfig getConfig() {
    return this.config;
  }

  @Override
  public Map getDefaults() {
    Map output = config.getMap(ConfigName.defaultValues);
    return output == null || output.isEmpty() ? Collections.EMPTY_MAP : Collections.unmodifiableMap(output);  
  }

  @Override
  public String[] getDatePatterns(){
    Object[] arr = this.config.getArray(ConfigName.datePatterns);
    return this.stringCopyOf(arr);
  }
  
  @Override
  public String[] getUrlDatePatterns(){
    Object[] arr = this.config.getArray(ConfigName.urlDatePatterns);
    return this.stringCopyOf(arr);
  }
  
  @Override
  public Path<String> getPathFlattened(Object id) {
    return this.getPath(id).flatten();
  }

  @Override
  public Path<String> getPath(Object id) {
    final String [] arr = this.getTransverseArray(id);
    final List<String> list = arr == null || arr.length == 0 ? Collections.EMPTY_LIST : Arrays.asList(arr);
    return new ListPath(list);
  }
  
  public String[] getTransverseArray(Object id) {
    return selectorConfigStringArray(id, ConfigName.transverse);
  }
  
  @Override
  public String[] getTextToReject(Object id) {
    return selectorConfigStringArray(id, ConfigName.textToReject);
  }
  
  @Override
  public boolean isConcatenateMultipleExtracts(Object id, boolean defaultValue) {
    Boolean b = this.selectorConfigBoolean(id, ConfigName.append, defaultValue);
    return b;
  }
  
  @Override
  public String getLineSeparator() {
    return config.getString(ConfigName.lineSeparator);
  }
  
  @Override
  public String getPartSeparator() {
    return config.getString(ConfigName.partSeparator);
  }
  
  @Override
  public String getDefaultTitle() {
    return (String)this.getDefaults().get(ConfigName.title);
  }
  
  @Override
  public String[] getColumns(Object id) {
    return selectorConfigStringArray(id, ConfigName.ids);
  }
  
  @Override
  public boolean isReplaceNonBreakingSpace(Object id, boolean defaultValue) {
    return this.selectorConfigBoolean(id, ConfigName.replaceNonBreakingSpace, defaultValue);
  }
  
  @Override
  public String[] getAttributesToExtract(Object id) {
    final String[] arr = selectorConfigStringArray(id, ConfigName.attributesToExtract);
    LOG.log(Level.FINER, "Attributes to extract: {0}", arr == null ? null : Arrays.toString(arr));
    return arr;
  }

    @Override
    public String getImageUrlUnwantedRegex() {
        return this.config.getString(ConfigName.imageUrl_unwantedRegex);
    }

    @Override
    public String getImageUrlRequiredRegex() {
        return this.config.getString(ConfigName.imageUrl_requiredRegex);
    }
  
  @Override
  public String[] getNodeTypesToAccept(Object id) {
    return toLowercaseStringArray(selectorConfigArray(id, ConfigName.nodeTypesToAccept));
  }
  
  @Override
  public String[] getNodeTypesToReject(Object id)  {
    return toLowercaseStringArray(selectorConfigArray(id, ConfigName.nodeTypesToReject));
  }
  
  @Override
  public String[] getNodesToAccept(Object id) {
    return toLowercaseStringArray(selectorConfigArray(id, ConfigName.nodesToAccept));
  }
  
  @Override
  public String[] getNodeToReject(Object id) { 
      return toLowercaseStringArray(selectorConfigArray(id, ConfigName.nodesToReject)); 
  }
  
  private String[] toLowercaseStringArray(Object[] arr) {
    String[] output;
    if (arr == null || arr.length == 0) {
      output = EMPTY_STRING_ARRAY;
    } else {
      output = new String[arr.length];
      for (int i = 0; i < arr.length; i++) {
        output[i] = arr[i].toString().toLowerCase();
      }
    }
    return output;
  }

  
  private String[] stringCopyOf(Object... src)  {
    String[] output;
    if (src != null && src.length != 0) {
      output = new String[src.length];
      System.arraycopy(src, 0, output, 0, src.length);
    } else {
      output = EMPTY_STRING_ARRAY;
    }
    
    return output;
  }

  private Object[] selectorConfigArray(Object listItemPos, Object second) {
    List<String> list = config.getList(ConfigName.selectorConfigList, listItemPos, second);
    if(list == null) {
      list = (List)config.getObject(second);
    }
    return list == null || list.isEmpty() ? new Object[0] : list.toArray();
  }
  
  private String[] selectorConfigStringArray(Object listItemPos, Object second) {
    List<String> list = config.getList(ConfigName.selectorConfigList, listItemPos, second);
    if(list == null) {
      list = config.getList(second);
    }
    return list == null || list.isEmpty() ? EMPTY_STRING_ARRAY : list.toArray(new String[0]);
  }
  
  private boolean selectorConfigBoolean(Object listItemPos, Object second, boolean defaultValue) {
    Object val = config.getBoolean(ConfigName.selectorConfigList, listItemPos, second);
    if(val == null) {
      val = config.getObject(second);
    }
    return val == null ? defaultValue : val instanceof Boolean ? (Boolean)val : Boolean.valueOf(val.toString().trim());
  } 
}
/**
 * 

  private Object[] getSelectorConfigArray(Object first, Object second) {
    final Map selectorConfig = this.getSelectorConfig(first);
    List list = (List)selectorConfig.get(second);
    if(list == null) {
      list = (List)config.getObject(second);
    }
    return list == null || list.isEmpty() ? new Object[0] : list.toArray();
  }
  private String[] getSelectorConfigStringArray(Object first, Object second) {
    final Map selectorConfig = this.getSelectorConfig(first);
    List<?> list = (List<?>)selectorConfig.get(second);
    if(list == null) {
      list = (List<?>)config.getObject(second);
    }
    return list == null || list.isEmpty() ? EMPTY_STRING_ARRAY : 
            list.stream().map(Object::toString).collect(Collectors.toList()).toArray(new String[0]);
  }
  private boolean getSelectorConfigBoolean(Object first, Object second, boolean defaultValue) {
    final Map selectorConfig = this.getSelectorConfig(first);
    Object val = selectorConfig.get(second);
    if(val == null) {
      val = config.getObject(second);
    }
    return val == null ? defaultValue : val instanceof Boolean ? (Boolean)val : Boolean.valueOf(val.toString().trim());
  } 
  private Map getSelectorConfig(Object id) {
      final Integer ival = id instanceof Integer ? (Integer)id : Integer.parseInt(id.toString());
      return (Map)config.getList(ConfigName.selectorConfigList).get(ival);
  }

  private String[] getStringArrayOld(String first, Object second) {
      
    Object[] arr = config.getArray(new Object[] { first, second });
    
    if (arr == null) {
      arr = config.getArray(new Object[] { second });
    }
    
    return this.stringCopyOf(arr);
  }

  private boolean getBooleanOld(String first, Object second, boolean defaultValue) {
    Boolean bool = config.getBoolean(new Object[] { first, second });
    if (bool == null) {
      bool = config.getBoolean(new Object[] { second });
    }
    return bool==null?defaultValue:bool;
  } 
  
 * 
 */