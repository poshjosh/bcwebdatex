package com.bc.webdatex.locator;

import java.util.Iterator;
import java.util.List;
import org.htmlparser.Tag;
import org.htmlparser.visitors.NodeVisitor;


/**
 * @(#)TransverseVisitorIx.java   22-Sep-2015 20:44:09
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
public interface TagLocator extends NodeVisitor, Iterator<String> {

    boolean isFoundTarget();

    boolean isLastTagAccepted();
    
    boolean isProceed();
    
    int getHorizontalIndex();

    String getId();
    
    Tag getTarget();
    
    /**
     * @return a copy of the transverse, which is the series of html tags leading
     * up to the target node.
     */
    List<String>[] getTransverse();
    
    int getVerticalIndex();

    void reset();
}
