package com.bc.webdatex.nodedata;

import com.bc.webdatex.tags.Link;
import java.util.List;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;

public interface Dom{
    
    String getURL();

    String getFormattedURL();

    NodeList getNodeList();
    
    List<MetaTag> getMetaTags();
    
    List<MetaTag> getMetaTags(NodeFilter filter);

    Node getElementById(String id);
    
    NodeList getElementsByClassName(String className);
    
    NodeList getElementsByTagName(String nodeName);
    
    NodeList getElementsByTagName(String nodeName, String attributeName, String attributeValue);
    
    NodeList getElementsByAttribute(String attributeName, String attributeValue);
    
    NodeList getElements(NodeFilter filter);
    
    MetaTag getRobots();

    MetaTag getKeywords();

    MetaTag getDescription();

    Link getIco();

    Link getIcon();

    TitleTag getTitle();

    BodyTag getBody();
}
