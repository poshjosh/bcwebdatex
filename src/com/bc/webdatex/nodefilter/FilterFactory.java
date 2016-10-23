package com.bc.webdatex.nodefilter;

import com.bc.webdatex.util.Util;
import com.bc.util.StringArrayUtils;
import com.bc.util.XLogger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;

/**
 * @(#)FilterFactory.java   29-Sep-2015 16:18:04
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
public class FilterFactory {
    
    public static enum FilterType{ tagNameOnly, tagName, textToDisableOn,
    textToReject, rejectNode, hasIdAttribute, attributes, attributesRegex,
    nodesToAccept, nodesToReject, nodeTypesToAccept, nodeTypesToReject}
    
    public static final String TARGET = "targetNode";
    public static final String PARENT = "parentNode";
    public static final String START_AT = "startAtNode";
    public static final String STOP_AT = "stopAtNode";
    public static final String [] NODE_TYPES = {PARENT, TARGET, START_AT, STOP_AT};
    
    public NodeFilter createTagNameFilter(final String ID, final String nodeName) {

XLogger.getInstance().log(Level.FINER, "PropertyKey: {0}, Node to create filter for: {1}", 
FilterFactory.class, ID, nodeName);        

        if(nodeName == null) return null;

        return new TagNameFilter(nodeName){

            @Override
            public boolean accept(Node node) {
                
                boolean accept = super.accept(node);
              
XLogger.getInstance().log(Level.FINER, "{0}-TagnameFilter, Accept: {1}, Node: {2}", 
this.getClass(), ID, accept, node);
                return accept;
            }
            @Override
            public String toString() {
                return ID+"-TagNameFilter. Node name: "+nodeName;
            }
        };
    }

    public NodeFilter createTagNameOnlyFilter(final String ID, final String nodeName) {

XLogger.getInstance().log(Level.FINER, "PropertyKey: {0}, Node to create filter for: {1}", 
FilterFactory.class, ID, nodeName);        

        if(nodeName == null) return null;

        // Note we use TagNameOnlyFilter here because we want
        // to accept Tags which do not have any attributes
        //
        return new TagNameOnlyFilter(nodeName){
            @Override
            public boolean accept(Node node) {
                boolean accept = super.accept(node);
XLogger.getInstance().log(Level.FINER, "TagNameOnlyFilter, Accept: {0}, Node: {1}", 
this.getClass(), accept, node);                
                return accept;
            }
            @Override
            public String toString() {
                return ID+"-TagNameOnlyFilter. Node name: "+nodeName;
            }
        };
    }
    
    public NodeFilter createHasAttributesRegexFilter(final String ID, final Map<String, String> attributes) {

XLogger.getInstance().log(Level.FINER, "Attributes to create filter for: {0}={1}", 
FilterFactory.class, ID, attributes);        

        NodeFilter [] predicates = new NodeFilter[attributes.size()];

        int i = 0;
        for(String key:attributes.keySet()) {
            Object value = attributes.get(key);
XLogger.getInstance().log(Level.FINEST, "@newAttributesRegexInstance. Key: {0}, Value: {1}", 
FilterFactory.class, key, value);
            predicates[i++] = new HasAttributeRegexFilter(key, value.toString());
        }

        AndFilter andFilter = new AndFilter(predicates){
            @Override
            public boolean accept(Node node) {
                
                boolean accept = super.accept(node);
                
XLogger.getInstance().log(Level.FINER, "{0}-HasAttributesRegexFilter, Accept: {1}, Node: {2}", 
this.getClass(), ID, accept, node);                
                return accept;
            }
            @Override
            public String toString() {
                return ID+"-HasAttributesRegexFilter. attributes: "+attributes;
            }
        };
        return andFilter;
    }

    public NodeFilter createHasAttributesFilter(
            final String ID, final Map<String, String> attributes) {

XLogger.getInstance().log(Level.FINER, "Attributes to create filter for: {0}={1}", 
FilterFactory.class, ID, attributes);        

        NodeFilter [] predicates = new NodeFilter[attributes.size()];

        int i = 0;
        for(String key:attributes.keySet()) {
            String value = attributes.get(key);
XLogger.getInstance().log(Level.FINEST, "@newAttributesInstance. Key: {0}, Value: {1}", 
FilterFactory.class, key, value);
            predicates[i++] = new HasAttributeFilter(key, value);
        }

        return new AndFilter(predicates){
            @Override
            public boolean accept(Node node) {
                
                boolean accept = super.accept(node);

XLogger.getInstance().log(Level.FINER, "{0}-HasAttributesFilter, Accept: {1}, Node: {2}", 
this.getClass(), ID, accept, node);                

                return accept;
            }
            @Override
            public String toString() {
                return ID+"-HasAttributesFilter. attributes: "+attributes;
            }
        };
    }

    public NodeFilter createHasAttributeFilter(
            final String filterID, final String name, final String value) {
        
XLogger.getInstance().log(Level.FINER, "{0}-HasIdAttribute. id value: {1}", 
FilterFactory.class, filterID, value);        

        return new HasAttributeFilter(name, value){
            @Override
            public boolean accept(Node node) {
                boolean accept = super.accept(node);
XLogger.getInstance().log(Level.FINER, "HasIdAttributeFilter. Accept: {0}, Node: {1}", 
this.getClass(), accept, node);                
                return accept;
            }
            @Override
            public String toString() {
                return filterID + "-HasIdAttributeFilter["+name+"="+value+"]";
            }
        };
    }

    public NodeFilter createUnwantedTextFilter(
            final String ID, final String [] toReject) {
        
XLogger.getInstance().log(Level.FINER, "{0}={1}", 
FilterFactory.class, ID,toReject==null?null:Arrays.asList(toReject));        

        if(toReject == null) return null;
        
        TextFilter filter = new TextFilterImpl() {
            @Override
            public boolean accept(Node node) {
                boolean accept = super.accept(node);
XLogger.getInstance().log(Level.FINER, "UnwantedTextFilter, Accept: {0}, Node: {1}", 
this.getClass(), accept, node);                
                return accept;
            }
            @Override
            public String toString() {
                return ID + "-UnwantedTextFilter. To reject: "+Arrays.toString(toReject);
            }
        };
        
        filter.setTextToReject(toReject);

        return filter;
    }

    public NodeFilter createNodesFilter(
            final String ID, final FilterType filterType) {
        
        return new NodesFilterImpl() {
            @Override
            public FilterFactory getFilterFactory() {
                return FilterFactory.this;
            }
        };
    }
    
    public NodeFilter createNodesFilter(
            final String ID, String [] nodes, final FilterType filterType) {

        final String [] nodeIds = getLowerCaseArray(nodes);
        
//XLogger.getInstance().log(Level.INFO, "= = = = = = = = = =  Level: {0}", FilterFactory.class, level.getName());

XLogger.getInstance().log(Level.FINER, "{0}={1}",
FilterFactory.class, ID, nodeIds==null?null:Arrays.toString(nodeIds));        
        
        return new NodeFilter() {
            
            @Override
            public boolean accept(Node node) {
                boolean accept = FilterFactory.this.filterNode(filterType, nodeIds, node);
XLogger.getInstance().log(Level.FINER, "{0}-{1}Filter, accept: {2}, Node: {3}", 
this.getClass(), ID, filterType, accept, node);                
                return accept;    
            }
            
            @Override
            public String toString() {
                return ID + "-"+filterType+"Filter. Nodes: " + Arrays.toString(nodeIds);  
            }
        };        
    }

    public boolean filterNode(FilterType filterType, String [] nodeIds, Node node) {
        boolean accept;
        switch(filterType) {
            case nodeTypesToAccept:
                 nodeTypesToReject:
                 accept = FilterFactory.this.acceptNodeType(filterType, nodeIds, node);
                 break;
            case nodesToAccept:
                 nodesToReject:
                 accept = FilterFactory.this.acceptNodeName(filterType, nodeIds, node);
                 break;
            default:    
                throw new IllegalArgumentException("Unexpected "+FilterType.class.getName()+": "+filterType);
        }
        return accept;    
    }
    
    private boolean acceptNodeName(FilterType filterType, String [] nodeNames, Node node) {

        String nodeID = null;

        switch(filterType) {
            case nodesToReject:
            case nodesToAccept:    
                if(node instanceof Tag) {
                    nodeID = ((Tag)node).getTagName();
                }else if (node instanceof Text) {
                    nodeID = node.getText();
                }else if (node instanceof Remark) {
                    nodeID = node.getText();
                }else{
                    StringBuilder nodeStr = Util.appendTag(node, 50);
                    throw new IllegalArgumentException(nodeStr.toString());
                }               
                break;
            default:    
                throw new IllegalArgumentException("Unexpected "+FilterType.class.getName()+": "+filterType);
        }

        boolean accept = true;
        switch(filterType) {
            case nodesToReject:
                accept = !StringArrayUtils.matches(nodeNames, nodeID, StringArrayUtils.MatchType.EQUALS_IGNORE_CASE); 
                break;
            case nodesToAccept:
                accept = StringArrayUtils.matches(nodeNames, nodeID, StringArrayUtils.MatchType.EQUALS_IGNORE_CASE); 
                break;
            default:
                throw new IllegalArgumentException("Unexpected "+FilterType.class.getName()+": "+filterType);
        }

        return accept;
    }    

    private boolean acceptNodeType(FilterType filterType, String [] nodeTypes, Node node) {

        String nodeID = null;

        switch(filterType) {
            case nodeTypesToReject:
            case nodeTypesToAccept:   
                if(node instanceof Tag) {
                    nodeID = "tag";
                }else if (node instanceof Text) {
                    nodeID = "text";
                }else if (node instanceof Remark) {
                    nodeID = "remark";
                }else{
                    StringBuilder nodeStr = Util.appendTag(node, new StringBuilder(), false, -1);
                    throw new IllegalArgumentException(nodeStr.toString());
                }                
                break;
            default:    
                throw new IllegalArgumentException("Unexpected "+FilterType.class.getName()+": "+filterType);
        }

        boolean accept = true;
        switch(filterType) {
            case nodeTypesToReject:    
                accept = !StringArrayUtils.matches(nodeTypes, nodeID, StringArrayUtils.MatchType.EQUALS_IGNORE_CASE); 
                break;
            case nodeTypesToAccept:    
                accept = StringArrayUtils.matches(nodeTypes, nodeID, StringArrayUtils.MatchType.EQUALS_IGNORE_CASE); 
                break;
            default:
                throw new IllegalArgumentException("Unexpected "+FilterType.class.getName()+": "+filterType);
        }

        return accept;
    }    
    
    private String [] getLowerCaseArray(Object [] arr) {

        String [] output = null;
        
        if(arr != null) {
            
            output = new String[arr.length];
            
            for(int i=0; i<output.length; i++) {
                
                output[i] = arr[i].toString().toLowerCase();
            }
        }
        
        return output;
    }
    
    private String [] getLowerCaseArray(List list) {

        String [] output = null;
        
        if(list != null) {
            
            output = new String[list.size()];
            
            for(int i=0; i<output.length; i++) {
                
                output[i] = list.get(i).toString().toLowerCase();
            }
        }
        
        return output;
    }
    
}
