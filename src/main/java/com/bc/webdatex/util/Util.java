package com.bc.webdatex.util;

import com.bc.util.QueryParametersConverter;
import com.bc.util.Log;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.htmlparser.Attribute;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Tag;
import org.htmlparser.Text;
import org.htmlparser.util.NodeList;


/**
 * @(#)Util.java   24-Sep-2015 16:14:07
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
public class Util {

    private transient static final Logger logger = Logger.getLogger(Util.class.getName());
    
    private static final Pattern [] datePatterns;
    
    private static final Pattern emailPattern;

    static {

        String months = "jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec";

        Pattern dp1 = Pattern.compile("\\d{1,2}[st|nd|rd|th]*\\s*[of]{0,1}[\\s]*["+months+"][a-zA-Z]*[\\p{Punct}]?\\s*[\\d]{4}\\.*", Pattern.CASE_INSENSITIVE);

        Pattern dp2 = Pattern.compile("["+months+"][a-zA-Z]*\\s*[\\d]{1,2}[st|nd|rd|th]*[\\p{Punct}]?\\s*[\\d]{4}\\.*", Pattern.CASE_INSENSITIVE);

        datePatterns = new Pattern[]{dp1, dp2};
        
        emailPattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
    }
    
  public static Node deepClone(Node node) throws CloneNotSupportedException {
    return deepClone(node, true, true);
  }
  
  public static Node deepClone(
          Node node, boolean parents, boolean children) 
          throws CloneNotSupportedException {
      
    Node clone = (Node)node.clone();
    
    if (parents) {
        
      Node nodeParent = node.getParent();
      
      Node cloneParent;
      if (nodeParent == null) {
        cloneParent = null;
      } else {
        cloneParent = deepClone(nodeParent, true, false);
      }
      
      clone.setParent(cloneParent);
      
      if (nodeParent != null) {
          
        NodeList nodeSiblings = nodeParent.getChildren();
        
        NodeList cloneSiblings = new NodeList();
        
        for (Node nodeSibling : nodeSiblings) {
            
          Node cloneSibling;
          if (nodeSibling.equals(node)) {
            cloneSibling = clone;
          } else {
            cloneSibling = deepClone(nodeSibling, false, true);
          }
          
          cloneSiblings.add(cloneSibling);
        }
        
        cloneParent.setChildren(cloneSiblings);
      }
    }
    
    if (children) {
        
      NodeList nodeChildren = node.getChildren();
      
      NodeList cloneChildren;
      if (nodeChildren == null) {
          
        cloneChildren = null;
        
      } else {
          
        cloneChildren = new NodeList();
        
        for (Node child : nodeChildren) {
            
          Node childClone = deepClone(child, false, true);
          
          childClone.setParent(clone);
          
          cloneChildren.add(childClone);
        }
      }
      
      clone.setChildren(cloneChildren);
    }
    
    return clone;
  }
    
    public static void printIfEquals(Node node, String expectedNodeName) {
        
        Tag tag = null;
        try{
            tag = (Tag)node;
        }catch(ClassCastException ignored) { }
        
        if(tag == null) {
            return;
        }

        if(tag.getTagName().equals(expectedNodeName.toUpperCase())) {
Log.getInstance().log(Level.FINEST, "@Util.printIfEquals Tag: {0}", Util.class, tag);
        }
    }
    
    public static int lookForInNode(Node node, String expectedNode, 
            final String expectedChild, final String childAttrKey, final String childAttrVal) {
        
        if (!(node instanceof Tag)) return -1;
        
        Tag tag = (Tag)node;
        
        if(tag.getTagName().equals(expectedNode.toUpperCase())) {

            NodeList list = node.getChildren();

            if(list != null) {
Log.getInstance().log(Level.INFO, "Child count: {0}", Util.class, list.size());
                NodeList extr = list.extractAllNodesThatMatch(new NodeFilter(){
                    @Override
                    public boolean accept(Node node) {
                        if (!(node instanceof Tag) ) return false;
                        Tag tag = ((Tag)node);
                        String name = tag.getTagName();
                        if(!name.equals(expectedChild.toUpperCase())) return false;
//XLogger.getInstance().log(Level.INFO, "Found {0}", Util.class, tag);
                        Attribute attr = tag.getAttribute(childAttrKey);
//XLogger.getInstance().log(Level.INFO, "Found attribute: {0}", this.getClass(), attr);
                        return (attr != null && childAttrVal.equals(attr.getValue()));
                    }
                }, true);
Log.getInstance().log(Level.FINEST, "Extract count: {0}", Util.class, extr.size());
                return extr == null ? -1 : extr.size();
            }
        }

        return -1;
    }

    /**
     * abc.com becomes www.abc.com<br/>
     * sub.abc.com becomes www.abc.com
     */
    public static String toWWWFormat(String url) throws MalformedURLException {

        String x = "//";
        int n = url.indexOf(x);
        String a;
        String b;
        if(n == -1) {
            a = "";
            b = url;
        }else{
            n += x.length();
            a = url.substring(0, n);
            b = url.substring(n);
        }

        String [] parts = b.split("\\.");

        if(parts.length == 1) {
            throw new MalformedURLException("Not a URL: "+url);
        }
        
        StringBuilder builder = new StringBuilder(a);
        
        if(parts.length == 2) {
            builder.append("www.").append(parts[0]).append('.').append(parts[1]);
        }else{
            for(int i=0; i<parts.length; i++) {
                String part = i == 0 ? "www" : parts[i];
                builder.append(part);
                if(i<parts.length-1) {
                    builder.append('.');
                }
            }
        }
        
        return builder.toString();
    }

    public static String createURL(String parent, String child) {

        child = prepareLink(child);

        // found some links //abc.def.com
        if(child.startsWith("//")) {

            try{
                String s = "http:" + child;
                URL url = new URL(s);
                return s;
            }catch(MalformedURLException e) {
                String base = getBaseURL(parent);
                if(base == null) {
                    base = parent;
                }
                return base + child;
            }
        }else{
            String base = getBaseURL(parent);
            if(base == null) {
                base = parent;
            }
            return base + child;
        }
    }
    
    /**
     * Well formed URLs remain the same. However:<br/>
     * ...abc.html becomes /abc.html<br/>
     * abc.html becomes /abc.html
     */
    public static String prepareLink(String link) {
        link = link.toLowerCase();
        if(link.startsWith("http://") || link.startsWith("file://")) return link;
        // Remove dots
        while(link.startsWith(".")) {
            link = link.substring(1);
        }
        // Add / if necessary
        if(!link.startsWith("/")) {
            link = "/" + link;
        }
        return link;
    }
    
    /**
     * For a URL of format http://www.site.com/folder1/folder2/file.html
     * returns:<br/>
     * <ul>
     *  <li>http://www.site.com</li> 
     *  <li>http://www.site.com/folder1</li>
     *  <li>http://www.site.com/folder1/folder2</li>
     *  <li>http://www.site.com/folder1/folder2/file.html</li>
     * </ul>
     */
    public static List<String> getBaseURLs(String urlString) {
        String baseURL = getBaseURL(urlString);
logger.log(Level.FINER, "{0}. Base url: {1}", new Object[]{logger.getName(), baseURL});        
        LinkedList<String> urls = new LinkedList<String>();
        urls.add(baseURL);
        if(baseURL.equals(urlString)) return urls;
        String s = urlString.substring(baseURL.length());
logger.log(Level.FINER, "{0}. URL file: {1}", new Object[]{logger.getName(), s});        
        // Remove the first '/'
        if(s.startsWith("/")) s = s.substring(1);

        String [] parts = s.split("/");
logger.log(Level.FINER, "{0}. URL file parts: {1}", new Object[]{logger.getName(), Arrays.toString(parts)});        
        StringBuilder builder = new StringBuilder();
        for(String part:parts) {
            builder.setLength(0);
            baseURL = builder.append(baseURL).append('/').append(part).toString();
            urls.add(baseURL);
        }
        return urls;
    }
    
    public static String getBaseURL(String urlString) {
Log.getInstance().log(Level.FINER, "Input url: {0}", Util.class, urlString);        
        URL url = null;
        try{
            url = new URL(urlString);
            StringBuilder builder = new StringBuilder(url.getProtocol());
            builder.append("://");
            String x = url.getHost();
            if(x == null) {
                x = url.getAuthority();
            }
            builder.append(x);
            url = new URL(builder.toString());
        }catch(MalformedURLException e) {
            logger.log(Level.WARNING, "{0}", e.toString());
        }
        String output  = url == null ? null : url.toString();
Log.getInstance().log(Level.FINER, "Output url: {0}", Util.class, output);        
        return output;
    }
    
    public static StringBuilder appendQuery(Map params, StringBuilder builder) {
        return appendQuery(params, builder, "&");
    }
    
    public static StringBuilder appendQuery(Map params, StringBuilder builder, String separator) {
        
        if(builder == null) {
            builder = new StringBuilder();
        }
        
        QueryParametersConverter c = new QueryParametersConverter(separator);
        
        return builder.append(c.convert(params));
    }

    public static Map<String, String> getParameters(
            String input, String separator) {
        return getParameters(input, separator, false);
    }

    public static Map<String, String> getParameters(String input, 
            String separator, boolean emptyStringAllowed) {

        QueryParametersConverter c = new QueryParametersConverter(emptyStringAllowed, separator);
        
        return c.reverse(input);
    }
    
    /**
     * @param node
     * @return The (direct) text content of the supplied node. Child nodes
     * are ignored
     */
    public static String getDirectTextContents(Text node) {
        
        int startpos;
        int endpos;
        String s;
        char c;
        StringBuffer ret;

        startpos = node.getStartPosition ();
        endpos = node.getEndPosition ();
        ret = new StringBuffer (endpos - startpos + 20);
        s = node.toHtml ();
        for (int i = 0; i < s.length (); i++)
        {
            c = s.charAt (i);
            switch (c)
            {
                case '\t':
                    ret.append ("\\t");
                    break;
                case '\n':
                    ret.append ("\\n");
                    break;
                case '\r':
                    ret.append ("\\r");
                    break;
                default:
                    ret.append (c);
            }
        }

        return (ret.toString ());
    }

    public static StringBuilder appendTag(Node node, int maxLen) {
        StringBuilder builder = new StringBuilder();
        return appendTag(node, builder, false, maxLen);
    }
    public static StringBuilder appendTag(Node node, StringBuilder builder) {
        if(builder == null) builder = new StringBuilder();
        return appendTag(node, builder, false, 100);
    }
    public static StringBuilder appendTag(Node node, StringBuilder builder, boolean recurse, int maxLength) {
        doAppendTag(node, builder, maxLength);
        if(!recurse) return builder;
        NodeList list = node.getChildren();
        if(list == null) return builder;
        for(int i=0; i<list.size(); i++) {
            Node child = list.elementAt(i);
            appendTag(child, builder, recurse, maxLength);
        }
        return builder;
    }
    private static StringBuilder doAppendTag(Node node, StringBuilder builder, int maxLength) {
        Tag tag = null;
        if(node instanceof Tag) {
            tag = (Tag)node;
            for(Object attr:tag.getAttributes()) {
                builder.append(Util.toString(attr, maxLength));
            }
        }else{
            if(node == null) {
                builder.append("NULL");
            }else{
                builder.append(Util.toString(node.getText(), maxLength));
            }
        }
        return builder;
    }
    
    public static boolean hasEmail(String str) {
        return emailPattern.matcher(str).find();
    }

    public static boolean isEmail(String str) {
        return emailPattern.matcher(str).matches();
    }
    
    public static boolean hasDate(String str) {
        for(Pattern p:datePatterns) {
            if (p.matcher(str).find()) return true;
        }
        return false;
    }

    public static boolean isDate(String str) {
        for(Pattern p:datePatterns) {
            if (p.matcher(str).matches()) return true;
        }
        return false;
    }
    
    public static Pattern [] getDatePatterns() {
        return datePatterns;
    }

    public static Pattern getEmailPattern() {
        return emailPattern;
    }

    public static boolean hasImage(NodeList nodeList, String name, boolean recurse) {

        for(int i=0; i<nodeList.size(); i++) {

            Node child = nodeList.elementAt(i);

            if(recurse) {
                boolean hasChildren = child.getFirstChild() != null;
                if(hasChildren) {
                    if(hasImage(child.getChildren(), name, recurse)) {
                        return true;
                    }
                }else{
                    if(isRequiredImage(child, name)) {
                        return true;
                    }
                }
            }else{
                if(isRequiredImage(child, name)) {
                    return true;
                }
            }
        }

        return false;
    }
    /**
     * @param node The node for which we want to know if it is an image
     * @param name The name of the image file specified in the source attribute
     * of the specified node.
     * @return True if the node is an <tt>IMG</tt> node with source ending with
     * the specified name. False, otherwise.
     */
    private static boolean isRequiredImage(Node node, String name) {
        if(!(node instanceof Tag)) return false;
        Tag tag = (Tag)node;
        if(!tag.getTagName().equals("IMG")) return false;
        final String src = tag.getAttributeValue("src");
        if(src == null) return false;
        return src.endsWith(name);
    }

    public static String toString(Object input, int maxLength) {

        if(input == null) {
            return "NULL";
        }
        
        String output;
        if(input instanceof Collection) {
            output = toString((Collection)input, maxLength);
        }else if (input instanceof Map) {
            output = toString((Map)input, maxLength);
        }else{
            output = toString(input.toString(), maxLength);
        }
        
        return output;
    }

    public static String toString(String str, int maxLength) {

        if(str == null) {
            return "NULL";
        }

        final int len = str.length();

        return (maxLength < 0 || len <= maxLength) ? str : str.substring(0, maxLength);
    }
    
    public static String toString(Collection input, int maxLength) {

        if(input == null) {
            return "NULL";
        }
        
        Iterator i = input.iterator();
	if (! i.hasNext())
	    return "[]";

	StringBuilder sb = new StringBuilder();
	sb.append('[');
	for (;;) {
	    Object e = i.next();
	    sb.append(e == input ? "(this Collection)" : toString(e, maxLength));
	    if (! i.hasNext()) {
		return sb.append(']').toString();
            }    
	    sb.append(", ");
	}
    }
    
    public static String toString(Map input, int maxLength) {

        if(input == null) {
            return "NULL";
        }

	Iterator<Map.Entry> i = input.entrySet().iterator();
	if (! i.hasNext()) {
	    return "{}";
        }    

	StringBuilder sb = new StringBuilder();
	sb.append('{');
	for (;;) {
	    Map.Entry e = i.next();
	    Object key = e.getKey();
	    Object value = e.getValue();
	    sb.append(key   == input ? "(this Map)" : toString(key, maxLength));
	    sb.append('=');
	    sb.append(value == input ? "(this Map)" : toString(value, maxLength));
	    if (! i.hasNext()) {
		return sb.append('}').toString();
            }    
	    sb.append(", ");
	}
    }

    /**
     * Remove nodes not matching the given filter non-recursively.
     * The difference between this method and the similar method
     * {@link org.htmlparser.util.NodeList#keepAllNodesThatMatch(org.htmlparser.NodeFilter)}
     * is that; Once the variable 'recursive' is <tt>true</tt>, a Node will be
     * assessed regardless of whether its parent was accepted by the supplied
     * {@link org.htmlparser.NodeFilter}.
     * 
     * @param filter The filter to use.
     *
     * @see org.htmlparser.util.NodeList#keepAllNodesThatMatch(org.htmlparser.NodeFilter)
     * @deprecated
     */
    private static void keepAllNodesThatMatch (NodeList nodeList, NodeFilter filter) {
        keepAllNodesThatMatch (nodeList, filter, false);
    }

    /**
     * Remove nodes not matching the given filter.
     * The difference between this method and the similar method
     * {@link org.htmlparser.util.NodeList#keepAllNodesThatMatch(org.htmlparser.NodeFilter, boolean)}
     * is that; Once the variable 'recursive' is <tt>true</tt>, a Node will be
     * assessed regardless of whether its parent was accepted by the supplied
     * {@link org.htmlparser.NodeFilter}.
     *
     * @param filter The filter to use.
     * @param recursive If <code>true<code> digs into the children recursively.
     *
     * @see org.htmlparser.util.NodeList#keepAllNodesThatMatch(org.htmlparser.NodeFilter, boolean)
     * @deprecated
     */
    private static void keepAllNodesThatMatch (NodeList nodeList, NodeFilter filter, boolean recursive){

        Node node;
        NodeList children;

        for (int i = 0; i < nodeList.size(); i++){

            node = nodeList.elementAt(i);

            if(!recursive) {

                if(!filter.accept(node)) {
                    nodeList.remove(i);
                }
            }else{

                children = node.getChildren();

                if (children == null) {

                    if(!filter.accept(node)) {
                        nodeList.remove(i);
                    }

                }else{

                    Util.keepAllNodesThatMatch (nodeList, filter, recursive);
                }
            }
        }
    }
    
    public static String toString(Node node, int maxLen) {
        String output;
        if(node instanceof Tag) {
            Tag tag = ((Tag)node);
            if(tag.isEndTag()) {
                output = tag.toHtml(true);
            }else {
                output = tag.toTagHtml();
            }
        }else {
            output = node.getText();
        }
        return toString(output, maxLen);
    }
}//~END
