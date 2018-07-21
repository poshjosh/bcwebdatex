/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bc.webdatex.functions;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * @author Chinomso Bassey Ikwuagwu on Mar 24, 2018 4:02:04 PM
 */
public class FindValueWithMatchingKey 
        implements BiFunction<Map, String, String>, Serializable {

  @Override
  public String apply(Map m, String text) {
      
    Object column = null;
    
    Iterator iter = m.keySet().iterator();
    
    while (iter.hasNext()){
        
      String key = iter.next().toString();
      
      String tgt;
      String input;
      
      if (key.length() > text.length()) {
        tgt = text.toLowerCase();
        input = key.toLowerCase();
      } else {
        tgt = key.toLowerCase();
        input = text.toLowerCase();
      }
      
      if (input.contains(tgt)) {
        column = m.get(key);
        break;
      }
    }
    
    return column == null ? null : column.toString();
  }
}
