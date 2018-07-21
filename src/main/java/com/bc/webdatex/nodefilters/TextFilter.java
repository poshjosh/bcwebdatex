package com.bc.webdatex.nodefilters;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;


/**
 * @(#)TextFilter.java   05-Oct-2015 12:14:20
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
public interface TextFilter extends NodeFilter {

    String [] EMPTY_ARRAY = new String[0];
    
    TextFilter ACCEPT_ALL = new TextFilter() {
        @Override
        public boolean accept(Node node) { return true; }
        @Override
        public String[] getTextToAccept() { return EMPTY_ARRAY; }
        @Override
        public String[] getTextToReject() { return EMPTY_ARRAY; }
    };
    
    @Override
    boolean accept(Node node);

    String[] getTextToAccept();

    String[] getTextToReject();
}
