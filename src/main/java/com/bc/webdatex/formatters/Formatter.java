package com.bc.webdatex.formatters;

import java.io.Serializable;
import java.util.function.UnaryOperator;

public interface Formatter<E> extends UnaryOperator<E>, Serializable {
    
  
}
