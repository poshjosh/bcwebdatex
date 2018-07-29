package com.bc.webdatex.nodefilters;

import com.bc.nodelocator.ConfigName;
import java.util.Arrays;
import java.util.Objects;
import org.htmlparser.Node;
import com.bc.webdatex.context.ExtractionConfig;


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
public class NodesFilterImpl extends FilterNode implements NodesFilter {
    
    private final Object id;

    private final String [] nodeTypesToAccept;
    
    private final String [] nodeTypesToReject;
    
    private final String [] nodesToAccept;
    
    private final String [] nodesToReject;

    public NodesFilterImpl(Object id, ExtractionConfig config) { 
        this(
                id, 
                config.getNodeTypesToAccept(id),
                config.getNodeTypesToReject(id),
                config.getNodesToAccept(id),
                config.getNodeToReject(id));
    }
    
    public NodesFilterImpl(Object id, 
            String [] nodeTypesToAccept, String [] nodeTypesToReject,
            String [] nodesToAccept, String [] nodesToReject) { 
        this.id = Objects.requireNonNull(id);
        this.nodeTypesToAccept = nodeTypesToAccept;
        this.nodeTypesToReject = nodeTypesToReject;
        this.nodesToAccept = nodesToAccept;
        this.nodesToReject = nodesToReject;
    }

    @Override
    public boolean accept(Node node) {

        boolean accept = this.accept(ConfigName.nodeTypesToAccept, node) &&
                 this.accept(ConfigName.nodeTypesToReject, node) &&
                 this.accept(ConfigName.nodesToAccept, node) &&
                 this.accept(ConfigName.nodesToAccept, node);
        
        return accept;
    }
    
    private boolean accept(ConfigName filterType, Node node) {

        String [] nodeIds = this.getNodeIds(filterType);
        
        if(nodeIds == null || nodeIds.length == 0) {
            return true;
        }else{
            return this.execute(filterType, nodeIds, node);
        }
    }    
    
    private String [] getNodeIds(ConfigName filterType) {
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
                throw new IllegalArgumentException("Unexpected " + filterType.getClass().getName() + 
                ": "+filterType+", expected any of: "+Arrays.toString(ConfigName.values()));
        }
    }

    public final Object getId() {
        return id;
    }

    @Override
    public String[] getNodeTypesToAccept() {
        return nodeTypesToAccept;
    }

    @Override
    public String[] getNodeTypesToReject() {
        return nodeTypesToReject;
    }

    @Override
    public String[] getNodesToAccept() {
        return nodesToAccept;
    }

    @Override
    public String[] getNodesToReject() {
        return nodesToReject;
    }

    @Override
    public String toString() {
        return id + "-"+this.getClass().getSimpleName();
    }
}
