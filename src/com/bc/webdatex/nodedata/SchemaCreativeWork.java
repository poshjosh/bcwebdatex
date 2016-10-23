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
import java.util.Set;
import java.util.TimeZone;
import org.htmlparser.util.NodeList;

/**
 * Extract some <code>http://schema.org/CreativeWork</code> meta data
 * @see http://schema.org/CreativeWork
 * @see http://schema.org/Thing
 * @see http://schema.org/docs/schemas.html
 * @see http://schema.org/docs/full.html
 * @author Chinomso Bassey Ikwuagwu on Oct 22, 2016 4:53:15 PM
 */
public class SchemaCreativeWork extends SchemaThing {

    public SchemaCreativeWork(String url, NodeList nodeList) {
        super(url, nodeList);
    }

    public SchemaCreativeWork(String url, NodeList nodeList, DateFormat dateFormat) {
        super(url, nodeList, dateFormat);
    }

    public SchemaCreativeWork(Dom dom) {
        super(dom);
    }

    public SchemaCreativeWork(Dom dom, DateFormat dateFormat) {
        super(dom, dateFormat);
    }

    public SchemaCreativeWork(Dom dom, Collection<String> dateFormatPatterns, TimeZone inputTimeZone, TimeZone outputTimeZone) {
        super(dom, dateFormatPatterns, inputTimeZone, outputTimeZone);
    }

    public SchemaCreativeWork(Dom dom, Extractor<Date> dateExtractor) {
        super(dom, dateExtractor);
    }

    private String title;
    @Override
    public String getTitle() {
        if(title == null) {
            title = this.getFirstMetaTagContent(null, false, "headline", "name");
        }
        return title;
    }
    
    private String keywords;
    @Override
    public String getKeywords() {
        if(keywords == null) {
            keywords = this.getFirstMetaTagContent("keywords", null, false);
        }
        return keywords;
    }

    private String author;
    @Override
    public String getAuthor() {
        if(author == null) {
            author = this.getFirstMetaTagContent(null, false, "author", "creator");
        }
        return author;
    }
    
    private String publisher;
    @Override
    public String getPublisher() {
        if(publisher == null) {
            publisher = this.getFirstMetaTagContent(null, false, "publisher", "sourceOrganization", "producer");
        }
        return publisher;
    }
    
    private Date dateCreated;
    @Override
    public Date getDateCreated() throws ParseException {
        if(dateCreated == null) {
            dateCreated = this.getDate(null, false, "dateCreated");
        }
        return dateCreated;
    }
    
    private Date datePublished;
    @Override
    public Date getDatePublished() throws ParseException {
        if(datePublished == null) {
            datePublished = this.getDate(null, false, "datePublished");
        }
        return datePublished;
    }
    
    private Date dateModified;
    @Override
    public Date getDateModified() throws ParseException {
        if(dateModified == null) {
            dateModified = this.getDate(null, false, "dateModified");
        }
        return dateModified;
    }

    private Set<String> imageUrls;
    @Override
    public Set<String> getImageUrls() {
        if(imageUrls == null) {
            imageUrls = this.getStringSet(false, "image", "thumbnailUrl");
        }
        return imageUrls;
    }
}
