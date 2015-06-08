package com.chih.suexclusive;

/**
 * Created by parallels on 6/6/15.
 */
enum PermissionLevel {
    DENY,
    GRANT,
    EXCLUSIVE
}
public class PackagePermission {
    public String name;
    public Integer uid;
    public PermissionLevel permission;

    public PackagePermission(String name, Integer uid, PermissionLevel permission) {
        this.name = name;
        this.uid = uid;
        this.permission = permission;
    }

}
