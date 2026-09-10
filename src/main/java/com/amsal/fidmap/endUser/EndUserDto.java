package com.amsal.fidmap.endUser;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class EndUserDto {

    private UUID id;

    private String name;

    private String email;

}
