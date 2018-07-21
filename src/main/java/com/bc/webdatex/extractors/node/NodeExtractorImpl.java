package com.bc.webdatex.extractors.node;

import com.bc.webdatex.context.NodeExtractorConfig;
import com.bc.webdatex.nodefilters.NodeVisitingFilter;
import com.bc.webdatex.nodefilters.NodesFilter;
import com.bc.util.StringArrayUtils;
import com.bc.webdatex.util.Util;
import com.bc.webdatex.nodefilters.NodeVisitingFilterImpl;
import com.bc.webdatex.nodefilters.TextFilter;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.util.NodeList;
import org.htmlparser.visitors.NodeVisitorImpl;
import org.htmlparser.util.NodeListImpl;

/**
 * @(#)DataExtractor.java   29-Sep-2015 14:17:51
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
public class NodeExtractorImpl extends NodeVisitorImpl implements NodeExtractor {

    private static final Logger logger = Logger.getLogger(NodeExtractorImpl.class.getName());
    
    public static final String USE_DEFAULT = "";
    
    private boolean concatenateMultipleExtracts;
    
    private boolean enabled;
    
    private boolean acceptScripts;
    
    private boolean finishedParsing;
    
    private boolean replaceNonBreakingSpace;
    
    private Object id;
    
    private String separator;

    private final NodeList remainginEndTags;
    
    private StringBuilder extract;
    
    private String [] attributesToExtract;
    
    private final AttributesExtractor attributesExtractor;
    
    private final NodeVisitingFilter nodeVisitingFilter;

    public NodeExtractorImpl(Object id, NodeExtractorConfig config) {
        this(id, config, (tag, attrs) -> new String[0], 0.0f, false);
    }
    
    public NodeExtractorImpl(Object id, NodeExtractorConfig config, AttributesExtractor ae, float tolerance, boolean greedy) {
        this.id = Objects.requireNonNull(id);
        this.attributesExtractor = Objects.requireNonNull(ae);
        this.acceptScripts = false;
        this.concatenateMultipleExtracts = true;
        this.enabled = true;
        this.extract = new StringBuilder();
        this.remainginEndTags = new NodeListImpl();
        this.separator = USE_DEFAULT;
        this.attributesToExtract = config.getAttributesToExtract(id);
        this.concatenateMultipleExtracts = config.isConcatenateMultipleExtracts(id, false);

        this.nodeVisitingFilter = new NodeVisitingFilterImpl(id, config, tolerance, greedy);

        this.replaceNonBreakingSpace = config.isReplaceNonBreakingSpace(id, false);
    }
    
    @Override
    public void reset() {
        
// I replaced this with the line below        
//        if(!this.isEnabled()) return;
        this.enabled = true;
        
        this.finishedParsing = false;
        this.remainginEndTags.removeAll();
        this.extract = new StringBuilder();
        this.nodeVisitingFilter.reset();
    }

    @Override
    public void beginParsing() {
        
        if(this.separator == USE_DEFAULT) {
            
            final String [] nodeTypes = this.getNodesFilter() == null ? null : this.getNodesFilter().getNodeTypesToAccept();
            
            if(nodeTypes != null && StringArrayUtils.matches(nodeTypes, "tag", StringArrayUtils.MatchType.EQUALS_IGNORE_CASE)) {
                this.separator = "<BR/><BR/>\n";
            }else{
                // This may seem OK but separators should not add any visible meaning
                // to the extract. For this reason space chars are recommended.
                // <BR/> etc for html
                // ' ', '\n', '\t' etc for text
//                this.separator = ". ";
                this.separator = " ";
            }
        }
        
    }

    @Override
    public void finishedParsing() {
        
//        if(!this.isEnabled()) return; BAD BAD BAD
        
        if(this.isFinishedParsing()) return;
        
        this.finishedParsing = true;
        
        boolean hasExtract = extract.length() > 0;
        
        if(hasExtract) {
            
            // Close unclosed CompositeTags
            //
            if(this.remainginEndTags.size() > 0) {
                
                logger.finer(() -> this.getId()+"-Extractor, remaining CompositeTags: " + this.remainginEndTags.toHtml());
                
                extract.append(this.remainginEndTags.toHtml());
            }
            
            extract = new StringBuilder().append(this.format(extract));
            
            logger.fine(() -> this.getId()+"-Extractor, Extract: "+this.extract); 
        }  
    }
    
    @Override
    public void visitTag(Tag tag) {

final boolean LOG = false; //"targetNode6".equals(id);// && tag.toTagHtml().startsWith("<meta"); 
//if(LOG) System.out.println(this.getClass().getName()+"#visitTag. "+tag.toTagHtml());
        
        if(!acceptScripts && tag.getTagName().equalsIgnoreCase("SCRIPT")) {
            this.setEnabled(false);
        }
        
        if(!this.isEnabled()) return;
        
        final String html = tag.toTagHtml(); 
        
        logger.finer(() -> this.getId()+"-Extractor.visitTag: " + html);

        boolean attributesOnly = this.isExtractOnlyAttributes();
        
  
//if(LOG) System.out.println("================== #visitTag(Tag): "+html);        
//if(LOG) System.out.println("================== Extract only attributes "+attributesOnly);        
        // This comes before filtering
        if(this.shouldAppendSeparator(tag)) {
            this.extract.append(separator);
        }
        
        if(!nodeVisitingFilter.acceptTag(tag)) {
           return;
        }
        
        logger.finer(() -> this.getId()+"-Extractor Extracting: "+html);

//XLogger.getInstance().log(Level.FINER, "Attributes to extract: {0}", this.getClass(), 
//(this.getAttributesToExtract()==null?null:Arrays.toString(this.getAttributesToExtract())));

        if(this.shouldAppendEndTag(tag)) {
            this.remainginEndTags.add(tag.getEndTag());
        }
        
//if(LOG) System.out.println("================== ArributesToAccept "+(this.attributesToAccept==null?null:Arrays.toString(this.attributesToAccept)));        
        
        final String htmlUpdated = tag.toTagHtml();
        
//if(LOG) System.out.println("================== After keeping attributes to accept, tag: "+html);                    
            
if(LOG) System.out.println(this.getClass().getName()+". APPENDING attributes only: " + attributesOnly);
        
        if(attributesOnly) {

            final String [] extractedAttributes = this.attributesExtractor.extract(tag, this.attributesToExtract);
            
if(LOG) System.out.println("================== Extracted "+Arrays.toString(extractedAttributes)+", from: "+html);

            logger.fine(() -> MessageFormat.format("Extracted attributes: {0} from tag: {1}",
                    Arrays.toString(extractedAttributes), html));

            for(String attrVal : extractedAttributes) {
                extract.append(attrVal).append(' ');
            }    
            
        }else{
            
            extract.append(htmlUpdated);
        }
    }

    @Override
    public void visitEndTag(Tag tag) {

//if("targetNode0".equals(id)) System.out.println(this.getClass().getName()+"#visitEndTag. "+tag.getRawTagName());

        if(!this.acceptScripts && !this.isEnabled() && tag.getTagName().equalsIgnoreCase("SCRIPT")) {
            this.setEnabled(true);
        }
        
        if(!this.isEnabled()) return;

        // Order of method call important
        
        super.visitEndTag(tag);
        
        if(!nodeVisitingFilter.acceptEndTag(tag)) {
            return;
        }

        if(this.isExtractOnlyAttributes()) {
            return;
        }

        this.remainginEndTags.remove(tag);
        
        logger.finer(() -> this.getId()+"-Extractor Extracting: "+tag.toTagHtml());

//if("targetNode0".equals(id)) System.out.println(this.getClass().getName()+". APPENDING = = = = = = = = = = = = = = = ");
        
        extract.append('<').append('/').append(tag.getTagName()).append('>');
    }

    @Override
    public void visitStringNode(Text node) {

final boolean LOG = false; //"targetNode5".equals(id);// && tag.toTagHtml().startsWith("<meta"); 
if(LOG) System.out.println(this.getClass().getName()+"#visitStringNode(Text) Node: "+Util.toString(node, 100));

        if(!this.isEnabled()) return;
        
        // Order of method call important
        
        super.visitStringNode(node);
        
        if(!nodeVisitingFilter.acceptStringNode(node)) {
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
            extract.append(" ");
        }
        
        final String logRef = text;
        logger.finer(() -> this.getId()+"-Extractor Extracting: "+logRef);

if(LOG) System.out.println(this.getClass().getName()+". APPENDING text: "+text);

        extract.append(text);
    }

    @Override
    public void visitRemarkNode(Remark remark) {
        
        if(!this.isEnabled()) return;
        
        super.visitRemarkNode(remark);
        
        // We don't extract comments
    }

    protected Object format(Object o) {
        return o;
    }
    
    protected boolean shouldAppendSpace() {
        boolean shouldAppendSpace;
        if(this.extract.length() > 0) {
            final char last = this.extract.charAt(extract.length()-1);
            shouldAppendSpace = !Character.isSpaceChar(last);
        }else{
            shouldAppendSpace = false;
        }
        return shouldAppendSpace;
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
    
    public boolean isExtractOnlyAttributes() {
        return this.attributesExtractor != null && this.attributesToExtract != null && this.attributesToExtract.length != 0;
    }
    
    @Override
    public boolean isFinishedParsing() {
        return finishedParsing;
    }

    @Override
    public StringBuilder getExtract() {
        return extract;
    }
    
//    private NodeVisitingFilter initNodeVisitingFilter() {
//        if(this.nodeVisitingFilter == null) {
//            this.nodeVisitingFilter = new NodeVisitingFilterImpl(this.id, null);
//        }
//        return this.nodeVisitingFilter;
//    }

    public String[] getAttributesToExtract() {
        return this.attributesToExtract;
    }

    public void setAttributesToExtract(String[] attributesToExtract) {
        this.attributesToExtract = attributesToExtract;
    }
    
    @Override
    public NodeVisitingFilter getFilter() {
        return nodeVisitingFilter;
    }

    public String [] getTextToReject() {
        final TextFilter textFilter = this.getTextFilter();
        return textFilter == null ? null : textFilter.getTextToReject();
    }

    public String [] getTextToAccept() {
        final TextFilter textFilter = this.getTextFilter();
        return textFilter == null ? null : textFilter.getTextToAccept();
    }
    
    @Override
    public NodesFilter getNodesFilter() {
        return this.nodeVisitingFilter == null ? null : this.nodeVisitingFilter.getNodesFilter();
    }

    @Override
    public TextFilter getTextFilter() {
        return this.nodeVisitingFilter == null ? null : this.nodeVisitingFilter.getTextFilter();
    }

    @Override
    public boolean isConcatenateMultipleExtracts() {
        return concatenateMultipleExtracts;
    }

//    @Override
    public void setConcatenateMultipleExtracts(boolean concatenateMultipleExtracts) {
        this.concatenateMultipleExtracts = concatenateMultipleExtracts;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isAcceptScripts() {
        return acceptScripts;
    }

//    @Override
    public void setAcceptScripts(boolean acceptScripts) {
        this.acceptScripts = acceptScripts;
    }

    @Override
    public boolean isReplaceNonBreakingSpace() {
        return replaceNonBreakingSpace;
    }

//    @Override
    public void setReplaceNonBreakingSpace(boolean replaceNonBreakingSpace) {
        this.replaceNonBreakingSpace = replaceNonBreakingSpace;
    }

    @Override
    public Object getId() {
        return id;
    }

//    @Override
    public void setId(Object id) {
        this.id = id;
    }

    @Override
    public String getSeparator() {
        return separator;
    }

//    @Override
    public void setSeparator(String separator) {
        this.separator = separator;
    }
}
/**
 * 
    public boolean isExtractAttributes() {
        return this.getAttributesToExtract() != null && this.getAttributesToExtract().length != 0;
    }

    private TagLocatorImpl initTagLocator() {
        if(tagLocator == null) {
            tagLocator = new TagLocatorImpl();
            tagLocator.setId(this.getId());
            
        }
        return tagLocator;
    }
    
    public TagLocatorImpl getTagLocator() {
        return tagLocator;
    }

    public void setTagLocator(TagLocatorImpl tagLocator) {
        this.tagLocator = tagLocator;
    }

    public boolean isTagNameOnly() {
        return this.tagLocator == null ? false : this.tagLocator.isTagNameOnly();
    }

    public void setTagNameOnly(boolean tagNameOnly) {
        this.initTagLocator().setTagNameOnly(tagNameOnly);
    }

    public float getTolerance() {
        return this.tagLocator == null ? -1.0f : this.tagLocator.getTolerance();
    }

    public void setTolerance(float tolerance) {
        this.initTagLocator().setTolerance(tolerance);
    }

    public void setPath(Object [] path) {
        this.tagLocator.setPath(path);
    }

    public List<String>[] getTransverse() {
        return this.tagLocator == null ? null : this.tagLocator.getTransverse();
    }

    public void setTransverse(List<String>[] transverse) {
        this.initTagLocator().setTransverse(transverse);
    }
 * 
 */