package ru.diszexuf.streamlive.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserGetRequest {
    private UUID id;
    private String username;
    private String email;
    private String bio;
    private Integer followersCount;
    private UUID streamKey;
}