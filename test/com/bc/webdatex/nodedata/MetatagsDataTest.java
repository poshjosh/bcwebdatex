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
package com.bc.webdatex.nodedata;

import com.bc.net.ConnectionManager;
import com.bc.webdatex.TestBase;
import com.bc.webdatex.URLParser;
import com.bc.webdatex.util.ImageInfo;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Josh
 */
public class MetatagsDataTest extends TestBase {
    
    public MetatagsDataTest() { 
        super(Level.FINE);
    }
    
    @BeforeClass
    public static void setUpClass() { }
    @AfterClass
    public static void tearDownClass() { }
    @Before
    public void setUp() { }
    @After
    public void tearDown() { }

    @Test
    public void testAll() throws ParserException {
        System.out.println("testAll");
        this.test("naij");
        this.test("thisday");
        this.testAcceptImageUrl("leadership.ng");
    }

    private void testAcceptImageUrl(String site) throws ParserException {
System.out.println("testAcceptImageUrl. Site: "+site);        
System.out.println("----------------------------------------------------");
        final String url = this.getUrl(site);
System.out.println("URL: "+url);        
        final URLParser parser = new URLParser();
        final NodeList nodes = parser.parse(url);
        final List dateFmtPatterns = Arrays.asList("yyyy-MM-dd'T'HH:mm:ss");
        final TimeZone inputTimeZone = TimeZone.getTimeZone("CET");
        final TimeZone outputTimeZone = TimeZone.getTimeZone("IST");
        
        MetatagsDataBuilder builder = new MetatagsDataBuilder();
        
        BasicMetatagsData metatagsData = builder.dom(url, nodes)
                .dateExtractor(dateFmtPatterns, inputTimeZone, outputTimeZone)
                .build(BasicMetatagsData.class);
        
        String [] imageUrls = {
            "",
            "http://leadership.ng/wp-content/uploads/2016/09/buhari.jpg",
            "http://leadership.ng/wp-content/uploads/2016/04/Unilorin.jpg",
            "http://leadership.ng/wp-content/uploads/2016/10/CJN-mahmud-2-1.jpg",
            "http://leadership.ng/wp-content/uploads/2016/09/buhari-new-1.jpg",
            "https://fb.onthe.io/vllkytaHR0cDovL2kuY2RubmFpai5jb20vby92Vks4UzNuTW9UOTdNMXZmUUQwcVVUTGMuanBn.prx.r600x315.a270ab2c.jpg",
            "https://fb.onthe.io/vllkytaHR0cDovL2kuY2RubmFpai5jb20vby9XcnFpelc0bkpWR3ZKekdmYTQyODBtVjAuanBn.prx.r600x315.03027e25.jpg"
        };
        
        ConnectionManager connMgr = metatagsData.getConnectionManager();
        connMgr.setConnectTimeout(7000);
        connMgr.setReadTimeout(7000);
        ImageInfo imageInfo = new ImageInfo();
        
        for(String imageUrl : imageUrls) {
        
            boolean accepted = metatagsData.acceptImageUrl(connMgr, imageInfo, imageUrl, true);
            
System.out.println("Accepted: "+accepted+", imageUrl: "+imageUrl);            
        }
    }
    
    private void test(String site) throws ParserException {
System.out.println("--------------------- "+site+" ---------------------");        
        final String url = this.getUrl(site);
System.out.println("URL: "+url);        
        final URLParser parser = new URLParser();
        final NodeList nodes = parser.parse(url);
        final List dateFmtPatterns = Arrays.asList("yyyy-MM-dd'T'HH:mm:ss");
        final TimeZone inputTimeZone = TimeZone.getTimeZone("CET");
        final TimeZone outputTimeZone = TimeZone.getTimeZone("IST");
        
        MetatagsDataBuilder builder = new MetatagsDataBuilder();
        
        MetatagsData lhs = builder.dom(url, nodes)
                .dateExtractor(dateFmtPatterns, inputTimeZone, outputTimeZone)
                .build(MetatagsDataImpl.class);

        MetatagsData rhs = builder.buildComposite(
                BasicMetatagsData.class, SchemaArticle.class, OpenGraph.class, TwitterCard.class);
        
        this.print(lhs, rhs);
    }
    
    private void print(MetatagsData... arr) {
        Method[] methods = MetatagsData.class.getMethods();
        for(Method method : methods) {
            final String methodName = method.getName();
            if(!methodName.startsWith("get")) {
                continue;
            }
            System.out.println('\t'+methodName);            
            for(MetatagsData instance : arr) {
                try{
                    System.out.println(method.invoke(instance));
                }catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    String msg = "Error invoking: "+instance.getClass().getName()+'#'+methodName;
                    throw new RuntimeException(msg, e);
                }
            }
        }
    }

    private MetatagsData getCompositeInstance(
            Dom dom, Collection<String> dateFormatPatterns,
            TimeZone inputTimeZone, TimeZone outputTimeZone, 
            Class<? extends MetatagsData>... types) {
        return this.getCompositeInstance(dom, dateFormatPatterns, 
                inputTimeZone, outputTimeZone, Arrays.asList(types));
    }
    
    private MetatagsData getCompositeInstance(
            Dom dom, Collection<String> dateFormatPatterns,
            TimeZone inputTimeZone, TimeZone outputTimeZone, 
            Collection<Class<? extends MetatagsData>> types) {
        List<MetatagsData> list = new ArrayList(types.size());
        for(Class type : types) {
            list.add(this.getInstance(dom, dateFormatPatterns, inputTimeZone, outputTimeZone, type));
        }
        MetatagsData chain = new MetatagsDataChain(list);
        return chain;
    }
    
    private MetatagsData getInstance(
            Dom dom, Collection<String> dateFormatPatterns,
            TimeZone inputTimeZone, TimeZone outputTimeZone,
            Class<? extends MetatagsData> type) {
        try{
            Constructor constructor = type.getConstructor(Dom.class, Collection.class, TimeZone.class, TimeZone.class);
            return (MetatagsData)constructor.newInstance(dom, dateFormatPatterns, inputTimeZone, outputTimeZone);
        }catch(NoSuchMethodException | SecurityException | InstantiationException | 
                IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
