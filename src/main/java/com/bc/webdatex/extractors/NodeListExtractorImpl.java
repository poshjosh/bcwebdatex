package com.bc.webdatex.extractors;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.NodeVisitorImpl;

public class NodeListExtractorImpl extends NodeVisitorImpl 
        implements NodeListExtractor, Serializable {

  private transient static final Logger LOG = Logger.getLogger(NodeListExtractorImpl.class.getName());
    
  private boolean started;
  private boolean stopInitiated;
  private boolean stopped;
  private long startTime;
  private NodeList source;
  private Map extractedData;
  
  public NodeListExtractorImpl() {
    this(null);
  }

  public NodeListExtractorImpl(NodeList source) {
    this.source = source;
    this.extractedData = new HashMap();
  }
  
  public void reset()
  {
    this.started = false;
    this.stopInitiated = false;
    this.stopped = false;
    this.source = null;
    
    this.extractedData = new HashMap();
  }
  
  @Override
  public Map extractData(NodeList nodeList) throws ParserException
  {
    LOG.fine(() -> MessageFormat.format("{0} process: {1}", this.started ? "Resuming" : "Starting", this));

    this.started = true;
    this.startTime = System.currentTimeMillis();
    this.stopInitiated = false;
    this.stopped = false;
    
    try
    {
      reset();
      
      nodeList.visitAllNodesWith(this);
    }
    finally
    {
      this.stopped = true;
      
      LOG.fine(() -> MessageFormat.format("{0} process: {1}", 
              this.stopInitiated ? "Pausing" : "Completed", this));
    }
    
    return this.extractedData;
  }
  
  @Override
  public Map call() {
    this.run();
    return this.extractedData;
  }
  

  @Override
  public void run(){
    try{
      extractData(this.source);
    } catch (ParserException|RuntimeException e) {
      LOG.log(Level.WARNING, null, e);
    }
  }

  @Override
  public long getStartTime() {
    return this.startTime;
  }
  
  @Override
  public boolean isStopRequested(){
    return this.stopInitiated;
  }
  
  @Override
  public boolean isStopped(){
    return this.stopped;
  }
  
  @Override
  public void stop() {
    this.stopInitiated = true;
  }
  
  @Override
  public boolean isCompleted() {
    return (this.started) && (this.stopped) && (!this.stopInitiated);
  }
  
  @Override
  public boolean isStarted() {
    return this.started;
  }
  
  protected Map getExtractedData() {
    return this.extractedData;
  }
  
  @Override
  public String getTaskName() {
    return this.getClass().getName();
  }
  
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(getTaskName());
    builder.append(", started: ").append(this.started);
    builder.append(", stopInitiated: ").append(this.stopInitiated);
    builder.append(", stopped: ").append(this.stopped);
    return builder.toString();
  }
}
