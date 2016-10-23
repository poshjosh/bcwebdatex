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

import org.htmlparser.NodeFilter;
import org.htmlparser.filters.HasParentFilter;
import org.htmlparser.filters.OrFilter;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 14, 2016 8:51:53 PM
 */
public class IsOrHasParentFilter extends OrFilter {

    public IsOrHasParentFilter(NodeFilter targetFilter) {
        super(targetFilter, new HasParentFilter(targetFilter));
    }
}
