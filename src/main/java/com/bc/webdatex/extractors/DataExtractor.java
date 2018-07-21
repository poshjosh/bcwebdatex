package com.bc.webdatex.extractors;

import java.util.Map;

public interface DataExtractor<E> {
    
  Map extractData(E source) throws Exception;
}
