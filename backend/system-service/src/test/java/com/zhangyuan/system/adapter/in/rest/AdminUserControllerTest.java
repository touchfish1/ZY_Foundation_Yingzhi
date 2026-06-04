package com.zhangyuan.system.adapter.in.rest;

import com.zhangyuan.system.client.UserServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class AdminUserControllerTest {
    private final UserServiceClient userServiceClient = mock(UserServiceClient.class);
    private AdminUserController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminUserController(userServiceClient);
    }

    @Test
    void listUsers_delegatesToClient() {
        controller.listUsers(1, 20);
        verify(userServiceClient).listUsers(1, 20);
    }

    @Test
    void getUser_delegatesToClient() {
        controller.getUser(1L);
        verify(userServiceClient).getUser(1L);
    }

    @Test
    void updateStatus_delegatesToClient() {
        controller.updateStatus(1L, "disabled");
        verify(userServiceClient).updateUserStatus(1L, "disabled");
    }
}
