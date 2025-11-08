package com.aslmk.trackerservice.client;

import com.aslmk.trackerservice.dto.UserInfoDto;

public interface AuthServiceClient {
    String fetchUserAccessToken(UserInfoDto userInfo);
}
