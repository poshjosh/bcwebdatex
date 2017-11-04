package com.bc.webdatex.formatter;

import java.io.Serializable;
import java.util.function.UnaryOperator;

public interface Formatter<E> extends UnaryOperator<E>, Serializable {
    
  
}
