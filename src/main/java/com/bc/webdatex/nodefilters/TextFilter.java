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

    @Override
    boolean accept(Node node);

    String getId();

    String[] getTextToAccept();

    String[] getTextToReject();

    void setId(String id);

    void setTextToAccept(String[] textToAccept);

    void setTextToReject(String[] textToReject);

}
