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

import com.bc.util.Log;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.util.NodeList;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 19, 2016 10:33:58 PM
 */
public class NodeUtil {

    /**
     * Clone methods provided for Nodes in the htmlparser library
     * Clones only a Node's attributes, page, start and end position.
     * This also clones all the Node's parent and children. 
     * @param node The node to clone
     * @return The clone 
     * @throws CloneNotSupportedException 
     */
    public static Node deepClone(Node node) throws CloneNotSupportedException {
        
        return deepClone(node, true, true);
    }

    /**
     * Clone method provided for Nodes in the htmlparser library
     * clones only a Node's attributes, page, start and end position.
     * However, this method also clones all the Node's parent, if parameter 
     * parents is <tt>true</tt> and all the Node's children if parameter children
     * is <tt>true</tt>
     * @param node The node to clone
     * @param parents If <tt>true</tt> all the Node's parents will be cloned
     * @param children If <tt>true</tt> all the Node's parents will be cloned
     * @return The clone
     * @throws CloneNotSupportedException 
     */
    public static Node deepClone(Node node, boolean parents, 
            boolean children) throws CloneNotSupportedException {
        
        Node clone = (Node)node.clone();
            
        if(parents) {

            Node nodeParent = node.getParent();

            Node cloneParent;

            if(nodeParent == null) {
                cloneParent = null;                  
            }else{    
                cloneParent = deepClone(nodeParent, true, false);
            }

            clone.setParent(cloneParent);
            
            if(nodeParent != null) {
                
                NodeList nodeSiblings = nodeParent.getChildren();

                NodeList cloneSiblings = new NodeList();

                for(Node nodeSibling:nodeSiblings) {

                    Node cloneSibling;
                    
                    if(nodeSibling.equals(node)) {
                        cloneSibling = clone;
                    }else{
                        cloneSibling = deepClone(nodeSibling, false, true);
                    }    

                    cloneSiblings.add(cloneSibling);
                }

                if(cloneParent != null) {
                    cloneParent.setChildren(cloneSiblings);
                }
            }
        }

        if(children) {

            NodeList nodeChildren = node.getChildren();

            NodeList cloneChildren;
            
            if(nodeChildren == null) {
                
                cloneChildren = null;
                
            }else{

                cloneChildren = new NodeList();

                for(Node child:nodeChildren) {

                    Node childClone = deepClone(child, false, true);

                    childClone.setParent(clone);

                    cloneChildren.add(childClone);
                }
            }
            
            clone.setChildren(cloneChildren);
        }
        
        return clone;
    }
    
    public static void insertBefore(Node node, Node toInsert, NodeFilter filter) {

        if(filter != null && filter.accept(node)) {
Log.getInstance().log(Level.FINER, "Accepted: {0}", NodeUtil.class, node);
        
            insertBefore(node, toInsert);
            
        }else{
            
Log.getInstance().log(Level.FINER, "Rejected: {0}", NodeUtil.class, node);
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
            NodeList update = new NodeList();
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
