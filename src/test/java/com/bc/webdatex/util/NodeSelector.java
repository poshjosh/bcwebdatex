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

import com.bc.webdatex.extractors.Extractor;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.beans.StringExtractingNodeVisitor;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * @author Chinomso Bassey Ikwuagwu on Nov 5, 2016 3:56:56 PM
 */
public class NodeSelector implements Extractor<List<Node>, Node> {

    private transient static final Logger LOG = Logger.getLogger(NodeSelector.class.getName());
    
    private final int maxDepth;
    
    private final int bufferSize;
    
    private final int minSize;
    
    private final NodeFilter filter;
    
    private int depth;

    public NodeSelector(NodeFilter filter, int bufferSize, int minSize) {
        
        this(filter, bufferSize, minSize, Integer.MAX_VALUE);
    }
    
    public NodeSelector(NodeFilter filter, int bufferSize, int minSize, int maxDepth) {
        this.maxDepth = maxDepth;
        this.minSize = minSize;
        this.bufferSize = bufferSize;
        this.filter = filter;
    }

    @Override
    public Node extract(List<Node> nodes, Node outputIfNone) {
        try{
            return this.select(nodes, outputIfNone);
        }catch(ParserException e) {
            LOG.log(Level.WARNING, null, e);
            return outputIfNone;
        }
    }
    
    public Node select(List<Node> nodes, Node outputIfNone) throws ParserException {
        
        this.depth = 0;

        return this.doSelect(nodes, outputIfNone);
    }

    private Node doSelect(List<Node> nodes, Node outputIfNone) throws ParserException {
        
        Objects.requireNonNull(nodes);
        
        Node ret = outputIfNone;
        int retLen = minSize;
        
        final StringExtractingNodeVisitor nodeVisitor = new StringExtractingNodeVisitor(bufferSize, bufferSize);
        
        for(Node node : nodes) {
            
            if(filter == null || filter.accept(node)) {
            
                NodeList children = node.getChildren();
                
                nodeVisitor.setCollapse(true);
                nodeVisitor.setLinks(false);
                nodeVisitor.setReplaceNonBreakingSpaces(true);
                
                if(children == null || children.isEmpty()) {
                    node.accept(nodeVisitor);
                }else{
                    children.visitAllNodesWith(nodeVisitor);
                }
                
                final String text = nodeVisitor.getStrings();
                
                nodeVisitor.reset();
                
                final int len = text == null ? 0 : text.length();
                
                final Level level = len > retLen ? Level.FINER : Level.FINEST;
                
                if(LOG.isLoggable(level)) {
                    LOG.log(level, "Text length. current: {0} > previous: {1}, node: {2}, text: {3} ", 
                            new Object[]{len, retLen, node, text}); 
                }
                
                if(len > retLen) {
                    
                    ret = node;
                    retLen = len;
                }
            }
        }

        if(LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "Text length: {0}, node: {1}", new Object[]{retLen, ret}); 
        }
        
        if(ret != outputIfNone && this.depth < maxDepth) {

            NodeList children = ret.getChildren();
            
            if(children != null && !children.isEmpty()) {
                
                LOG.log(Level.FINER, "Recursing {0} children", children.size()); 
            
                ++this.depth;
                
                ret = doSelect(children, ret);
            }
        }
        
        return ret;
    }
}
