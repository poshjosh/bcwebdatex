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

import java.text.ParseException;
import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Josh
 */
public class SimpleDateFormatAddTimeIfNoneTest {
    
    public SimpleDateFormatAddTimeIfNoneTest() { }
    
    @BeforeClass
    public static void setUpClass() { }
    
    @AfterClass
    public static void tearDownClass() { }
    
    @Before
    public void setUp() { }
    
    @After
    public void tearDown() { }

    /**
     * Test of parse method, of class SimpleDateFormatAddTimeIfNone.
     */
    @Test
    public void testParse() throws ParseException {
        
        System.out.println("parse");
        
        String text = "16-09-13";
        SimpleDateFormatAddTimeIfNone instance = new SimpleDateFormatAddTimeIfNone("yy-MM-dd");
        instance.setLenient(true);
//        instance.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        Date expResult = null;
        Date result = instance.parse(text);
System.out.println("Result: " + result);         
System.out.println("Result: " + instance.format(result));        
//        assertEquals(expResult, result);
    }
}
