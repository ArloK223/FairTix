package com.fairtix.auth.application;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
public class RecaptchaService {

  private static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

  private final RestTemplate restTemplate;
  private final boolean enabled;
  private final int failureThreshold;
  private final String secret;

  @Autowired
  public RecaptchaService(
      @Value("${auth.recaptcha.enabled:false}") boolean enabled,
      @Value("${auth.recaptcha.failure-threshold:3}") int failureThreshold,
      @Value("${recaptcha.secret:}") String secret) {
    this(new RestTemplate(), enabled, failureThreshold, secret);
  }

  RecaptchaService(RestTemplate restTemplate, boolean enabled, int failureThreshold, String secret) {
    this.restTemplate = restTemplate;
    this.enabled = enabled;
    this.failureThreshold = failureThreshold;
    this.secret = secret;
  }

  // returns true if the reCAPTCHA is enabled and has a valid secret key
  public boolean isEnabled() {
    return enabled && secret != null && !secret.isBlank();
  }

  // returns true if the number of failed login attempts >= the threshold
  public boolean isCaptchaRequired(long failedAttempts) {
    return isEnabled() && failedAttempts >= failureThreshold;
  }

  // returns the set failure threshold for triggering reCAPTCHA
  public int getFailureThreshold() {
    return failureThreshold;
  }

  // validates reCAPTCHA token and throws an exception if its invalid or missing
  public void assertValidToken(String token) {
    if (!isEnabled()) {
      return;
    }

    if (token == null || token.isBlank()) {
      throw new CaptchaRequiredException();
    }

    if (!verifyToken(token)) {
      throw new InvalidCaptchaException();
    }
  }
  /**
   * Verifies a reCAPTCHA response token against Google's siteverify endpoint.
   *
   * @param token the user-submitted reCAPTCHA response token
   * @return true when Google reports successful verification; otherwise false
   */
  private boolean verifyToken(String token) {
    try {
      MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
      form.add("secret", secret);
      form.add("response", token);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

      HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
      ResponseEntity<Map> response = restTemplate.postForEntity(VERIFY_URL, request, Map.class);

      if (response.getBody() == null) {
        return false;
      }

      return Boolean.TRUE.equals(response.getBody().get("success"));
    } catch (Exception ex) {
      return false;
    }
  }
}