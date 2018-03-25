package com.bc.webdatex.nodefilters;

import com.bc.webdatex.bounds.BoundsMarker;
import com.bc.webdatex.visitors.BoundsVisitor;
import com.bc.util.StringArrayUtils;
import com.bc.util.Log;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import com.bc.webdatex.locator.TagLocator;


/**
 * @(#)NodeVisitingFilter.java   29-Sep-2015 13:57:11
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
public class NodeVisitingFilterImpl 
        implements NodeVisitingFilter, Serializable {

    private boolean started;
    
    /**
     * If true, the extraction of the current page should not be continued
     */
    private boolean done;
    
    /**
     * Once started, this variable indicates the index
     */
    private int visitedStartTags;
    
    private String id;
    
    private String [] textToDisableOn;
    
    private List<Tag> transverseEndTags;
    
    private List<Tag> expectedEndTags;
    
    private TextFilter textFilter;
    
    private BoundsVisitor boundsVisitor;
    
    private NodesFilter nodesFilter;
    
    private NodeFilter tagFilter;
    
    private TagLocator tagLocator;
    
    public NodeVisitingFilterImpl( ) { 
        this.init(null, null, null);
    }
    
    public NodeVisitingFilterImpl(String id, BoundsVisitor v, NodeFilter target) {
        this.init(id, v, target);
    }
    
    public NodeVisitingFilterImpl(String id, NodeFilter parent, 
    NodeFilter startAt, NodeFilter stopAt, NodeFilter target) {

        this.id = id;
        
        BoundsVisitor visitor = new BoundsVisitor(id, parent, startAt, stopAt);
        
        visitor.setStrict(true);
        
        this.init(id, visitor, target);
    }
    
    private void init(String id, BoundsVisitor visitor, NodeFilter target) {

        this.id = id;
        
        this.boundsVisitor = visitor;
        
        this.tagFilter = target;
    }
    
    @Override
    public void reset() {
        this.started = false;
        this.done = false;
        this.visitedStartTags = 0; 
        if(this.transverseEndTags != null) this.transverseEndTags.clear();
        this.transverseEndTags = null;
        if(this.expectedEndTags != null) this.expectedEndTags.clear();
        this.expectedEndTags = null;
        if(this.tagLocator != null) {
            this.tagLocator.reset();
        }
        if(this.boundsVisitor != null) {
            this.boundsVisitor.reset();
        }
    }
    
    @Override
    public boolean accept(Node node) {
        boolean output;
        if(node instanceof Tag) {
            Tag tag = (Tag)node;
            if(!tag.isEndTag()) {
                output = this.acceptTag(tag);
            }else {
                output = this.acceptEndTag(tag);
            }
        }else if(node instanceof Text) {
            output = this.acceptStringNode((Text)node);
        }else if(node instanceof Remark) {
            output = this.acceptRemarkNode((Remark)node);
        }else {
            // Unknown node, may be Tag type was not registered
            output = false;
        }
        return output;
    }
    
    @Override
    public boolean acceptTag(Tag tag) {
        
//final boolean LOG = "targetNode5".equals(id);
        
final Level level = started && !done ? Level.FINE : Level.FINER;  

Log.getInstance().log(level, "{0}-{1}, Tag: {2}", 
this.getClass(), id, this.getClass().getSimpleName(), tag.toTagHtml());

        // Only start tags are counted
        visitedStartTags++;
        
        if(this.done) {
            return false;
        }
//if(LOG) System.out.println(this.getClass().getName()+". Tag: "+tag.toTagHtml());

        if(this.boundsVisitor != null) {
            boundsVisitor.visitTag(tag);
        }    
        
        boolean foundTarget;
        
        if(this.tagLocator != null) {
            
            this.tagLocator.visitTag(tag);

            foundTarget = this.tagLocator.isFoundTarget();
            
        }else{
            
            foundTarget = false;
        }

        if(foundTarget || this.isWithinTarget()) {
            
            this.addTransverseEndTag(tag);
        }
        
        boolean accept;
        
        boolean withinBounds = this.isWithinBounds(); 
        
Log.getInstance().log(level, "Locator accept: {0}, found target: {1}, within bounds: {2}, within target: {3}", 
this.getClass(), (this.tagLocator==null?null:this.tagLocator.isLastTagAccepted()), 
foundTarget, withinBounds, this.isWithinTarget());
        
        if(withinBounds && (foundTarget || this.isWithinTarget())) {

if(!started) {            
    Log.getInstance().log(Level.FINER, "{0}-{1} STARTED ", this.getClass(), id, this.getClass().getSimpleName());
}            
            started = true;
            
            accept = (tagFilter == null || tagFilter.accept(tag));

            if(accept) {
                
                this.addExpectedEndTag(tag);
            }
        }else{
            
            if(started) {
                done = true;
Log.getInstance().log(Level.FINER, "{0}-{1} DONE ", this.getClass(), id, this.getClass().getSimpleName());
            }
            
            accept = false;
        }
        
Log.getInstance().log(Level.FINER, "{0}-NodeVisitingFilter. accepted: {1}, index: {2}, Tag: {3}", 
        this.getClass(), id, accept, visitedStartTags, tag.toTagHtml()); 

//if(LOG && accept) System.out.println(this.getClass().getName()+". Accepted: "+accept+", "+tag.toTagHtml());

        return accept;
    }
    
    @Override
    public boolean acceptEndTag(Tag tag) {
        
        if(this.done) {
            return false;
        }
        
        if(this.boundsVisitor != null) {
            boundsVisitor.visitEndTag(tag);
        }    

        boolean a = this.expectedEndTags != null && this.expectedEndTags.remove(tag);

        boolean b = this.transverseEndTags != null && this.transverseEndTags.remove(tag);

        if(started && !this.isWithinTarget()) {
            done = true;
Log.getInstance().log(Level.FINER, "{0}-{1} DONE ", this.getClass(), id, this.getClass().getSimpleName());
        }
        
Log.getInstance().log(Level.FINER, "  Expected end tags: {0}\nTransverse end tags: {1}", 
this.getClass(), this.toSimpleString(expectedEndTags), this.toSimpleString(transverseEndTags));
        
        
        boolean accept;
        if(this.tagLocator != null) {
            accept = a || b;
        }else{
            accept = a;
        }
        
        return accept;
    }
    
    @Override
    public boolean acceptStringNode(Text node) {

        final boolean LOG = false; //"targetNode5".equals(id);
if(LOG) System.out.println(this.getClass().getName() + "#acceptStringNode(Text)");        
        if(this.done) {
            return false;
        }
        
        if(this.boundsVisitor != null) {
            boundsVisitor.visitStringNode(node);
        }   

        this.tryDisable(node);
        
        final boolean withinBounds;
        boolean acceptedByTextFilter = false;
        boolean withinTarget = false;
        
        final boolean accept = (withinBounds = this.isWithinBounds()) && 
                (acceptedByTextFilter = this.isAcceptedByTextFilter(node, true)) && 
                (withinTarget = this.isWithinTarget());
//                this.isWithinBounds() && this.isAcceptedByTextFilter(node) && this.isWithinTarget();
        
if(LOG) System.out.println(this.getClass().getName() + ". Accept: " +accept+ ". within bounds: "+withinBounds+", textFilter accepts: "+acceptedByTextFilter+", within target: "+withinTarget);

Log.getInstance().log(Level.FINER, "{0}-NodeVisitingFilter@acceptStringNode. accepted: {1}, Text: {2}", 
        this.getClass(), id, accept, node);                
        
        return accept;
    }
    
    private boolean isAcceptedByTextFilter(Text node, boolean defaultValue) {
        return textFilter == null ? defaultValue : textFilter.accept(node);
    }
    
    @Override
    public boolean acceptRemarkNode(Remark node) {
        
        if(this.done) {
            return false;
        }
        
        if(this.boundsVisitor != null) {
            boundsVisitor.visitRemarkNode(node);
        }    

        this.tryDisable(node);
        
        // We don't accept remarks
        return false;
    }
    
    private void tryDisable(Node node) {
        if(!this.done) {
            this.done = this.textToDisableOn != null && this.textToDisableOn.length != 0 && 
                    StringArrayUtils.matches(textToDisableOn, node.getText(), StringArrayUtils.MatchType.EQUALS);
        }
    }
    
    public boolean isWithinTarget() {
        boolean output;
        boolean a = this.expectedEndTags != null && !this.expectedEndTags.isEmpty();
        if(this.tagLocator != null) {
            boolean b = this.transverseEndTags != null && !this.transverseEndTags.isEmpty();
            output = a || b;
        }else{
            output = a;
        }
        return output;
    }
    
    @Override
    public boolean isWithinBounds() {
    
        boolean isWithin = boundsVisitor == null || (boundsVisitor.isStarted() && !boundsVisitor.isDone());
        
Log.getInstance().log(Level.FINER, "Is within: {0}, is started: {1}, is done: {2}", 
        this.getClass(), isWithin, 
        boundsVisitor==null?"null":boundsVisitor.isStarted(), 
        boundsVisitor==null?"null":boundsVisitor.isDone());

        return isWithin;
    }
    
    private NodesFilter initNodesFilter() {
        if(nodesFilter == null) {
            nodesFilter = new NodesFilterImpl();
            nodesFilter.setId(id);
        }
        return nodesFilter;
    }
    
    private void addTransverseEndTag(Tag tag) {
        
        if(tag.getEndTag() != null) {

            if(this.transverseEndTags == null) {
                this.transverseEndTags = new ArrayList<>();
            }

            this.transverseEndTags.add(tag.getEndTag());

Log.getInstance().log(Level.FINEST, "Transverse end tags: {0}", this.getClass(), this.toSimpleString(transverseEndTags));
        }
    }

    private void addExpectedEndTag(Tag tag) {
        
        if(tag.getEndTag() != null) {

            if(this.expectedEndTags == null) {
                this.expectedEndTags = new ArrayList<>();
            }

            this.expectedEndTags.add(tag.getEndTag());

Log.getInstance().log(Level.FINEST, "Expected end tags: {0}", this.getClass(), this.toSimpleString(expectedEndTags));
        }
    }
    
    private StringBuilder toSimpleString(List<Tag> nodes) {
        if(nodes == null) {
            return null;
        }else{
            StringBuilder builder = new StringBuilder();
            builder.append('[');
            for(Tag tag:nodes) {
                builder.append(tag.getRawTagName());
                builder.append(',').append(' ');
            }
            builder.append(']');
            return builder;
        }
    }
    
    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public boolean isDone() {
        return done;
    }
    
    public List<Tag> getTransverseEndTags() {
        return transverseEndTags;
    }

    public List<Tag> getExpectedEndTags() {
        return expectedEndTags;
    }
    
    @Override
    public int getVisitedStartTags() {
        return visitedStartTags;
    }
    
    @Override
    public String [] getTextToReject() {
        return this.textFilter == null ? null : this.textFilter.getTextToReject();
    }
    
    @Override
    public void setTextToReject(String [] textToReject) {
        this.initTextFilter();
        this.textFilter.setTextToReject(textToReject);
    }

    @Override
    public String [] getTextToAccept() {
        return this.textFilter == null ? null : this.textFilter.getTextToAccept();
    }
    
    @Override
    public void setTextToAccept(String [] textToAccept) {
        this.initTextFilter();
        this.textFilter.setTextToAccept(textToAccept);
    }
    
    private void initTextFilter() {
        if(this.textFilter == null) {
            this.textFilter = new TextFilterImpl();
            this.textFilter.setId(id);
        }
    }

    @Override
    public String [] getTextToDisableOn() {
        return this.textToDisableOn;
    }
    
    @Override
    public void setTextToDisableOn(String [] disablingText) {
        this.textToDisableOn = disablingText;
    }
    
    @Override
    public NodeFilter getStartAtFilter() {
        return this.boundsVisitor == null ? null : this.boundsVisitor.getBoundsMarker().getStartAtFilter();
    }

    @Override
    public void setStartAtFilter(NodeFilter startAtFilter) {
        this.initBoundsVisitor().getBoundsMarker().setStartAtFilter(startAtFilter);
    }

    @Override
    public NodeFilter getStopAtFilter() {
        return this.boundsVisitor == null ? null : this.boundsVisitor.getBoundsMarker().getStopAtFilter();
    }

    @Override
    public void setStopAtFilter(NodeFilter stopAtFilter) {
        this.initBoundsVisitor().getBoundsMarker().setStartAtFilter(stopAtFilter);
    }

    private BoundsVisitor initBoundsVisitor() {
        if(boundsVisitor == null) {
            BoundsMarker marker = new BoundsMarker(id, null, null);
            this.boundsVisitor = new BoundsVisitor(null, marker);
            this.boundsVisitor.setId(id);
            this.boundsVisitor.setStrict(true);
        }
        return boundsVisitor;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public TextFilter getTextFilter() {
        return textFilter;
    }

    @Override
    public void setTextFilter(TextFilter textFilter) {
        this.textFilter = textFilter;
    }

    @Override
    public BoundsVisitor getBoundsVisitor() {
        return boundsVisitor;
    }

    @Override
    public void setBoundsVisitor(BoundsVisitor boundsVisitor) {
        this.boundsVisitor = boundsVisitor;
    }

    @Override
    public NodeFilter getTagFilter() {
        return tagFilter;
    }

    @Override
    public void setTagFilter(NodeFilter tagFilter) {
        this.tagFilter = tagFilter;
    }

    @Override
    public TagLocator getTagLocator() {
        return tagLocator;
    }

    @Override
    public void setTagLocator(TagLocator tagLocator) {
        this.tagLocator = tagLocator;
    }

    @Override
    public String[] getNodeTypesToAccept() {
        return nodesFilter == null ? null : nodesFilter.getNodeTypesToAccept();
    }

    @Override
    public void setNodeTypesToAccept(String[] nodeTypesToAccept) {
        this.initNodesFilter().setNodeTypesToAccept(nodeTypesToAccept);
    }

    @Override
    public String[] getNodeTypesToReject() {
        return nodesFilter == null ? null : nodesFilter.getNodeTypesToReject();
    }

    @Override
    public void setNodeTypesToReject(String[] nodeTypesToReject) {
        this.initNodesFilter().setNodeTypesToReject(nodeTypesToReject);
    }

    @Override
    public String[] getNodesToAccept() {
        return nodesFilter == null ? null : nodesFilter.getNodesToAccept();
    }

    @Override
    public void setNodesToAccept(String[] nodesToAccept) {
        this.initNodesFilter().setNodesToAccept(nodesToAccept);
    }

    @Override
    public String[] getNodesToReject() {
        return nodesFilter == null ? null : nodesFilter.getNodesToReject();
    }

    @Override
    public void setNodesToReject(String[] nodesToReject) {
        this.initNodesFilter().setNodesToReject(nodesToReject);
    }

    @Override
    public NodesFilter getNodesFilter() {
        return nodesFilter;
    }

    @Override
    public void setNodesFilter(NodesFilter nodesFilter) {
        this.nodesFilter = nodesFilter;
    }
}    
/**
//    @Override
    public boolean acceptTagOld(Tag tag) {
        
Level level = started && !done ? Level.INFO : Level.FINER;     
XLogger.getInstance().log(level, "{0}-{1}, Tag: {2}", 
this.getClass(), id, this.getClass().getSimpleName(), tag.toTagHtml());

        // Only start tags are counted
        visitedStartTags++;
        
        if(this.done) {
            return false;
        }

        if(this.boundsVisitor != null) {
            boundsVisitor.visitTag(tag);
        }    
        
        boolean locatorAccept;
        
        if(this.tagLocator != null) {
            
            this.tagLocator.visitTag(tag);

            boolean proceed = this.tagLocator.isProceed();

            boolean foundTarget = this.tagLocator.isFoundTarget();

            locatorAccept = proceed && foundTarget;
            
        }else{
            
            locatorAccept = true;
        }

//System.out.println(this.getClass().getName()+". transverse accept: "+proceed+
//    ", found target: "+foundTarget+", last tag accept: "+tagLocator.isLastTagAccepted()+
//    ", transversing target: "+this.isWithinTarget());                

        boolean wasInTarget = this.isWithinTarget();
        boolean isInTarget;
        
        if(locatorAccept) {
            
            this.addTransverseEndTag(tag);
            
            isInTarget = this.isWithinTarget();
            
        }else{
            
            isInTarget = false;
        }
        
        boolean filterAccept;
        
        boolean accept;
        
        boolean withinBounds = this.isWithinBounds();
        
XLogger.getInstance().log(level, "Accepted by locator: {0}, bounds: {1}, target bounds: {2}", 
this.getClass(), locatorAccept, withinBounds, wasInTarget);
        
        if(locatorAccept && withinBounds && (this.tagLocator == null || wasInTarget)) {

if(!started) {            
    XLogger.getInstance().log(Level.INFO, "{0}-{1} =============================== STARTED ", this.getClass(), id, this.getClass().getSimpleName());
}            
            started = true;
            
            filterAccept = (tagFilter == null || tagFilter.accept(tag));

            accept = filterAccept;

            if(accept) {
                
                this.addExpectedEndTag(tag);
            }
        }else{
            
            if(started) {
                done = true;
XLogger.getInstance().log(Level.INFO, "{0}-{1} =============================== DONE ", this.getClass(), id, this.getClass().getSimpleName());
            }
            
            accept = false;
        }
        
XLogger.getInstance().log(Level.FINER, "{0}-NodeVisitingFilter. accepted: {1}, index: {2}, Tag: {3}", 
        this.getClass(), id, accept, visitedStartTags, tag.toTagHtml());        
        
        return accept;
    }
    
 * 
 */

