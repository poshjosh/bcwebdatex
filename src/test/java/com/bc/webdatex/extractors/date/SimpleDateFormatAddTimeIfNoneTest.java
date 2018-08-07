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

import java.text.ParseException;
import java.util.Date;
import org.junit.Test;

/**
 *
 * @author Josh
 */
public class SimpleDateFormatAddTimeIfNoneTest {
    
    public SimpleDateFormatAddTimeIfNoneTest() { }

    /**
     * Test of parse method, of class SimpleDateFormatAddTimeIfNone.
     */
    @Test
    public void testParse() {
        try{
            this.doTestParse();
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void doTestParse() {
        
        System.out.println("parse");
        
        final String [] dateStrArr = {"16-09-13", "2018-08-06T00:37:41+00:00", "2018/08/06"};
        final String [] patternArr = {"dd MMM yyyy", "MMM dd, yyyy", "dd MMM yyyy", 
                "dd-MM-yyyy", "MMMM dd',' yyyy", "EEEE',' MMMM dd',' yyyy", 
                "EEEE',' d MMMM yyyy", "MM/dd/yyyy KK:mm:ss a"};
        
        final SimpleDateFormatAddTimeIfNone dateFormat = new SimpleDateFormatAddTimeIfNone("yy-MM-dd");
        final boolean lenientMayCauseFalsePositives = true;
        dateFormat.setLenient(!lenientMayCauseFalsePositives);
        
        for(String pattern : patternArr) {

            System.out.println("\nPattern: " + pattern + "\n------------------------------------");
            
            dateFormat.applyPattern(pattern);
            
            for(String dateStr : dateStrArr) {
                
//                instance.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));

                Date date = null;
                try{
                    date = dateFormat.parse(dateStr); 
                }catch(ParseException e) {
//                    System.err.println(e.toString());
                }
System.out.println("parse(String:"+dateStr+") =\t" + date); 
                if(date == null) {
                    continue;
                }
System.out.println("format(Date:"+date+") =\t" + dateFormat.format(date));        
            }
        }
    }
}
