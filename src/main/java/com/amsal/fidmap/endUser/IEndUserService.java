package com.amsal.fidmap.endUser;

import com.amsal.fidmap.apiResponse.ApiResponse;

import java.util.UUID;

public interface IEndUserService {

    ApiResponse<EndUserDto> createEndUser(UUID workspaceId, EndUserDto dto);
}
