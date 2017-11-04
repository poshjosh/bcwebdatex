/*
 * Copyright 2017 NUROX Ltd.
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

import com.bc.webdatex.tags.ArticleTag;
import com.bc.webdatex.tags.Link;
import com.bc.webdatex.tags.NoscriptTag;
import com.bc.webdatex.tags.StrongTag;
import com.bc.webdatex.tags.Tbody;
import com.bc.webdatex.tags.Tfoot;
import com.bc.webdatex.tags.Thead;
import com.bc.webdatex.util.ParserConnectionManager;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.http.ConnectionMonitor;
import org.htmlparser.util.EncodingChangeException;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.util.ParserFeedback;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 7, 2017 4:05:42 PM
 */
public class ParserImpl extends org.htmlparser.Parser{

    private static final Logger logger = Logger.getLogger(ParserImpl.class.getName());

    private final List<String> cookies;
  
    public ParserImpl() {
        
        cookies = new ArrayList();
        
        org.htmlparser.Parser.setConnectionManager(new ParserConnectionManager());

        org.htmlparser.http.ConnectionManager cm = org.htmlparser.Parser.getConnectionManager();

        cm.setRedirectionProcessingEnabled(true);

        cm.setCookieProcessingEnabled(true);

        cm.setMonitor(newConnectionMonitor());

        PrototypicalNodeFactory factory = new PrototypicalNodeFactory();

        factory.registerTag(new StrongTag());
        factory.registerTag(new Tbody());
        factory.registerTag(new Thead());
        factory.registerTag(new Tfoot());
        factory.registerTag(new NoscriptTag());
        factory.registerTag(new ArticleTag());
        factory.registerTag(new Link());

        this.setNodeFactory(factory);

        this.setFeedback(new ParserFeedback(){
            @Override
            public void info(String message) {
                logger.info(message);

            }
            @Override
            public void warning(String message) {
                logger.warning(message);
            }
            @Override
            public void error(String message, ParserException e) {
                logger.log(Level.WARNING, message, e);
            }
        });
    }

    public NodeList parse(String url)  throws ParserException {
     
        return this.parse(url, (node) -> true);
    }

    public NodeList parse(String url, NodeFilter filter)  throws ParserException {
      
        this.setURL(url); 
        
        return this.parse(filter);
    }

    @Override
    public NodeList parse(NodeFilter filter) throws ParserException {

        NodeList list;

        try {

            list = super.parse(filter);

        }catch (EncodingChangeException ece) {

            list = applyBugfix991895(ece, filter);
        }
        
        return list;
    }

    private NodeList applyBugfix991895(EncodingChangeException ece, NodeFilter filter)
            throws ParserException {
        logger.log(Level.WARNING, ece, () -> "PARSER CRASHED! Caught: " + ece.getClass().getName() + "\nApplying bug fix #991895");

        this.reset();
        
        return super.parse(filter);
    }

    private ConnectionMonitor newConnectionMonitor(){
        
        return new ConnectionMonitor(){

            @Override
            public void preConnect(HttpURLConnection connection) throws ParserException {
                logger.log(Level.FINER, "#preConnect. Connection: {0}", connection);
                for (String cookie : cookies) {
                    final String str = cookie.split(";", 2)[0].trim();
                    System.out.println("====================================\nAdding cookie: " + str);
                    connection.addRequestProperty("Cookie", str);
                }
            }

            @Override
            public void postConnect(HttpURLConnection connection) throws ParserException {}
        };
    }

    public List<String> getCookies() {
        return cookies;
    }

    public boolean addCookies(List<String> cookieList) {
        if(cookieList == null) {
            return false;
        }else{
            cookieList.stream().forEach((cookie) -> {
                if(!cookies.contains(cookie)) {
                    cookies.add(cookie);
                }
            });
            return true;
        }
    }
}
