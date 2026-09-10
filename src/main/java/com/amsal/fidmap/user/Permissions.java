package com.amsal.fidmap.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Permissions {

    SUPER_ADMIN_READ("super_admin:read"),
    SUPER_ADMIN_CREATE("super_admin:create"),
    SUPER_ADMIN_UPDATE("super_admin:update"),
    SUPER_ADMIN_DELETE("super_admin:delete"),


    OWNER_READ("owner:read"),
    OWNER_CREATE("owner:create"),
    OWNER_UPDATE("owner:update"),
    OWNER_DELETE("owner:delete"),


    ADMIN_READ("admin:read"),
    ADMIN_CREATE("admin:create"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_DELETE("admin:delete"),


    MEMBER_READ("member:read"),
    MEMBER_CREATE("member:create"),
    MEMBER_UPDATE("member:update"),
    MEMBER_DELETE("member:delete");


    @Getter
    private final String permission;
}
