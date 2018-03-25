package com.bc.webdatex.locator;

import com.bc.webdatex.locator.impl.TagLocatorImpl;
import com.bc.webdatex.locator.impl.TransverseBuilderImpl;
import com.bc.util.Log;
import com.bc.webdatex.TransverseProvider;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import org.htmlparser.Tag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Josh
 */
public class TransverseBuilderTest {
    
    private final String id = "testTargetNode0";
    
    public TransverseBuilderTest() { 
        final Log xlog = Log.getInstance();
        xlog.setLogLevel("", Level.FINE);
    }
    @BeforeClass
    public static void setUpClass() { }
    @AfterClass
    public static void tearDownClass() { }
    @Before
    public void setUp() { }
    @After
    public void tearDown() { }
    
    /**
     * Test of format method, of class TransverseBuilderImpl.
     * @throws org.htmlparser.util.ParserException
     */
    @Test
    public void testFormat() throws ParserException {
System.out.println(this.getClass().getName()+"#testFormat");

        TransverseBuilderImpl instance = new TransverseBuilderImpl();
        
        String [] path = TransverseProvider.getPath();
System.out.println("Path: "+Arrays.toString(path));        
        List[] input = instance.toTransverse(path);
        
        List[] output = instance.format(input);
        
System.out.println("Input: "+(input==null?null:Arrays.toString(input))+"\nOutput: "+(output==null?null:Arrays.toString(output)));
    }

    /**
     * Test of build method, of class TransverseBuilderImpl.
     * @throws org.htmlparser.util.ParserException
     */
    @Test
    public void testBuild() throws ParserException { 
    
System.out.println(this.getClass().getName()+"#testBuild");

        NodeList nodes = TransverseProvider.getNodeList();
        
        Tag targetNode = TransverseProvider.getTargetNode(nodes);
System.out.println("Target: "+targetNode.toTagHtml());        
        TransverseBuilderImpl instance = new TransverseBuilderImpl();
        
        List<String> path = instance.build(targetNode);
System.out.println("Path: "+path);        
        TagLocatorImpl locator = new TagLocatorImpl(id, path.toArray(new String[0]));

        nodes.visitAllNodesWith(locator);

        Tag locatedNode = locator.getTarget();

System.out.println("Target: "+targetNode.toTagHtml()+"\nLocated: "+(locatedNode==null?null:locatedNode.toTagHtml()));
        
        assertEquals(targetNode, locatedNode);
    }
}
