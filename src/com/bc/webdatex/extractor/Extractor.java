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

package com.bc.webdatex.extractor;

/**
 * @author Chinomso Bassey Ikwuagwu on Sep 17, 2016 12:41:58 PM
 * @param <O> The <tt>type</tt> of the output.
 */
public interface Extractor<O> {
    
    Extractor NO_INSTANCE = new Extractor() { 
        @Override
        public Object extract(String input, Object defaultOutput) {
            return defaultOutput;
        }
    };

    O extract(String input, O defaultOutput);
}
