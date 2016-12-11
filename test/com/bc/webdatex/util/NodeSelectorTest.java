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
package com.bc.webdatex.util;

import com.bc.webdatex.extractor.Extractor;
import com.bc.webdatex.extractor.node.NodeSelector;
import com.bc.dom.HtmlPageDomImpl;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.List;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.NotFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.tags.ScriptTag;
import org.htmlparser.tags.StyleTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.junit.Test;
import com.bc.dom.HtmlPageDom;

/**
 *
 * @author Josh
 */
public class NodeSelectorTest {
    
    public NodeSelectorTest() { }

    @Test
    public void testExtract() throws ParserException {
        
        final String [] urls = {
//            "http://www.ngrguardiannews.com/2015/06/new-york-rally-launches-clintons-bid-for-white-house/"
            "http://sunnewsonline.com/trained-in-niger-delta-creeks-returned-to-kogi-set-up-robbery-gang/",
/**            
            "http://sunnewsonline.com/new/?p=123586",
            "http://thenationonlineng.net/new/first-lady-dont-pay-any-money-to-see-president/",
            "http://www.thisdaylive.com/index.php/2016/10/22/heineken-lagos-fashion-and-design-week-cocktail-party/",
            "http://www.aitonline.tv/post-council_of_state_approves_president___s_nomination_of_new_inec_chairman__5_national_commissioners",
            "http://www.channelstv.com/2015/10/21/tribunal-adjourns-trial-of-bukola-saraki-till-november-5/",
            "http://saharareporters.com/2015/06/15/exclusive-garba-shehu-speaks-saharatv-explains-delayed-ministerial-appointments",
            "https://www.naij.com/460491-photos-dprince-is-now-a-father.html",
            "http://www.vanguardngr.com/2016/09/masaris-mistakes-kachikwus-naivety/",
            "http://www.thenewsminute.com/article/why-bystanders-tend-look-other-way-when-people-get-attacked-50369",
            "http://www.leadership.ng/business/555767/economy-the-cross-of-militancy",
            "https://www.bellanaija.com/2016/06/get-inspired-with-forbes-list-of-amercias-richest-self-made-women-oprah-winfrey-beyonce-taylor-swift-sheryl-sandberg-more/",
            "http://www.lindaikejisblog.com/2016/05/okonjo-iweala-reacts-to-reports-that.html",
            "http://www.dailytrust.com.ng/news/politics/2019-inec-to-deploy-new-tech-for-collation/130017.html",
            "http://www.punchng.com/bizarre-social-media-craze-accident-victims-groan-as-sympathisers-record-agony-on-smart-phones/"
*/             
        };
        
        for(String url : urls) {
        
System.out.println("URL: "+url);

            this.testExtract(url);
            
System.out.println("--------------------------------------");            
System.out.println();
        }
    }

    private void testExtract(String url) throws ParserException {
        System.out.println("extract");
        Parser parser = new Parser();
        parser.setURL(url);
        NodeList nodes = parser.parse(null);
System.out.println("\n--------------- HTML -----------------");
System.out.println(nodes.toHtml(true).replace('\n', ' '));        
        HtmlPageDom dom = new HtmlPageDomImpl(url, nodes);
        nodes = dom.getBody().getChildren();
        
        NodeFilter filter = new NotFilter(
                new OrFilter(
                        new NodeClassFilter(ScriptTag.class), 
                        new NodeClassFilter(StyleTag.class)
                )
        );

        final int bufferSize = 32000;
        final int minLen = dom.getTitle() == null ? 0 : dom.getTitle().getTitle() == null ? 0 : dom.getTitle().getTitle().length();
        
        final int [] depths = {0, 1, 2};
        
        Dimension size;
        try{
            size = Toolkit.getDefaultToolkit().getScreenSize();
            size.setSize(size.getWidth()- size.getWidth() * 0.2, size.getHeight() - size.getHeight() * 0.2);
        }catch(Exception e) {
            e.printStackTrace();
            size = new Dimension(800, 650);
        }
        
        for(int depth : depths) {
System.out.println("\n--------------- Depth: " + depth + " -----------------");
            
            Extractor<List<Node>, Node> extractor = new NodeSelector(filter, bufferSize, minLen, depth);

            Node node = extractor.extract(nodes, Node.BLANK_NODE);
            
            JEditorPane editorPane =new JEditorPane("text/html", node.toHtml(false));
            editorPane.setSize(size);
            JScrollPane message = new JScrollPane(editorPane);
            message.setSize(size);
            
            JOptionPane.showMessageDialog(null, message, "HTML selected using depth: "+depth, JOptionPane.INFORMATION_MESSAGE);
            
System.out.println(node.toHtml().replace('\n', ' ')); 
        }
    }
}
