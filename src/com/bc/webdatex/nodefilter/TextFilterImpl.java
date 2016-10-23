package com.bc.webdatex.nodefilter;

import com.bc.util.StringArrayUtils;
import com.bc.util.XLogger;
import java.util.Arrays;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.Remark;
import org.htmlparser.Text;


/**
 * @(#)TextFilter.java   01-Oct-2015 13:02:11
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
public class TextFilterImpl implements TextFilter {

    private String id;
    
    private String [] textToReject;
    
    private String [] textToAccept;
            
    @Override
    public boolean accept(Node node) {

        String text;

        if (node instanceof Text) {
            text = node.getText();
        }else if (node instanceof Remark) {
            text = node.getText();
        }else{
            text = null;
        }
        
        boolean output;

        boolean reject = false;
        boolean accept = true;

        if(text != null && !text.trim().isEmpty()) {
            
            output = !(reject = this.isReject(text)) && (accept = this.isAccept(text));
            
//if("targetNode5".equals(id)) System.out.println("- - - - - - - - - - Reject: "+reject+", accept: "+accept);
    
        }else{
            
            output = false;
        }
        
XLogger.getInstance().log(Level.FINER, "Accept: {0}, Text: {1}", this.getClass(), output, text);                

        return output;
    } 
    
    private boolean isReject(String text) {
        boolean reject = textToReject != null && textToReject.length != 0 &&
        StringArrayUtils.matches(textToReject, text, StringArrayUtils.MatchType.CONTAINS);
        return reject;
    }
    
    private boolean isAccept(String text) {
        boolean accept = textToAccept == null || textToAccept.length == 0 ||
                StringArrayUtils.matches(textToAccept, text, StringArrayUtils.MatchType.CONTAINS);
        return accept;
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
    public String[] getTextToReject() {
        return textToReject;
    }

    @Override
    public void setTextToReject(String[] textToReject) {
        this.textToReject = textToReject;
    }

    @Override
    public String[] getTextToAccept() {
        return textToAccept;
    }

    @Override
    public void setTextToAccept(String[] textToAccept) {
        this.textToAccept = textToAccept;
    }
    
    @Override
    public String toString() {
        return id + "-"+this.getClass().getSimpleName() +
        ". To reject: "+(textToReject==null?null:Arrays.toString(textToReject)) +
        ", to accept: "+(textToAccept==null?null:Arrays.toString(textToAccept));
    }
}
