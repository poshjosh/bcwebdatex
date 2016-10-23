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
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;
import org.htmlparser.util.NodeList;

/**
 * Extract twitter card meta data
 * @see https://dev.twitter.com/cards/overview
 * @see https://dev.twitter.com/cards/markup
 * @author Chinomso Bassey Ikwuagwu on Oct 22, 2016 3:36:39 PM
 */
public class TwitterCard extends AbstractMetatagsData {

    public TwitterCard(String url, NodeList nodeList) {
        super(url, nodeList);
    }

    public TwitterCard(String url, NodeList nodeList, DateFormat dateFormat) {
        super(url, nodeList, dateFormat);
    }

    public TwitterCard(Dom dom) {
        super(dom);
    }

    public TwitterCard(Dom dom, DateFormat dateFormat) {
        super(dom, dateFormat, defaultAttributeNames());
    }

    public TwitterCard(Dom dom, Collection<String> dateFormatPatterns, TimeZone inputTimeZone, TimeZone outputTimeZone) {
        super(dom, dateFormatPatterns, inputTimeZone, outputTimeZone);
    }

    public TwitterCard(Dom dom, Extractor<Date> dateExtractor) {
        super(dom, dateExtractor, defaultAttributeNames());
    }

    private static String [] defaultAttributeNames() {
        return new String[]{"name"};
    }
    
    private String title;
    @Override
    public String getTitle() {
        if(title == null) {
            title = this.getFirstMetaTagContent("twitter:title", null, false);
        }
        return title;
    }
    
    private String author;
    @Override
    public String getAuthor() {
        if(author == null) {
            author = this.getFirstMetaTagContent(null, false, "twitter:creator", "twitter:creator:id");
        }
        return author;
    }

    private String publisher;
    @Override
    public String getPublisher() {
        if(publisher == null) {
            publisher = this.getFirstMetaTagContent(null, false, "twitter:site", "twitter:site:id");
        }
        return publisher;
    }

    private String type;
    @Override
    public String getType() {
        if(type == null) {
            type = this.getFirstMetaTagContent("twitter:card", null, false);
        }
        return type;
    }
    
    private String description;
    @Override
    public String getDescription() {
        if(description == null) {
            description = this.getFirstMetaTagContent("twitter:description", null, false);
        }
        return description;
    }
    
    private Set<String> imageUrls;
    @Override
    public Set<String> getImageUrls() {
        if(imageUrls == null) {
            imageUrls = this.getStringSet(false, "twitter:image");
        }
        return imageUrls;
    }
}
