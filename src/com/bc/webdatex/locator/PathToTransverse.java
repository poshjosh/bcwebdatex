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

import com.bc.util.XLogger;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 20, 2016 8:07:56 PM
 */
public class PathToTransverse {

    public List<String> [] toTransverse(List path) {
        return this.toTransverse(path.toArray(new Object[0]));
    }
    
    public List<String> [] toTransverse(Object [] path) {

        List<String> [] output;
        
        if(path == null) {
        
            output = null;
            
        }else{
            
            TransverseList transverseList = new TransverseSet(path);

            output = transverseList.getBackingArray();
        }
        
        if(XLogger.getInstance().isLoggable(Level.FINER, this.getClass())) {        
            XLogger.getInstance().log(Level.FINER, "Path: {0}, Transverse: {1}", 
                    this.getClass(), Arrays.toString(path), Arrays.toString(output)); 
        }

        return output;
    }
    
}
