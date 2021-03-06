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

package com.bc.webdatex.filters;

import com.bc.net.util.UrlProbe;
import com.bc.net.util.UrlProbeImpl;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 3, 2016 10:13:40 AM
 */
public class ImageSrcFilter implements Filter<String> {
    
    private transient static final Logger logger = Logger.getLogger(ImageSrcFilter.class.getName());
    
    private final Pattern toReject;
    
    private final Pattern toAccept;
    
    private final String baseUrl;

    private final UrlProbe urlProbe;

    public ImageSrcFilter(String baseUrl) {
    
        this(baseUrl, null, null);
    }
    
    public ImageSrcFilter(String baseUrl, String regexToAccept, String regexToReject) {
    
        this.baseUrl = baseUrl;
        
        this.toAccept = getPattern(regexToAccept);
        
        this.toReject = getPattern(regexToReject);
        
        this.urlProbe = new UrlProbeImpl();
    }
    
    private Pattern getPattern(String regex) {
         return regex == null ? null : Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public boolean test(String imageSrc) {
        
        final Level level = Level.FINER;
        
        logger.log(level, "Image src: {0}", imageSrc);

        if(rejectNullOrEmpty(imageSrc)) {
            return false;
        }

        if(baseUrl != null && !imageSrc.startsWith(baseUrl)) {
            logger.log(level, "Accepted: false. Reason: imageUrl does not start with: {0}", baseUrl);
            return false;
        }
        
        if(toReject != null && toReject.matcher(imageSrc).find()) {
            logger.log(level, "Accepted: false. Reason: imageUrl contains: {0}", toReject);
            return false;
        }
        
        if(toAccept != null && !toAccept.matcher(imageSrc).find()) {
            logger.log(level, "Accepted: false. Reason: imageUrl does not contain: {0}", toAccept);
            return false;
        }
        
        return true;
    }

    protected boolean rejectNullOrEmpty(String imageUrl) {
        return imageUrl == null || imageUrl.isEmpty();
    }

    public boolean isValid(String imageUrl) {
        boolean accepted;
        try{
            URL url = new URL(imageUrl);
            accepted = true;
        }catch(MalformedURLException e) {
            accepted = false;
        }
        if(!accepted) {
            logger.log(Level.FINER, "Accepted: false. Reason: imageUrl is malformed: {0}", imageUrl);
        }
        return accepted;
    }

    public boolean isExisting(URL imageUrl) {
        try{
            return this.urlProbe.exists(imageUrl);
        }catch(Exception e) {
            return false;
        }    
    }
}
