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
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.bc.webdatex.extractor.TextParser;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 4, 2016 8:35:55 PM
 */
public class DateStringExtractor0 implements TextParser<String> {

    private final Pattern isoDatePattern;
    private final Pattern digitDatePattern;
    private final Pattern monthsPattern;

    public DateStringExtractor0() {
        // @todo  Extract timeZone e.g +0100 
        // 2015-05-06T06:56:54+0100   Oct 3, 2016   February 28 2015 1400hrs etc
        this.isoDatePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\+\\d{2}:\\d{2}){0,1}", Pattern.CASE_INSENSITIVE);
        DigitDateRegex dateRegex = new DigitDateRegexImpl(true, true, false);
        this.digitDatePattern = Pattern.compile(dateRegex.getRegex(), Pattern.CASE_INSENSITIVE);
        this.monthsPattern = Pattern.compile("(january|jan|february|feb|march|mar|april|apr|may|june|jun|july|jul|august|aug|september|sep|october|oct|november|nov|december|dec)", Pattern.CASE_INSENSITIVE);
    }
    
    @Override
    public String extract(String dateString, String outputIfNone) { 
        final String output;
        Matcher matcher = this.isoDatePattern.matcher(dateString);
        if(matcher.find()) {
            output = matcher.group();
//System.out.println("------------------ Iso date group: "+output);            
        }else if((matcher = this.monthsPattern.matcher(dateString)).find()){

//            final String month = matcher.group();
//System.out.println("------------------ Possible date: "+dateString);                        
            final int monthLetterStart = matcher.start();
            final int monthLetterEnd = matcher.end();
            
            final String afterMonth = dateString.substring(monthLetterEnd);
            
            int amOrPmStart = -1;
            int amOrPmEnd = -1;
            Matcher apmMatcher = Pattern.compile("AM|PM", Pattern.CASE_INSENSITIVE).matcher(afterMonth);
            if(apmMatcher.find()) {
                amOrPmStart = apmMatcher.start();
                amOrPmEnd = apmMatcher.end();
            }
            
            boolean appendEnabled = false;
            int lastCharOrDigitPos = -1;
            StringBuilder dateCharsBuilder = new StringBuilder(20);
            for(int appended = 0, i=0; i<dateString.length(); i++) {
                final char ch = dateString.charAt(i);
                final boolean monthLetter = i >= monthLetterStart && i < monthLetterEnd;
                final boolean amOrPm = i >= amOrPmStart && i < amOrPmEnd;
                final boolean append;
                if(monthLetter || amOrPm) {
                    appendEnabled = true;
                    lastCharOrDigitPos = appended;
                    append = true;
                }else if(Character.isDigit(ch)) {
                    appendEnabled = true;
                    lastCharOrDigitPos = appended;
                    append = true;
                }else{
                    switch(ch) {
                        case '/':
                        case '-':
                        case ':':
                        case ' ':
                        case ',':
//                            case 'T':
//                            case '+':    
                            append = true; break;
                        default:
                            append = false;
                    }
                }
                if(appendEnabled && append) {
                    dateCharsBuilder.append(ch);
                    ++appended;
                }
            }
            if(lastCharOrDigitPos != -1) {
                output = dateCharsBuilder.substring(0, lastCharOrDigitPos+1);
            }else{
                output = dateCharsBuilder.toString();
            }
        }else if((matcher = this.digitDatePattern.matcher(dateString)).find()){    
            output = matcher.group();
//System.out.println("------------------ Digit date group: "+output);                        
        }else {
XLogger.getInstance().log(Level.FINER, 
"Could not find date pattern in: {0}", 
this.getClass(), dateString);
            output = outputIfNone;
        }
        
        return output.trim();
    }
    
    public String extract_old(String dateString, String outputIfNone) { 
        // 2015-05-06T06:56:54+0100   Oct 3, 2016   February 28 2015 1400hrs etc
        final String output;
        Matcher matcher = this.digitDatePattern.matcher(dateString);
        if(matcher.find()) {
            output = matcher.group();
        }else{
            matcher = this.monthsPattern.matcher(dateString);
            if(matcher.find()) {
//                final String month = matcher.group();
                final int start = matcher.start();
                final int end = matcher.end();
                StringBuilder dateCharsBuilder = new StringBuilder(20);
                for(int i=0; i<dateString.length(); i++) {
                    final char ch = dateString.charAt(i);
                    final boolean monthLetter = i >= start && i < end;
                    final boolean append;
                    if(monthLetter) {
                        append = true;
                    }else if(Character.isDigit(ch)) {
                        append = true;
                    }else{
                        switch(ch) {
                            case '/':
                            case ':':
                            case ' ':
                            case '-':
                            case 'T':
                            case '+':    
                                append = true; break;
                            default:
                                append = false;
                        }
                    }
                    if(append) {
                        dateCharsBuilder.append(ch);
                    }
                }
                output = dateCharsBuilder.toString();
            }else{
XLogger.getInstance().log(Level.FINER, 
    "Could not find date pattern in: {0}", 
    this.getClass(), dateString);
                output = outputIfNone;
            }
        }
        
        return output.trim();
    }
}