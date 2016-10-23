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
 * Extract some <code>http://schema.org/Article</code> meta data
 * @see http://schema.org/Article
 * @see http://schema.org/CreativeWork
 * @see http://schema.org/Thing
 * @see http://schema.org/docs/schemas.html
 * @see http://schema.org/docs/full.html
 * @author Chinomso Bassey Ikwuagwu on Oct 22, 2016 5:21:16 PM
 */
public class SchemaArticle extends SchemaCreativeWork {

    public SchemaArticle(String url, NodeList nodeList) {
        super(url, nodeList);
    }

    public SchemaArticle(String url, NodeList nodeList, DateFormat dateFormat) {
        super(url, nodeList, dateFormat);
    }

    public SchemaArticle(Dom dom) {
        super(dom);
    }

    public SchemaArticle(Dom dom, DateFormat dateFormat) {
        super(dom, dateFormat);
    }

    public SchemaArticle(Dom dom, Collection<String> dateFormatPatterns, TimeZone inputTimeZone, TimeZone outputTimeZone) {
        super(dom, dateFormatPatterns, inputTimeZone, outputTimeZone);
    }

    public SchemaArticle(Dom dom, Extractor<Date> dateExtractor) {
        super(dom, dateExtractor);
    }

    private Set<String> categorySet;
    @Override
    public Set<String> getCategorySet() {
        if(categorySet == null) {
            categorySet = this.getStringSet(false, "articleSection");
        }
        return categorySet;
    }

    private String content;
    @Override
    public String getContent() {
        if(content == null) {
            content = this.getFirstMetaTagContent("articleBody", null, false);
        }
        return content;
    }
}
