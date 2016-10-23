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

import com.bc.util.XLogger;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 14, 2016 3:49:18 PM
 */
public class AcceptDateHasTime implements Filter<Date> {

    private final Calendar calendar;

    public AcceptDateHasTime() {
        this.calendar = Calendar.getInstance();
    }
    
    @Override
    public boolean accept(Date date) {
        this.calendar.setTime(date);
        boolean output = 
                this.calendar.get(Calendar.HOUR_OF_DAY) != 0 ||
                this.calendar.get(Calendar.MINUTE) != 0 ||
                this.calendar.get(Calendar.SECOND) != 0;
        XLogger.getInstance().log(Level.FINER, "Has time: {0}, date: {1}", this.getClass(), output, date);
        return output;
    }
}
