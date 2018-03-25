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
package com.bc.webdatex.image;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Test;

/**
 * @author Josh
 */
public class ImageSizeComparatorTest {
    
    public ImageSizeComparatorTest() { }

    /**
     * Test of compare method, of class ImageSizeComparator.
     */
    @Test
    public void testCompare() {
        System.out.println("compare");
        
        final ImageSizeComparator instance = new ImageSizeComparator();
        
        final String expResult = "http://leadership.ng/wp-content/uploads/2013/12/nigeria_soldiers-300x224.jpg";
        
        final List<String> imageUrls = new ArrayList<>(
                Arrays.asList(
                        "http://leadership.ng/wp-content/uploads/2016/11/donALD-TRUMPEE-300x160.jpg",
                        "http://leadership.ng/wp-content/uploads/2014/11/Mechanics-working-at-an-open-space-in-Abuja.-180x300.jpg",
                        "http://leadership.24hubs.netdna-cdn.com/wp-content/uploads/2016/03/button-extension-install.c284a921ad9c8cae6279fe7a2dea5b5c.png",
                        "http://www.leadership.ng/wp-content/uploads/2016/10/explosion-300x188.jpg",
                        "http://leadership.ng/wp-content/uploads/2013/12/nigeria_soldiers-300x224.jpg",
                        "http://leadership.ng/wp-content/uploads/2016/11/trump-wins-300x212.jpg"
                )
        );
        
        Collections.sort(imageUrls, instance);
        
        final String result = imageUrls.get(imageUrls.size() - 1);
        
System.out.println("\nExp result: "+expResult+"\n    Result: "+result);        
    }
}
