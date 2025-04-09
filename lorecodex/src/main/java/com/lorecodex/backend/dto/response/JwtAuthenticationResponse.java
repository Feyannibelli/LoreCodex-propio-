package com.lorecodex.backend.dto.response;

import lombok.*;
import java.util.Set;

@Getter
@Setter
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthenticationResponse {
    private String token;
    private Integer userId;
    private Set<String> roles;
}