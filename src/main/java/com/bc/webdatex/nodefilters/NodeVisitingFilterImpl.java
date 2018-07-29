package com.bc.webdatex.nodefilters;

import com.bc.nodelocator.NodeLocatingFilter;
import com.bc.nodelocator.impl.NodeLocatingFilterGreedy;
import com.bc.nodelocator.impl.NodeLocatingFilterImpl;
import com.bc.nodelocator.htmlparser.NodeMatcherHtmlparser;
import java.util.logging.Logger;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import com.bc.webdatex.context.ExtractionConfig;

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
public class NodeVisitingFilterImpl implements NodeVisitingFilter, Serializable {
    
    private transient static final Logger LOG = Logger.getLogger(NodeVisitingFilterImpl.class.getName());

    private boolean startedTarget;
    
    /**
     * If true, the extraction of the current page should not be continued
     */
    private boolean doneTarget;
    
    /**
     * Once started, this variable indicates the index
     */
    private int visitedStartTags;
    
    private List<Tag> transverseEndTags;
    
    private List<Tag> expectedEndTags;
    
    private final Object id;
    
    private final TextFilter textFilter;
    
    private final NodesFilter nodesFilter;
    
    private NodeLocatingFilter<Node> nodeLocator;

    public NodeVisitingFilterImpl(Object id, ExtractionConfig config, float tolerance, boolean greedy) {
        this(id, config, 
                greedy ? 
                new NodeLocatingFilterGreedy(
                        id, config.getPathFlattened(id).toList(), new NodeMatcherHtmlparser(tolerance)
                ) :
                new NodeLocatingFilterImpl(
                        id, config.getPathFlattened(id).toList(), new NodeMatcherHtmlparser(tolerance)
                ) 
        );
    }
    
    public NodeVisitingFilterImpl(Object id, ExtractionConfig config, NodeLocatingFilter<Node> nodeLocator) {
        
        this.id = Objects.requireNonNull(id);
        
        final String[] textToReject = config.getTextToReject(id);
        this.textFilter = textToReject == null || textToReject.length == 0 ?
                TextFilter.ACCEPT_ALL : new TextFilterImpl(id, null, config.getTextToReject(id));
        
        final String [] nodeTypesToAccept = config.getNodeTypesToAccept(id); 
        final String [] nodeTypesToReject = config.getNodeTypesToReject(id);
        final String [] nodesToAccept = config.getNodesToAccept(id);
        final String [] nodesToReject = config.getNodeToReject(id);
        if(nodeTypesToAccept == null || nodeTypesToAccept.length == 0 &&
                nodeTypesToReject == null || nodeTypesToReject.length == 0 &&
                nodesToAccept == null || nodesToAccept.length == 0 &&
                nodesToReject == null || nodesToReject.length == 0) {
            this.nodesFilter = NodesFilter.ACCEPT_ALL;
        }else{
            this.nodesFilter = new NodesFilterImpl(
                    id, 
                    nodeTypesToAccept, nodeTypesToReject,
                    nodesToAccept, nodesToReject
            );
        }

        this.nodeLocator = Objects.requireNonNull(nodeLocator);
    }
    
    @Override
    public void reset() {
        this.startedTarget = false;
        this.doneTarget = false;
        this.visitedStartTags = 0; 
        if(this.transverseEndTags != null) this.transverseEndTags.clear();
        this.transverseEndTags = null;
        if(this.expectedEndTags != null) this.expectedEndTags.clear();
        this.expectedEndTags = null;
        if(this.nodeLocator != null) {
            this.nodeLocator.reset();
        }
    }
    
    @Override
    public boolean acceptTag(Tag tag) {
        
        final Level level = startedTarget && !doneTarget ? Level.FINER : Level.FINEST;  

        if(LOG.isLoggable(level)) {
            LOG.log(level, "{0} Tag: {1}", new Object[]{id, tag.toTagHtml()});
        }
        
        // Only start tags are counted
        visitedStartTags++;
        
        if(this.doneTarget) {
            return false;
        }
        
        final boolean foundTarget = this.nodeLocator.test(tag);

        if(foundTarget || this.isWithinTarget()) {
            this.addTransverseEndTag(tag);
        }
        
        boolean accept;
        
        if(LOG.isLoggable(level)) {
            LOG.log(level, "Found target: {0}, within target: {1}", 
                new Object[]{foundTarget, this.isWithinTarget()});
        }
        
        if(foundTarget || this.isWithinTarget()) {

            if(!startedTarget) {            
                LOG.log(Level.FINER, "{0} STARTED", id);
            }            
            
            startedTarget = true;
            
            accept = true;

            this.addExpectedEndTag(tag);

        }else{
            
            if(startedTarget) {
                doneTarget = true;
                LOG.log(Level.FINER, "{0} DONE", id);
            }
            
            accept = false;
        }
        
        if(LOG.isLoggable(Level.FINER)){
            LOG.log(Level.FINER, "{0}-NodeVisitingFilter. accepted: {1}, index: {2}, Tag: {3}", 
                new Object[]{id, accept, visitedStartTags, tag.toTagHtml()});
        } 

        return accept;
    }
    
    @Override
    public boolean acceptEndTag(Tag tag) {

        if(this.doneTarget) {
            return false;
        }
        
        final boolean a = this.expectedEndTags != null && this.expectedEndTags.remove(tag);

        final boolean b = this.transverseEndTags != null && this.transverseEndTags.remove(tag);

        if(startedTarget && !this.isWithinTarget()) {
            doneTarget = true;
            LOG.log(Level.FINER, "{0} DONE", id);
        }

        if(LOG.isLoggable(Level.FINER)){
            LOG.log(Level.FINER, "  Expected end tags: {0}\nTransverse end tags: {1}", 
                new Object[]{this.toSimpleString(expectedEndTags), this.toSimpleString(transverseEndTags)});
        }
        
        final boolean accept = a || b;

        LOG.finer(() -> "Accepted END: "+accept+", "+tag.toTagHtml());

        return accept;
    }
    
    @Override
    public boolean acceptStringNode(Text node) {

        if(this.doneTarget) {
            return false;
        }

        boolean acceptedByTextFilter;
        boolean withinTarget = false;
        
        final boolean accept = (acceptedByTextFilter = this.isAcceptedByTextFilter(node, true)) && 
                (withinTarget = this.isWithinTarget());
//                this.isAcceptedByTextFilter(node) && this.isWithinTarget();
        
        if(LOG.isLoggable(Level.FINER)){
            LOG.log(Level.FINER, 
                    "{0}-NodeVisitingFilter@acceptStringNode. accepted: {1}, Text: {2}\n accepted by text filter: {3}, within target: {4}", 
                    new Object[]{id, accept, node, acceptedByTextFilter, withinTarget});
        }                
        
        return accept;
    }
    
    private boolean isAcceptedByTextFilter(Text node, boolean defaultValue) {
        return textFilter == null ? defaultValue : textFilter.accept(node);
    }
    
    @Override
    public boolean acceptRemarkNode(Remark node) {
        
        if(this.doneTarget) {
            return false;
        }
        
        // We don't accept remarks
        return false;
    }
    
    public boolean isWithinTarget() {
        final boolean output = (this.expectedEndTags != null && !this.expectedEndTags.isEmpty()) ||
                (this.transverseEndTags != null && !this.transverseEndTags.isEmpty());
        return output;
    }
    
    private void addTransverseEndTag(Tag tag) {
        
        if(tag.getEndTag() != null) {

            if(this.transverseEndTags == null) {
                this.transverseEndTags = new ArrayList<>();
            }

            this.transverseEndTags.add(tag.getEndTag());

            if(LOG.isLoggable(Level.FINEST)){
                LOG.log(Level.FINEST, "Transverse end tags: {0}", 
                        this.toSimpleString(transverseEndTags));
            }
        }
    }

    private void addExpectedEndTag(Tag tag) {
        
        if(tag.getEndTag() != null) {

            if(this.expectedEndTags == null) {
                this.expectedEndTags = new ArrayList<>();
            }

            this.expectedEndTags.add(tag.getEndTag());

            if(LOG.isLoggable(Level.FINEST)){
                LOG.log(Level.FINEST, "Expected end tags: {0}", 
                        this.toSimpleString(expectedEndTags));
            }
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
    
    public Object getId() {
        return id;
    }

    @Override
    public TextFilter getTextFilter() {
        return textFilter;
    }

    @Override
    public NodeLocatingFilter<Node> getNodeLocator() {
        return nodeLocator;
    }

    public void setNodeLocator(NodeLocatingFilter<Node> nodeLocator) {
        this.nodeLocator = nodeLocator;
    }

    @Override
    public NodesFilter getNodesFilter() {
        return nodesFilter;
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

