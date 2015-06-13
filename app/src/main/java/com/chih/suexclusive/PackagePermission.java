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
    public String uid;
    public String permission;

}
