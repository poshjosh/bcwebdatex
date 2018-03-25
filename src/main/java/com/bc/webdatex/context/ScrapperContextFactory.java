package com.bc.webdatex.context;

import com.bc.json.config.JsonConfig;
import com.bc.webdatex.config.FTPClientImpl;
import com.bc.webdatex.config.JsonConfigFactory;
import com.bc.webdatex.context.CapturerContext;
import com.bc.webdatex.context.CapturerContextImpl;
import com.ftpmanager.DefaultFTPClient;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScrapperContextFactory extends JsonConfigFactory {

  private transient static final Logger LOG = Logger.getLogger(ScrapperContextFactory.class.getName());

  public ScrapperContextFactory(URI configsDir, Properties ftpProps) {
    super(configsDir, "default", new FTPClientImpl(ftpProps));
  }
  
  public ScrapperContextFactory(URI configsDir, String defaultConfigName, DefaultFTPClient ftpClient) {
    super(configsDir, defaultConfigName, ftpClient);
  }

  public ScrapperContextFactory(URI configsDir, String defaultConfigName, DefaultFTPClient ftpClient, String searchNodeName, boolean useCache, boolean remote) {
    super(configsDir, defaultConfigName, ftpClient, searchNodeName, useCache, remote);
  }

  @Override
  public JsonConfigFactory newSyncFactory() {
    final JsonConfigFactory factory = new ScrapperContextFactory(
            this.getConfigDir(), this.getDefaultConfigName(), 
            this.getFtpClient(), this.getSearchNodeName(), 
            this.isUseCache(), !this.isRemote());
    return factory;
  }
  
  public CapturerContext getContext(String name) {
      
    return getContext(getConfig(name));
  }
  
  public CapturerContext getContext(JsonConfig config) {
      
    if (config == null) {
      throw new NullPointerException();
    }
    
    final String className = getClassName(config.getName());
    
    LOG.log(Level.FINER, "Class name: {0}", className);
    
    CapturerContext output = null;
    try
    {
      Class aClass = Class.forName(className);
      
      output = (CapturerContext)aClass.getConstructor(new Class[] { JsonConfig.class }).newInstance(new Object[] { config });
    }
    catch (ClassNotFoundException e)
    {
      output = newDefaultContext(config);
    }
    catch (NoSuchMethodException|SecurityException|InstantiationException|IllegalAccessException|IllegalArgumentException|InvocationTargetException e)
    {
      LOG.log(Level.WARNING, "Failed to create: " + className, e);
    }
    
    return output;
  }
  
  protected CapturerContext newDefaultContext(JsonConfig config) {
    return new CapturerContextImpl(config);
  }
  
  private String getClassName(String sitename){
    String packageName = CapturerContextImpl.class.getPackage().getName();
    StringBuilder builder = new StringBuilder(packageName);
    builder.append('.').append(toTitleCase(sitename)).append("Context");
    return builder.toString();
  }
  
  private Object toTitleCase(String arg0) {
    char ch0 = arg0.charAt(0);
    if (Character.isUpperCase(ch0)) return arg0;
    return Character.toTitleCase(ch0) + arg0.substring(1);
  }
}
