package com.bc.webdatex.nodefilters;

import com.bc.nodelocator.NodeLocatingFilter;
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
public interface NodeVisitingFilter extends NodeFilter {
    
    @Override
    default boolean accept(Node node) {
        return accept(node, false);
    }
    
    default boolean accept(Node node, boolean defaultOutput) {
        boolean output;
        if(node instanceof Tag) {
            Tag tag = (Tag)node;
            if(!tag.isEndTag()) {
                output = this.acceptTag(tag);
            }else {
                output = this.acceptEndTag(tag);
            }
        }else if(node instanceof Text) {
            output = this.acceptStringNode((Text)node);
        }else if(node instanceof Remark) {
            output = this.acceptRemarkNode((Remark)node);
        }else {
            // Unknown node, may be Tag type was not registered
            output = defaultOutput;
        }
        return output;
    }

    boolean acceptEndTag(Tag tag);

    boolean acceptRemarkNode(Remark node);

    boolean acceptStringNode(Text node);

    boolean acceptTag(Tag tag);

    NodeLocatingFilter<Node> getNodeLocator();

    TextFilter getTextFilter();

    NodesFilter getNodesFilter();
    
    int getVisitedStartTags();

    void reset();
}
