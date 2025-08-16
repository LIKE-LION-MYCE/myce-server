package com.myce.auth.repository;

public interface TokenBlackListRepository {

    void save(String accessToken, long limitTime);

    boolean containsByAccessToken(String accessToken);

}
