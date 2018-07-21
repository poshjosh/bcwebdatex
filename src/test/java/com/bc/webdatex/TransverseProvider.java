package com.bc.webdatex;

import com.bc.nodelocator.htmlparser.PathBuilderHtmlparser;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.dom.HtmlDocument;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import com.bc.nodelocator.Path;


/**
 * @(#)TransverseProvider.java   24-Sep-2015 17:53:36
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
public class TransverseProvider {

    public static String [] getPath() throws ParserException {
        return getPath(getNodeList());
    }    
    
    public static Tag getTargetNode(HtmlDocument nodes) {
        HasAttributeFilter f = new HasAttributeFilter("id", "target");
        NodeList target = nodes.extractAllNodesThatMatch(f, true);
        Tag tag = (Tag)target.get(0);
        return tag;
    }
        
    public static String [] getPath(HtmlDocument nodes) throws ParserException {
        
        Tag tag = getTargetNode(nodes);
//System.out.println("Target node: "+tag.toTagHtml());        
        
        PathBuilderHtmlparser transverseBuilder = new PathBuilderHtmlparser();
//System.out.println("Parents: "+pathBuilder.getParents());        
//System.out.println("Siblings: "+pathBuilder.getSiblings());
        Path<String> path = transverseBuilder.build(tag);
//System.out.println("Path: "+path);
        
        return path.toArray(new String[0]);
    }
    
    public static HtmlDocument getNodeList() throws ParserException {
        return getNodeList(getHtml());
    }
    
    public static HtmlDocument getNodeList(String html) throws ParserException {
        Parser parser = new Parser();
        parser.setInputHTML(html);
        return parser.parse(null);
    }
    
    public static String getHtml() {
        String html = "<!DOCTYPE html>\n" +
"<html>\n" +
"    <head>\n" +
"        <title>Sample</title>\n" +
"        <meta charset=\"UTF-8\">\n" +
"        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
"    </head>\n" +
"    <body>\n" +
"        <div id=\"container\" attr1='post-2651' attr2=\"this is a very long attribute value for simulation purposes only\">\n" +
"            <span></span>\n" +
"            <div></div>\n" +
"            <div>\n" +
"                <div></div>\n" +
"                <p id=mypara class='myparaclass'></p>\n" +
"                <div class=\"content\">\n" +
"                    <h1>Heading 1</h1>    \n" +
"                    <span id=\"target\">False target does NOT agree that <span>Jesus is Lord</span></span>\n" +
"                </div>\n" +
"                <div class=\"content\">\n" +
"                    <h1>Heading 2</h1>    \n" +
"                    <span id=\"target\">Target agrees that <span>Jesus is Lord</span></span>\n" +
"                </div>\n" +
"                <div></div>\n" +
"            </div>\n" +
"            <p></p>\n" +
"        </div>\n" +
"    </body>\n" +
"</html>\n";
        return html;
    }
}
