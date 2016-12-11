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
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.bc.webdatex.extractor.TextParser;

/**
 *
 * @author Josh
 */
public class DateFromUrlExtractorTest {
    
    public DateFromUrlExtractorTest() { }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of extract method, of class DateFromUrlExtractor.
     */
    @Test
    public void testExtract() {
        System.out.println("extract()");
        
        Map<String, Date> ioPairs = new LinkedHashMap();
        Calendar cal = Calendar.getInstance();
        cal.set(2016, 7, 15, 0, 0, 0);
        ioPairs.put("http://www.abc.com.ng/dir/15-08-2016/file.xhtml", cal.getTime());
        cal.set(2016, 7, 1, 0, 0, 0);
        ioPairs.put("http://www.abc.com.ng/dir/08-2016/file.xhtml", cal.getTime());
        ioPairs.put("http://www.abc.com.ng/dir/8-2016/file.xhtml", cal.getTime());
        ioPairs.put("http://www.abc.com.ng/dir/2016/file.xhtml", null);
        
        Date defaultOutput = null;
        boolean acceptSingleDigitDay = true;
        boolean acceptSingleDigitMonth = true; 
        boolean acceptTwoDigitYear = false;
        DateStringFromUrlExtractor dateStringExtractor =  
                new DateStringFromUrlExtractor(acceptSingleDigitDay, acceptSingleDigitMonth, acceptTwoDigitYear);
        TimeZone from = TimeZone.getDefault();
        TimeZone to = from;
        SimpleDateFormat dateFormat = new SimpleDateFormat();
        dateFormat.setLenient(true);
        TextParser<Date> dateFromDateStringExtractor = new DateExtractor(
                dateFormat, Arrays.asList("dd-MM-yyyy", "MM-yyyy"), from, to);
        DateFromUrlExtractor instance = new DateFromUrlExtractor(
                dateStringExtractor, dateFromDateStringExtractor);
       
        Set<String> keys = ioPairs.keySet();
        for(String url:keys) {
            Date expResult = ioPairs.get(url);
            Date result = instance.extract(url, defaultOutput);
System.out.println("\n     URL: "+url);            
System.out.println("Expected: "+expResult+", found: "+result);            
            assertEquals(this.getTimeSeconds(expResult, 0), this.getTimeSeconds(result, 0));
        }
    }
    
    private long getTimeSeconds(Date date, long defaultValue) {
        return date == null ? defaultValue : date.getTime() / 1000;
    }
}
