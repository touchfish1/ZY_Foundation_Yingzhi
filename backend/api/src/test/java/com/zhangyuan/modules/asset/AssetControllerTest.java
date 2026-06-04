package com.zhangyuan.modules.asset;

import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.common.security.AuthUser;
import com.zhangyuan.modules.asset.dto.AssetFileInfo;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AssetControllerTest {

    private final AssetService assetService = mock(AssetService.class);
    private final AssetController controller = new AssetController(assetService);

    @Test
    void uploadDelegatesToService() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        AssetFileInfo expected = new AssetFileInfo(1L, "http://example.com/file.png", "file.png", "image/png", 1024L);
        when(assetService.upload(file, 1L)).thenReturn(expected);

        AuthUser user = mock(AuthUser.class);
        when(user.getId()).thenReturn(1L);
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        ApiResponse<AssetFileInfo> response = controller.upload(file);

        assertThat(response.data().id()).isEqualTo(1L);
        assertThat(response.data().originalName()).isEqualTo("file.png");
        verify(assetService).upload(file, 1L);

        SecurityContextHolder.clearContext();
    }
}
