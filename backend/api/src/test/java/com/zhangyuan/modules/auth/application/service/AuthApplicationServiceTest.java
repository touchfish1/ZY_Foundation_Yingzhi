package com.zhangyuan.modules.auth.application.service;

import com.zhangyuan.modules.auth.domain.model.User;
import com.zhangyuan.modules.auth.domain.repository.UserRepository;
import com.zhangyuan.modules.auth.domain.service.AuthDomainService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthApplicationServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final AuthDomainService authDomainService = new AuthDomainService();
    private final AuthApplicationService service = new AuthApplicationService(userRepository, authDomainService);

    @Test
    void createUserSaves() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        User result = service.createUser("newuser", "hash", "New", "new@test.com");

        assertThat(result.getUsername()).isEqualTo("newuser");
        verify(userRepository).save(any());
    }

    @Test
    void findByUsernameDelegates() {
        service.findByUsername("admin");

        verify(userRepository).findByUsername("admin");
    }

    @Test
    void listAllDelegates() {
        service.listAll();

        verify(userRepository).findAll();
    }
}
