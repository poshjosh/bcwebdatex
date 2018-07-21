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
public interface NodesFilter extends NodeFilter {
    
    String [] EMPTY_ARRAY = new String[0];
    
    NodesFilter ACCEPT_ALL = new NodesFilter() {
        @Override
        public boolean accept(Node node) { return true; }
        @Override
        public String[] getNodeTypesToAccept() { return EMPTY_ARRAY; }
        @Override
        public String[] getNodeTypesToReject() { return EMPTY_ARRAY; }
        @Override
        public String[] getNodesToAccept() { return EMPTY_ARRAY; }
        @Override
        public String[] getNodesToReject() { return EMPTY_ARRAY; }
    };

    @Override
    boolean accept(Node node);

    String[] getNodeTypesToAccept();

    String[] getNodeTypesToReject();

    String[] getNodesToAccept();

    String[] getNodesToReject();
}
