package com.zhangyuan.modules.auth.adapter.out.persistence;

import com.zhangyuan.modules.auth.domain.model.User;
import com.zhangyuan.modules.auth.repository.AdminUserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JpaUserRepositoryTest {

    private final AdminUserRepository jpaRepository = mock(AdminUserRepository.class);
    private final JpaUserRepository repository = new JpaUserRepository(jpaRepository);

    @Test
    void findByUsernameDelegates() {
        com.zhangyuan.modules.auth.domain.AdminUser entity = new com.zhangyuan.modules.auth.domain.AdminUser(
                "admin", "hash", "Admin", "admin@test.com");
        when(jpaRepository.findByUsername("admin")).thenReturn(Optional.of(entity));

        Optional<User> result = repository.findByUsername("admin");

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("admin");
    }

    @Test
    void saveNewUser() {
        User user = new User("newuser", "hash", "New", "new@test.com");
        com.zhangyuan.modules.auth.domain.AdminUser savedEntity = new com.zhangyuan.modules.auth.domain.AdminUser(
                "newuser", "hash", "New", "new@test.com");
        when(jpaRepository.save(any())).thenReturn(savedEntity);

        User result = repository.save(user);

        assertThat(result.getUsername()).isEqualTo("newuser");
        verify(jpaRepository).save(any());
    }

    @Test
    void existsByUsername() {
        when(jpaRepository.findByUsername("admin")).thenReturn(Optional.of(mock(com.zhangyuan.modules.auth.domain.AdminUser.class)));

        boolean exists = repository.existsByUsername("admin");

        assertThat(exists).isTrue();
    }
}
