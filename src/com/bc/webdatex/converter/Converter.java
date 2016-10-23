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

package com.bc.webdatex.converter;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 4, 2016 9:54:56 AM
 * @param <I>
 * @param <O>
 */
public interface Converter<I, O> {

    Converter NO_OP = new Converter() { 
        @Override
        public Object convert(Object input) {
            return input;
        }
        @Override
        public Object reverse(Object toReverse) {
            return toReverse;
        }
    };
    
    O convert(I input);
    
    I reverse(O toReverse);
}
