package com.bc.webdatex.bounds;

import com.bc.util.XLogger;
import java.io.Serializable;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;


/**
 * @(#)BoundsMarker.java   24-Sep-2015 15:54:25
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */

/**
 * @author   chinomso bassey ikwuagwu
 * @version  2.0
 * @since    2.0
 */
public class BoundsMarker 
        implements HasBounds, Serializable {

    private boolean started;
    
    private boolean done;
    
    private String id;
    
    private NodeFilter startAtFilter;
    
    private NodeFilter stopAtFilter;
    
    public BoundsMarker() { }
    
    public BoundsMarker(String id, NodeFilter startAt, NodeFilter stopAt) {
        this.id = id;
        this.startAtFilter = startAt;
        this.stopAtFilter = stopAt;
    }
    
    @Override
    public void reset() {
        this.done = false;
        this.started = false;
    }
    
    public void visitStartTag(Node node) {
        
        if(this.isDone()) return;
        
        if(!this.isStarted()) {
            started = startAtFilter == null || startAtFilter.accept(node);
            if(started) {
XLogger.getInstance().log(Level.FINER, "Started at Node: {0}", 
        this.getClass(), node);                
            }
        }else{
            done = stopAtFilter != null && stopAtFilter.accept(node);
            if(done) {
XLogger.getInstance().log(Level.FINER, "Ended at Node: {0}", 
        this.getClass(), node);                
            }
        }
    }
    
    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public String getId() {
        return id;
    }

    public NodeFilter getStartAtFilter() {
        return startAtFilter;
    }

    public void setStartAtFilter(NodeFilter startAtFilter) {
        this.startAtFilter = startAtFilter;
    }

    public NodeFilter getStopAtFilter() {
        return stopAtFilter;
    }

    public void setStopAtFilter(NodeFilter stopAtFilter) {
        this.stopAtFilter = stopAtFilter;
    }
}

