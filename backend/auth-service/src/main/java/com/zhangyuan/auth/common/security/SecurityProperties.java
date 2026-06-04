package com.zhangyuan.auth.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "zhangyuan.security")
public class SecurityProperties {

    private String jwtSecret;

    private long jwtExpiresSeconds = 7200;

    private DefaultAdmin defaultAdmin = new DefaultAdmin();

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public long getJwtExpiresSeconds() {
        return jwtExpiresSeconds;
    }

    public void setJwtExpiresSeconds(long jwtExpiresSeconds) {
        this.jwtExpiresSeconds = jwtExpiresSeconds;
    }

    public DefaultAdmin getDefaultAdmin() {
        return defaultAdmin;
    }

    public void setDefaultAdmin(DefaultAdmin defaultAdmin) {
        this.defaultAdmin = defaultAdmin;
    }

    public static class DefaultAdmin {

        private String username = "admin";

        private String password = "admin123";

        private String nickname = "Super Admin";

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }
    }
}
