/*
 * Copyright (C) 2023-2025 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.phase4.peppolstandalone.config;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.stereotype.Component;

/**
 * Bridge class that converts Spring Boot YAML configuration to properties format
 * for the phase4 library to consume.
 */
@Component
public class YamlConfigurationBridge implements ApplicationListener<ApplicationEnvironmentPreparedEvent>
{
  private static final Logger LOGGER = LoggerFactory.getLogger (YamlConfigurationBridge.class);
  
  @Override
  public void onApplicationEvent(final ApplicationEnvironmentPreparedEvent event)
  {
    final ConfigurableEnvironment environment = event.getEnvironment();
    
    try 
    {
      final Properties props = new Properties();
      
      // Global settings
      addPropertyIfExists(props, environment, "global.debug");
      addPropertyIfExists(props, environment, "global.production");
      addPropertyIfExists(props, environment, "global.nostartupinfo");
      addPropertyIfExists(props, environment, "global.datapath");
      
      // Peppol settings
      addPropertyIfExists(props, environment, "peppol.stage");
      addPropertyIfExists(props, environment, "peppol.seatid");
      addPropertyIfExists(props, environment, "peppol.owner.countrycode");
      addPropertyIfExists(props, environment, "peppol.reporting.senderid");
      addPropertyIfExists(props, environment, "peppol.reporting.scheduled");
      
      // Phase4 settings
      addPropertyIfExists(props, environment, "phase4.endpoint.address");
      addPropertyIfExists(props, environment, "phase4.api.requiredtoken");
      addPropertyIfExists(props, environment, "phase4.dump.path");
      
      // Crypto settings
      addPropertyIfExists(props, environment, "org.apache.wss4j.crypto.merlin.keystore.type");
      addPropertyIfExists(props, environment, "org.apache.wss4j.crypto.merlin.keystore.file");
      addPropertyIfExists(props, environment, "org.apache.wss4j.crypto.merlin.keystore.password");
      addPropertyIfExists(props, environment, "org.apache.wss4j.crypto.merlin.keystore.alias");
      addPropertyIfExists(props, environment, "org.apache.wss4j.crypto.merlin.keystore.private.password");
      addPropertyIfExists(props, environment, "org.apache.wss4j.crypto.merlin.truststore.type");
      addPropertyIfExists(props, environment, "org.apache.wss4j.crypto.merlin.truststore.file");
      addPropertyIfExists(props, environment, "org.apache.wss4j.crypto.merlin.truststore.password");
      
      // Add the properties as system properties so phase4 can access them
      for (final String key : props.stringPropertyNames())
      {
        System.setProperty(key, props.getProperty(key));
      }
      
      // Also add to Spring environment for immediate availability
      environment.getPropertySources().addFirst(new PropertiesPropertySource("yamlBridge", props));
      
      LOGGER.info("Successfully bridged {} YAML properties to system properties for phase4", props.size());
    }
    catch (Exception e)
    {
      LOGGER.error("Failed to convert YAML configuration to properties", e);
      throw new RuntimeException("Configuration conversion failed", e);
    }
  }
  
  private void addPropertyIfExists(final Properties props, final ConfigurableEnvironment environment, final String key)
  {
    final String value = environment.getProperty(key);
    if (value != null)
    {
      props.setProperty(key, value);
      LOGGER.debug("Bridged property: {} = {}", key, value);
    }
  }
}