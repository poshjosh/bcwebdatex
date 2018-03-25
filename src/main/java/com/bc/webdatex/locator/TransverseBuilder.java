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

package com.bc.webdatex.locator;

import java.util.List;
import org.htmlparser.Tag;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 20, 2016 7:32:53 PM
 */
public interface TransverseBuilder {

    /**
     * @param tag
     * @return
     * @throws IllegalArgumentException if the input Tag does not have any parent Node
     */
    List<String> build(Tag tag);
}
