package com.bc.webdatex.extractors.node;

import com.bc.webdatex.nodefilters.NodeVisitingFilter;
import com.bc.webdatex.nodefilters.NodesFilter;
import com.bc.webdatex.nodefilters.TextFilter;
import java.io.Serializable;
import org.htmlparser.visitors.NodeVisitor;


/**
 * @(#)DataExtractingNodeVisitorIx.java   01-Oct-2015 14:44:28
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
public interface NodeExtractor extends NodeVisitor, Serializable {

    StringBuilder getExtract();

    NodeVisitingFilter getFilter();

    Object getId();

    String getSeparator();

    boolean isAcceptScripts();

    boolean isConcatenateMultipleExtracts();

    boolean isEnabled();

    boolean isFinishedParsing();

    boolean isReplaceNonBreakingSpace();

    void reset();

    void setEnabled(boolean enabled);

    NodesFilter getNodesFilter();
    
    TextFilter getTextFilter();
}
