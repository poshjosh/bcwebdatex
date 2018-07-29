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

package com.bc.webdatex.functions;

import java.io.Serializable;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import org.htmlparser.Node;
import org.htmlparser.util.NodeList;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 27, 2018 9:17:08 PM
 */
public class FindFirstNode implements BiFunction<List<Node>, Predicate<Node>, Node>, Serializable {

    @Override
    public Node apply(List<Node> nodeList, Predicate<Node> nodeTest) {
        return this.apply(nodeList, nodeTest, Integer.MAX_VALUE);
    }
    
    public Node apply(List<Node> nodeList, Predicate<Node> nodeTest, int depth) {
//        NodeList nodes = nodeList.extractAllNodesThatMatch(nodeTest, true);        
        Node output = null;
        for (Node node : nodeList) {
            if (nodeTest.test(node)) {
                output = node;
                break;
            }
            if(depth <= 0) {
                continue;
            }    
            NodeList children = node.getChildren();
            if (null != children) {
                output = apply(children, nodeTest, depth - 1);
                if(output != null) {
                    break;
                }
            }    
        }
        return output;
    }
}
