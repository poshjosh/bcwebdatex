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

import com.bc.webdatex.extractor.Extractor;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;
import org.htmlparser.util.NodeList;

/**
 * Extract Open Graph meta data
 * @see http://ogp.me/
 * @author Chinomso Bassey Ikwuagwu on Oct 21, 2016 11:43:34 AM
 */
public class OpenGraph extends AbstractMetatagsData {

    public OpenGraph(String url, NodeList nodeList) {
        super(url, nodeList);
    }

    public OpenGraph(String url, NodeList nodeList, DateFormat dateFormat) {
        super(url, nodeList, dateFormat);
    }

    public OpenGraph(Dom dom) {
        super(dom);
    }

    public OpenGraph(Dom dom, DateFormat dateFormat) {
        this(dom, dateFormat, true);
    }

    public OpenGraph(Dom dom, DateFormat dateFormat, boolean lenient) {
        super(dom, dateFormat, defaultAttributeNames(lenient));
    }

    public OpenGraph(Dom dom, Collection<String> dateFormatPatterns, TimeZone inputTimeZone, TimeZone outputTimeZone) {
        super(dom, dateFormatPatterns, inputTimeZone, outputTimeZone);
    }

    public OpenGraph(Dom dom, Extractor<Date> dateExtractor) {
        this(dom, dateExtractor, true);
    }
    
    public OpenGraph(Dom dom, Extractor<Date> dateExtractor, boolean lenient) {
        super(dom, dateExtractor, defaultAttributeNames(lenient));
    }

    private static String [] defaultAttributeNames(boolean lenient) {
        return lenient ? new String[]{"property", "name", "itemprop"} : new String[]{"property"};
    }
    
    private String title;
    @Override
    public String getTitle() {
        if(title == null) {
            title = this.getFirstMetaTagContent("og:title", null, false);
        }
        return title;
    }
    
    private String author;
    @Override
    public String getAuthor() {
        if(author == null) {
            author = this.getFirstMetaTagContent(null, false, "article:author", "book:author");
        }
        return author;
    }

    private String publisher;
    @Override
    public String getPublisher() {
        if(publisher == null) {
            publisher = this.getFirstMetaTagContent("og:site_name", null, false);
        }
        return publisher;
    }

    private String type;
    @Override
    public String getType() {
        if(type == null) {
            type = this.getFirstMetaTagContent("og:type", null, false);
        }
        return type;
    }

    private Set<String> tagSet;
    @Override
    public Set<String> getTagSet() {
        if(tagSet == null) {
            final boolean regex = true;
            tagSet = this.getStringSet(regex, Pattern.quote(":tag"));
        }
        return tagSet;
    }

    private Set<String> categorySet;
    @Override
    public Set<String> getCategorySet() {
        if(categorySet == null) {
            final boolean regex = true;
            categorySet = this.getStringSet(regex, Pattern.quote(":section"));
        }
        return categorySet;
    }
    
    private String description;
    @Override
    public String getDescription() {
        if(description == null) {
            description = this.getFirstMetaTagContent("og:description", null, false);
        }
        return description;
    }
    
    private Date datePublished;
    @Override
    public Date getDatePublished() throws ParseException {
        if(datePublished == null) {
            final boolean regex = true;
            datePublished = this.getDate(null, regex, 
                    Pattern.quote(":release_date"), Pattern.quote(":published_time"));
        }
        return datePublished;
    }
    
    private Date dateModified;
    @Override
    public Date getDateModified() throws ParseException {
        if(dateModified == null) {
            final String foundOnASiteButNotInSpecs = "og:updated_time";
            dateModified = this.getDate(null, false, "article:modified_time", foundOnASiteButNotInSpecs);
        }
        return dateModified;
    }
    
    private Set<String> imageUrls;
    @Override
    public Set<String> getImageUrls() {
        if(imageUrls == null) {
            imageUrls = this.getStringSet(false, 
                    "og:image:secure_url", "og:image", "og:image_url");
        }
        return imageUrls;
    }

    private Locale locale;
    @Override
    public Locale getLocale() throws ParseException {
        if(locale == null) {
            locale = this.getLocale(null, false, "og:locale");
        }
        return locale;
    }
}
