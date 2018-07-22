package com.bc.webdatex.context;

import java.util.Map;
import com.bc.nodelocator.Path;

public interface NodeExtractorConfig {
    
    Map getDefaults();
    
    String[] getDatePatterns(); 
    
    String [] getUrlDatePatterns();

    Path<String> getPath(Object id);
    
    Path<String> getPathFlattened(Object id);

    String[] getTextToReject(Object id);

    boolean isConcatenateMultipleExtracts(Object id, boolean defaultValue);

    String getLineSeparator();

    String getPartSeparator();

    String getDefaultTitle();

    String[] getColumns(Object id);

    String[] getAttributesToExtract(Object id);
    
    String getImageUrlUnwantedRegex();
    
    String getImageUrlRequiredRegex();

    String[] getNodeToReject(Object id);

    String[] getNodeTypesToAccept(Object id);

    String[] getNodeTypesToReject(Object id);

    String[] getNodesToAccept(Object id);

    boolean isReplaceNonBreakingSpace(Object id, boolean defaultValue);
}
