package com.bc.webdatex.extractors;

import java.util.Map;

public interface DataExtractor<E> {
    
  Map extractData(E paramE) throws Exception;
}
