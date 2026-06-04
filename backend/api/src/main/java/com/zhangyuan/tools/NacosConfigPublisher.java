package com.zhangyuan.tools;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * 将本地配置 YAML 发布到 Nacos 配置中心。
 * 通过 Gradle task 运行: ./gradlew publishNacosConfig
 */
public class NacosConfigPublisher {

    private static final String NACOS_HOST = System.getenv().getOrDefault("NACOS_HOST", "100.125.148.23");
    private static final String NACOS_PORT = System.getenv().getOrDefault("NACOS_PORT", "8848");
    private static final String NACOS_USER = System.getenv().getOrDefault("NACOS_USERNAME", "nacos");
    private static final String NACOS_PASS = System.getenv().getOrDefault("NACOS_PASSWORD", "chengccn");
    private static final String GROUP = "DEFAULT_GROUP";
    private static final String CONFIG_DIR = "../../infrastructure/nacos/configs";

    public static void main(String[] args) {
        String serverAddr = NACOS_HOST + ":" + NACOS_PORT;
        System.out.println("Connecting to Nacos at " + serverAddr + " ...");

        Properties props = new Properties();
        props.setProperty("serverAddr", serverAddr);
        props.setProperty("username", NACOS_USER);
        props.setProperty("password", NACOS_PASS);

        try {
            ConfigService configService = NacosFactory.createConfigService(props);
            System.out.println("Connected to Nacos successfully!");

            Path configDir = Paths.get(CONFIG_DIR).toAbsolutePath().normalize();
            if (!Files.exists(configDir)) {
                System.err.println("Config directory not found: " + configDir);
                System.exit(1);
            }

            System.out.println("Reading configs from: " + configDir);
            try (Stream<Path> files = Files.list(configDir)) {
                files.filter(f -> f.toString().endsWith(".yaml"))
                     .sorted()
                     .forEach(f -> publishConfig(configService, f));
            }

            System.out.println("\nAll configs published successfully!");
        } catch (NacosException | IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void publishConfig(ConfigService configService, Path file) {
        String dataId = file.getFileName().toString();
        try {
            String content = Files.readString(file);
            System.out.print("Publishing " + dataId + " ... ");

            boolean success = configService.publishConfig(dataId, GROUP, content);
            if (success) {
                System.out.println("OK");
            } else {
                System.out.println("FAILED");
            }
        } catch (IOException | NacosException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }
}
