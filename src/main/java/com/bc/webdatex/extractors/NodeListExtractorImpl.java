package com.bc.webdatex.extractors;

import com.bc.util.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.AbstractNodeVisitor;

public class NodeListExtractorImpl extends AbstractNodeVisitor implements NodeListExtractor {
    
  private boolean started;
  private boolean stopInitiated;
  private boolean stopped;
  private long startTime;
  private NodeList source;
  private Map extractedData;
  
  public NodeListExtractorImpl() {
    this.extractedData = new HashMap();
  }
  
  @Override
  public void reset()
  {
    this.started = false;
    this.stopInitiated = false;
    this.stopped = false;
    this.source = null;
    

    this.extractedData = new HashMap();
  }
  
  public Map extractData(NodeList nodeList) throws ParserException
  {
    Log.getInstance().log(Level.FINE, "{0} process: {1}", getClass(), this.started ? "Resuming" : "Starting", this);
    


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
      
      Log.getInstance().log(Level.FINE, "{0} process: {1}", getClass(), this.stopInitiated ? "Pausing" : "Completed", this);
    }
    


    return this.extractedData;
  }
  
  @Override
  public Object call() {
    this.run();
    return this.extractedData;
  }
  

  @Override
  public void run()
  {
    try
    {
      extractData(this.source);
    } catch (ParserException|RuntimeException e) {
      Log.getInstance().log(Level.WARNING, null, getClass(), e);
    }
  }

  @Override
  public long getStartTime() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  
  public boolean isStopRequested()
  {
    return this.stopInitiated;
  }
  
  public boolean isStopped()
  {
    return this.stopped;
  }
  
  public void stop()
  {
    this.stopInitiated = true;
  }
  
  public boolean isCompleted()
  {
    return (this.started) && (this.stopped) && (!this.stopInitiated);
  }
  
  public boolean isStarted()
  {
    return this.started;
  }
  


  public void visitEndTag(Tag tag)
  {
    if (this.stopInitiated) {
      return;
    }
    super.visitEndTag(tag);
  }
  
  public void visitRemarkNode(Remark remark)
  {
    if (this.stopInitiated) {
      return;
    }
    super.visitRemarkNode(remark);
  }
  
  public void visitStringNode(Text string)
  {
    if (this.stopInitiated) {
      return;
    }
    super.visitStringNode(string);
  }
  
  public void visitTag(Tag tag)
  {
    if (this.stopInitiated) {
      return;
    }
    super.visitTag(tag);
  }
  
  protected Map getExtractedData() {
    return this.extractedData;
  }
  
  public NodeList getSource()
  {
    return this.source;
  }
  
  public void setSource(NodeList source)
  {
    this.source = source;
  }
  
  public String getTaskName()
  {
    return NodeListExtractorImpl.class.getName();
  }
  
  public String toString()
  {
    StringBuilder builder = new StringBuilder(getTaskName());
    builder.append(", started: ").append(this.started);
    builder.append(", stopInitiated: ").append(this.stopInitiated);
    builder.append(", stopped: ").append(this.stopped);
    return builder.toString();
  }
}
