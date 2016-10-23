package com.bc.webdatex.nodefilter;

import java.util.Arrays;
import org.htmlparser.Node;


/**
 * @(#)NodesFilterImpl.java   06-Oct-2015 15:51:58
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
public class NodesFilterImpl implements NodesFilter {
    
    private String id;

    private String [] nodeTypesToAccept;
    
    private String [] nodeTypesToReject;
    
    private String [] nodesToAccept;
    
    private String [] nodesToReject;
    
    public NodesFilterImpl() { }

    @Override
    public boolean accept(Node node) {

        boolean accept = this.accept(FilterFactory.FilterType.nodeTypesToAccept, node) &&
                 this.accept(FilterFactory.FilterType.nodeTypesToReject, node) &&
                 this.accept(FilterFactory.FilterType.nodesToAccept, node) &&
                 this.accept(FilterFactory.FilterType.nodesToAccept, node);
        
        return accept;
    }
    
    private boolean accept(FilterFactory.FilterType filterType, Node node) {

        String [] nodeIds = this.getNodeIds(filterType);
        
        if(nodeIds == null || nodeIds.length == 0) {
            return true;
        }else{
            return this.getFilterFactory().filterNode(filterType, nodeIds, node);
        }
    }    
    
    private FilterFactory _ff_accessViaGetter;
    public FilterFactory getFilterFactory() {
        if(_ff_accessViaGetter == null) {
            _ff_accessViaGetter = new FilterFactory();
        }
        return _ff_accessViaGetter;
    }
        
    private String [] getNodeIds(FilterFactory.FilterType filterType) {
        switch(filterType) {
            case nodeTypesToAccept:
                return this.nodeTypesToAccept;
            case nodeTypesToReject:
                return this.nodeTypesToReject;
            case nodesToAccept:
                return this.nodesToAccept;
            case nodesToReject:
                return this.nodesToReject;
            default:
                throw new IllegalArgumentException("Unexpected " + FilterFactory.FilterType.class.getName() + 
                ": "+filterType+", expected any of: "+Arrays.toString(FilterFactory.FilterType.values()));
        }
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }
    
    @Override
    public String[] getNodeTypesToAccept() {
        return nodeTypesToAccept;
    }

    @Override
    public void setNodeTypesToAccept(String[] nodeTypesToAccept) {
        this.nodeTypesToAccept = nodeTypesToAccept;
    }

    @Override
    public String[] getNodeTypesToReject() {
        return nodeTypesToReject;
    }

    @Override
    public void setNodeTypesToReject(String[] nodeTypesToReject) {
        this.nodeTypesToReject = nodeTypesToReject;
    }

    @Override
    public String[] getNodesToAccept() {
        return nodesToAccept;
    }

    @Override
    public void setNodesToAccept(String[] nodesToAccept) {
        this.nodesToAccept = nodesToAccept;
    }

    @Override
    public String[] getNodesToReject() {
        return nodesToReject;
    }

    @Override
    public void setNodesToReject(String[] nodesToReject) {
        this.nodesToReject = nodesToReject;
    }

    @Override
    public String toString() {
        return id + "-"+this.getClass().getSimpleName();
    }
}
