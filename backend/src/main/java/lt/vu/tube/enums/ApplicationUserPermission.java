package lt.vu.tube.enums;

public enum ApplicationUserPermission {
    USER_GENERAL("user:general");

    private final String permission;

    ApplicationUserPermission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }

}
