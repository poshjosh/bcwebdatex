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

package com.bc.webdatex.extractor.date;

import com.bc.webdatex.extractor.Extractor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Chinomso Bassey Ikwuagwu on Sep 17, 2016 5:57:57 PM
 */
public class DateStringFromUrlExtractor implements Extractor<String> {

    private final Pattern datePattern;

    public DateStringFromUrlExtractor() {
        this(true, true, false);
    }

    public DateStringFromUrlExtractor(
            boolean acceptSingleDigitDay, boolean acceptSingleDigitMonth, boolean acceptTwoDigitYear) {
        this(new DigitDateRegexImpl(acceptSingleDigitDay, acceptSingleDigitMonth, acceptTwoDigitYear));
    }

    public DateStringFromUrlExtractor(DigitDateRegex dateRegex) {
        this(Pattern.compile(dateRegex.getRegex()));
    }
    
    public DateStringFromUrlExtractor(Pattern pattern) {
        datePattern = pattern;
    }
    
    @Override
    public String extract(String url, String defaultOutput) {
        
        final String output;
        
        Matcher matcher = datePattern.matcher(url);
        
        if(matcher.find()) {
            
            output = matcher.group();
            
        }else{
            
            output = defaultOutput;
        }
        
        return output;
    }
    
    public final Pattern getDatePattern() {
        return datePattern;
    }
}
