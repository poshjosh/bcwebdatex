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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.bc.webdatex.extractor.TextParser;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 5, 2016 6:06:36 PM
 */
public class DateStringExtractorImpl implements TextParser<String> {

    private static final Logger logger = Logger.getLogger(DateStringExtractorImpl.class.getName());

    private final Pattern isoDatePattern;
    private final Pattern digitDatePattern;
    private final Pattern monthsPattern;

    public DateStringExtractorImpl() {
        // @todo  Extract timeZone e.g +0100 
        // 2015-05-06T06:56:54+0100   Oct 3, 2016   February 28 2015 1400hrs etc
        this.isoDatePattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\+\\d{2}:\\d{2}){0,1}", Pattern.CASE_INSENSITIVE);
        DigitDateRegex dateRegex = new DigitDateRegexImpl(true, true, false);
        this.digitDatePattern = Pattern.compile(dateRegex.getRegex(), Pattern.CASE_INSENSITIVE);
        this.monthsPattern = Pattern.compile("(january|jan|february|feb|march|mar|april|apr|may|june|jun|july|jul|august|aug|september|sep|october|oct|november|nov|december|dec)", Pattern.CASE_INSENSITIVE);
    }
    
    @Override
    public String extract(String dateString, String outputIfNone) { 
        
        String output;
        
        boolean digitDateGroup = false;
        
        Matcher matcher = this.isoDatePattern.matcher(dateString);
        if(matcher.find()) {
            output = matcher.group();
//System.out.println("------------------ Iso date group: "+output);            
        }else if((matcher = this.monthsPattern.matcher(dateString)).find()){
            output = dateString;
//System.out.println("------------------ Has date chars: "+output);                                    
        }else if((matcher = this.digitDatePattern.matcher(dateString)).find()){    
            digitDateGroup = true;
            output = dateString;
//System.out.println("------------------ Digit date group: "+output);                        
        }else {
            output = null;
        }
        
        if(output != null) {
            
            final int end = this.getEndIndexOf(output, "date:", "Date:", "dated:", "Dated:");
            if(end != -1) {
                output = output.substring(end + 1);
            }

            if(digitDateGroup) {
                for(int pos=0; pos<output.length(); pos++) {
                    final char ch = output.charAt(pos);
                    if(Character.isDigit(ch)) {
                        output = pos == 0 ? output : output.substring(pos);
//System.out.println("------------------ After removing leading non-digits from 0 to "+pos+", output: "+output);           
                        break;
                    }
                }
            }else{
                for(int pos=0; pos<output.length(); pos++) {
                    final char ch = output.charAt(pos);
                    if(Character.isLetterOrDigit(ch)) {
                        output = pos == 0 ? output : output.substring(pos);
//System.out.println("------------------ After removing leading non-letters or digits from 0 to "+pos+", output: "+output);           
                        break;
                    }
                }
            }

            for(int pos=output.length()-1; pos>=0; pos--) {
                final char ch = output.charAt(pos);
                if(Character.isLetterOrDigit(ch)) {
                    output = pos == output.length() - 1 ? output : output.substring(0, pos+1);
//System.out.println("------------------ After removing trailing non-letters or digits from "+(pos+1)+", output: "+output);           
                    break;
                }
            }
        }
        
        return output == null ? outputIfNone : output;
    }

    private int getEndIndexOf(String target, String... toFinds) {
        int ret = -1;
        for(String prefixEnd : toFinds) {
            int end = this.getEndIndexOf(target, prefixEnd);
            if(end > ret) {
                ret = end;
            }
        }
        return ret;
    }
    
    private int getEndIndexOf(String target, String toFind) {
        final int i = target.indexOf(toFind);
        return i == -1 ? -1 : i + toFind.length();
    }
}