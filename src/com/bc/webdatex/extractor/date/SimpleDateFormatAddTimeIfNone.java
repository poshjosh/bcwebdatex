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

import com.bc.util.XLogger;
import com.bc.webdatex.filter.AcceptDateHasTime;
import com.bc.webdatex.filter.Filter;
import java.text.DateFormatSymbols;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 14, 2016 2:42:21 PM
 */
public class SimpleDateFormatAddTimeIfNone extends SimpleDateFormat {

    private final Filter<Date> acceptDateHasTime = new AcceptDateHasTime();
    
    private final Calendar mCalendar = new java.util.GregorianCalendar();
    
    public SimpleDateFormatAddTimeIfNone() { }

    public SimpleDateFormatAddTimeIfNone(String pattern) {
        super(pattern);
    }

    public SimpleDateFormatAddTimeIfNone(String pattern, Locale locale) {
        super(pattern, locale);
    }

    public SimpleDateFormatAddTimeIfNone(String pattern, DateFormatSymbols formatSymbols) {
        super(pattern, formatSymbols);
    }

    @Override
    public Date parse(String text, ParsePosition pos) {

        Date date = super.parse(text, pos);

        if(date != null && !this.acceptDateHasTime.test(date)) {
            
            this.mCalendar.setTimeZone(this.getTimeZone());
         
            this.mCalendar.setTimeInMillis(System.currentTimeMillis());
            final int HOURS = this.mCalendar.get(Calendar.HOUR_OF_DAY);
            final int MINUTES = this.mCalendar.get(Calendar.MINUTE);
            final int SECONDS = this.mCalendar.get(Calendar.SECOND);
            
            this.mCalendar.setTime(date);
            this.mCalendar.set(Calendar.HOUR_OF_DAY, HOURS);
            this.mCalendar.set(Calendar.MINUTE, MINUTES);
            this.mCalendar.set(Calendar.SECOND, SECONDS);
            
            Date update = this.mCalendar.getTime();
            
XLogger.getInstance().log(Level.FINE, "Added time: {0}:{1}. From: {2} to {3}", 
        this.getClass(), HOURS, MINUTES, date, update);

            date = update;
        }
        
        return date;
    }
}
