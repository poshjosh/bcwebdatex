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

import com.bc.webdatex.extractor.Extractor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Josh
 */
public class DateStringFromDateStringExtractorTest {
    
    public DateStringFromDateStringExtractorTest() { }

    /**
     * Test of extract method, of class DateStringExtractor0.
     */
    @Test
    public void testExtract() {
        System.out.println("extract");
        Map<String, String> ios = new HashMap<>();
        ios.put("By Adelanwa Bamgboye | Publish Date: Oct 4 2016 7:26PM", "Oct 4 2016 7:26PM");
        ios.put("2015-02-18T15:07:31+01:00", "2015-02-18T15:07:31+01:00");
        ios.put("2015/02/18 15:07:31 - ", "2015/02/18 15:07:31");
//        ios.put("18-02-2015 15:07:31 By the way", "18-02-2015");
        ios.put("Wednesday, October 4, 2016", "Wednesday, October 4, 2016");
        ios.put("10/05/2016 11:55:00 AM", "10/05/2016 11:55:00 AM");
        ios.put("— Oct 4, 2016 5:19 pm", "Oct 4, 2016 5:19 pm");
        Set<String> keys = ios.keySet();
        for(String key : keys) {
            final String val = ios.get(key);
            this.testExtract(key, val);
        }
    }

    private void testExtract(String dateString, String expResult) {
        String outputIfNone = null;
        Extractor<String> instance = new DateStringExtractorImpl();
        String result = instance.extract(dateString, outputIfNone);
System.out.println("Input: "+dateString+", result: "+result+", expected: "+expResult);        
        assertEquals(expResult, result);
    }
}
