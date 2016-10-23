package com.bc.webdatex.util;

import com.bc.util.XLogger;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import org.htmlparser.util.ParserException;


/**
 * @(#)ParserConnectionManager.java   02-Oct-2015 03:11:16
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */

/**
 * @author   chinomso bassey ikwuagwu
 * @version  2.0
 * @since    2.0
 */
public class ParserConnectionManager extends org.htmlparser.http.ConnectionManager {

    @Override
    public URLConnection openConnection(URL url) throws ParserException {
        
        try{
            
            URL modified = new URL(null, url.toExternalForm(), new com.bc.net.HttpStreamHandlerForBadStatusLine());
            
XLogger.getInstance().log(Level.FINER, "Input: {0}\nOutput: {1}", this.getClass(), url, modified);

            return super.openConnection(modified);
            
        }catch(MalformedURLException shouldNotHappen) {
            
            throw new ParserException(shouldNotHappen);
        }
    }
}
