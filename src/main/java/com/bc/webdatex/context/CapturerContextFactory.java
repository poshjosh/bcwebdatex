package com.bc.webdatex.context;

import com.bc.json.config.JsonConfig;
import com.bc.json.config.JsonConfigService;

public interface CapturerContextFactory {
    
    JsonConfigService getConfigService();

    CapturerContext getContext(JsonConfig config);

    CapturerContext getContext(String name);
}