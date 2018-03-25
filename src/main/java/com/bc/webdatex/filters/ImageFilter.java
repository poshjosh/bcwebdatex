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

import com.bc.imageutil.ImageInfo;
import com.bc.net.ConnectionManager;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Nov 3, 2016 7:10:22 PM
 */
public class ImageFilter extends ImageSrcFilter {

    private transient static final Logger LOG = Logger.getLogger(ImageFilter.class.getName());
    
    private final ImageInfo imageInfo;
    
    private final ConnectionManager connMgr;

    public ImageFilter(String baseUrl) {
        this(baseUrl, null, null);
    }

    public ImageFilter(String baseUrl, int connectTimeout, int readTimeout) {
        this(baseUrl, null, null, connectTimeout, readTimeout);
    }
    
    public ImageFilter(String baseUrl, String regexToAccept, String regexToReject) {
        this(baseUrl, regexToAccept, regexToReject, 7000, 7000);
    }
    
    public ImageFilter(String baseUrl, 
            String regexToAccept, String regexToReject, 
            int connectTimeout, int readTimeout) {
        super(baseUrl, regexToAccept, regexToReject);
        this.imageInfo = new ImageInfo();
        this.connMgr = new ConnectionManager();
 //       connMgr.setChunkedStreamingBuffer(8192);
        connMgr.setConnectTimeout(connectTimeout);
        connMgr.setReadTimeout(readTimeout);
        connMgr.setGenerateRandomUserAgent(true);

    }

    @Override
    public boolean isValid(String imageSrc) {
        boolean accepted;
        try{
            
            URL imageUrl = new URL(imageSrc);
            
            accepted = this.isValid(imageUrl);
            
        }catch(MalformedURLException e) {
            accepted = false;
        }
        if(!accepted) {
            log(imageSrc);
        }
        return accepted;
    }

    public boolean isValid(URL imageUrl) {
        boolean accepted;
        try(InputStream in = connMgr.getInputStream(imageUrl)) {
            imageInfo.setInput(in);
            accepted = imageInfo.check(); 
        }catch(IOException e) {
            LOG.fine(() -> "Error validating image link: "+imageUrl+". "+e.toString());
            accepted = false;
        }finally{
            imageInfo.setInput((InputStream)null);
        }
        return accepted;
    }
    
    public ImageInfo getImageInfo() {
        return imageInfo;
    }
    
    private void log(Object imageUrl) {
        LOG.log(Level.FINER, "Accepted: false. Reason: imageUrl is malformed: {0}", imageUrl);
    }
}
