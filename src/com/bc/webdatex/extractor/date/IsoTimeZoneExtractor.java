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
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extract {@link java.util.TimeZone TimeZone} from text of format 
 * <code>+0100, -0700</code> etc
 * @author Chinomso Bassey Ikwuagwu on Oct 14, 2016 3:22:43 PM
 */
public class IsoTimeZoneExtractor implements Extractor<TimeZone> {

    private final Pattern isoTimezonePattern;
    
    public IsoTimeZoneExtractor() {
        this.isoTimezonePattern = Pattern.compile("[+-](0[0-9]|1[012])(0|3)0");
    }

    @Override
    public TimeZone extract(String input, TimeZone defaultOutput) {
        
        Matcher matcher = isoTimezonePattern.matcher(input);
        
        if(matcher.find()) {
            
            final String timeZoneId = "GMT"+matcher.group();
            
            return TimeZone.getTimeZone(timeZoneId);
            
        }else{
            
            return defaultOutput;
        }
    }
}
