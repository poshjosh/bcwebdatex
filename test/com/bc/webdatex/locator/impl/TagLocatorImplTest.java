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
package com.bc.webdatex.locator.impl;

import com.bc.webdatex.locator.TagLocator;
import com.bc.webdatex.locator.TransverseNodeMatcher;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Josh
 */
public class TagLocatorImplTest {
    
    public TagLocatorImplTest() {
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
    
    @Test
    public void testAll() {
        
        final float tolerance = 0.1f;
        
        final String id = "";
        
        final List<String> [] transverse = new List[]{};
        
        TransverseNodeMatcher transverseNodeMatcher = new TransverseNodeMatcherImpl(tolerance);
        
        TagLocator tagLocator = new TagLocatorImpl(id, transverse, transverseNodeMatcher);
    }
}
