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

import com.bc.dom.metatags.AbstractMetadata;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.htmlparser.Attribute;
import org.htmlparser.util.NodeList;
import com.bc.dom.HtmlDocument;

/**
 * @author Chinomso Bassey Ikwuagwu on Sep 5, 2016 8:25:29 PM
 */
public class MetatagsDataImpl extends AbstractMetadata {

    public MetatagsDataImpl(String url, NodeList nodeList) {
        super(url, nodeList);
    }

    public MetatagsDataImpl(String url, NodeList nodeList, String[] defaultAttributeNames) {
        super(url, nodeList, defaultAttributeNames);
    }

    public MetatagsDataImpl(HtmlDocument dom) {
        super(dom);
    }

    public MetatagsDataImpl(HtmlDocument dom, String[] defaultAttributeNames) {
        super(dom, defaultAttributeNames);
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

    private String dateCreated;
    @Override
    public String getDateCreated() {
        if(dateCreated == null) {
            dateCreated = this.getFirstMetaTagContent("itemprop", "dateCreated", null, false);
        }
        return dateCreated;
    }
    
    private String datePublished;
    @Override
    public String getDatePublished() {
        if(datePublished == null) {
            datePublished = this.getFirstMetaTagContent(null, false,
                    new Attribute("itemprop", "datePublished"), 
                    new Attribute("property", "article:published_time"));
        }
        return datePublished;
    }
    
    private String dateModified;
    @Override
    public String getDateModified() {
        if(dateModified == null) {
            dateModified = this.getFirstMetaTagContent(null, false,
                    new Attribute("itemprop", "dateModified"), 
                    new Attribute("property", "og:updated_time"),
                    new Attribute("property", "article:modified_time"));
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
    
    private String locale;
    @Override
    public String getLocale() {
        if(locale == null) {
            locale = this.getFirstMetaTagContent("property", "og:locale", null, false);
        }
        return locale;
    }
}
