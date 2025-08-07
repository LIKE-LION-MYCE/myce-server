package com.myce.auth.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface AdminCodeDetailService {

    UserDetails loadLoginIdAndCode(String loginId, String code);

    UserDetails loadCode(String code);
}
