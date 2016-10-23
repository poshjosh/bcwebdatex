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

package com.bc.webdatex.extractor;

/**
 * @author Chinomso Bassey Ikwuagwu on Sep 17, 2016 12:43:23 PM
 */
public class TitleFromUrlExtractor implements Extractor<String> {

    @Override
    public String extract(String url, String defaultOutput) {
//   .../the-government-has-done-it-again.html            
// we extract: the government has done it again            
        url = url.trim();
        if(url.endsWith("/")) {
            url = url.substring(0, url.length()-1);
        }
        String title;
        int a = url.lastIndexOf('/');
        if(a != -1) {
//            int b = url.lastIndexOf('.', a); // This counts backwards from a
            int b = url.lastIndexOf('.');
            if(b == -1 || b < a) {
                b = url.length();
            }
            title = url.substring(a+1, b);
            title = title.replaceAll("\\W", " "); //replace all non word chars
        }else{
            title = null;
        }
        
        return title == null || title.isEmpty() ? defaultOutput : Character.toUpperCase(title.charAt(0)) + title.substring(1);
    }
}
