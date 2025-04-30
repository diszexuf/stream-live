package ru.diszexuf.streamlive.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserGetRequest {
    private UUID id;
    private String username;
    private String email;
    private String passwordHash;
    private String avatarUrl;
    private String bio;
    private Integer followersCount;
    private String streamKey;
}