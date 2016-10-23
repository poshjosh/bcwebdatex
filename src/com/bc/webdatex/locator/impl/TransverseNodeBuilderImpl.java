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

package com.bc.webdatex.locator.impl;

import com.bc.webdatex.locator.TransverseNodeBuilder;
import java.util.Locale;
import org.htmlparser.Tag;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 20, 2016 3:55:15 PM
 */
public class TransverseNodeBuilderImpl implements TransverseNodeBuilder {

    private final String [] nodesToRemoveAttributes;

    public TransverseNodeBuilderImpl() {
        this(null);
    }
    
    public TransverseNodeBuilderImpl(String[] nodesToRemoveAttributes) {
        this.nodesToRemoveAttributes = nodesToRemoveAttributes;
    }
    
    @Override
    public String build(Tag tag) {
        
        String output;
        
        if(this.isNodeToRemoveAttributes(tag)) {
            
            output = '<' + tag.getTagName() + '>';
            
        }else{
            
            output = this.toTagHtml(tag);
        }
        
        return output;
    }
    
    public String toTagHtml(Tag tag) {
        
        return tag.toTagHtml();
    }

    public boolean isNodeToRemoveAttributes(Tag tag) {
        if(this.nodesToRemoveAttributes == null) {
            return false;
        }
        String name = tag.getTagName();
        for(String s:nodesToRemoveAttributes) {
            if(name.equals(s.toUpperCase(Locale.ENGLISH))) {
                return true;
            }
        }
        return false;
    }
}
