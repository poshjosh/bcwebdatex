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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Josh
 */
public class IsoTimeZoneExtractorTest {
    
    public IsoTimeZoneExtractorTest() {
    }
    
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
     * Test of extract method, of class IsoTimeZoneExtractor.
     */
    @Test
    public void testExtract() {
        
        System.out.println("extract");
        
        Map<String, String> io = new HashMap<>();
        
        io.put("-050", null);
        io.put("+1200", "GMT+1200");
        io.put("-0510", null);
        io.put("-0530", "GMT-0530");
        
        TimeZone defaultOutput = null;
        
        IsoTimeZoneExtractor instance = new IsoTimeZoneExtractor();
        
        Set<String> inputs = io.keySet();
        
        for(String input : inputs) {
            
            String output = io.get(input);
            
            TimeZone expResult = output == null ? null : TimeZone.getTimeZone(output);
            
            TimeZone result = instance.extract(input, defaultOutput);
            
System.out.println("Expected: "+expResult);
System.out.println("   Found: "+result);

            if(result == null && expResult == null) {
                continue;
            }
            
            assertEquals(expResult.toString(), result.toString());
        }
    }
    
}
