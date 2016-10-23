package com.bc.webdatex.locator;

import com.bc.util.XLogger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

/**
 * @(#)TransverseSet.java   26-Apr-2014 16:34:19
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * <p>
 * This implementation creates a Transverse containing the minimum number
 * of parts required. To achieve this it does two things:
 * </p>
 * <p>
 * 1. It starts parsing at the last element in the list (other than the 
 * target node) which has an id attribute. It removes all the elements 
 * before the last element with an id attribute.
 * </p>
 * 2. It removes all elements that don't match the last element
 * in each horizontal part of a transverse.
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.2
 */
public class TransverseSet extends TransverseList {
    
    public TransverseSet() { }

    /**
     * @param transverse 
     * @see #setTransverse(java.lang.String) 
     */
    public TransverseSet(String transverse) { 
        super(transverse);
    }

    /**
     * @param verticalTransverse 
     * @see #setTransverse(java.lang.Object[]) 
     */
    public TransverseSet(Object [] verticalTransverse) { 
        
        super(verticalTransverse);
    }

    @Override
    protected void initArray(Object [] yTransverse) {
        
        List<String> [] output = new List[yTransverse.length];
        
        int lastIndexWithID = -1;
        
        for(int i=0; i<yTransverse.length; i++) {
            
            List<String> xTransverse = this.getHorizontalTransverse(yTransverse[i]);
            
            output[i] = xTransverse;
            
            for(String tagID:xTransverse) {
                if(this.hasIDAttribute(tagID)) {
                    lastIndexWithID = i;
                    break;
                }
            }
        }
        
//    Starting at the last element in the list (other than the 
//    target node) which has an id attribute. Remove all the elements 
//    before the last element with an id attribute.
         
        final int indexOfTarget = output.length - 1; 
        
        if(lastIndexWithID > 0 && lastIndexWithID < indexOfTarget) {
            output = Arrays.copyOfRange(output, lastIndexWithID, output.length);
        }
        
XLogger.getInstance().log(Level.FINER, "Input: {0}, output: {1}",
        this.getClass(), Arrays.toString(yTransverse), 
        output == null ? null : Arrays.toString(output));

        this.setBackingArray(output);
    }
    
    protected boolean hasIDAttribute(String tagID) {
        tagID = tagID.toLowerCase();
//@todo this commented logic is less strict        
//        return tagID.contains(" id=");
        return tagID.contains(" id=\"") || tagID.contains(" id=\'");
    }

    
    @Override
    protected List<String> getHorizontalTransverse(Object transversePart) {

        List<String> horizontalTransverse = super.getHorizontalTransverse(transversePart);

        // Remove all elements that don't match the last element
        //
        String lastElement = horizontalTransverse.get(horizontalTransverse.size()-1);

        String a = lastElement.split("\\s")[0];

XLogger.getInstance().log(Level.FINER, "Horizontal transverse: {0}\nLast element: {1}, to compare: {2}",
        this.getClass(), horizontalTransverse, lastElement, a);
        
        Iterator<String> iter = horizontalTransverse.iterator();

        while(iter.hasNext()) {

            String element = iter.next();

            if(!iter.hasNext()) { 
                // We are at the last element, the TARGET element,
                // which must not be removed.
                break;
            }

            // From node <SPAN id="row"> extract <SPAN
            // We want to compare tag names
            String b = element.split("\\s")[0];

            boolean toRemove = !a.equalsIgnoreCase(b);

XLogger.getInstance().log(Level.FINER, "To remove: {0}, Node {1} does not match {2}",
this.getClass(), toRemove, element, lastElement);

            if(toRemove) {
                iter.remove();
            }
        }

XLogger.getInstance().log(Level.FINER, "Horizontal transverse: {0}",
        this.getClass(), horizontalTransverse);
        
        return horizontalTransverse;
    }
}
