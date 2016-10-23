package com.bc.webdatex.nodefilter;

import java.util.Collection;
import org.htmlparser.Node;
import org.htmlparser.Tag;
import org.htmlparser.filters.TagNameFilter;


/**
 * @(#)TagNameOnlyFilter.java   24-Sep-2015 16:07:12
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */

/**
 * Accepts Tags of a specified tagName which do not have any attributes
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.0
 */
public class TagNameOnlyFilter extends TagNameFilter {

    /**
     * Creates a new instance of TagNameFilter.
     * With no name, this would always return <code>false</code>
     * from {@link #accept}.
     */
    public TagNameOnlyFilter () {
        this ("");
    }

    /**
     * Creates a TagNameOnlyFilter that accepts tags with the given name
     * which do not have any attribute pairs
     * @param name The tag name to match.
     */
    public TagNameOnlyFilter (String name) {
        super(name);
    }
    
    @Override
    public boolean accept(Node node) {
        if(node instanceof Tag) {
            Collection attributes = ((Tag)node).getAttributes();
            // A Tag which has no key=value attributes contains
            // the tagName as the only attributes
            if(attributes != null && attributes.size() > 1) return false;
        }
        return super.accept(node);
    }
}
