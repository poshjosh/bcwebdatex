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

package com.bc.webdatex.filter;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 14, 2016 3:48:26 PM
 * @param <E> The type to filter
 */
public interface Filter<E> {
    
    Filter NO_OP = new Filter() {
        @Override
        public boolean accept(Object e) {
            return true;
        }
    };
    
    boolean accept(E e);
}
