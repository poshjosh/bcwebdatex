package com.bc.webdatex.formatter;

import java.io.Serializable;

public interface Formatter<E> extends Serializable {
    
  E format(E input);
}
