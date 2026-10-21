package com.ruoyi.common.utils.file;

import static org.assertj.core.api.Assertions.*;
import java.io.IOException;
import java.nio.file.*;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockMultipartFile;
import com.ruoyi.common.config.RuoYiConfig;

class FileUploadStorageTest {
    Path temp;
    String oldProfile;
    @BeforeEach void before() throws Exception {
        Files.createDirectories(Path.of("target"));temp=Files.createTempDirectory(Path.of("target").toAbsolutePath(),"upload-storage-");
        oldProfile=RuoYiConfig.getProfile();new RuoYiConfig().setProfile(temp.toString());
    }
    @AfterEach void after() { new RuoYiConfig().setProfile(oldProfile); }

    @Test void blockedDirectoryReportsActionableStorageErrorInsteadOfOnlyFilename() throws Exception {
        Path blocked=Files.writeString(temp.resolve("not-a-directory"),"preserve");
        MockMultipartFile file=new MockMultipartFile("file","微信图片.jpg","image/jpeg",new byte[]{1,2,3});
        assertThatThrownBy(()->FileUploadUtils.upload(blocked.resolve("uploads").toString(),file))
            .isInstanceOf(IOException.class).hasMessageContaining("上传目录").hasMessageContaining("权限");
        assertThat(Files.readString(blocked)).isEqualTo("preserve");
    }
    @Test void failedTransferRemovesPartiallyWrittenFile() throws Exception {
        MockMultipartFile file=new MockMultipartFile("file","图片.png","image/png",new byte[]{1,2,3}) {
            @Override public void transferTo(Path destination) throws IOException {
                Files.write(destination,new byte[]{1});throw new IOException("simulated disk full");
            }
        };
        assertThatThrownBy(()->FileUploadUtils.upload(temp.toString(),file)).isInstanceOf(IOException.class);
        try(var files=Files.walk(temp)) { assertThat(files.filter(Files::isRegularFile)).isEmpty(); }
    }
    @Test void chineseFilenameWritesCorrectBytesIntoNewDateDirectories() throws Exception {
        byte[] content={1,2,3};
        String path=FileUploadUtils.upload(temp.toString(),new MockMultipartFile("file","屏幕截图 2026-10-09.png","image/png",content));
        Path physical=temp.resolve(path.substring("/profile/".length()));
        assertThat(Files.readAllBytes(physical)).isEqualTo(content);
    }
}
