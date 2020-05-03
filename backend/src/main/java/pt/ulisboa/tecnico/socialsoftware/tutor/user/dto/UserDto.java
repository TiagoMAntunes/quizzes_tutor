package pt.ulisboa.tecnico.socialsoftware.tutor.user.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.config.DateHandler;
import pt.ulisboa.tecnico.socialsoftware.tutor.user.User;

import java.io.Serializable;

public class UserDto implements Serializable {
    private int id;
    private String username;
    private String name;
    private User.Role role;
    private String creationDate;

    public UserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.name = user.getName();
        this.role = user.getRole();
        this.creationDate = DateHandler.toISOString(user.getCreationDate());
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User.Role getRole() {
        return role;
    }

    public void setRole(User.Role role) {
        this.role = role;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public String toString() {
        return "UserDto{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", role=" + role +
                ", creationDate=" + creationDate +
                '}';
    }
}
