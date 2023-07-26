package com.villa.upload.pinata.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Pinata methods.
 */
@Component
public class PinataConfig {
  @Value("${pinata.host:https://api.pinata.cloud}")
  private String host;
  @Value("${pinata.key:e028e312078838209621}")
  private String key;
  @Value("${pinata.secret:d28c66186146060dfcdccc37ae0b1f2d7e036d052fc78af189f0692893beb159}")
  private String secret;

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getSecret() {
    return secret;
  }

  public void setSecret(String secret) {
    this.secret = secret;
  }
}