package com.ruoyi.quartz.task;

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
            String backupDir = "F:/QQ/QQwenjian/backup";
            java.io.File dir = new java.io.File(backupDir);
            if (!dir.exists()) dir.mkdirs();
            String fileName = backupDir + "/talk_backup_" +
                    new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date()) + ".sql";
            ProcessBuilder pb = new ProcessBuilder(
                "mysqldump",
                "--defaults-extra-file=" + getDefaultsFile(),
                "xuexiaotanhua",
                "--result-file=" + fileName
            );
            pb.redirectErrorStream(true);
            Process p = pb.start();
            p.waitFor();
            System.out.println("谈话数据备份完成: " + fileName);
        } catch (Exception e) {
            System.err.println("备份失败: " + e.getMessage());
        }
    }

    private String getDefaultsFile() throws Exception {
        String appConfig = System.getProperty("user.dir") + "/ruoyi-admin/src/main/resources/application-druid.yml";
        java.io.File f = new java.io.File(appConfig);
        if (!f.exists()) {
            appConfig = System.getProperty("user.dir") + "/../ruoyi-admin/src/main/resources/application-druid.yml";
        }
        return appConfig;
    }
}
