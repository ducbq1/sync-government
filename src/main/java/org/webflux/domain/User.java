package org.webflux.domain;

import lombok.Builder;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class User {

    public User() {}

    public User(Long id, String userName, String password) {
        this.id = id;
        this.userName = userName;
        this.password = password;
    }

    public User(Long id, String userName, String password, String firstName, String lastName, String image, Boolean gender) {
        this.id = id;
        this.userName = userName;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.image = image;
        this.gender = gender;
    }

    public User(Long id, String userName, String password,
                String firstName, String lastName, String image,
                Boolean gender, Set<String> roles, Set<Menu> menus) {
        this.id = id;
        this.userName = userName;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.image = image;
        this.gender = gender;
        this.roles = roles;
        this.menus = menus;
    }

    private Long id;
    private String userName;
    private String password;
    private String firstName;
    private String lastName;
    private String image;
    private Boolean gender;
    @Builder.Default
    private Set<String> roles = new HashSet<>();
    @Builder.Default
    private Set<Menu> menus = new HashSet<>();

}
