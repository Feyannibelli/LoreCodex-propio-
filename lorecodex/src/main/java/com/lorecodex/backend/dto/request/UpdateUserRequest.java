package com.lorecodex.backend.dto.request;

import lombok.*;

@Getter
@Setter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    private String username;
    private String email;
    private String password;
}
