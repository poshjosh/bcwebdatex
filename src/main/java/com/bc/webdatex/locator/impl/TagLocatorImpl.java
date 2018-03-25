package com.bc.webdatex.locator.impl;

import com.bc.webdatex.locator.PathToTransverse;
import com.bc.webdatex.locator.TagLocator;
import com.bc.webdatex.locator.TransverseNodeMatcher;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.htmlparser.Tag;
import org.htmlparser.visitors.AbstractNodeVisitor;

/**
 * @(#)TransverseVisitor.java   22-Sep-2015 20:16:58
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */

/**
 * <p>This class filters Tags based on a pre-defined transverse.</p>
 * <p>
 * When depicting Tag transverses, it is only necessary to indicate a
 * path through the (only one) target child of each container Tag.
 * </p>
 * <p>For example, given the html below:</p>
 * <pre>
 * &lt;div class=main>
 *   &lt;span>&lt;/span>
 *   &lt;div class=abc>
 *   &lt;/div> 
 *   &lt;div class=xyz>
 *     &lt;span id=target>&lt;/span>
 *   &lt;/div> 
 * &lt;/div>
 * </pre>
 * <p>The correct transverse would be:</p> 
 * <p>
 * &lt;DIV class=main>,&lt;DIV class=xyz>,&lt;SPAN id=target>
 * </p>
 * <p>
 * <p>
 * From the above, only one target child Tag was specified within each containing
 * (parent) Tag. (i.e siblings of the target child Tag are ignored)
 * </p>
 * <b>Note:</b>
 * All start Tags start at the first Tag in the specified transverse
 * must be visited for this Tag to work properly.
 * </p>
 * <p>
 * <b>Example.</b>
 * Given the HTML doc below:
 * </p>
 * <pre>
 * &lt;div>             ... 1
 *   &lt;div>           ... 1a
 *     &lt;span>        ... 1a_1 
 *     &lt;/span>
 *     &lt;span>        ... 1a_2
 *     &lt;/span>
 *   &lt;/div>
 * &lt;/div>
 * &lt;div>             ... 2
 *   &lt;p>
 *   &lt;/p>
 * &lt;/div>
 * &lt;div>             ... 3 
 *   &lt;div>           ... 3a 
 *     &lt;span>        ... 3a_1
 *     &lt;/span>
 *     &lt;span id=target>
 *     &lt;/span>
 *   &lt;/div>
 * &lt;/div>
 * </pre>
 * <p>
 * The correct transverse would be:
 * </p>
 * <p>
 * &lt;div>, &lt;div>, &lt;span> &lt;span id=target>
 * </p>
 * For the above input, the Filter would see div 1, 1a as matching the first
 * two elements of the transverse. This would mislead the filter down the wrong
 * path. To surmount this problem, the filter creates diversion Filters each 
 * time it has to descend down a vertical level and there are multiple siblings 
 * at the current level matching the expected transverse element.
 * In this case at div 1, the filter creates 2 alternate Filters for divs 2 and 3
 * respectively. These 3 Filters work concurrently until one has a perfect match.
 * In this case the filter transversing the vertical level beginning at div 3.
 * @author   chinomso bassey ikwuagwu
 * @version  2.0
 * @since    2.0
 */
public class TagLocatorImpl extends AbstractNodeVisitor implements TagLocator {
    
    private final String CLASS_TAG = TagLocatorImpl.class.getSimpleName();
    
    private final String id;

    private final List<String> [] transverse;
    
    private final TransverseNodeMatcher transverseNodeMatcher;
    
    /**
     * Represents if the last Tag processed by the filter was accepted
     */
    private boolean accepted;
    
    private int verticalIndex;
    
    private int horizontalIndex;
    
    private List<String> xAxis;
    
    private Tag target;
    
    public TagLocatorImpl(String id, Object [] path) {

        this(id, path, new TransverseNodeMatcherImpl());
    }
    
    public TagLocatorImpl(String id, Object [] path, TransverseNodeMatcher transverseNodeMatcher) {
        
        this(id, new PathToTransverse().toTransverse(path), transverseNodeMatcher);
    }

    public TagLocatorImpl(String id, List<String> [] transverse) {
        
        this(id, transverse, new TransverseNodeMatcherImpl());
    }
    
    public TagLocatorImpl(String id, List<String> [] transverse, 
            TransverseNodeMatcher transverseNodeMatcher) {

        this.id = id;
        
        this.transverse = Objects.requireNonNull(transverse);
        
        this.transverseNodeMatcher = transverseNodeMatcher;
    }
    
    @Override
    public void reset() {
        this.accepted = false;
        this.xAxis = null;
        this.target = null;
        this.verticalIndex = 0;
        this.horizontalIndex = 0;
    }
    
    /**
     * All start Tags start at the first Tag in the specified transverse
     * must be visited for this method to work properly
     * @param tag
     */
    @Override
    public void visitTag(Tag tag) {
        
        if(this.isFoundTarget()) { return;  }

        log(Level.FINER, "{0}-{1}. Tag: {2}", id, CLASS_TAG, tag.toTagHtml(), null);     
        
        acceptTag(tag);
    }

    protected boolean acceptTag(Tag tag) {
    
        if(tag.isEndTag()) {
            throw new UnsupportedOperationException("Only start tags are supported by this method");
        }

        if(!this.hasNext()) {
            return false;
        }

        accepted = false;
        
        final String tagID = this.getTagId(horizontalIndex, verticalIndex);
        
        if(tagID != null) {
            
            accepted = this.accept(tagID, tag);
        }
        
        final boolean moveForward = accepted;
        
        this.updatePosition(moveForward);

        final boolean proceed = this.isProceed();
        
        final boolean foundTarget = this.accepted && !this.hasNext();
        
        final String tagHtml = tag.toTagHtml();
        
        log(Level.FINER, "{0}-{1}. Expected: {2}, found: {3}", 
            id, CLASS_TAG, tagID, tagHtml);     

        if(foundTarget) {
            
            this.target = tag;
            
            log(Level.FINE, "{0}-{1}. Found target: {2}", 
                id, CLASS_TAG, tagHtml, null);     
        }else{
            
            log(Level.FINER, "{0}-{1}. Accepted: {2}, proceed: {3}", 
                id, CLASS_TAG, accepted, proceed);   
        }
            
        return proceed;
    }
    
    protected boolean accept(String tagID, Tag tag) {
        
        return this.matches(tagID, tag);
    }
    
    @Override
    public boolean isProceed() {
        
        return this.isLastTagAccepted() || this.hasNext();        
    }
    
    public boolean matches(String transverseNode, Tag tag) {
        
        return this.transverseNodeMatcher.matches(tag, transverseNode);
    }
    
    public boolean isTransversingXAxis() {
        return xAxis != null && xAxis.size() > 1;
    }
    
    private void updatePosition(boolean stepForward) {
        
        if(!this.hasNext()) {
            return;
        }
        
        if(this.isConditionTrue()) {

            if(stepForward) {

                ++horizontalIndex;

                if(horizontalIndex == xAxis.size()) {
                    horizontalIndex = 0;
                    ++verticalIndex;
                }
            }

        }else{

            if(stepForward) {

                ++verticalIndex;
                horizontalIndex = 0;
            }
        }
    }
    
    public boolean isConditionTrue() {
        return this.isTransversingXAxis();
    }
    
    public String getTagId() {
        
        return this.getTagID(horizontalIndex, verticalIndex);
    }
    
    public String getTagId(int x, int y) {
        
        String tagID;
        
        if(y >= this.transverse.length) {

            tagID = null;

        log(Level.FINER, "{0}-TagLocatorImpl. has next {1} @[{2}:{3}]", 
            id, this.hasNext(), x, y);        

        }else{

            tagID = this.next();
        }
        
        return tagID;
    }
    
    @Override
    public boolean hasNext() {
        boolean hasNext = this.hasNextVertical() && this.hasNextHorizontal();
        return hasNext;
    }
    
    @Override
    public String next() {
        return getTagID(horizontalIndex, verticalIndex);
    }
    
    @Override
    public void remove() {
        throw new UnsupportedOperationException("This iterator implementation is not editable");
    }
    
    private boolean hasNextVertical() {
        return this.verticalIndex < this.transverse.length;
    }
    
    private boolean hasNextHorizontal() {
        return this.horizontalIndex < (this.transverse[verticalIndex].size());
    }

    private String getTagID(int x, int y) {

        xAxis = transverse[y];
        
        log(Level.FINER, "{0}-TagLocatorImpl. @[{1}:{2}]  xAxis: {3}", id, x, y, xAxis);        

        if(x >= xAxis.size()) {

            throw new RuntimeException("Horizontal index: "+x+" >= "+xAxis.size()+" elements in Horizontal transverse: "+xAxis);

        }else{

            return xAxis.get(x);
        }
    }
    
    @Override
    public Tag getTarget() {
        return target;
    }

    @Override
    public boolean isFoundTarget() {
        return target != null;
    }
    
    @Override
    public boolean isLastTagAccepted() {
        return accepted;
    }

    @Override
    public int getVerticalIndex() {
        return verticalIndex;
    }

    @Override
    public int getHorizontalIndex() {
        return horizontalIndex;
    }
    
    /**
     * @return a copy of the transverse, which is the series of html tags leading
     * up to the target node.
     */
    @Override
    public List<String>[] getTransverse() {
        List<String> [] copy = new List[transverse.length];
        System.arraycopy(transverse, 0, copy, 0, transverse.length);
        return copy;
    }
    
    @Override
    public String getId() {
        return id;
    }

    public TransverseNodeMatcher getTransverseNodeMatcher() {
        return transverseNodeMatcher;
    }

    private void log(Level level, String msg, Object o1, Object o2, Object o3, Object o4) {
        final String className = this.getClass().getName();
        final Logger clsLogger = Logger.getLogger(className);
        Object [] params = null;
        if(clsLogger.isLoggable(level)) {
            params = new Object[]{o1, o2, o3, o4};
            clsLogger.logp(level, className, "", msg, params);
        }
        final boolean notLogged = params == null;
        if(notLogged) {
            final Logger idLogger = Logger.getLogger(id);
            if(idLogger.isLoggable(level)) {
                params = new Object[]{o1, o2, o3, o4};
                idLogger.logp(level, className, "", msg, params);
            }
        }
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder(CLASS_TAG);
        builder.append(". Id: ").append(id);
        builder.append(", has next: ").append(this.hasNext());
        builder.append(", transverse: ").append(Arrays.toString(this.transverse));        
        return builder.toString();
    }
}
    /**
     * *************************************************************************
     * @TODO TEST AND IMPLEMENT PROPERLY
     * *************************************************************************
    private boolean acceptGreedy(Node node) {
        
        Tag tag = (Tag)node;
        
        int originalX = this.horizontalIndex;
        int originalY = this.verticalIndex;

        boolean output;

        try{
            
            do {

                output = this.acceptTag(tag);

                if(!accept) {
                    boolean stepForward = true;
                    this.updatePosition(stepForward);
                }

            }while(!accept && hasNext());
        
        }finally{
            
            if(!accept) {
                this.horizontalIndex = originalX;
                this.verticalIndex = originalY;
            }
        }
        
        return output;
    }
     */
    



