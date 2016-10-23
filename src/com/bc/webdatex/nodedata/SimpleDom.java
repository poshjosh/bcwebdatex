package com.bc.webdatex.nodedata;

import com.bc.webdatex.tags.Link;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;

public class SimpleDom implements Dom, Serializable {

    private final String url;
    private final String formattedUrl;
    private final NodeList nodeList;
    private final List<MetaTag> metaTags;
    private MetaTag robots;
    private MetaTag keywords;
    private MetaTag description;
    private Link ico;
    private Link icon;
    private final TitleTag title;
    private final BodyTag body;

    public SimpleDom(String url, NodeList nodes) {
        this(url, url, nodes);
    }

    public SimpleDom(String url, String formattedUrl, NodeList nodes) {
        
        this.url = url;
        this.formattedUrl = formattedUrl;
        this.nodeList = nodes;
        
        NodeList metaNodes = nodes.extractAllNodesThatMatch(new TagNameFilter("META"), true);
        if(metaNodes == null || metaNodes.isEmpty()) {
            this.metaTags = null;
        }else{
            List<MetaTag> temp = new ArrayList();
            for(Node metaNode:metaNodes) {
                temp.add((MetaTag)metaNode);
            }
            this.metaTags = temp.isEmpty() ? Collections.EMPTY_LIST : Collections.unmodifiableList(temp);
        }
        
        for (MetaTag metaTag : metaTags) {

            String name = metaTag.getAttributeValue("name");
            if ((this.robots == null) && ("robots".equals(name))) {
                this.robots = metaTag;
            } else if ((this.keywords == null) && ("keywords".equals(name))) {
                this.keywords = metaTag;
            } else if ((this.description == null) && ("description".equals(name))) {
                this.description = metaTag;
            }
        }

        NodeList titles = nodes.extractAllNodesThatMatch(new TagNameFilter("TITLE"), true);

        this.title = titles == null || titles.isEmpty() ? null : (TitleTag)titles.get(0);

        NodeList links = nodes.extractAllNodesThatMatch(new TagNameFilter("LINK"), true);
        for (Node node : links) {

            Link link = (Link)node;
            String rel = link.getAttributeValue("rel");
            if (rel != null)  {

                String lower = rel.toLowerCase().trim();
                if ((this.ico == null) && ("shortcut icon".equals(lower))) {
                    this.ico = link;
                } else if ((this.icon == null) && ("icon".equals(lower))) {
                    this.icon = link;
                }

                if ((this.ico != null) && (this.icon != null)) {
                    break;
                }
            }
        }

        NodeList bodies = nodes.extractAllNodesThatMatch(new TagNameFilter("BODY"), true);

        this.body = bodies == null || bodies.isEmpty() ? null : (BodyTag)bodies.get(0);
    }
    
    @Override
    public List<MetaTag> getMetaTags(NodeFilter filter) {
        List<MetaTag> output = null;
        for(MetaTag metaTag : this.metaTags) {
            if(filter.accept(metaTag)) {
                if(output == null) {
                    output = new ArrayList<>();
                }
                output.add(metaTag);
            }
        }
        return output == null ? Collections.EMPTY_LIST : output;
    }
    
    @Override
    public Node getElementById(String id) {
        
        NodeList nodes = this.getElementsByAttribute("id", id);
        
        return nodes == null || nodes.isEmpty() ? Node.BLANK_NODE : nodes.get(0);
    }
    
    @Override
    public NodeList getElementsByClassName(String className) {
        
        return this.getElementsByAttribute("class", className);
    }
    
    @Override
    public NodeList getElementsByTagName(String nodeName) {
        
        TagNameFilter filter = new TagNameFilter(nodeName);
        
        NodeList output = this.nodeList.extractAllNodesThatMatch(filter, true);
        
        return output;
    }
    
    @Override
    public NodeList getElementsByTagName(String nodeName, String attributeName, String attributeValue) {
        
        TagNameFilter tagNameFilter = new TagNameFilter(nodeName);
        HasAttributeFilter hasAttributeFilter = new HasAttributeFilter(attributeName, attributeValue);
        NodeFilter filter = new AndFilter(tagNameFilter, hasAttributeFilter);
        
        NodeList output = this.nodeList.extractAllNodesThatMatch(filter, true);
        
        return output;
    }
    
    @Override
    public NodeList getElementsByAttribute(String attributeName, String attributeValue) {
        
        return this.getElements(new HasAttributeFilter(attributeName, attributeValue));
    }
    
    @Override
    public NodeList getElements(NodeFilter filter) {
        
        NodeList output = this.nodeList.extractAllNodesThatMatch(filter, true);
        
        return output;
    }
    
    @Override
    public String getURL() {
        return this.url;
    }

    @Override
    public String getFormattedURL() {
        return this.formattedUrl;
    }

    @Override
    public MetaTag getRobots() {
        return this.robots;
    }

    @Override
    public MetaTag getKeywords() {
        return this.keywords;
    }

    @Override
    public MetaTag getDescription() {
        return this.description;
    }

    @Override
    public Link getIco(){
        return this.ico;
    }

    @Override
    public Link getIcon() {
        return this.icon;
    }

    @Override
    public TitleTag getTitle() {
        return this.title;
    }

    @Override
    public BodyTag getBody() {
        return this.body;
    }

    @Override
    public NodeList getNodeList(){
        return this.nodeList;
    }

    @Override
    public List<MetaTag> getMetaTags() {
        return this.metaTags;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getClass().getName());
        builder.append("{URL=").append(this.url);
        builder.append(", Nodes=").append(this.nodeList == null ? null : this.nodeList.size());
        builder.append('}');
        return builder.toString();
    }
}
