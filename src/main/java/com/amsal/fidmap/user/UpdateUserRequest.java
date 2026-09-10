package com.amsal.fidmap.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {

    private String fullName;

    private String email;

    private String password;

    private  Role role;

}
