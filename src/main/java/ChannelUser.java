public class ChannelUser {

    public static final Integer CHANNEL_ROLE_ADMIN = 100;
    public static final Integer CHANNEL_ROLE_MEMBERS = 110;
    
    private User user = null;
    private int role = 0;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }
}
