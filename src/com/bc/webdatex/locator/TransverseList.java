package com.bc.webdatex.locator;

import com.bc.util.XLogger;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * @(#)TransverseList.java   25-Mar-2014 08:59:31
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.2
 */
public class TransverseList extends AbstractList<List<String>> {

    private List<String> [] backingArray; 
    
//    public static void main(String [] args) {
//        String s = "[\"<HTML>\",\"<BODY>\",\"<div id=\"Main\">\",\"<div id=\"Contents\">\",\"<div id=\"BannerAdvert\"> <script language=\"javascript\"> <h1>\"]";
//s = "[\"<HTML>\",\"<BODY>\",\"<div id=\"Main\">\",\"<div id=\"Contents\">\",\"<div id=\"OverviewPanel\" class=\"tabPanel active\">\",\"<div id=\"CarPicture\"> <div id=\"Price\"> <br /> <div id=\"ContactInfo\"> <div id=\"CarDescription\">\"]";        
//        TransverseList tl = new TransverseList();
//        String [] v0 = tl.getVerticalTransverse(s);
//        String [] v1 = tl.getVerticalTransverseOld(s);
//for(int i=0; i<v0.length; i++) {
//System.out.println("Element("+i+") equals: "+v0[i].equals(v1[i]));
//}
//    }
    
    public TransverseList() { }

    /**
     * @param transverse 
     * @see #setTransverse(java.lang.String) 
     */
    public TransverseList(String transverse) { 
        
        TransverseList.this.setTransverse(transverse);
    }

    /**
     * @param verticalTransverse 
     * @see #setTransverse(java.lang.Object[]) 
     */
    public TransverseList(Object [] verticalTransverse) { 
        
        TransverseList.this.setTransverse(verticalTransverse);
    }
    
    @Override
    public List<String> get(int index) {
    
        return backingArray[index];
    }
    
    @Override
    public int size() {
        
        return backingArray.length;
    }
    
    private void initArray(String s) {
        
        // ["<DIV>","<DIV>","<SPAN>"]
        String [] x = this.getVerticalTransverse(s);

        this.initArray(x);
    }
    
    protected void initArray(Object [] verticalTransverse) {
        
XLogger.getInstance().log(Level.FINER, "Vertical path: {0}", 
this.getClass(), verticalTransverse==null?null:Arrays.toString(verticalTransverse));        
        
        List<String> [] output = new List[verticalTransverse.length];
        
        for(int i=0; i<verticalTransverse.length; i++) {
    
            output[i] = this.getHorizontalTransverse(verticalTransverse[i]);
            
XLogger.getInstance().log(Level.FINER, "Index: {0}, horizontal path: {1}", 
        this.getClass(), i, output[i]);        
            
        }
        
        this.backingArray = output;
    }

    protected String[] getVerticalTransverse(String transversePart) {

        // Input format ["<DIV>","<DIV>","<SPAN>"]
//@todo what of spaces between > and , etc
        String [] temp = transversePart.split(">\",\"<");
        
        if(temp.length == 0) {
            throw new IllegalArgumentException("Wrong format: "+transversePart);
        }
        
        StringBuilder tempBuilder = null;
        
        for(int i=0; i<temp.length; i++) {
            
            String x = temp[i].trim();

            if(i == 0) {
                if(x.startsWith("[\"<")) {
                    x = x.substring(3);
                }
            }
            
            if(i == (temp.length - 1)) {
                if(x.endsWith(">\"]")) {
                    x = x.substring(0, x.length()-3);
                }
            }
            
            if(tempBuilder == null) {
                tempBuilder = new StringBuilder();
            }else{
                tempBuilder.setLength(0);
            }        
            
            temp[i] = tempBuilder.append('<').append(x).append('>').toString();
        }
XLogger.getInstance().log(Level.FINER, "Vertical. Input: {0}\nOutput:{1}", 
this.getClass(), transversePart, Arrays.toString(temp));        
        return temp;
    }
    
    protected List<String> getHorizontalTransverse(Object transversePart) {
        
        // This is of format <div> <span id="x"> <br/>
        // We can't split(\\s) because of the space in the tag with attributes
        //
//@todo what of spaces between > and <
        String [] temp = transversePart.toString().split(">\\s<");
        
        if(temp.length == 0) {
            throw new IllegalArgumentException("Wrong format: "+transversePart);
        }
        
        for(int i=0; i<temp.length; i++) {
            
            String x = temp[i].trim();
            
            if(!x.startsWith("<")) {
                x = "<" + x;
            }
            
            if(!x.endsWith(">")) {
                x = x + ">";
            }
            
            temp[i] = x;
        }
        
        List output = new ArrayList(Arrays.asList(temp));
        
XLogger.getInstance().log(Level.FINER, "Horizontal. Input: {0}\nOutput:{1}", 
this.getClass(), transversePart, output);        
        
        return output;
    }

    /**
     * <b>Expected format:</b> ["<DIV>","<UL>","<LI> <LI>","<DIV>"]
     * <br/><br/>
     * (<tt>,</tt>) separates parent from child, while space (<tt>&nbsp;</tt>)
     * separates siblings.
     * @param transverse 
     */
    public void setTransverse(Object [] verticalTransverse) {
        this.initArray(verticalTransverse);
    }

    /**
     * <b>Expected format:</b> ["<DIV>","<UL>","<LI> <LI>","<DIV>"]
     * <br/><br/>
     * (<tt>,</tt>) separates parent from child, while space (<tt>&nbsp;</tt>)
     * separates siblings.
     * @param transverse 
     */
    public void setTransverse(String transverse) {
        this.initArray(transverse);
    }

    protected void setBackingArray(List<String>[] backingArray) {
        this.backingArray = backingArray;
    }

    public List<String>[] getBackingArray() {
        return backingArray;
    }
}
