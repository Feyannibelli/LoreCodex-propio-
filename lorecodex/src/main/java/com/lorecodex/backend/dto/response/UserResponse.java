package com.lorecodex.backend.dto.response;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Integer id;
    private String username;
    private String email;
    private Set<String> roles;
}