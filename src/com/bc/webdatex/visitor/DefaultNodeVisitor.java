package com.bc.webdatex.visitor;

import com.bc.util.XLogger;
import com.bc.webdatex.bounds.HasBounds;
import com.bc.webdatex.extractor.node.AttributesExtractor;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.htmlparser.Attribute;
import org.htmlparser.NodeFilter;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.util.NodeList;
import org.htmlparser.visitors.AbstractNodeVisitor;


/**
 * @(#)DefaultNodeVisitor.java   24-Sep-2015 16:45:15
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */

/**
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.0
 */
public class DefaultNodeVisitor 
        extends AbstractNodeVisitor 
        implements HasBounds, Serializable {
    
    private boolean enabled;
    
    private boolean acceptScripts;
    
    private boolean finishedParsing;
    
    private boolean extractAttributes;
    
    private boolean replaceNonBreakingSpace;
    
    private String separator;

    private final String id;
    
    private NodeList remainginEndTags;
    
    private String [] columns;
    
    private StringBuilder extract;
    
    private String [] attributesToAccept; 
    
    private String [] nodesToRetainAttributes;
    
    private AttributesExtractor attributesExtractor;
    
    private DefaultVisitingFilter filter;
    
    private NodeFilter targetFilter;
    
    public DefaultNodeVisitor(DefaultVisitingFilter filter) {
        
        this.enabled = true;
        
        this.id = filter.getId();
        
        if(this.id == null) {
            // id is used below
            throw new NullPointerException();
        }
                
        this.filter = filter;
        
        this.extract = new StringBuilder();
        
        this.remainginEndTags = new NodeList();
    }
    
    @Override
    public void reset() {
        
        if(!this.isEnabled()) return;
        
        this.finishedParsing = false;
        this.remainginEndTags.removeAll();
        this.extract = new StringBuilder();
        this.filter.reset();
    }

    @Override
    public void finishedParsing() {
        
XLogger.getInstance().log(Level.INFO, "{0}, Extract: {1}", this.getClass(), id, extract);
        if(!this.isEnabled()) return;
        
        if(this.isFinishedParsing()) return;
        
        this.finishedParsing = true;
        
        boolean hasExtract = extract.length() > 0;
        
        if(hasExtract) {
            
            // Close unclosed CompositeTags
            //
            if(this.remainginEndTags.size() > 0) {
XLogger.getInstance().log(Level.FINER, "{0}-Extractor, remaining compositeTags: {1}", 
            this.getClass(), this.id, this.remainginEndTags.toHtml());            
                extract.append(this.remainginEndTags.toHtml());
            }
            
            extract = new StringBuilder().append(this.format(extract));
            
XLogger.getInstance().log(Level.FINE, "{0}-Extractor, Extract: {1}", 
        this.getClass(), this.id, this.extract); 
        }    
    }
    
    @Override
    public void visitTag(Tag tag) {

        if(!acceptScripts && tag.getTagName().equalsIgnoreCase("SCRIPT")) {
            this.setEnabled(false);
        }
        
        if(!this.isEnabled()) return;
        
XLogger.getInstance().log(Level.FINER, "{0}-Extractor.visitTag: {1}", 
        this.getClass(), this.getId(), tag.toTagHtml());

        boolean attributesOnly = this.isExtractOnlyAttributes();
        
        // This comes before filtering
        if(this.shouldAppendSeparator(tag)) {
            this.extract.append(separator);
        }
        
        if(!filter.acceptTag(tag)) {
           return;
        }
        
XLogger.getInstance().log(Level.FINER, "{0}-Extractor Extracting: {1}", 
        this.getClass(), this.getId(), tag.toTagHtml());
        
        if(this.shouldAppendEndTag(tag)) {
            this.remainginEndTags.add(tag.getEndTag());
        }
        
        boolean keepAttributes = false;
        for(String node:this.nodesToRetainAttributes) {
            keepAttributes = node.equals(tag.getTagName());
            if(keepAttributes) break;
        }
        
        if(keepAttributes) {
            
            this.keepAttributesToAccept(tag);
        }
            
        if(attributesOnly) {

            String attrVal = this.attributesExtractor.extract(tag);
            
XLogger.getInstance().log(Level.FINE, "Extracted attributes: {0} from tag: {1}",
this.getClass(), attrVal, tag.toTagHtml());

            if(attrVal != null) {
                extract.append(attrVal);
            }
            
        }else if(keepAttributes) {
            
            extract.append(tag.toTagHtml());
            
        }else{
            
            extract.append('<').append(tag.getTagName()).append('>');
        }
    }

    @Override
    public void visitEndTag(Tag tag) {

        if(!this.acceptScripts && !this.isEnabled() && tag.getTagName().equalsIgnoreCase("SCRIPT")) {
            this.setEnabled(true);
        }
        
        if(!this.isEnabled()) return;
        
        // Order of method call important
        
        super.visitEndTag(tag);
        
        if(!filter.acceptEndTag(tag)) {
            return;
        }
        
        if(this.isExtractOnlyAttributes()) {
            return;
        }
        
        this.remainginEndTags.remove(tag);
        
XLogger.getInstance().log(Level.FINER, "{0}-Extractor Extracting: {1}", 
        this.getClass(), this.id, tag.toTagHtml());
        
        extract.append('<').append('/').append(tag.getTagName()).append('>');
    }

    @Override
    public void visitStringNode(Text node) {
        
        if(!this.isEnabled()) return;
        
        // Order of method call important
        
        super.visitStringNode(node);
        
        if(!filter.acceptStringNode(node)) {
            return;
        }

        if(this.isExtractOnlyAttributes()) {
            return;
        }
        
        String text = node.getText();
//        text = Translate.decode(text);

        if(this.isReplaceNonBreakingSpace()) {
            // Replace non breaking space with ordinary space
            text = text.replace('\u00a0', ' ');
        }
        
        if(this.shouldAppendSpace()) {
            text = " " + text;
        }
        
XLogger.getInstance().log(Level.FINER, "{0}-Extractor Extracting: {1}", 
        this.getClass(), this.id, text);

        extract.append(text);
    }

    @Override
    public void visitRemarkNode(Remark remark) {
        
        if(!this.isEnabled()) return;
        
        super.visitRemarkNode(remark);
        
        // We don't extract comments
    }

    public Object format(Object o) { 
        return o;
    }
    
    protected boolean shouldAppendSpace() {
        if(this.extract.length() > 0) {
            char last = this.extract.charAt(extract.length()-1);
            return last != ' ';
        }
        return false;
    }
    
    protected boolean shouldAppendSeparator(Tag tag) {
        if(this.isExtractOnlyAttributes()) {
            return false;
        }
        boolean shouldAppend = false;
        if(tag.breaksFlow()) {
            if(this.extract.length() > 0) {
                shouldAppend = !this.extract.toString().endsWith(separator);
            }
        }
        return shouldAppend;
    }
    
    protected boolean shouldAppendEndTag(Tag tag) {
        if(this.isExtractOnlyAttributes()) {
            return false;
        }
        return tag.getEndTag() != null;        
    }

    private void keepAttributesToAccept(Tag tag) {
        
        if(attributesToAccept == null) {
            return;
        }
        
        ArrayList<String> remove = new ArrayList<>();
        
        List attributes = tag.getAttributes();

        for(Object oval:attributes) {

            Attribute attr = (Attribute)oval;
            
            if(!attr.isValued()) continue;

            String name = attr.getName();
            
            if(name != null && !this.contains(this.attributesToAccept, name)) {

                remove.add(name);
            }
        }
        
        for(String name:remove) {
            tag.removeAttribute(name);
        }
    }
    
    private boolean contains(String [] arr, String elem) {
        for(String e:arr) {
            if(e.equals(elem)) return true;
        }
        return false;
    }
    
    public boolean isExtractOnlyAttributes() {
        return this.extractAttributes && this.attributesExtractor != null;
    }

    @Override
    public boolean isDone() {
        return filter.isDone();
    }

    @Override
    public boolean isStarted() {
        return filter.isStarted();
    }
    
    public StringBuilder getExtract() {
        return extract;
    }

    public String getId() {
        return id;
    }
    
    public DefaultVisitingFilter getFilter() {
        return filter;
    }

    public boolean isFinishedParsing() {
        return finishedParsing;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public boolean isAcceptScripts() {
        return acceptScripts;
    }

    public void setAcceptScripts(boolean acceptScripts) {
        this.acceptScripts = acceptScripts;
    }

    public NodeFilter getTargetFilter() {
        return targetFilter;
    }

    public void setTargetFilter(NodeFilter targetFilter) {
        this.targetFilter = targetFilter;
    }

    public boolean isExtractAttributes() {
        return extractAttributes;
    }

    public void setExtractAttributes(boolean extractAttributes) {
        this.extractAttributes = extractAttributes;
    }

    public String[] getAttributesToAccept() {
        return attributesToAccept;
    }

    public void setAttributesToAccept(String[] attributesToAccept) {
        this.attributesToAccept = attributesToAccept;
    }

    public AttributesExtractor getAttributesExtractor() {
        return attributesExtractor;
    }

    public void setAttributesExtractor(AttributesExtractor attributesExtractor) {
        this.attributesExtractor = attributesExtractor;
    }

    public boolean isReplaceNonBreakingSpace() {
        return replaceNonBreakingSpace;
    }

    public void setReplaceNonBreakingSpace(boolean replaceNonBreakingSpace) {
        this.replaceNonBreakingSpace = replaceNonBreakingSpace;
    }

    public String[] getColumns() {
        return columns;
    }

    public void setColumns(String[] columns) {
        this.columns = columns;
    }

    public String[] getNodesToRetainAttributes() {
        return nodesToRetainAttributes;
    }

    public void setNodesToRetainAttributes(String[] nodesToRetainAttributes) {
        this.nodesToRetainAttributes = nodesToRetainAttributes;
    }
}
