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

import com.bc.util.Log;
import java.util.Calendar;
import java.util.logging.Level;

/**
 * @author Chinomso Bassey Ikwuagwu on Sep 17, 2016 6:17:52 PM
 */
public class DigitDateRegexImpl implements DigitDateRegex {
    
    private final boolean acceptSingleDigitDay;
    private final boolean acceptSingleDigitMonth;
    private final boolean acceptDoubleDigitYear;
    
    public DigitDateRegexImpl() {
        this(false, false, false);
    }
    
    public DigitDateRegexImpl(
            boolean acceptSingleDigitDay, boolean acceptSingleDigitMonth, boolean acceptTwoDigitYear) {
        this.acceptSingleDigitDay = acceptSingleDigitDay;
        this.acceptSingleDigitMonth = acceptSingleDigitMonth;
        this.acceptDoubleDigitYear = acceptTwoDigitYear;
    }
    
    @Override
    public String getRegex() {
        String day = this.getRegex(Calendar.DATE);
        String month = this.getRegex(Calendar.MONTH);
        String year = this.getRegex(Calendar.YEAR);
        String separator = this.getSeparatorRegex();
        String yyyyMMdd = year + separator + month + '(' + separator + day + ")?";
        String ddMMyyyy = '(' + day + separator + ")?" + month + separator + year;
        String regex = '(' + yyyyMMdd + '|' + ddMMyyyy + ')';
        Log.getInstance().log(Level.FINE, "Regex: {0}", this.getClass(), regex);
        return regex;
    }
    
    @Override
    public String getSeparatorRegex() {
        return "[- /_.]";
    }
    
    @Override
    public String getRegex(int calendarField) {
        String regex;
        switch(calendarField) {
            case Calendar.DATE:
                regex = acceptSingleDigitDay ? "(0?[1-9]^\\d|[12][0-9]|3[01])" : "(0[1-9]|[12][0-9]|3[01])";
                break;
            case Calendar.MONTH: 
                regex = acceptSingleDigitMonth ? "(0?[1-9]|1[012])" : "(0[1-9]|1[012])";
                break;
            case Calendar.YEAR:
                regex = acceptDoubleDigitYear ? "((19|20)?\\d\\d)" : "((19|20)\\d\\d)";
                break;
            default:
                throw new IllegalArgumentException("Unexpected calendar field: "+calendarField);
        }
        return regex;
    }

    @Override
    public final boolean isAcceptSingleDigitDay() {
        return acceptSingleDigitDay;
    }

    @Override
    public final boolean isAcceptSingleDigitMonth() {
        return acceptSingleDigitMonth;
    }

    @Override
    public final boolean isAcceptDoubleDigitYear() {
        return acceptDoubleDigitYear;
    }
}
