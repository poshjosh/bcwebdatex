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

import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

/**
 * @author Chinomso Bassey Ikwuagwu on Sep 5, 2016 8:17:21 PM
 */
public interface MetatagsData {
    String getTitle();
    String getAuthor();
    String getPublisher();
    String getType();
    String getTags();
    Set<String> getTagSet();
    String getCategories();
    Set<String> getCategorySet();
    String getKeywords();
    String getDescription();
    String getContent();
    Date getDate();
    Date getDateCreated() throws ParseException;
    Date getDatePublished() throws ParseException;
    Date getDateModified() throws ParseException;
    String getFirstValidImageUrl();
    String getImageUrlOfLargestImage();
    Set<String> getImageUrls();
    Locale getLocale() throws ParseException;
}
