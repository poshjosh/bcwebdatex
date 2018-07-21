package com.bc.webdatex.nodefilters;

import com.bc.util.StringArrayUtils;
import java.util.logging.Logger;
import java.util.Arrays;
import java.util.Objects;
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
    
    private transient static final Logger LOG = Logger.getLogger(TextFilterImpl.class.getName());

    private final Object id;
    
    private final String [] textToAccept;

    private final String [] textToReject;
    
    public TextFilterImpl(Object id, String[] textToAccept, String[] textToReject) {
        this.id = Objects.requireNonNull(id);
        this.textToAccept = textToAccept;
        this.textToReject = textToReject;
    }
            
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
        
        final boolean output;

        final boolean reject;
        boolean accept = true;

        if(text != null && !text.trim().isEmpty()) {
            
            output = !(reject = this.isReject(text)) && (accept = this.isAccept(text));
            
            if(LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "Reject: {0}, accept: {1}", new Object[]{reject, accept});
            }
    
        }else{
            
            output = false;
        }
        
        if(LOG.isLoggable(Level.FINER)){
            LOG.log(Level.FINER, "Accept: {0}, text: {1}", new Object[]{output,  text});
        }                

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
    
    public Object getId() {
        return id;
    }

    @Override
    public String[] getTextToReject() {
        return textToReject;
    }

    @Override
    public String[] getTextToAccept() {
        return textToAccept;
    }

    @Override
    public String toString() {
        return id + "-"+this.getClass().getSimpleName() +
        ". To reject: "+(textToReject==null?null:Arrays.toString(textToReject)) +
        ", to accept: "+(textToAccept==null?null:Arrays.toString(textToAccept));
    }
}
