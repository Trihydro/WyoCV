package com.trihydro.timrefresh.config;

import com.trihydro.library.model.ConfigProperties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties("config")
public class BasicConfiguration extends ConfigProperties {

}