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

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Josh
 */
public class DateTimeConverterTest {
    
    public DateTimeConverterTest() { }
    
    /**
     * Test of convert method, of class DateTimeConverter.
     */
    @Test
    public void testConvertThenReverse() {
        System.out.println("convertThenReverse");
        
        TimeZone defaultTz = TimeZone.getDefault();
this.printTimeZoneOffset(defaultTz);
        
        TimeZone asiaKolkata = TimeZone.getTimeZone("Asia/Kolkata");
this.printTimeZoneOffset(asiaKolkata);
        
        TimeZone asiaTokyo = TimeZone.getTimeZone("Asia/Tokyo");
this.printTimeZoneOffset(asiaTokyo);

        Calendar calendar = Calendar.getInstance();
        
        this.testConvertThenReverse(calendar.getTime(), defaultTz, asiaKolkata, 4.5f);
        
        this.testConvertThenReverse(calendar.getTime(), asiaKolkata, defaultTz, -4.5f);
        
//        calendar.set(Calendar.HOUR, 0);
//        calendar.set(Calendar.MINUTE, 0);
//        calendar.set(Calendar.SECOND, 0);
//        calendar.set(Calendar.MILLISECOND, 0);
    
        this.testConvertThenReverse(calendar.getTime(), asiaTokyo, defaultTz, -8.0f);
        
        this.testConvertThenReverse(calendar.getTime(), defaultTz, asiaTokyo, 8.0f);
        
//System.out.println(Arrays.toString(TimeZone.getAvailableIDs()).replace(", ", ",\n"));
    }
    
    private void printTimeZoneOffset(TimeZone tz) {
System.out.println(tz.getID()+". Add "+(TimeUnit.MILLISECONDS.toMinutes(tz.getOffset(System.currentTimeMillis())))+" minutes to UTC to get local time in this TimeZone.");        
    }

    private void testConvertThenReverse(Date date, TimeZone from, TimeZone to, float offset) {
        
        final long time = date.getTime();
System.out.println("Input: "+TimeUnit.MILLISECONDS.toMinutes(time));

        DateTimeConverter instance = new DateTimeConverter(from, to);
System.out.println("TimeZones. From: "+from.getID()+", To: "+to.getID());

        final Predicate<Date> dateFilter = instance.getDateFilter();
                
        final long expResult = !dateFilter.test(date) ? time : time + TimeUnit.MINUTES.toMillis((long)(offset * 60));
        
        Date result = instance.convert(date);
System.out.println("Expected: "+TimeUnit.MILLISECONDS.toMinutes(expResult)+", found: "+TimeUnit.MILLISECONDS.toMinutes(result.getTime()));        
        assertEquals(expResult, result.getTime());
        
        final long resultTime = result.getTime();
        
        final long expResult2 = !dateFilter.test(result) ? resultTime :  time;
        
        Date result2 = instance.reverse(result);
System.out.println("Expected: "+TimeUnit.MILLISECONDS.toMinutes(expResult2)+", found: "+TimeUnit.MILLISECONDS.toMinutes(result2.getTime()));                
        assertEquals(expResult2, result2.getTime());
    }
}
