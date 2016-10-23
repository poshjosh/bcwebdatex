package com.bc.webdatex.visitor;

import com.bc.util.XLogger;
import com.bc.webdatex.bounds.HasBounds;
import java.io.Serializable;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.util.NodeList;
import com.bc.webdatex.locator.TagLocator;


/**
 * @(#)DefaultVisitingFilterProcess.java   24-Sep-2015 15:48:46
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
public class DefaultVisitingFilter implements HasBounds, Serializable {

    private boolean transverseEnabled;
    
    /**
     * If true, the extraction of the current page should not be continued
     */
    private boolean disabled;
    
    /**
     * Once started, this variable indicates the index
     */
    private int index;
    
    private String id;
    
    private NodeList transverseEndTags;
    
    private NodeList expectedEndTags;
    
    private NodeFilter tagFilter;
    
    private NodeFilter unwantedTextFilter;
    
    private NodeFilter disablingTextFilter;
    
    private TagLocator transverseVisitor;
    
    private BoundsVisitor boundsVisitor;

    public DefaultVisitingFilter( ) { 
        this.init(null, null, null);
    }
    
    public DefaultVisitingFilter(String id, BoundsVisitor v, NodeFilter target) {
        this.init(id, v, target);
    }
    
    public DefaultVisitingFilter(String id, NodeFilter parent, 
    NodeFilter startAt, NodeFilter stopAt, NodeFilter target) {
        
        this.id = id;
        
        BoundsVisitor visitor = new BoundsVisitor(id, parent, startAt, stopAt);
        
        visitor.setStrict(true);
        
        this.init(id, visitor, target);
    }
    
    private void init(String id, BoundsVisitor visitor, NodeFilter target) {

        this.setTransverseEnabled(true);
        
        this.id = id;
        
        this.boundsVisitor = visitor;
        
        this.tagFilter = target;
        
        this.expectedEndTags = new NodeList();
        
        this.transverseEndTags = new NodeList();
    }
    
    @Override
    public void reset() {
        this.disabled = false;
        this.index = 0;
        this.transverseEndTags.removeAll();
        this.expectedEndTags.removeAll();
        if(this.transverseVisitor != null) {
            this.transverseVisitor.reset();
        }
        if(this.boundsVisitor != null) {
            this.boundsVisitor.reset();
        }
    }
    
    public boolean acceptTag(Tag tag) {
XLogger.getInstance().log(Level.FINER, "{0}-Filtering, Tag: {1}", 
this.getClass(), id, tag.toTagHtml());
        
        // Only start tags are counted
        index++;
        
        if(this.disabled) {
            return false;
        }

        if(this.boundsVisitor != null) {
            boundsVisitor.visitTag(tag);
        }    
        
        boolean transverseProceed;
        
        boolean foundTarget = true;
        
        if(this.isTransverseEnabled() && this.transverseVisitor != null) {
            
            this.transverseVisitor.visitTag(tag);
            
            transverseProceed = this.transverseVisitor.isProceed();
            
            foundTarget = transverseProceed && this.transverseVisitor.isFoundTarget();
            
            if(foundTarget) {

                if(tag.getEndTag() != null) {
                    this.transverseEndTags.add(tag.getEndTag());
                }
            }
        }
        
        boolean filterAccept;
        
        boolean accept;
        
        if(foundTarget && this.isWithin(tag)) {
            
            filterAccept = (tagFilter == null || tagFilter.accept(tag));
            
            accept = filterAccept;

            if(accept) {

                if(tag.getEndTag() != null) {
                    expectedEndTags.add(tag.getEndTag());
                }
            }
        }else{
            
            accept = false;
        }
        
XLogger.getInstance().log(Level.FINER, "{0}-NodeVisitingFilter. accepted: {1}, index: {2}, Tag: {3}", 
        this.getClass(), id, accept, index, tag.toTagHtml());        
        
        return accept;
    }
    
    public boolean acceptEndTag(Tag tag) {
        
        if(this.disabled) {
            return false;
        }
        
        if(this.boundsVisitor != null) {
            boundsVisitor.visitEndTag(tag);
        }    
        
        this.transverseEndTags.remove(tag);
        
        return expectedEndTags.remove(tag);
    }
    
    public boolean acceptStringNode(Text node) {

        if(this.disabled) {
            return false;
        }
        
        if(this.boundsVisitor != null) {
            boundsVisitor.visitStringNode(node);
        }   
        
        if(!this.disabled) {
            
            this.disabled =
                    (this.disablingTextFilter != null && this.disablingTextFilter.accept(node));
        }
        
        boolean accept;
        
        if(this.isWithin(node)) {

            accept = unwantedTextFilter == null || unwantedTextFilter.accept(node);
  
            if(accept) {

                boolean a = this.expectedEndTags.size() > 0;
                
                boolean b = this.transverseEndTags.size() > 0;
                
                if(this.isTransverseEnabled()) {
                    accept = a || b;
                }else{
                    accept = a;
                }
            }

        }else{
            
            accept = false;
            
//XLogger.getInstance().log(Level.INFO, "{0}-Filter, NOT WITHIN, node: {1}", 
//this.getClass(), this.id, node);                
            
        }

XLogger.getInstance().log(Level.FINER, "{0}-NodeVisitingFilter@acceptStringNode. accepted: {1}, Text: {2}", 
        this.getClass(), id, accept, node);                
        
        return accept;
    }
    
    public boolean acceptRemarkNode(Remark node) {
        
        if(this.disabled) {
            return false;
        }
        
        if(this.boundsVisitor != null) {
            boundsVisitor.visitRemarkNode(node);
        }    
        
        // We don't accept remarks
        return false;
    }

    private boolean isWithin(Node node) {
    
        boolean isWithin = boundsVisitor == null || (boundsVisitor.isStarted() && !boundsVisitor.isDone());
        
XLogger.getInstance().log(Level.FINER, "Is within: {0}, is started: {1}, is done: {2}, Node: {3}", 
        this.getClass(), 
        isWithin, 
        boundsVisitor==null?"null":boundsVisitor.isStarted(), 
        boundsVisitor==null?"null":boundsVisitor.isDone(), 
        node instanceof Tag ? ((Tag)node).toTagHtml() : "[DO NOT PRINT NODES]" );

        return isWithin;
    }
    
    @Override
    public boolean isStarted() {
        return boundsVisitor.isStarted();
    }

    @Override
    public boolean isDone() {
        return boundsVisitor.isDone();
    }
    
    public int getVisitedStartTags() {
        return index;
    }
    
    public String getId() {
        return id;
    }

    public BoundsVisitor getBoundsVisitor() {
        return boundsVisitor;
    }

    public void setBoundsVisitor(BoundsVisitor boundsVisitor) {
        this.boundsVisitor = boundsVisitor;
    }

    public NodeFilter getTagFilter() {
        return tagFilter;
    }

    public void setTagFilter(NodeFilter tagFilter) {
        this.tagFilter = tagFilter;
    }

    public NodeFilter getUnwantedTextFilter() {
        return unwantedTextFilter;
    }

    public void setUnwantedTextFilter(NodeFilter unwantedTextFilter) {
        this.unwantedTextFilter = unwantedTextFilter;
    }

    public boolean isTransverseEnabled() {
        return transverseEnabled;
    }

    public void setTransverseEnabled(boolean transverseEnabled) {
        this.transverseEnabled = transverseEnabled;
    }

    public TagLocator getTransverseVisitor() {
        return transverseVisitor;
    }

    public void setTransverseVisitor(TagLocator visitor) {
        this.transverseVisitor = visitor;
    }

    public NodeFilter getDisablingTextFilter() {
        return disablingTextFilter;
    }

    public void setDisablingTextFilter(NodeFilter disablingTextFilter) {
        this.disablingTextFilter = disablingTextFilter;
    }
}    

