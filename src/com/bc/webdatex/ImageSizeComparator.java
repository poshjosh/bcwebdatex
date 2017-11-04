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

package com.bc.webdatex;

import com.bc.webdatex.filter.ImageFilter;
import com.bc.webdatex.util.ImageInfo;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Chinomso Bassey Ikwuagwu on Nov 9, 2016 9:25:27 AM
 */
public class ImageSizeComparator implements Comparator<String> {
    
    private final Map<String, Integer> imageSizes;
    
    private final ImageFilter imageFilter;

    public ImageSizeComparator() {
        this(null);
    }
    
    public ImageSizeComparator(String baseUrl) {
        this(baseUrl, null, null);
    }

    public ImageSizeComparator(String baseUrl, int connectTimeout, int readTimeout) {
        this(baseUrl, null, null, connectTimeout, readTimeout);
    }
    
    public ImageSizeComparator(String baseUrl, String regexToAccept, String regexToReject) {
        this(baseUrl, regexToAccept, regexToReject, 7000, 7000);
    }
    
    public ImageSizeComparator(String baseUrl, 
            String regexToAccept, String regexToReject, 
            int connectTimeout, int readTimeout) {
    
        this.imageFilter = new ImageFilter(baseUrl, 
                regexToAccept, regexToReject, connectTimeout, readTimeout);
        
        this.imageSizes = new HashMap<>();
    }
    
    @Override
    public int compare(String imageUrl1, String imageUrl2) {
        
        final Integer size1 = this.getSize(imageUrl1);
        
        final Integer size2 = this.getSize(imageUrl2);
        
        return size1.compareTo(size2);
    }
    
    private Integer getSize(String imageUrl) {
        
        Integer size;
        
        if(this.imageSizes.containsKey(imageUrl)) {
            
            final Integer cachedSize = this.imageSizes.get(imageUrl);
            
            size = cachedSize == null ? 0 : cachedSize;
            
        }else{
            
            if(this.imageFilter.test(imageUrl)) {
                
                final ImageInfo imageInfo = this.imageFilter.getImageInfo();
                
                size = imageInfo.getWidth() * imageInfo.getHeight();
                
                this.imageSizes.put(imageUrl, size);
                
            }else{
                
                size = 0;
                
                this.imageSizes.put(imageUrl, null);
            }
        }
        
        assert size != null;
        
        return size;
    }
}
