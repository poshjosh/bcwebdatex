package com.bc.webdatex.locator.impl;

import com.bc.util.StringComparator;
import com.bc.util.StringComparatorImpl;
import com.bc.util.Log;
import com.bc.webdatex.locator.TransverseNodeBuilder;
import com.bc.webdatex.locator.TransverseNodeMatcher;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.htmlparser.Tag;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 19, 2016 8:32:25 PM
 */
public class TransverseNodeMatcherImpl implements TransverseNodeMatcher {
    
    private final float tolerance;
    
    private final Pattern regexPattern;
    
    private final StringComparator stringComparator;
    
    private final TransverseNodeBuilder transverseNodeBuilder;

    public TransverseNodeMatcherImpl() {
        this(0.0f);
    }
    
    public TransverseNodeMatcherImpl(float tolerance) {
        // ${@regex(XXX)} where XXX is the regex literal
        this(new TransverseBuilderImpl().getTransverseNodeBuilder(), 
                new StringComparatorImpl(true, true), 
                Pattern.compile("\\$\\{\\@regex(\\(.+?\\))\\}"), 
                tolerance);
    }
    
    public TransverseNodeMatcherImpl(TransverseNodeBuilder transverseNodeBuilder, 
            StringComparator stringComparator, Pattern regexPattern, float tolerance) {
        this.stringComparator = stringComparator;
        this.regexPattern = regexPattern;
        this.tolerance = tolerance;
        this.transverseNodeBuilder = transverseNodeBuilder;
    }

    @Override
    public boolean matches(Tag tag, String transverseNode) {
        
        final String fromTag = this.transverseNodeBuilder.build(tag);
        
        final boolean equals = this.matches(transverseNode, fromTag);
        
        return equals;
    }
    
    protected boolean matches(String node1, String node2) {
        
        boolean matches;
        
        final boolean LOG = false;//"targetNode5".equals(id);// && "<html>".equalsIgnoreCase(toCompare);
        
// @related_keywords @related_@regex   
        final String regexStart = "${@regex(";
        
        final boolean containsRegex = node1.contains(regexStart) || node2.contains(regexStart);

        if(!containsRegex) {
        
            // <span id="1"> matches the <span portions
            //
            final String name1 = node1.split("\\s")[0];

            final String name2 = node2.split("\\s")[0];

            boolean tagnameEquals = name1.equalsIgnoreCase(name2);
            
if(LOG) System.out.println(this.getClass().getName()+"- - - - - - - TagName equals: "+tagnameEquals);  

            if(!tagnameEquals) {
                
                matches = false;
                
            }else{
                
                // We matches starting after the names
                //
                node1 = node1.substring(name1.length());

                node2 = node2.substring(name2.length());

                matches = stringComparator.compare(node1, node2, tolerance);
            
if(LOG) System.out.println(this.getClass().getName()+"- - - - - - - Matched: "+matches+", using tolerance: "+tolerance+", on argument0: "+node1+" and argument1: "+node2); 
            }
        }else {
            
            // Resolve regexes if available
            StringBuffer buff = new StringBuffer(node1.length()) ;

            this.resolveRegex(node1, buff);
            
            // new-lines were causing problems 
            // We were looking for <div class= we often found <div\nclass=
            // 
            String regex = buff.toString().replaceAll("\\s{1,}", "(\\\\s{1,})");
            
            matches = Pattern.matches(regex, node2);
            
if(LOG) System.out.println(this.getClass().getName()+"- - - - - - - Match found: "+matches+" for regex: "+regex+" in: "+node2); 

Log.getInstance().log(Level.FINER, "  Regex: {0}\nTo match: {1}\nMatch: {2}", this.getClass(), regex, node2, matches);
        }
        
        return matches;
    }
    
    /**
     * Regexes are enclosed in ${@regex(XXX)}
     * Where the XXX is the actual regex ligeral
     * The method removes the scripting parts such that:<br/>
     * &lt;span class="product-${@regex(.+?)}" becomes &lt;span class="product-.+?"
     * @return boolean. true if a regex script was resolved, false otherwise.
     */
    private boolean resolveRegex(String s, StringBuffer appendTo) {
        
        Matcher m = regexPattern.matcher(s);
        
        boolean resolvedRegex = false;
        
        while(m.find()) {
            
            String replacement = m.group(1);

            m.appendReplacement(appendTo, Matcher.quoteReplacement(replacement));
            
            resolvedRegex = true;
        }

        m.appendTail(appendTo);

if(resolvedRegex) {
Log.getInstance().log(Level.FINER, "{0} resolved to: {1}", this.getClass(), s, appendTo);
}        
        return resolvedRegex;
    }
}
