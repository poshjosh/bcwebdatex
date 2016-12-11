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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.bc.webdatex.extractor.TextParser;

/**
 *
 * @author Josh
 */
public class DateExtractorTest {
    
    public DateExtractorTest() { }
    
    private void printOffsetInMinutes(TimeZone timeZone, long time) {
        final long offsetMillis = timeZone.getOffset(time);
        System.out.println("Add "+TimeUnit.MILLISECONDS.toMinutes(offsetMillis)+" minutes to UTC time to get time in TimeZone: "+timeZone.getID());
    }
    
    /**
     * Test of format method, of class DateExtractor.
     */
    @Test
    public void testAll() {
        
System.out.println("\nCurrent time");
this.print(TimeZone.getDefault(), new Date());

        List patterns = Arrays.asList("MM/dd/yy HH:mm a", "yyyy MMM dd HH:mm:ss", "MMMM dd, yyyy HH:mm");
        TimeZone in = TimeZone.getDefault();
        TimeZone out = TimeZone.getTimeZone("Asia/Kolkata");
        
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        dateFormat.setLenient(true);
        TextParser<Date> instance = new DateExtractor(dateFormat, patterns, in, out); 
        
        String dateString = "10/14/16 12:00 AM";
        Date result = instance.extract(dateString, null);
System.out.println("\n");
this.print(out, result);

        dateString = "2016 Oct 03 21:54:30";
        result = instance.extract(dateString, null);
        
System.out.println("\n");
this.print(out, result);

        dateString = "Oct 03, 2016 21:54:30";
        result = instance.extract(dateString, null);
        
System.out.println("\n");
this.print(out, result);
    }
    
    private void print(TimeZone timeZone, Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.setTimeZone(timeZone);
System.out.println(sdf.format(date));        
System.out.println(timeZone);
        final long offset = timeZone.getOffset(date.getTime());
System.out.println("Offset in minutes: "+TimeUnit.MILLISECONDS.toMinutes(offset));
    }
}
