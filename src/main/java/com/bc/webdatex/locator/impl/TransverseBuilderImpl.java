package com.bc.webdatex.locator.impl;

import com.bc.util.Log;
import com.bc.webdatex.locator.PathToTransverse;
import com.bc.webdatex.locator.TransverseBuilder;
import com.bc.webdatex.locator.TransverseNodeBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.htmlparser.Node;
import org.htmlparser.Tag;
import org.htmlparser.util.NodeList;

/**
 * @(#)TransverseBuilder.java   14-Nov-2013 16:19:47
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
public class TransverseBuilderImpl 
        extends PathToTransverse implements TransverseBuilder {

    private List<String> parents;
    
    private List<String> siblings;
    
    private final Map<String, Pattern> patternCache;
    
    private final TransverseNodeBuilder transverseNodeBuilder;

    public TransverseBuilderImpl() { 
        // DONOT CHANGE THE DEFAULT VALUES PROVIDED IN THIS CLASS
        // If you change these values you have to rebuild each transverse created
        // by this class in each config file.
        //
        // There is only one HTML as well as BODY tag. Hence they should be
        // accepted based on their tag names only.
        //
        // Each targetpage could have a different image src, causing the src attribute
        // to chage. Same applies for FORM, and A tags
        //
        this(new String[]{"HTML", "BODY", "FORM", "IMG", "A"});
    }
    
    public TransverseBuilderImpl(String [] nodesToRemoveAttributes) { 
        
        this(new TransverseNodeBuilderImpl(nodesToRemoveAttributes));
    }

    public TransverseBuilderImpl(TransverseNodeBuilder transverseNodeBuilder) { 
        this.transverseNodeBuilder = transverseNodeBuilder;
        this.patternCache = new HashMap<String, Pattern>(){
            @Override
            public Pattern put(String key, Pattern value) {
                Objects.requireNonNull(key);
                Objects.requireNonNull(value);
                return super.put(key, value);
            }
        };
    }
    
    public List<String> [] format(List<String> [] transverse) {
        
        return this.format(transverse, ".+?");
    }
    
    public List<String> [] format(List<String> [] transverse, String regex) {
        
        List<String> [] output = new List[transverse.length];
        
        for(int i=0; i<transverse.length; i++) {

            List<String> in = transverse[i];
            
            List<String> out = new ArrayList<>(in.size());
            
            for(String inval:in) {
                
                String outval = this.format(inval, regex);
                
                out.add(outval);
            }
            
            output[i] = out;
        }

if(Log.getInstance().isLoggable(Level.FINE, this.getClass()))        
Log.getInstance().log(Level.FINE, "Input: {0}\nOutput: {1}", 
this.getClass(), Arrays.toString(transverse), Arrays.toString(output));
        
        return output;
    }
    
    public String format(String input, String regex) {
        return input;
    }

    public String toReplacementTransverseGenericAttributeRegex(String s, String regex) {
        
        s = this.toReplacementTransverseGenericAttributeRegex(s, regex, "\"");
        
        s = this.toReplacementTransverseGenericAttributeRegex(s, regex, "\'");
        
        return s;
    }
    
    public String toReplacementTransverseGenericAttributeRegex(String input, String regex, String quote) {
        
        return this.toReplacementTransverseGenericAttributeRegex(input, regex, "=", quote);
    }
    
    public String toReplacementTransverseGenericAttributeRegex(String input, String regex, String assignment, String quote) {
        
//        assignment = Pattern.quote(assignment);
//        quote = Pattern.quote(quote);
        
        String s = assignment + quote + '(' + regex + ')' + quote;
        
        Pattern pattern = this.getPattern(s, true);

        String replacement = assignment + quote + this.toTransverseRegex(regex) + quote;

Log.getInstance().log(Level.FINEST, "Pattern: {0}, replacement: {1}", this.getClass(), pattern.pattern(), replacement);
        
        String output = pattern.matcher(input).replaceAll(Matcher.quoteReplacement(replacement));        

Log.getInstance().log(Level.FINEST, "Regex: {0}, quote: {1}\n Input: {2}\nOutput: {3}", 
        this.getClass(), regex, quote, input, output);
        return output;
    }

    /**
     * @param tag
     * @return 
     * @throws IllegalArgumentException if the input Tag does not have any parent Node
     */
    @Override
    public List<String> build(Tag tag) {

        if(tag.getParent() == null) {
            throw new IllegalArgumentException("Has no Parent node, Node: "+tag.toTagHtml());
        }

        List<Tag> parentTags = this.getParentTags(tag);
        
        this.parents = new LinkedList<>();
  
// Once this caused Out of Memory Error ???        
//XLogger.getInstance().log(Level.FINE, "Parent names: {0}", this.getClass(), parentTags);

        Iterator<Tag> iter = parentTags.iterator();
        
        while(iter.hasNext()) {
            
            final String transverseNode = this.transverseNodeBuilder.build(iter.next());
            
            this.parents.add(transverseNode);
        }

        List<Tag> siblingTags = this.getListOfStartTagsBefore(tag.getParent().getChildren(), tag);
        
        this.siblings = new LinkedList<>();
        
        iter = siblingTags.iterator();
        
        while(iter.hasNext()) {
            
            final String transverseNode = this.transverseNodeBuilder.build(iter.next());
            
            this.siblings.add(transverseNode);
        }
        
Log.getInstance().log(Level.FINE, "Parents: {0}, Siblings: {1}", 
        this.getClass(), this.parents, this.siblings); 

        return this.getPath();
    }
    
    public List<String> getPath() {
        
        LinkedList<String> transverse = new LinkedList<>();
        
        for(String parent:parents) {
            transverse.add(parent);
        }
        StringBuilder builder = new StringBuilder();
        Iterator<String> sibling = siblings.iterator();
        while(sibling.hasNext()) {
            builder.append(sibling.next());
            if(sibling.hasNext()) {
                builder.append(' ');
            }
        }         
        // In class org.json.simple.JSONObject.toJSONString only 
        // String values are enclosed in quotes (") 
        //
        transverse.add(builder.toString());

        return transverse;
    }
    
    private List<Tag> getParentTags(Node node) {
        
        LinkedList<Tag> parentTags = new LinkedList<>();
        
        Node parent;
        
        while((parent=node.getParent()) != null) {
            
            if( !(parent instanceof Tag) ) {
                continue;
            }
            
            parentTags.add(((Tag)parent));
            
            node = parent;
        }
        
        Collections.reverse(parentTags);
        
        return parentTags;
    }
    
    /**
     * Returns start tags in the node NodeList starting at the first node and 
     * ending at the target node. Text and Remark nodes are not appended.
     */
    private List<Tag> getListOfStartTagsBefore(NodeList children, Node target) {
        
        if(children == null || children.isEmpty()) {
            throw new IllegalArgumentException("NodeList cannot be empty");
        }
        
        // We use our own indexOf based on our own equals method
        //
        int index = this.indexOf(children, target);

        if(index == -1) {
            throw new IllegalArgumentException("NodeList does not contain target");
        }
        
        return this.getListOfStartTags(children, 0, index + 1);
    }

    private List<Tag> getListOfStartTags(NodeList children, int start, int end) {
        
        LinkedList<Tag> toAppend = new LinkedList<>();
        
        for(int i=start; i<end; i++) {
            
            Node child = children.get(i);
            
            if( !(child instanceof Tag) ) {
                continue;
            }

            Tag tag = (Tag)child;
            
            if(tag.isEndTag()) {
                continue;
            }
            
            toAppend.add(tag);
        }
        
        return toAppend;
    }
    
    private int indexOf(List<Node> list, Node node) {
        
        for(int i=0; i<list.size(); i++) {
            
            if(equals(node, list.get(i))) {
                return i;
            }
        }
        
        return -1;
    }
    
    protected boolean equals(Node node1, Node node2) {
        return node1.equals(node2);
    }
    
    private boolean equals_1(Node node1, Node node2) {
        
        // Default is just ==
        if(node1.equals(node2)) {
            return true;
        }
        
        if(node1 instanceof Tag && node2 instanceof Tag) {
            Tag tag1 = (Tag)node1;
            Tag tag2 = (Tag)node2;
            return tag1.toHtml().equals(tag2.toHtml());
        }
        
        return node1.getText().equals(node2.getText());
    }
    
    /**
     * Regexes in transverses are represented in format: ${@regex(XXX)} where XXX is the regex literal
     * @param regex
     * @return The regex in the format used from transverses
     */
    public String toTransverseRegex(String regex) {
// @related_keywords @related_@regex  Regexes are represented in format: ${@regex(XXX)} where XXX is the regex literal
        return "${@regex("+regex+")}";
    }

    private Pattern getPattern(String regex, boolean createIfNone) {
        Pattern pattern = patternCache.get(regex);
        if(pattern == null && createIfNone) {
            pattern = Pattern.compile(regex);
            patternCache.put(regex, pattern);
        }
        return pattern;
    }
    
    public final List<String> getParents() {
        return parents==null || parents.isEmpty() ? Collections.EMPTY_LIST : new ArrayList(parents);
    }

    public final List<String> getSiblings() {
        return siblings==null || siblings.isEmpty() ? Collections.EMPTY_LIST : new ArrayList(siblings);
    }

    public TransverseNodeBuilder getTransverseNodeBuilder() {
        return transverseNodeBuilder;
    }
}
