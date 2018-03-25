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

package com.bc.webdatex.extractors.date;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 4, 2016 10:52:02 AM
 */
public class DatePatternsBuilder {

//    public final List<String> build(DigitDateRegex fmt) {
//        List<String> basePatterns = Arrays.asList("yyyy/MM/dd", "yyyy/MM", "dd/MM/yyyy", "MM/yyyy");        
//        return build(fmt, basePatterns);
//    }
    
    public final List<String> build(DigitDateRegex fmt, List<String> baseDateFormatPatterns) {
        List<String> all = new ArrayList<>();
        all.addAll(baseDateFormatPatterns);
        for(String pattern:baseDateFormatPatterns) {
            List<String> offshoots = new ArrayList<>();
            if(fmt.isAcceptDoubleDigitYear()) {
                offshoots.add(pattern.replace("yyyy", "yy"));
            }
            if(fmt.isAcceptSingleDigitDay()) {
                offshoots.add(pattern.replace("dd", "d"));
            }
            if(fmt.isAcceptSingleDigitMonth()) {
                offshoots.add(pattern.replace("MM", "m"));
            }
            all.addAll(offshoots);
        }
        return all;
    }
}
