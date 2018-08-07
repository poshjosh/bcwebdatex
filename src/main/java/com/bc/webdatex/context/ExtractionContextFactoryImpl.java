/*
 * Copyright 2018 NUROX Ltd.
 *
 * Licensed under the NUROX Ltd Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.looseboxes.com/legal/licenses/software.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bc.webdatex.context;

import com.bc.json.config.JsonConfig;
import com.bc.json.config.JsonConfigFromDirService;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.parser.ParseException;
import com.bc.json.config.JsonConfigService;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 16, 2018 3:12:46 PM
 */
public class ExtractionContextFactoryImpl implements ExtractionContextFactory {

  private transient static final Logger LOG = Logger.getLogger(ExtractionContextFactoryImpl.class.getName());
  
  private final JsonConfigService configService;

  public ExtractionContextFactoryImpl(File configsDir) {
    this.configService = new JsonConfigFromDirService(configsDir);
  }

  public ExtractionContextFactoryImpl(File configsDir, String defaultConfigName) {
    this.configService = new JsonConfigFromDirService(configsDir, defaultConfigName);
  }

  @Override
  public ExtractionContext getContext(String name) {
    try{
      return getContext(configService.getConfig(name, configService.createConfig(name)));
    }catch(IOException | ParseException e) {
      throw new RuntimeException(e);  
    }
  }
  
  @Override
  public ExtractionContext getContext(JsonConfig config) {
      
    Objects.requireNonNull(config);
    
    final String className = getClassName(config.getName());
    
    LOG.log(Level.FINER, "Class name: {0}", className);
    
    ExtractionContext output = null;
    try
    {
      Class aClass = Class.forName(className);
      
      output = (ExtractionContext)aClass.getConstructor(new Class[] { JsonConfig.class }).newInstance(new Object[] { config });
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
  
  protected ExtractionContext newDefaultContext(JsonConfig config) {
    return new ExtractionContextImpl(config);
  }
  
  private String getClassName(String sitename){
    String packageName = ExtractionContextImpl.class.getPackage().getName();
    StringBuilder builder = new StringBuilder(packageName);
    builder.append('.').append(toTitleCase(sitename)).append("Context");
    return builder.toString();
  }
  
  private Object toTitleCase(String arg0) {
    char ch0 = arg0.charAt(0);
    if (Character.isUpperCase(ch0)) return arg0;
    return Character.toTitleCase(ch0) + arg0.substring(1);
  }

  @Override
  public JsonConfigService getConfigService() {
    return configService;
  }
}
