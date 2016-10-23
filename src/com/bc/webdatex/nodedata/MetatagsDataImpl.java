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
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import org.htmlparser.Attribute;
import org.htmlparser.util.NodeList;

/**
 * @author Chinomso Bassey Ikwuagwu on Sep 5, 2016 8:25:29 PM
 */
public class MetatagsDataImpl extends AbstractMetatagsData {

    public MetatagsDataImpl(String url, NodeList nodeList) {
        super(url, nodeList);
    }

    public MetatagsDataImpl(String url, NodeList nodeList, DateFormat dateFormat) {
        super(url, nodeList, dateFormat);
    }

    public MetatagsDataImpl(Dom dom) {
        super(dom);
    }

    public MetatagsDataImpl(Dom dom, DateFormat dateFormat) {
        super(dom, dateFormat);
    }

    public MetatagsDataImpl(Dom dom, Collection<String> dateFormatPatterns, TimeZone inputTimeZone, TimeZone outputTimeZone) {
        super(dom, dateFormatPatterns, inputTimeZone, outputTimeZone);
    }

    public MetatagsDataImpl(Dom dom, Extractor<Date> dateExtractor) {
        super(dom, dateExtractor);
    }
    
    private String title;
    @Override
    public String getTitle() {
        if(title == null) {
            title = this.getFirstMetaTagContent(null, false,
                    new Attribute("itemprop", "name"), 
                    new Attribute("property", "og:title"),
                    new Attribute("name", "twitter:title"));
        }
        return title;
    }
    
    private String author;
    @Override
    public String getAuthor() {
        if(author == null) {
            author = this.getFirstMetaTagContent(null, false,
                    new Attribute("property", "author"), 
                    new Attribute("name", "author"),
                    new Attribute("property", "article:author"));
        }
        return author;
    }

    private String publisher;
    @Override
    public String getPublisher() {
        if(publisher == null) {
            publisher = this.getFirstMetaTagContent(null, false,
                    new Attribute("property", "og:site_name"), 
                    new Attribute("name", "twitter:site"),
                    new Attribute("property", "article:publisher"));
        }
        return publisher;
    }

    private String type;
    @Override
    public String getType() {
        if(type == null) {
            type = this.getFirstMetaTagContent("property", "og:type", null, false);
        }
        return type;
    }

    private Set<String> tagSet;
    @Override
    public Set<String> getTagSet() {
        if(tagSet == null) {
            List<String> list = this.getMetaTagContents("property", "article:tag", false);
            tagSet = list == null || list.isEmpty() ? Collections.EMPTY_SET : 
                    Collections.unmodifiableSet(new HashSet(list));
        }
        return tagSet;
    }

    private Set<String> categorySet;
    @Override
    public Set<String> getCategorySet() {
        if(categorySet == null) {
            List<String> list = this.getMetaTagContents(false,
                    new Attribute("itemprop", "articleSection"),
                    new Attribute("property", "article:section"));
            categorySet = list == null || list.isEmpty() ? Collections.EMPTY_SET : 
                    Collections.unmodifiableSet(new HashSet(list));
        }
        return categorySet;
    }

    private String keywords;
    @Override
    public String getKeywords() {
        if(this.keywords == null) {
            List<String> list = this.getMetaTagContents(false,
                    new Attribute("name", "keywords"), 
                    new Attribute("name", "news_keywords"));
            this.keywords = list == null || list.isEmpty() ? null : this.toPlainString(list, null);
        }
        return this.keywords;
    }

    private String description;
    @Override
    public String getDescription() {
        if(description == null) {
            description = this.getFirstMetaTagContent(null, false,
                    new Attribute("name", "description"), 
                    new Attribute("itemprop", "description"),
                    new Attribute("property", "og:description"),
                    new Attribute("name", "twitter:description"));
        }
        return description;
    }

    private Date dateCreated;
    @Override
    public Date getDateCreated() throws ParseException {
        if(dateCreated == null) {
            final String sval = this.getFirstMetaTagContent("itemprop", "dateCreated", null, false);
            dateCreated = sval == null ? null : parseDate(sval);
        }
        return dateCreated;
    }
    
    private Date datePublished;
    @Override
    public Date getDatePublished() throws ParseException {
        if(datePublished == null) {
            final String sval = this.getFirstMetaTagContent(null, false,
                    new Attribute("itemprop", "datePublished"), 
                    new Attribute("property", "article:published_time"));
            datePublished = sval == null ? null : parseDate(sval);
        }
        return datePublished;
    }
    
    private Date dateModified;
    @Override
    public Date getDateModified() throws ParseException {
        if(dateModified == null) {
            final String sval = this.getFirstMetaTagContent(null, false,
                    new Attribute("itemprop", "dateModified"), 
                    new Attribute("property", "og:updated_time"),
                    new Attribute("property", "article:modified_time"));
            dateModified = sval == null ? null : parseDate(sval);
        }
        return dateModified;
    }

    private Set<String> imageUrls;
    @Override
    public Set<String> getImageUrls() {
        if(imageUrls == null) {
            List<String> contents = this.getMetaTagContents(false, 
                    new Attribute("itemprop", "thumbnailUrl"),
                    new Attribute("property", "og:image"),
                    new Attribute("name", "twitter:image"));
            imageUrls = contents == null || contents.isEmpty() ? Collections.EMPTY_SET : 
                    Collections.unmodifiableSet(new HashSet(contents));
        }
        return imageUrls;
    }
    
    private Locale locale;
    @Override
    public Locale getLocale() throws ParseException {
        if(locale == null) {
            final String sval = this.getFirstMetaTagContent("property", "og:locale", null, false);
            locale = sval == null ? null : this.toLocale(sval);
        }
        return locale;
    }
}
