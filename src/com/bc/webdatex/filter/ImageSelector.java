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

package com.bc.webdatex.filter;

import com.bc.util.XLogger;
import com.bc.webdatex.util.ImageInfo;
import java.util.Set;
import java.util.logging.Level;

/**
 * @author Chinomso Bassey Ikwuagwu on Nov 3, 2016 7:29:21 PM
 */
public class ImageSelector extends ImageFilter {

    public ImageSelector() {
        super(null);
    }
    
    public ImageSelector(String baseUrl) {
        super(baseUrl);
    }

    public ImageSelector(String baseUrl, int connectTimeout, int readTimeout) {
        super(baseUrl, connectTimeout, readTimeout);
    }

    public ImageSelector(String baseUrl, String regexToAccept, String regexToReject) {
        super(baseUrl, regexToAccept, regexToReject);
    }

    public ImageSelector(String baseUrl, String regexToAccept, String regexToReject, int connectTimeout, int readTimeout) {
        super(baseUrl, regexToAccept, regexToReject, connectTimeout, readTimeout);
    }

    public String getFirstValidImageUrl(Set<String> imageUrls, String outputIfNone) {
        String firstValidImageUrl = outputIfNone;
final long tb4 = System.currentTimeMillis();
final long mb4 = this.freeMemory();
        if(imageUrls != null && !imageUrls.isEmpty()) {
            for(String url : imageUrls) {
                if(this.accept(url)) {
                    firstValidImageUrl = url;
                    break;
                }
            }
        }
XLogger.getInstance().log(Level.FINER, 
    "getFirstValidImageUrl. Consumed. time: {0}, memory: {1}",
    this.getClass(), System.currentTimeMillis()-tb4, mb4-this.freeMemory());
        return firstValidImageUrl;
    }

    public String getImageUrlOfLargestImage(Set<String> imageUrls, String outputIfNone) {
        String imageUrlOfLargestImage = outputIfNone;
final long tb4 = System.currentTimeMillis();
final long mb4 = this.freeMemory();
        if(imageUrls != null && !imageUrls.isEmpty()) {
            int largestSize = 0;
            for(String url : imageUrls) {
                if(this.accept(url)) {
                    final ImageInfo imageInfo = this.getImageInfo();
                    final int size = imageInfo.getWidth() * imageInfo.getHeight();
                    if(size > largestSize) {
                        largestSize = size;
                        imageUrlOfLargestImage = url;
                    }
                }
            }
        }
XLogger.getInstance().log(Level.FINER, 
    "getImageUrlOfLargestImage. Consumed. time: {0}, memory: {1}",
    this.getClass(), System.currentTimeMillis()-tb4, mb4-this.freeMemory());
        return imageUrlOfLargestImage;
    }
    
    private long freeMemory() {
        Runtime r = Runtime.getRuntime();
        return r.maxMemory() - r.totalMemory() - r.freeMemory();
    }
}
