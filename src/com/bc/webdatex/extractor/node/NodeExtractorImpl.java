package com.bc.webdatex.extractor.node;

import com.bc.webdatex.nodefilter.NodeVisitingFilter;
import com.bc.webdatex.nodefilter.NodesFilter;
import com.bc.util.StringArrayUtils;
import com.bc.webdatex.util.Util;
import com.bc.webdatex.locator.TagLocator;
import com.bc.webdatex.locator.impl.TagLocatorImpl;
import com.bc.webdatex.nodefilter.NodeVisitingFilterImpl;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import org.htmlparser.Attribute;
import org.htmlparser.NodeFilter;
import org.htmlparser.Remark;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.util.NodeList;
import org.htmlparser.visitors.AbstractNodeVisitor;

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
public class NodeExtractorImpl extends AbstractNodeVisitor implements NodeExtractor {

    private static final Logger logger = Logger.getLogger(NodeExtractorImpl.class.getName());
    
    public static final String USE_DEFAULT = "";
    
    private boolean concatenateMultipleExtracts;
    
    private boolean enabled;
    
    private boolean acceptScripts;
    
    private boolean finishedParsing;
    
    private boolean replaceNonBreakingSpace;
    
    private String id;
    
    private String separator;

    private final NodeList remainginEndTags;
    
    private StringBuilder extract;
    
    private String [] nodesToRetainAttributes;
    
    private String [] attributesToAccept; 
    
    private String [] attributesToExtract;
    
    private AttributesExtractor attributesExtractor;
    
    private NodeVisitingFilter nodeVisitingFilter;
    
    private Set<String> attributesToAcceptAll;

    public NodeExtractorImpl() {
        this(Long.toHexString(System.currentTimeMillis()));
    }
    
    public NodeExtractorImpl(String id) {
        this.id = Objects.requireNonNull(id);
        this.acceptScripts = false;
        this.concatenateMultipleExtracts = true;
        this.enabled = true;
        this.extract = new StringBuilder();
        this.remainginEndTags = new NodeList();
        this.separator = USE_DEFAULT;
    }
    
    public NodeExtractorImpl(NodeExtractorConfig config, String id) {
        this(id);
        this.attributesToAccept = config.getAttributesToAccept(id);
        this.attributesToExtract = config.getAttributesToExtract(id);
        this.concatenateMultipleExtracts = config.isConcatenateMultipleExtracts(id, false);

        this.nodesToRetainAttributes = config.getNodesToRetainAttributes(id); 
        this.setNodeTypesToAccept(config.getNodeTypesToAccept(id));
        this.setNodeTypesToReject(config.getNodeTypesToReject(id));
        this.setNodesToAccept(config.getNodesToAccept(id));
        this.setNodesToReject(config.getNodeToReject(id));

        this.setTagLocator(new TagLocatorImpl(id, config.getTransverse(id)));

        this.replaceNonBreakingSpace = config.isReplaceNonBreakingSpace(id, false);

        this.setTextToAccept(null);
        this.setTextToDisableOn(config.getTextToDisableOn(id));
        this.setTextToReject(config.getTextToReject(id));
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
        
        if(this.attributesToAcceptAll == null) {
            this.attributesToAcceptAll = new HashSet<>();
        }else{
            this.attributesToAcceptAll.clear();
        }
        if(this.attributesToAccept != null) {
            this.attributesToAcceptAll.addAll(Arrays.asList(attributesToAccept));
        }
        if(this.getAttributesToExtract() != null) {
            this.attributesToAcceptAll.addAll(Arrays.asList(this.getAttributesToExtract()));
        }

        this.attributesExtractor = new AttributesExtractorImpl(this.id);
        
        if(this.separator == USE_DEFAULT) {
            String [] nodeTypes = this.getNodeTypesToAccept();
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
        
        logger.finer(() -> this.getId()+"-Extractor.visitTag: "+tag.toTagHtml());

        boolean attributesOnly = this.isExtractOnlyAttributes();
        
  
//if(LOG) System.out.println("================== #visitTag(Tag): "+tag.toTagHtml());        
//if(LOG) System.out.println("================== Extract only attributes "+attributesOnly);        
        // This comes before filtering
        if(this.shouldAppendSeparator(tag)) {
            this.extract.append(separator);
        }
        
        if(!nodeVisitingFilter.acceptTag(tag)) {
           return;
        }
        
        logger.finer(() -> this.getId()+"-Extractor Extracting: "+tag.toTagHtml());

//XLogger.getInstance().log(Level.FINER, "Attributes to extract: {0}", this.getClass(), 
//(this.getAttributesToExtract()==null?null:Arrays.toString(this.getAttributesToExtract())));

        if(this.shouldAppendEndTag(tag)) {
            this.remainginEndTags.add(tag.getEndTag());
        }
        
        boolean keepAttributes = false;
//if(LOG) System.out.println("================== NodesToRetainAttributes "+(this.nodesToRetainAttributes==null?null:Arrays.toString(this.nodesToRetainAttributes)));        
        if(this.nodesToRetainAttributes != null) {
            for(String node:this.nodesToRetainAttributes) {
                keepAttributes = node.equals(tag.getTagName());
                if(keepAttributes) break;
            }
        }
        
        if(keepAttributes) {
//if(LOG) System.out.println("================== ArributesToAccept "+(this.attributesToAccept==null?null:Arrays.toString(this.attributesToAccept)));        
            
            this.keepAttributesToAcceptOrExtract(tag);
//if(LOG) System.out.println("================== After keeping attributes to accept, tag: "+tag.toTagHtml());                    
        }
            
if(LOG) System.out.println(this.getClass().getName()+". APPENDING "+
        (attributesOnly?"Only tag attributes":keepAttributes?"Tag + attributes":"Tag - attributes"));
        
        if(attributesOnly) {

            final String [] extractedAttributes = this.attributesExtractor.extract(tag, this.attributesToExtract);
            
if(LOG) System.out.println("================== Extracted "+Arrays.toString(extractedAttributes)+", from: "+tag.toTagHtml());

            logger.fine(() -> MessageFormat.format("Extracted attributes: {0} from tag: {1}",
                    Arrays.toString(extractedAttributes), tag.toTagHtml()));

            for(String attrVal : extractedAttributes) {
                extract.append(attrVal).append(' ');
            }    
            
        }else if(keepAttributes) {
            
            extract.append(tag.toTagHtml());
            
        }else{
            
            extract.append('<').append(tag.getTagName()).append('>');
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

    private void keepAttributesToAcceptOrExtract(Tag tag) {
        
        if(this.attributesToAcceptAll == null || this.attributesToAcceptAll.isEmpty()) {
            return;
        }
        
        ArrayList<String> remove = new ArrayList<>();
        
        List attributes = tag.getAttributes();

        for(Object oval:attributes) {

            Attribute attr = (Attribute)oval;
            
            if(!attr.isValued()) continue;

            String name = attr.getName();
            
            if(name == null) {
                continue;
            }
            
            if(!this.attributesToAcceptAll.contains(name)) {
                remove.add(name);
            }
        }
        
        for(String name:remove) {
            tag.removeAttribute(name);
        }
    }
    
    public boolean isExtractOnlyAttributes() {
        return this.attributesExtractor != null && this.attributesToExtract != null && this.attributesToExtract.length != 0;
    }

    @Override
    public boolean isDone() {
        return nodeVisitingFilter.isDone();
    }

    @Override
    public boolean isStarted() {
        return nodeVisitingFilter.isStarted();
    }
    
    @Override
    public boolean isFinishedParsing() {
        return finishedParsing;
    }

    @Override
    public StringBuilder getExtract() {
        return extract;
    }
    
    private NodeVisitingFilter initNodeVisitingFilter() {
        if(this.nodeVisitingFilter == null) {
            this.nodeVisitingFilter = new NodeVisitingFilterImpl(this.id, null, null);
        }
        return this.nodeVisitingFilter;
    }

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

//    @Override
    public void setFilter(NodeVisitingFilter nodeVisitingFilter) {
        this.nodeVisitingFilter = nodeVisitingFilter;
    }

    public String [] getTextToReject() {
        return this.nodeVisitingFilter == null ? null : this.nodeVisitingFilter.getTextToReject();
    }
    
    public void setTextToReject(String [] textToReject) {
        this.initNodeVisitingFilter().setTextToAccept(textToReject);
    }

    public String [] getTextToAccept() {
        return this.nodeVisitingFilter == null ? null : this.nodeVisitingFilter.getTextToAccept();
    }
    
    public void setTextToAccept(String [] textToAccept) {
        this.initNodeVisitingFilter().setTextToAccept(textToAccept);
    }

    public String [] getTextToDisableOn() {
        return this.nodeVisitingFilter == null ? null : this.nodeVisitingFilter.getTextToDisableOn();
    }
    
    public void setTextToDisableOn(String [] textToDisableOn) {
        this.initNodeVisitingFilter().setTextToDisableOn(textToDisableOn);
    }
    
    public NodeFilter getStartAtFilter() {
        return this.nodeVisitingFilter == null ? null : this.nodeVisitingFilter.getStartAtFilter();
    }

    public void setStartAtFilter(NodeFilter startAtFilter) {
        this.initNodeVisitingFilter().setStartAtFilter(startAtFilter);
    }

    public NodeFilter getStopAtFilter() {
        return this.nodeVisitingFilter == null ? null : this.nodeVisitingFilter.getStopAtFilter();
    }

    public void setStopAtFilter(NodeFilter stopAtFilter) {
        this.initNodeVisitingFilter().setStopAtFilter(stopAtFilter);
    }
    
    public void setTagLocator(TagLocator tagLocator) {
        this.initNodeVisitingFilter().setTagLocator(tagLocator);
    }
    
    @Override
    public String[] getNodeTypesToAccept() {
        return nodeVisitingFilter == null ? null : nodeVisitingFilter.getNodeTypesToAccept();
    }

//    @Override
    public void setNodeTypesToAccept(String[] nodeTypesToAccept) {
        this.initNodeVisitingFilter().setNodeTypesToAccept(nodeTypesToAccept);
    }

    @Override
    public String[] getNodeTypesToReject() {
        return nodeVisitingFilter == null ? null : nodeVisitingFilter.getNodeTypesToReject();
    }

//    @Override
    public void setNodeTypesToReject(String[] nodeTypesToReject) {
        this.initNodeVisitingFilter().setNodeTypesToReject(nodeTypesToReject);
    }

    @Override
    public String[] getNodesToAccept() {
        return nodeVisitingFilter == null ? null : nodeVisitingFilter.getNodesToAccept();
    }

//    @Override
    public void setNodesToAccept(String[] nodesToAccept) {
        this.initNodeVisitingFilter().setNodesToAccept(nodesToAccept);
    }

    @Override
    public String[] getNodesToReject() {
        return nodeVisitingFilter == null ? null : nodeVisitingFilter.getNodesToReject();
    }

//    @Override
    public void setNodesToReject(String[] nodesToReject) {
        this.initNodeVisitingFilter().setNodesToReject(nodesToReject);
    }

    @Override
    public NodesFilter getNodesFilter() {
        return this.nodeVisitingFilter == null ? null : this.nodeVisitingFilter.getNodesFilter();
    }

//    @Override
    public void setNodesFilter(NodesFilter nodesFilter) {
        this.initNodeVisitingFilter().setNodesFilter(nodesFilter);
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
    public String getId() {
        return id;
    }

//    @Override
    public void setId(String id) {
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

    @Override
    public String[] getAttributesToAccept() {
        return attributesToAccept;
    }

//    @Override
    public void setAttributesToAccept(String[] attributesToAccept) {
        this.attributesToAccept = attributesToAccept;
    }

    @Override
    public String[] getNodesToRetainAttributes() {
        return nodesToRetainAttributes;
    }

//    @Override
    public void setNodesToRetainAttributes(String[] nodesToRetainAttributes) {
        this.nodesToRetainAttributes = nodesToRetainAttributes;
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