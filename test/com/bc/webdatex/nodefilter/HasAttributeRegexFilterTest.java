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
package com.bc.webdatex.nodefilter;

import org.htmlparser.filters.HasAttributeRegexFilter;
import com.bc.webdatex.TestBase;
import com.bc.webdatex.BaseCrawler;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.htmlparser.Node;
import org.htmlparser.Tag;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.junit.Test;

/**
 *
 * @author Josh
 */
public class HasAttributeRegexFilterTest extends TestBase {
    
    public HasAttributeRegexFilterTest() {
        super(Level.FINE);
    }

    /**
     * Test of accept method, of class HasAttributeRegexFilter.
     * @throws org.htmlparser.util.ParserException
     */
    @Test
    public void testAccept() throws ParserException {
        this.testAccept("naij");
    }

    private void testAccept(String site) throws ParserException {
        
        System.out.println("accept");
        System.out.println("--------------------- "+site+" ---------------------");
        final String url = this.getUrl(site);
        System.out.println("URL: "+url);
        BaseCrawler parser = new BaseCrawler();
        NodeList nodes = parser.parse(url);
        final String attributeName = "property";
        
        List<String> values = new ArrayList();
        values.add("article:tag");
        values.add("article:section");
        values.add("book:tag");
        values.add("book:section");
        this.test(nodes, attributeName, values, false);

        values.clear();
        values.add(Pattern.quote(":tag"));
        values.add(Pattern.quote(":section"));
        this.test(nodes, attributeName, values, true);
    }
    
    private void test(NodeList nodes, String name, List<String> values, boolean regex) {
        HasAttributeFilter instance = !regex ? new HasAttributeFilter() : new HasAttributeRegexFilter();
        instance.setAttributeName(name);
        for(String value : values) {
            instance.setAttributeValue(value);
            NodeList matchingNodes = nodes.extractAllNodesThatMatch(instance, true);
System.out.println("Attribute: " + name + '=' + value);
            for(Node node : matchingNodes) {
                if(node instanceof Tag) {
System.out.println(((Tag)node).toTagHtml());                    
                }
            }
        }
    }
}
