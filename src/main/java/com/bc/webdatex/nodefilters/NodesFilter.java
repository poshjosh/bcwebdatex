package com.bc.webdatex.nodefilters;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;


/**
 * @(#)NodesFilter.java   06-Oct-2015 18:51:17
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
public interface NodesFilter extends NodeFilter<Node> {

    @Override
    boolean accept(Node node);

    String getId();

    String[] getNodeTypesToAccept();

    String[] getNodeTypesToReject();

    String[] getNodesToAccept();

    String[] getNodesToReject();

    void setId(String id);

    void setNodeTypesToAccept(String[] nodeTypesToAccept);

    void setNodeTypesToReject(String[] nodeTypesToReject);

    void setNodesToAccept(String[] nodesToAccept);

    void setNodesToReject(String[] nodesToReject);

}
