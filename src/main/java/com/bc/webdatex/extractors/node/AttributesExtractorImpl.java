package com.bc.webdatex.extractors.node;

import java.util.logging.Level;
import java.util.regex.Pattern;
import org.htmlparser.Attribute;
import org.htmlparser.Tag;
import org.htmlparser.tags.ImageTag;
import java.io.Serializable;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * @(#)DefaultAttributesExtractor.java   20-Feb-2014 23:08:22
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * @author   chinomso bassey ikwuagwu
 * @version  0.3
 * @since    0.0
 */
public class AttributesExtractorImpl implements AttributesExtractor, Serializable {

    private static final Logger logger = Logger.getLogger(AttributesExtractorImpl.class.getName());

    private final Object id;

    public AttributesExtractorImpl() {
        this(Long.toHexString(System.currentTimeMillis()));
    }
    
    public AttributesExtractorImpl(Object id) {
        this.id = Objects.requireNonNull(id);
    }

    @Override
    public String[] extract(Tag tag, String... attributesToExtract) {
        
        final String [] extract = new String[attributesToExtract.length];
        
        for(int i=0; i<attributesToExtract.length; i++) {
            
            final String name = attributesToExtract[i];
            
            Attribute attr = tag.getAttribute(name);
            
            if(attr == null) {
                continue;
            }
            
            String value = attr.getValue();

            if(value == null) {
                continue;
            }
            
            if(tag instanceof ImageTag && name.equals("src")) {
//src=http://www.abc.com/image.jpg was often found to be 
//src=http://1.1.1.1/bmi/www.abc.com/image.jpg
                value = Pattern.compile("\\d\\.\\d\\.\\d\\.\\d/bmi/").matcher(value).replaceFirst("");                
                logger.log(Level.FINER, "Updated image source to: {0}", value);                
            }
            
            extract[i] = value;
        }

        return extract;
    }
    
    public final Object getId() {
        return id;
    }
}
