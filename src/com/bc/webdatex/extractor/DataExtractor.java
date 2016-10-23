package com.bc.webdatex.extractor;

import java.util.Map;

public interface DataExtractor<E> {
    
  Map extractData(E paramE) throws Exception;
}
