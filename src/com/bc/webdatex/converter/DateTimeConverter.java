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

package com.bc.webdatex.converter;

import com.bc.webdatex.filter.Filter;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 4, 2016 9:52:57 AM
 */
public class DateTimeConverter implements Converter<Date, Date> {

    private final TimeZone inputTimeZone;
    private final TimeZone outputTimeZone;
    private final Filter<Date> dateFilter;

    public DateTimeConverter(TimeZone inputTimeZone, TimeZone outputTimeZone) {
        this(Filter.NO_OP, inputTimeZone, outputTimeZone);
    }
    
    public DateTimeConverter(Filter<Date> dateFilter, TimeZone inputTimeZone, TimeZone outputTimeZone) {
        this.inputTimeZone = Objects.requireNonNull(inputTimeZone);
        this.outputTimeZone = Objects.requireNonNull(outputTimeZone);
        this.dateFilter = Objects.requireNonNull(dateFilter);
    }
    
    @Override
    public Date convert(Date date) {
        
        return this.translate(date, false);
    }
    
    @Override
    public Date reverse(Date date) {
        
        return this.translate(date, true);
    }
    
    protected Date translate(Date date, boolean add) {
        
        if(this.dateFilter.accept(date)) {
            
            final long time = date.getTime();

            final long diff = inputTimeZone.getOffset(time) - outputTimeZone.getOffset(time);

            final long update = add ? time + diff : time - diff;
            
            Date output = new Date(update);

            date = output;
        }
        
        return date;
    }

    public final String getInputTimeZoneId() {
        return inputTimeZone.getID();
    }

    public final String getOutputTimeZoneId() {
        return outputTimeZone.getID();
    }

    public final Filter<Date> getDateFilter() {
        return dateFilter;
    }
}
