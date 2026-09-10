package com.amsal.fidmap.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.amsal.fidmap.user.Permissions.*;

@RequiredArgsConstructor
public enum Role {

    SUPER_ADMIN(
            Set.of(
                    SUPER_ADMIN_READ,
                    SUPER_ADMIN_CREATE,
                    SUPER_ADMIN_UPDATE,
                    SUPER_ADMIN_DELETE
            )
    ),


    OWNER(
            Set.of(
               OWNER_READ,
               OWNER_CREATE,
               OWNER_UPDATE,
               OWNER_DELETE
            )
    ),
    ADMIN(
            Set.of(
                    ADMIN_READ,
                    ADMIN_CREATE,
                    ADMIN_UPDATE,
                    ADMIN_DELETE
            )
    ),

    MEMBER(
            Set.of(
                    MEMBER_READ,
                    MEMBER_CREATE,
                    MEMBER_UPDATE,
                    MEMBER_DELETE
            )
    );


    @Getter
    private final Set<Permissions> permissions;

    public List<SimpleGrantedAuthority> getAuthorities() {

        var authorities = new java.util.ArrayList<>(permissions
                .stream()
                .map(permission -> {
                    return new SimpleGrantedAuthority(permission.getPermission());
                })
                .toList());


        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));

        return authorities;

    }
}
