package com.epita.controller.contract;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class UsersContract {
    private UUID followeeId;
    private UUID blockedUserId;
}
