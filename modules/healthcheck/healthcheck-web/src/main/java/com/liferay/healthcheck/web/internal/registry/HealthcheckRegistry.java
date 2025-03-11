package com.liferay.healthcheck.web.internal.registry;

import org.osgi.service.component.annotations.Component;

@Component(
		immediate=true, 
		service=HealthcheckRegistry.class
)
public class HealthcheckRegistry {
    public void registerHealthcheckResult(int failures) {
        _lastFailures = failures;
    }
    
    public int getHealthcheckResult() {
        return _lastFailures;
    }
    
    public boolean didRun() {
        return _lastFailures != -1;
    }
    
    private int _lastFailures = -1;
}
