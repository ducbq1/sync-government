package org.webflux.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class UserDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String userName;
    private String password;
    private String firstName;
    private String lastName;
    private byte[] image;

}
