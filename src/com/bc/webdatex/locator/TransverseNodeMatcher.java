package com.bc.webdatex.locator;

import org.htmlparser.Tag;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 19, 2016 8:31:26 PM
 */
public interface TransverseNodeMatcher {

    boolean matches(Tag tag, String transverseNode);
}
