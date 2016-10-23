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
import com.bc.util.XLogger;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * @author Chinomso Bassey Ikwuagwu on Sep 17, 2016 12:51:05 PM
 */
public class DateFromUrlExtractor implements Extractor<Date> {
    
    private final Extractor<String> dateStringFromUrlExtractor;
    private final Extractor<Date> dateExtractor;

    public DateFromUrlExtractor(
            boolean acceptSingleDigitDay, boolean acceptSingleDigitMonth, boolean acceptTwoDigitYear, 
            List<String> dateFormatPatterns, TimeZone inputTimeZone, TimeZone outputTimeZone) {
        this(new DateStringFromUrlExtractor(
                acceptSingleDigitDay, acceptSingleDigitMonth, acceptTwoDigitYear
            ), 
            new DateExtractor(
                dateFormatPatterns, inputTimeZone, outputTimeZone
            )
        );
    }
    
    public DateFromUrlExtractor(
            Pattern patternOfDateInUrl, 
            List<String> dateFormatPatterns, TimeZone inputTimeZone, TimeZone outputTimeZone) {
        this(new DateStringFromUrlExtractor(patternOfDateInUrl), 
             new DateExtractor(
                dateFormatPatterns, inputTimeZone, outputTimeZone
        ));
    }
    
    public DateFromUrlExtractor(
            Extractor<String> dateStringFromUrlExtractor, 
            Extractor<Date> dateFromDateStringExtractor) {
        this.dateStringFromUrlExtractor = Objects.requireNonNull(dateStringFromUrlExtractor);
        this.dateExtractor = Objects.requireNonNull(dateFromDateStringExtractor);
    }
    
    @Override
    public Date extract(String url, Date defaultOutput) {
        
        final String dateString = this.dateStringFromUrlExtractor.extract(url, null);

        XLogger.getInstance().log(Level.FINER, "Found date string: {0}, in URL: {1}", 
                this.getClass(), dateString, url);
        
        Date output;
        
        if(dateString != null) {
            
            output = this.dateExtractor.extract(dateString, defaultOutput);
            
        }else{
            
            output = defaultOutput;
        }
        
        return output;
    }
}
