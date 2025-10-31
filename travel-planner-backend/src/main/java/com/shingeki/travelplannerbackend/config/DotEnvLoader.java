package com.shingeki.travelplannerbackend.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 环境变量加载器
 * 支持从 .env 文件加载环境变量(跨平台 Windows/Linux)
 * 
 * 工作原理:
 * 1. 在 Spring Boot 启动前加载 .env 文件
 * 2. 将 .env 文件中的配置添加到 Spring Environment
 * 3. 支持 ${VAR_NAME} 语法在 application.properties 中引用
 * 
 * 使用方式:
 * 在 src/main/resources/META-INF/spring.factories 中配置:
 * org.springframework.context.ApplicationContextInitializer=\
 * com.shingeki.travelplannerbackend.config.DotEnvLoader
 */
public class DotEnvLoader implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final String DOT_ENV_FILE = ".env";
    private static final String PROPERTY_SOURCE_NAME = "dotenv";

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        
        try {
            // 查找 .env 文件
            Path envPath = findDotEnvFile();
            
            if (envPath != null && Files.exists(envPath)) {
                System.out.println("Loading environment variables from: " + envPath.toAbsolutePath());
                
                // 加载 .env 文件
                Map<String, Object> envVars = loadDotEnvFile(envPath);
                
                // 添加到 Spring Environment
                if (!envVars.isEmpty()) {
                    environment.getPropertySources()
                            .addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, envVars));
                    
                    System.out.println("Successfully loaded " + envVars.size() + " environment variables from .env file");
                }
            } else {
                System.out.println("Warning: .env file not found. Using system environment variables or IDEA run configuration.");
                System.out.println("Searched in: " + (envPath != null ? envPath.toAbsolutePath() : "project root"));
            }
        } catch (Exception e) {
            System.err.println("Error loading .env file: " + e.getMessage());
            e.printStackTrace();
            // 不中断启动,继续使用系统环境变量
        }
    }

    /**
     * 查找 .env 文件
     * 优先级: 
     * 1. 项目根目录/travel-planner-backend/.env
     * 2. 当前工作目录/.env
     * 3. 用户主目录/.env
     */
    private Path findDotEnvFile() {
        // 1. 尝试项目根目录 (travel-planner-backend/.env)
        Path projectRoot = Paths.get("").toAbsolutePath();
        Path envPath = projectRoot.resolve(DOT_ENV_FILE);
        if (Files.exists(envPath)) {
            return envPath;
        }

        // 2. 尝试父目录 (如果在 target 目录中运行)
        Path parentPath = projectRoot.getParent();
        if (parentPath != null) {
            envPath = parentPath.resolve(DOT_ENV_FILE);
            if (Files.exists(envPath)) {
                return envPath;
            }
        }

        // 3. 尝试当前工作目录
        envPath = Paths.get(DOT_ENV_FILE);
        if (Files.exists(envPath)) {
            return envPath;
        }

        // 4. 尝试用户主目录
        String userHome = System.getProperty("user.home");
        envPath = Paths.get(userHome, DOT_ENV_FILE);
        if (Files.exists(envPath)) {
            return envPath;
        }

        return null;
    }

    /**
     * 加载 .env 文件内容
     * 支持格式:
     * - KEY=VALUE
     * - KEY="VALUE"
     * - # 注释
     * - 空行
     * 
     * 跨平台支持:
     * - Windows: 支持 CRLF (\r\n) 换行符
     * - Linux: 支持 LF (\n) 换行符
     * - macOS: 支持 LF (\n) 换行符
     */
    private Map<String, Object> loadDotEnvFile(Path envPath) throws IOException {
        Map<String, Object> envVars = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(envPath, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // 去除首尾空白
                line = line.trim();

                // 跳过空行和注释
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // 解析 KEY=VALUE
                int equalsIndex = line.indexOf('=');
                if (equalsIndex == -1) {
                    System.err.println("Warning: Invalid line " + lineNumber + " in .env file (no '=' found): " + line);
                    continue;
                }

                String key = line.substring(0, equalsIndex).trim();
                String value = line.substring(equalsIndex + 1).trim();

                // 移除值两端的引号(如果有)
                if ((value.startsWith("\"") && value.endsWith("\"")) ||
                    (value.startsWith("'") && value.endsWith("'"))) {
                    value = value.substring(1, value.length() - 1);
                }

                // 跳过示例值(包含 your_ 或 _here)
                if (value.contains("your_") || value.contains("_here")) {
                    System.out.println("Skipping example value for: " + key);
                    continue;
                }

                envVars.put(key, value);
                
                // 也设置到系统属性,以便在整个应用中使用
                System.setProperty(key, value);
            }
        }

        return envVars;
    }
}
