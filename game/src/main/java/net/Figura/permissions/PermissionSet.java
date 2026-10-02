package net.Figura.permissions;

import java.util.EnumSet;

public final class PermissionSet {
    private final EnumSet<Permission> allowed;

    public PermissionSet() { this.allowed = EnumSet.noneOf(Permission.class); }
    public PermissionSet(EnumSet<Permission> allowed) { this.allowed = allowed.clone(); }
    public boolean allows(Permission permission) { return allowed.contains(permission); }
    public void allow(Permission permission) { allowed.add(permission); }
    public void deny(Permission permission) { allowed.remove(permission); }
    public EnumSet<Permission> copy() { return allowed.clone(); }
}
