/*
 * Copyright 2016 NUROX Ltd.
 *
 * Licensed under the NUROX Ltd Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.looseboxes.com/legal/licenses/software.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bc.webdatex.util;

import java.util.logging.Logger;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.util.NodeList;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 19, 2016 10:33:58 PM
 */
public class NodeUtil {

    private transient static final Logger LOG = Logger.getLogger(NodeUtil.class.getName());
    
    public static void insertBefore(Node node, Node toInsert, NodeFilter filter) {

        if(filter != null && filter.accept(node)) {
if(LOG.isLoggable(Level.FINER)){
LOG.log(Level.FINER, "Accepted: {0}", node);
}
        
            insertBefore(node, toInsert);
            
        }else{
            
if(LOG.isLoggable(Level.FINER)){
LOG.log(Level.FINER, "Rejected: {0}", node);
}
        }
        
        NodeList children = node.getChildren();
        
        if(children != null && !children.isEmpty()) {
            
            for(Node child : children) {
                
                insertBefore(child, toInsert, filter);
            }
        }
    }
    
    private static boolean insertBefore(Node node, Node toInsert) {
        boolean output = false;
        Node parent = node.getParent();
        if(parent != null) {
            NodeList siblings = parent.getChildren();
            final int tgtIndex = siblings.indexOf(node);
            NodeList update = new org.htmlparser.util.NodeListImpl();
            for(int i=0; i<siblings.size(); i++) {
                if(i == tgtIndex) {
                    update.add(toInsert);
                    output = true;
                }
                update.add(siblings.elementAt(i));
            }
            parent.setChildren(update);
        }
        return output;
    }
    
}
