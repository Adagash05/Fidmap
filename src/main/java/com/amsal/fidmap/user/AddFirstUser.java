package com.amsal.fidmap.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddFirstUser {

    private String fullName;

    private String email;
    //optional
    private String password;
}
