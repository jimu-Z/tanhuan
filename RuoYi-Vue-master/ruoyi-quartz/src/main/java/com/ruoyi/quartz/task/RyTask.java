package com.ruoyi.quartz.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import com.ruoyi.common.utils.StringUtils;

/**
 * 定时任务调度测试
 * 
 * @author ruoyi
 */
@Component("ryTask")
public class RyTask
{
    public void ryMultipleParams(String s, Boolean b, Long l, Double d, Integer i)
    {
        System.out.println(StringUtils.format("执行多参方法： 字符串类型{}，布尔类型{}，长整型{}，浮点型{}，整形{}", s, b, l, d, i));
    }

    public void ryParams(String params)
    {
        System.out.println("执行有参方法：" + params);
    }

    public void ryNoParams()
    {
        System.out.println("执行无参方法");
    }

    public void talkBackup()
    {
        try {
            String backupDir = System.getProperty("talk.backup.dir",
                    System.getProperty("user.home") + "/talk_backup");
            File dir = new File(backupDir);
            if (!dir.exists()) dir.mkdirs();

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = backupDir + "/talk_backup_" + timestamp + ".sql";

            Map<String, String> dbConfig = loadDbConfig();
            String host = dbConfig.getOrDefault("host", "localhost");
            String port = dbConfig.getOrDefault("port", "3306");
            String user = dbConfig.getOrDefault("username", "root");
            String password = dbConfig.getOrDefault("password", "");
            String dbName = dbConfig.getOrDefault("database", "xuexiaotanhua");

            ProcessBuilder pb = new ProcessBuilder(
                "mysqldump",
                "--host=" + host,
                "--port=" + port,
                "-u" + user,
                "-p" + password,
                "--single-transaction",
                "--default-character-set=utf8mb4",
                "--result-file=" + fileName,
                dbName
            );
            pb.redirectErrorStream(true);

            Map<String, String> env = pb.environment();
            env.put("MYSQL_PWD", password);

            Process p = pb.start();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(p.getInputStream(), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("[备份] " + line);
            }
            int exitCode = p.waitFor();
            if (exitCode == 0) {
                System.out.println("谈话数据备份完成: " + fileName);
                cleanOldBackups(backupDir, 7);
            } else {
                System.err.println("备份失败，退出码: " + exitCode);
            }
        } catch (Exception e) {
            System.err.println("备份失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Map<String, String> loadDbConfig() {
        Map<String, String> config = new LinkedHashMap<>();
        List<String> searchPaths = Arrays.asList(
            System.getProperty("user.dir") + "/ruoyi-admin/src/main/resources/application-druid.yml",
            System.getProperty("user.dir") + "/../ruoyi-admin/src/main/resources/application-druid.yml"
        );
        for (String path : searchPaths) {
            File f = new File(path);
            if (!f.exists()) continue;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(f), "UTF-8"))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.startsWith("#")) continue;
                    if (line.contains(":") && !line.startsWith("spring:") && !line.startsWith("datasource:")) {
                        String[] parts = line.split(":", 2);
                        if (parts.length == 2) {
                            String key = parts[0].trim();
                            String value = parts[1].trim().replace("\"", "");
                            config.put(key, value);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("读取数据库配置失败: " + e.getMessage());
            }
            break;
        }
        if (config.isEmpty()) {
            config.put("host", "localhost");
            config.put("port", "3306");
            config.put("database", "xuexiaotanhua");
            config.put("username", "root");
        }
        return config;
    }

    private void cleanOldBackups(String backupDir, int keepDays) {
        File dir = new File(backupDir);
        File[] files = dir.listFiles((d, name) -> name.startsWith("talk_backup_") && name.endsWith(".sql"));
        if (files == null) return;
        long cutoff = System.currentTimeMillis() - (long) keepDays * 24 * 60 * 60 * 1000;
        for (File f : files) {
            if (f.lastModified() < cutoff) {
                if (f.delete()) {
                    System.out.println("清理过期备份: " + f.getName());
                }
            }
        }
    }
}