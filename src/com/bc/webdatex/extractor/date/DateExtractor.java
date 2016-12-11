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
import com.bc.webdatex.converter.Converter;
import com.bc.webdatex.converter.DateTimeConverter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.logging.Level;
import com.bc.webdatex.extractor.TextParser;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 3, 2016 8:20:03 PM
 */
public class DateExtractor implements TextParser<Date> {

    private final Collection<String> dateFormatPatterns;
    private final TimeZone inputTimeZone;
    private final TimeZone outputTimeZone;
    private final Converter<Date, Date> converter;
    private final SimpleDateFormat dateFormat;
    private final TextParser<TimeZone> timeZoneExtractor;
    private final TextParser<String> dateStringExtractor;

    public DateExtractor(Collection<String> dateFormatPatterns, 
            TimeZone inputTimeZone, TimeZone outputTimeZone) {
        
        this(getDateFormat(), dateFormatPatterns, inputTimeZone, outputTimeZone);
    }
    
    static SimpleDateFormat getDateFormat() {
        SimpleDateFormat df = new SimpleDateFormatAddTimeIfNone();
        df.setLenient(true);
        return df; 
    }
    
    public DateExtractor(
            SimpleDateFormat dateFormat, Collection<String> dateFormatPatterns, 
            TimeZone inputTimeZone, TimeZone outputTimeZone) {
        
        List<String> allPatterns = new ArrayList<>(dateFormatPatterns.size() + 3);
        allPatterns.addAll(dateFormatPatterns);
        allPatterns.addAll(Arrays.asList("MMMM dd',' yyyy", "EEEE',' MMMM dd',' yyyy", 
                "EEEE',' d MMMM yyyy", "MM/dd/yyyy KK:mm:ss a"));
        this.dateFormatPatterns = Collections.unmodifiableCollection(allPatterns);
        
        this.inputTimeZone = Objects.requireNonNull(inputTimeZone);
        this.outputTimeZone = Objects.requireNonNull(outputTimeZone);
        
        this.converter = inputTimeZone.equals(outputTimeZone) ?
                Converter.NO_OP : new DateTimeConverter(inputTimeZone, outputTimeZone);
        
        this.dateFormat = dateFormat;
        
        this.timeZoneExtractor = new IsoTimeZoneExtractor();
        
        this.dateStringExtractor = new DateStringExtractorImpl();
    }
    
    @Override
    public Date extract(String dateString, Date defaultOutput) {
        
        final XLogger logger = XLogger.getInstance();
        final Class cls = this.getClass();
        final Level level = Level.FINER;
        
        Date output = defaultOutput;
        
        final String beforeFormat = dateString;
        
        dateString = this.dateStringExtractor.extract(dateString, dateString);

        ParseException parseException = null;
        
        for(String dateFormatPattern:dateFormatPatterns) {

            try{
                
                output = this.extract(logger, level, cls, dateFormatPattern, dateString);
                
                break;

            }catch(ParseException e) {

                if(parseException  == null) {
                    parseException = e;
                }else{
                    parseException.addSuppressed(e);
                }
            }
        }
        
        if(output == defaultOutput && parseException != null) {
            logger.log(Level.WARNING, "{0}\nError parsing: {1}, even after formatting to: {2}. DateFormats: {3}",
                    cls, parseException, beforeFormat, dateString, this.dateFormatPatterns);
        }
        
        return output;
    }
    
    private Date extract(
            XLogger logger, Level level, Class cls, 
            String dateFormatPattern, String dateString) throws ParseException {
        
        dateFormat.applyPattern(dateFormatPattern);
        
        final Date date_outputTimeZone;
        
        final String from;
        final String to;
        
        if(this.containsTimeZoneFormat(dateFormatPattern)) {
            
            date_outputTimeZone = this.parse(dateString, outputTimeZone);
            
            from = dateString;
            to = this.format(date_outputTimeZone, outputTimeZone);
            
        }else{
            
            final TimeZone timeZoneFromText = this.timeZoneExtractor.extract(dateString, null);
            
            Date date_inputTimeZone = parse(dateString, timeZoneFromText==null?inputTimeZone:timeZoneFromText);
            
            date_outputTimeZone = this.converter.convert(date_inputTimeZone);
            
            from = this.format(date_inputTimeZone, inputTimeZone);
            to = this.format(date_inputTimeZone, outputTimeZone);
        }
        
        if(logger.isLoggable(level, cls)) {

            logger.log(level, 
                "TimeZone: {0}, date: {1}, Output TimeZone: {2}, date: {3}", cls,
                inputTimeZone.getID(), from, outputTimeZone.getID(), to);
        }

        return date_outputTimeZone;
    }
    
    public Date parse(String dateStr, TimeZone timeZone) throws ParseException {
        dateFormat.setTimeZone(timeZone);
        return dateFormat.parse(dateStr);
    }
    
    public String format(Date date, TimeZone timeZone) {
        dateFormat.setTimeZone(timeZone);
        return dateFormat.format(date);
    }
    
    private boolean containsTimeZoneFormat(String dateFormatPattern) {
        return dateFormatPattern.endsWith(" z") ||
                dateFormatPattern.startsWith("z ") ||
                dateFormatPattern.contains(" z ");
    }
}
