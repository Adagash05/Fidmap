package com.amsal.fidmap.user;


import org.springframework.stereotype.Service;

import java.util.List;

import static com.amsal.fidmap.user.Role.ADMIN;

@Service
public class UserMapper {


    public User toUser(AddFirstUser firstUser) {

        User user =  new User();
        user.setFullName(firstUser.getFullName());
        user.setEmail(firstUser.getEmail());
//        user.setRole(ADMIN);

        return user;
    }

    public UserDto toUserDto(User user) {

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setWorkspaceId(user.getWorkspace().getId());




        return dto;


    }
}

