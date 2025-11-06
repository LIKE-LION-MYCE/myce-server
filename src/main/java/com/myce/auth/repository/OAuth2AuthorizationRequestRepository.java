package com.myce.auth.repository;

import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

public interface OAuth2AuthorizationRequestRepository extends
        AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

}
