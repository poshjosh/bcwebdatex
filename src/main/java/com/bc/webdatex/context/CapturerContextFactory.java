package com.bc.webdatex.context;

import com.bc.json.config.JsonConfig;
import com.bc.json.config.JsonConfigService;

public interface CapturerContextFactory {
    
    JsonConfigService getConfigService();

    ExtractionContext getContext(JsonConfig config);

    ExtractionContext getContext(String name);
}