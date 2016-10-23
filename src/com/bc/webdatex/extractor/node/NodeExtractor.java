package com.bc.webdatex.extractor.node;

import com.bc.webdatex.bounds.HasBounds;
import com.bc.webdatex.nodefilter.NodeVisitingFilter;
import com.bc.webdatex.nodefilter.NodesFilter;
import java.io.Serializable;
import org.htmlparser.visitors.NodeVisitor;


/**
 * @(#)DataExtractingNodeVisitorIx.java   01-Oct-2015 14:44:28
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
public interface NodeExtractor extends NodeVisitor, HasBounds, Serializable {

    String[] getAttributesToAccept();

    StringBuilder getExtract();

    NodeVisitingFilter getFilter();

    String getId();

    String[] getNodesToRetainAttributes();

    String getSeparator();

    boolean isAcceptScripts();

    boolean isConcatenateMultipleExtracts();

    @Override
    boolean isDone();

    boolean isEnabled();

    boolean isFinishedParsing();

    boolean isReplaceNonBreakingSpace();

    @Override
    boolean isStarted();

    @Override
    void reset();

    void setAcceptScripts(boolean acceptScripts);
//
    void setAttributesToAccept(String[] attributesToAccept);
//
    void setConcatenateMultipleExtracts(boolean concatenateMultipleExtracts);

    void setEnabled(boolean enabled);

    void setFilter(NodeVisitingFilter filter);

    void setId(String id);

    void setNodesToRetainAttributes(String[] nodesToRetainAttributes);

    void setReplaceNonBreakingSpace(boolean replaceNonBreakingSpace);

    void setSeparator(String separator);

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
