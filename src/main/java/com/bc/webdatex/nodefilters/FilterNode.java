/*
 * Copyright 2018 NUROX Ltd.
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

package com.bc.webdatex.nodefilters;

import com.bc.nodelocator.ConfigName;
import com.bc.nodelocator.NodeTypes;
import com.bc.util.StringArrayUtils;
import com.bc.webdatex.util.Util;
import org.htmlparser.Node;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 16, 2018 9:19:10 PM
 */
public class FilterNode {

    public boolean execute(ConfigName filterType, String [] nodeIds, Node node) {
        boolean accept;
        switch(filterType) {
            case nodeTypesToAccept:
                 nodeTypesToReject:
                 accept = this.acceptNodeType(filterType, nodeIds, node);
                 break;
            case nodesToAccept:
                 nodesToReject:
                 accept = this.acceptNodeName(filterType, nodeIds, node);
                 break;
            default:    
                throw new IllegalArgumentException("Unexpected "+filterType.getClass().getName()+": "+filterType);
        }
        return accept;    
    }
    
    private boolean acceptNodeName(ConfigName filterType, String [] nodeNames, Node node) {

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
                throw new IllegalArgumentException("Unexpected "+filterType.getClass().getName()+": "+filterType);
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
                throw new IllegalArgumentException("Unexpected "+filterType.getClass().getName()+": "+filterType);
        }

        return accept;
    }    

    private boolean acceptNodeType(ConfigName filterType, String [] nodeTypes, Node node) {

        String nodeID = null;

        switch(filterType) {
            case nodeTypesToReject:
            case nodeTypesToAccept:   
                if(node instanceof Tag) {
                    nodeID = NodeTypes.TAG;
                }else if (node instanceof Text) {
                    nodeID = NodeTypes.TEXT;
                }else if (node instanceof Remark) {
                    nodeID = NodeTypes.REMARK;
                }else{
                    StringBuilder nodeStr = Util.appendTag(node, new StringBuilder(), false, -1);
                    throw new IllegalArgumentException(nodeStr.toString());
                }                
                break;
            default:    
                throw new IllegalArgumentException("Unexpected "+filterType.getClass().getName()+": "+filterType);
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
                throw new IllegalArgumentException("Unexpected "+filterType.getClass().getName()+": "+filterType);
        }

        return accept;
    }    
}
