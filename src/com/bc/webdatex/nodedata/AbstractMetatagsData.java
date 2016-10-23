/*
 * Copyright 2016 NUROX Ltd.
 *
 * Licensed under the NUROX Ltd Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.looseboxes.com/legal/licenses/software.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bc.webdatex.nodedata;

import com.bc.net.ConnectionManager;
import com.bc.util.XLogger;
import com.bc.webdatex.util.ImageInfo;
import com.bc.webdatex.extractor.Extractor;
import com.bc.webdatex.extractor.date.DateExtractor;
import com.bc.webdatex.extractor.date.SimpleDateFormatAddTimeIfNone;
import com.bc.webdatex.nodefilter.HasAttributeRegexFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import org.htmlparser.Attribute;
import org.htmlparser.NodeFilter;
import org.htmlparser.Tag;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.util.NodeList;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 21, 2016 11:46:33 AM
 */
public class AbstractMetatagsData implements MetatagsData {
//@todo add getGeo or getPlacename,getPosition,getRegion,getICBM
//<meta name="geo.placename" content="Lagos island, Lagos, Nigeria"/>
//<meta name="geo.position" content="6.4548790;3.4245980"/>
//<meta name="geo.region" content="NG-Lagos"/>
    
    private final List<String> attributeNames;

    private final Dom dom;
    
    private final DateFormat dateFormat;
    
    private final Extractor<Date> dateExtractor;

    public AbstractMetatagsData(String url, NodeList nodeList) {
        this(new SimpleDom(url, url, nodeList));
    }
    
    public AbstractMetatagsData(String url, NodeList nodeList, DateFormat dateFormat) {
        this(new SimpleDom(url, url, nodeList), dateFormat);
    }

    public AbstractMetatagsData(Dom dom) {
        this(dom, new SimpleDateFormatAddTimeIfNone("yyyy-MM-dd'T'HH:mm:ss"));
    }

    public AbstractMetatagsData(Dom dom, DateFormat dateFormat) {
        this(dom, dateFormat, defaultAttributeNames());
    }
    
    public AbstractMetatagsData(Dom dom, DateFormat dateFormat, String [] defaultAttributeNames) {
        this.dom = Objects.requireNonNull(dom);
        this.dateFormat = Objects.requireNonNull(dateFormat);
        this.dateExtractor = null;
        this.attributeNames = Arrays.asList(requireNonNullOrEmpty(defaultAttributeNames));
    }

    public AbstractMetatagsData(Dom dom,
            Collection<String> dateFormatPatterns, TimeZone inputTimeZone, TimeZone outputTimeZone) {
        
        this(dom, new DateExtractor(dateFormatPatterns, inputTimeZone, outputTimeZone));
    }

    public AbstractMetatagsData(Dom dom, Extractor<Date> dateExtractor) {
        
        this(dom, dateExtractor, defaultAttributeNames());
    }
    
    public AbstractMetatagsData(Dom dom, Extractor<Date> dateExtractor, String [] defaultAttributeNames) {
        this.dom = Objects.requireNonNull(dom);
        this.dateFormat = null;
        this.dateExtractor = Objects.requireNonNull(dateExtractor);
        this.attributeNames = Arrays.asList(requireNonNullOrEmpty(defaultAttributeNames));
    }
    
    private static String [] defaultAttributeNames() {
        return new String[]{"property", "itemprop", "name"};
    }
    
    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getAuthor() {
        return null;
    }

    @Override
    public String getPublisher() {
        return null;
    }

    @Override
    public String getType() {
        return null;
    }

    @Override
    public String getTags() {
        return this.toPlainString(this.getTagSet(), null);
    }

    @Override
    public Set<String> getTagSet() {
        return Collections.EMPTY_SET;
    }

    @Override
    public String getCategories() {
        return this.toPlainString(this.getCategorySet(), null);
    }

    @Override
    public Set<String> getCategorySet() {
        return Collections.EMPTY_SET;
    }

    @Override
    public String getKeywords() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getContent() {
        return null;
    }
    
    @Override
    public Date getDate() {
        try{
            Date date = this.getDateCreated();
            if(date == null) {
                date = this.getDatePublished();
                if(date == null) {
                    date = this.getDateModified();
                }
            }
            return date;
        }catch(ParseException e) {
            return null;
        }
    }

    @Override
    public Date getDateCreated() throws ParseException {
        return null;
    }

    @Override
    public Date getDatePublished() throws ParseException {
        return null;
    }

    @Override
    public Date getDateModified() throws ParseException {
        return null;
    }

    private String firstValidImageUrl;
    @Override
    public String getFirstValidImageUrl() {
        if(firstValidImageUrl == null) {
final long tb4 = System.currentTimeMillis();
final long mb4 = this.freeMemory();
            Set<String> urls = this.getImageUrls();
            if(urls != null && !urls.isEmpty()) {
                ConnectionManager connMgr = this.getConnectionManager();
                ImageInfo imageInfo = new ImageInfo();
                for(String url : urls) {
                    if(this.acceptImageUrl(connMgr, imageInfo, url, true)) {
                        firstValidImageUrl = url;
                        break;
                    }
                }
            }
XLogger.getInstance().log(Level.FINER, 
        "getFirstValidImageUrl. Consumed. time: {0}, memory: {1}",
        this.getClass(), System.currentTimeMillis()-tb4, mb4-this.freeMemory());
        }
        return firstValidImageUrl;
    }

    private String imageUrlOfLargestImage;
    @Override
    public String getImageUrlOfLargestImage() {
        if(this.imageUrlOfLargestImage == null) {
final long tb4 = System.currentTimeMillis();
final long mb4 = this.freeMemory();
            Set<String> urls = this.getImageUrls();
            if(urls != null && !urls.isEmpty()) {
                ConnectionManager connMgr = this.getConnectionManager();
                ImageInfo imageInfo = new ImageInfo();
                int largestSize = 0;
                for(String url : urls) {
                    if(this.acceptImageUrl(connMgr, imageInfo, url, false)) {
                        final int size = imageInfo.getWidth() * imageInfo.getHeight();
                        if(size > largestSize) {
                            largestSize = size;
                            imageUrlOfLargestImage = url;
                        }
                    }
                }
            }
XLogger.getInstance().log(Level.FINER, 
        "getImageUrlOfLargestImage. Consumed. time: {0}, memory: {1}",
        this.getClass(), System.currentTimeMillis()-tb4, mb4-this.freeMemory());
        }
        return this.imageUrlOfLargestImage;
    }
    
    private long freeMemory() {
        Runtime r = Runtime.getRuntime();
        return r.maxMemory() - r.totalMemory() - r.freeMemory();
    }
    
    @Override
    public Set<String> getImageUrls() {
        return Collections.EMPTY_SET;
    }
    
    @Override
    public Locale getLocale() throws ParseException {
        return null;
    }

    public Set<String> getStringSet(boolean regex, String... attributeValues) {
        List<String> contents = this.getMetaTagContents(regex, attributeValues);
        Set<String> output = contents == null || contents.isEmpty() ? Collections.EMPTY_SET : 
                Collections.unmodifiableSet(new HashSet(contents));
        return output;
    }

    public Date getDate(Date outputIfNone, boolean regex, String... attributeValues) throws ParseException {
        final String sval = this.getFirstMetaTagContent(null, regex, attributeValues);
        final Date output = sval == null || sval.isEmpty() ? null : parseDate(sval);
        return output;
    }

    public Locale getLocale(Locale outputIfNone, boolean regex, String... attributeValues) throws ParseException {
        final String sval = this.getFirstMetaTagContent(null, regex, attributeValues);
        final Locale output = sval == null || sval.isEmpty() ? null : this.toLocale(sval);
        return output;
    }
    
    public String getFirstMetaTagContent(String outputIfNone, boolean regex, String... attributeValues) {
        String output = null;
        if(attributeValues != null && attributeValues.length != 0) {
            for(String attributeName : this.attributeNames) {
                Attribute [] attributes = new Attribute[attributeValues.length];
                for(int i=0; i<attributes.length; i++) {
                    attributes[i] = new Attribute(attributeName, attributeValues[i]);
                }
                output = this.getFirstMetaTagContent(null, regex, attributes);
                if(output != null) {
                    break;
                }
            }
        }
        return output == null ? outputIfNone : output;
    }
    
    public String getFirstMetaTagContent(String outputIfNone, boolean regex, Attribute... attributes) {
        List<String> contents = this.getMetaTagContents(regex, attributes);
        return this.getFirst(contents, outputIfNone);
    }

    public List<String> getMetaTagContents(boolean regex, String... attributeValues) {
        List<String> output = null;
        if(attributeValues != null && attributeValues.length != 0) {
            for(String attributeName : this.attributeNames) {
                Attribute [] attributes = new Attribute[attributeValues.length];
                for(int i=0; i<attributes.length; i++) {
                    attributes[i] = new Attribute(attributeName, attributeValues[i]);
                }
                output = this.getMetaTagContents(regex, attributes);
                if(!output.isEmpty()) {
                    break;
                }
            }
        }
        return output == null || output.isEmpty() ? Collections.EMPTY_LIST : output;
    }
    
    public List<String> getMetaTagContents(boolean regex, Attribute... attributes) {
        List<String> output = null;
        if(attributes != null && attributes.length != 0) {
            HasAttributeFilter filter = this.getAttributeFilter(null, null, regex);
            for(Attribute attribute : attributes) {
                filter.setAttributeName(attribute.getName());
                filter.setAttributeValue(attribute.getValue());
                List<String> contents = this.getMetaTagContents(filter);
                if(contents.isEmpty()) {
                    continue;
                }
                if(output == null) {
                    output = new ArrayList<>();
                }
                output.addAll(contents);
            }
        }
        return output == null || output.isEmpty() ? Collections.EMPTY_LIST : output;
    }

    public String getFirstMetaTagContent(String attributeValue, String outputIfNone, boolean regex) {
        String output = null;
        HasAttributeFilter filter = this.getAttributeFilter(null, attributeValue, regex);
        filter.setAttributeValue(attributeValue);
        for(String attributeName : this.attributeNames) {
            filter.setAttributeName(attributeName);
            output = this.getFirstMetaTagContent(filter, null);
            if(output != null) {
                break;
            }
        }
        return output == null ? outputIfNone : output;
    }
    
    public String getFirstMetaTagContent(String attributeName, String attributeValue, String outputIfNone, boolean regex) {
        NodeFilter filter = this.getAttributeFilter(attributeName, attributeValue, regex);
        return this.getFirstMetaTagContent(filter, outputIfNone);
    }

    public String getFirstMetaTagContent(NodeFilter filter, String outputIfNone) {
        List<String> contents = this.getMetaTagContents(filter);
        return this.getFirst(contents, outputIfNone);
    }
    
    public List<String> getMetaTagContents(String attributeValue, boolean regex) {
        List<String> output = null;
        HasAttributeFilter filter = this.getAttributeFilter(null, attributeValue, regex);
        filter.setAttributeValue(attributeValue);
        for(String attributeName : this.attributeNames) {
            filter.setAttributeName(attributeName);
            output = this.getMetaTagContents(filter);
            if(!output.isEmpty()) {
                break;
            }
        }
        return output == null || output.isEmpty() ? Collections.EMPTY_LIST : output;
    }

    public List<String> getMetaTagContents(String attributeName, String attributeValue, boolean regex) {
        NodeFilter filter = this.getAttributeFilter(attributeName, attributeValue, regex);
        return this.getMetaTagContents(filter);
    }
    
    public List<String> getMetaTagContents(NodeFilter filter) {
        List<String> output = null;
        List<MetaTag> metaTags = dom.getMetaTags();
        for(MetaTag metaTag : metaTags) {
            if(!filter.accept(metaTag)) {
                continue;
            }
            String content = this.getAttributeValue(metaTag, "content", null);
            if(content == null) {
                continue;
            }
            if(output ==  null) {
                output = new ArrayList<>();
            }
            output.add(content);
        }
        return output == null || output.isEmpty() ? Collections.EMPTY_LIST : output;
    }

    public MetaTag getFirstMetaTag(String attributeValue, MetaTag outputIfNone, boolean regex) {
        MetaTag output = null;
        HasAttributeFilter filter = this.getAttributeFilter(null, attributeValue, regex);
        filter.setAttributeValue(attributeValue);
        for(String attributeName : this.attributeNames) {
            filter.setAttributeName(attributeName);
            output = this.getFirstMetaTag(filter, null);
            if(output != null) {
                break;
            }
        }
        return output == null ? outputIfNone : output;
    }

    public MetaTag getFirstMetaTag(String attributeName, String attributeValue, 
            MetaTag outputIfNone, boolean regex) {
        NodeFilter filter = this.getAttributeFilter(attributeName, attributeValue, regex);
        return this.getFirstMetaTag(filter, outputIfNone);
    }
    
    public MetaTag getFirstMetaTag(NodeFilter filter, MetaTag outputIfNone) {
        MetaTag output = outputIfNone;
        List<MetaTag> metaTags = this.dom.getMetaTags();
        for(MetaTag metaTag : metaTags) {
            if(filter.accept(metaTag)) {
                output = metaTag;
                break;
            }
        }
        return output;
    }
    
    public HasAttributeFilter getAttributeFilter(String attributeName, String attributeValue, boolean regex) {
        HasAttributeFilter filter = !regex ? new HasAttributeFilter() : new HasAttributeRegexFilter();
        filter.setAttributeName(attributeName);
        filter.setAttributeValue(attributeValue);
        return filter;
    }

    public <E> E getFirst(Collection<E> c, E outputIfNone) {
        E first = c == null || c.isEmpty() ? outputIfNone : c.iterator().next();
        return first == null ? outputIfNone : first;
    }
    
    public String getAttributeValue(Tag tag, String attributeName, String outputIfNone) {
        final String attributeValue = tag.getAttributeValue(attributeName); 
        return attributeValue == null ? outputIfNone : attributeValue;
    }

    public boolean acceptImageUrl(String imageUrl) {
        return this.acceptImageUrl(this.getConnectionManager(), new ImageInfo(), imageUrl, true);
    }
    
    public boolean acceptImageUrl(ConnectionManager connMgr, ImageInfo imageInfo, String imageUrl, boolean clearImageInfo) {
        boolean accepted;
        if(imageUrl.isEmpty()) {
            accepted = false;
        }else{
            try(InputStream in = connMgr.getInputStream(new URL(imageUrl))) {
                imageInfo.setInput(in);
                accepted = imageInfo.check();
            }catch(IOException e) {
                XLogger.getInstance().log(Level.WARNING, "Error validating image link: "+imageUrl, this.getClass(), e.toString());
                accepted = false;
            }finally{
                if(clearImageInfo) {
                    imageInfo.setInput((InputStream)null);
                }
            }
        }
        return accepted;
    }
    
    public ConnectionManager getConnectionManager() {
        ConnectionManager connMgr = new ConnectionManager();
 //       connMgr.setChunkedStreamingBuffer(8192);
        connMgr.setConnectTimeout(7000);
        connMgr.setReadTimeout(7000);
        connMgr.setGenerateRandomUserAgent(true);
        return connMgr;
    }
    
    public Date parseDate(String sval) throws ParseException {
        
        Date output = this.dateExtractor == null ? null : this.dateExtractor.extract(sval, null);
        
        if(output == null && this.dateFormat != null) {
            
            output = this.dateFormat.parse(sval);
        }
        return output;
    }
    
    public String toPlainString(Collection<String> c, String outputIfNone) {
        String output;
        if(c == null || c.isEmpty()) {
            output = outputIfNone;
        }else{
            StringBuilder builder = new StringBuilder();
            int i = 0;
            for(String s:c) {
                builder.append(s);
                if(i < c.size() -1) {
                    builder.append(',').append(' ');
                }
                ++i;
            }
            output = builder.toString();
        }
        return output == null ? outputIfNone : output;
    }

    /**
     * <p>Source: http://www.java2s.com/Code/Java/Data-Type/ConvertsaStringtoaLocale.htm</p>
     * 
     * <p>Converts a String to a Locale.</p>
     *
     * <p>This method takes the string format of a locale and creates the
     * locale object from it.</p>
     *
     * <pre>
     *   LocaleUtils.toLocale("en")         = new Locale("en", "")
     *   LocaleUtils.toLocale("en_GB")      = new Locale("en", "GB")
     *   LocaleUtils.toLocale("en_GB_xxx")  = new Locale("en", "GB", "xxx")   (#)
     * </pre>
     *
     * <p>(#) The behaviour of the JDK variant constructor changed between JDK1.3 and JDK1.4.
     * In JDK1.3, the constructor upper cases the variant, in JDK1.4, it doesn't.
     * Thus, the result from getVariant() may vary depending on your JDK.</p>
     *
     * <p>This method validates the input strictly.
     * The language code must be lowercase.
     * The country code must be uppercase.
     * The separator must be an underscore.
     * The length must be correct.
     * </p>
     *
     * @param str  the locale String to convert, null returns null
     * @return a Locale, null if null input
     * @throws ParseException if the string is an invalid format
     */
    public Locale toLocale(String str) throws ParseException {
        if (str == null) {
            return null;
        }
        int len = str.length();
        if (len != 2 && len != 5 && len < 7) {
            throw new ParseException("Invalid locale format: " + str, 0);
        }
        char ch0 = str.charAt(0);
        char ch1 = str.charAt(1);
        if (ch0 < 'a' || ch0 > 'z' || ch1 < 'a' || ch1 > 'z') {
            throw new ParseException("Invalid locale format: " + str, 0);
        }
        if (len == 2) {
            return new Locale(str, "");
        } else {
            if (str.charAt(2) != '_') {
                throw new ParseException("Invalid locale format: " + str, 2);
            }
            char ch3 = str.charAt(3);
            if (ch3 == '_') {
                return new Locale(str.substring(0, 2), "", str.substring(4));
            }
            char ch4 = str.charAt(4);
            if (ch3 < 'A' || ch3 > 'Z' || ch4 < 'A' || ch4 > 'Z') {
                throw new ParseException("Invalid locale format: " + str, 4);
            }
            if (len == 5) {
                return new Locale(str.substring(0, 2), str.substring(3, 5));
            } else {
                if (str.charAt(5) != '_') {
                    throw new ParseException("Invalid locale format: " + str, 5);
                }
                return new Locale(str.substring(0, 2), str.substring(3, 5), str.substring(6));
            }
        }
    }
    
    private String [] requireNonNullOrEmpty(String... arr) {
        if(arr.length == 0) {
            throw new IllegalArgumentException();
        }
        return arr;
    }

    public final Dom getDom() {
        return dom;
    }

    public final DateFormat getDateFormat() {
        return dateFormat;
    }

    public final Extractor<Date> getDateExtractor() {
        return dateExtractor;
    }

    public final List<String> getAttributeNames() {
        return attributeNames;
    }
}
