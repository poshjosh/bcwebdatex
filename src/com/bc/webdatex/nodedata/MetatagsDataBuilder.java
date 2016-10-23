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

import com.bc.webdatex.extractor.Extractor;
import com.bc.webdatex.extractor.date.DateExtractor;
import com.bc.webdatex.extractor.date.SimpleDateFormatAddTimeIfNone;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import org.htmlparser.util.NodeList;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 22, 2016 11:05:57 PM
 */
public class MetatagsDataBuilder {
    
    private Dom dom;
    
    private DateFormat dateFormat;
    
    private Extractor<Date> dateExtractor;

    public MetatagsDataBuilder() { }
    
    public void clear() {
        this.dom = null;
        this.dateFormat = null;
        this.dateExtractor = null;
    }
    
    public MetatagsDataBuilder dom(String url, NodeList nodeList) {
        return dom(new SimpleDom(url, nodeList));
    }
    
    public MetatagsDataBuilder dom(Dom dom) {
        this.dom = Objects.requireNonNull(dom);
        return this;
    }
    
    public MetatagsDataBuilder dateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
        this.dateExtractor = null;
        return this;
    }
    
    public MetatagsDataBuilder dateExtractor(Collection<String> dateFormatPatterns, 
            TimeZone inputTimeZone, TimeZone outputTimeZone) {
        
        return this.dateExtractor(new DateExtractor(dateFormatPatterns, inputTimeZone, outputTimeZone));
    }
    
    public MetatagsDataBuilder dateExtractor(Extractor<Date> dateExtractor) {
        this.dateExtractor = dateExtractor;
        this.dateFormat = null;
        return this;
    }

    public MetatagsData buildComposite(Class<? extends MetatagsData>... types) {
        List<MetatagsData> list = this.build(types);
        return new MetatagsDataChain(list);
    }
    
    public List<MetatagsData> build(Class<? extends MetatagsData>... types) {
        types = Objects.requireNonNull(types);
        if(dateExtractor == null && dateFormat == null) {
            dateFormat = new SimpleDateFormatAddTimeIfNone("yyyy-MM-dd'T'HH:mm:ss");
        }
        
        List<MetatagsData> output = new ArrayList(types.length);
        for(Class<? extends MetatagsData> type : types) {
            MetatagsData instance;
            if(dateFormat == null) {
                instance = this.getInstance(dom, dateExtractor, type);
            }else{
                instance = this.getInstance(dom, dateFormat, type);
            }
            output.add(instance);
        }
        return Collections.unmodifiableList(output);
    }
    
    public <E extends MetatagsData> E build(Class<E> type) {
        if(dateExtractor == null && dateFormat == null) {
            dateFormat = new SimpleDateFormatAddTimeIfNone("yyyy-MM-dd'T'HH:mm:ss");
        }
        E output;
        if(dateFormat == null) {
            output = this.getInstance(dom, dateExtractor, type);
        }else{
            output = this.getInstance(dom, dateFormat, type);
        }
        return output;
    }
    
    private <E extends MetatagsData> E getInstance(
            Dom dom, DateFormat dateFormat, Class<E> type) {
        try{
            Constructor<E> constructor = type.getConstructor(Dom.class, DateFormat.class);
            return constructor.newInstance(dom, dateFormat);
        }catch(NoSuchMethodException | SecurityException | InstantiationException | 
                IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private <E extends MetatagsData> E getInstance(
            Dom dom, Extractor<Date> dateExtractor, Class<E> type) {
        try{
            Constructor<E> constructor = type.getConstructor(Dom.class, Extractor.class);
            return constructor.newInstance(dom, dateExtractor);
        }catch(NoSuchMethodException | SecurityException | InstantiationException | 
                IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
