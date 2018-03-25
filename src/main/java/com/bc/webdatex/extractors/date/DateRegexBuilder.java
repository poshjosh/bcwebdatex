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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * @author Chinomso Bassey Ikwuagwu on Sep 17, 2016 2:27:47 PM
 */
public class DateRegexBuilder {
    
    private final int dayOrder;
    private final int monthOrder;
    private final int yearOrder;
    
    private final DigitDateRegex dateRegex;

    private class CalendarFieldPosition extends Number implements Comparable<CalendarFieldPosition> {
        int calendarFiled;
        double position;
        private CalendarFieldPosition(int calendarField, double position) {
            this.calendarFiled = calendarField;
            this.position = position;
        }
        @Override
        public int compareTo(CalendarFieldPosition o) {
            return Double.compare(this.position, o.position);
        }
        @Override
        public int intValue() {
            return (int)position;
        }
        @Override
        public long longValue() {
            return (long)position;
        }
        @Override
        public float floatValue() {
            return (float)position;
        }
        @Override
        public double doubleValue() {
            return position;
        }
    }
    
    public DateRegexBuilder() {
        this(false, false, false);
    }
    
    public DateRegexBuilder(int dayOrder, int monthOrder, int yearOrder) {
        this(false, false, false, dayOrder, monthOrder, yearOrder);
    }

    public DateRegexBuilder(boolean acceptSingleDigitDay, boolean acceptSingleDigitMonth, boolean acceptTwoDigitYear) {
        this(acceptSingleDigitDay, acceptSingleDigitMonth, acceptTwoDigitYear, 2, 1, 0);
    }
    
    public DateRegexBuilder(
            boolean acceptSingleDigitDay, boolean acceptSingleDigitMonth, boolean acceptTwoDigitYear, 
            int dayOrder, int monthOrder, int yearOrder) {
        dateRegex = new DigitDateRegexImpl(acceptSingleDigitDay, acceptSingleDigitMonth, acceptTwoDigitYear);
        this.dayOrder = dayOrder;
        this.monthOrder = monthOrder;
        this.yearOrder = yearOrder;
    }
    
    public String build() {
        StringBuilder builder = new StringBuilder();
        this.append(builder);
        return builder.toString();
    }
    
    public void append(StringBuilder builder) {
        
        List<CalendarFieldPosition> order = new ArrayList(Arrays.asList(
                new CalendarFieldPosition(Calendar.DATE, this.dayOrder),
                new CalendarFieldPosition(Calendar.MONTH, this.monthOrder),
                new CalendarFieldPosition(Calendar.YEAR, this.yearOrder)));
        
        Collections.sort(order);

        int position = 0;
        
        for(CalendarFieldPosition calendarFieldPosition:order) {
            
            final int calendarField = calendarFieldPosition.calendarFiled;
            
            final String regex = this.dateRegex.getRegex(calendarField);

            throw new UnsupportedOperationException("Not yet implemented");
            
//            ++position;
        }
    }
    
    public final int getDayOrder() {
        return dayOrder;
    }

    public final int getMonthOrder() {
        return monthOrder;
    }

    public final int getYearOrder() {
        return yearOrder;
    }
}
