package com.amsal.fidmap.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserDto {

    private UUID id;

    private String fullName;

    private String email;

    private String accessToken;

    private String refreshToken;

    private UUID workspaceId;

    private Role role;
    //optional

}
