package com.oa.admin.approval.config;

import com.oa.admin.approval.listener.ProcessEndEventListener;
import lombok.RequiredArgsConstructor;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class FlowableConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {
    private final ProcessEndEventListener processEndEventListener;

    @Override
    public void configure(SpringProcessEngineConfiguration engineConfig) {
        List<org.flowable.common.engine.api.delegate.event.FlowableEventListener> listeners =
            engineConfig.getEventListeners();
        if (listeners == null) {
            listeners = new ArrayList<>();
            engineConfig.setEventListeners(listeners);
        }
        listeners.add(processEndEventListener);
    }
}
