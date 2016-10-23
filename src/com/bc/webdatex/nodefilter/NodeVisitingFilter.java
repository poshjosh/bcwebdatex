package com.bc.webdatex.nodefilter;

import com.bc.webdatex.bounds.HasBounds;
import com.bc.webdatex.locator.TagLocator;
import com.bc.webdatex.visitor.BoundsVisitor;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;


/**
 * @(#)NodeVisitingFilterIx.java   01-Oct-2015 13:30:03
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
public interface NodeVisitingFilter extends HasBounds, NodeFilter {
    
    @Override
    boolean accept(Node node);

    boolean acceptEndTag(Tag tag);

    boolean acceptRemarkNode(Remark node);

    boolean acceptStringNode(Text node);

    boolean acceptTag(Tag tag);

    BoundsVisitor getBoundsVisitor();

    String getId();

    NodeFilter getStartAtFilter();

    NodeFilter getStopAtFilter();

    NodeFilter getTagFilter();

    TagLocator getTagLocator();

    TextFilter getTextFilter();

    String[] getTextToAccept();

    String [] getTextToDisableOn();
    
    String[] getTextToReject();
    
    int getVisitedStartTags();

    @Override
    boolean isDone();

    @Override
    boolean isStarted();

    boolean isWithinBounds();
    
    @Override
    void reset();

    void setBoundsVisitor(BoundsVisitor boundsVisitor);

    void setStartAtFilter(NodeFilter startAtFilter);

    void setStopAtFilter(NodeFilter stopAtFilter);

    void setTagFilter(NodeFilter tagFilter);

    void setTagLocator(TagLocator tagLocator);
    
    void setTextFilter(TextFilter filter);

    void setTextToAccept(String[] textToAccept);

    void setTextToDisableOn(String [] textToAccept);
    
    void setTextToReject(String[] textToReject);
    
    String[] getNodeTypesToAccept();

    void setNodeTypesToAccept(String[] nodeTypesToAccept);

    String[] getNodeTypesToReject();

    void setNodeTypesToReject(String[] nodeTypesToReject);

    String[] getNodesToAccept();

    void setNodesToAccept(String[] nodesToAccept);

    String[] getNodesToReject();

    void setNodesToReject(String[] nodesToReject);

    NodesFilter getNodesFilter();

    void setNodesFilter(NodesFilter nodesFilter);
}
