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
            String cmd = "mysqldump -uroot -p123456 xuexiaotanhua --result-file=F:/QQ/QQwenjian/backup/talk_backup_" +
                    new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date()) + ".sql";
            Process p = Runtime.getRuntime().exec(new String[]{"cmd", "/c", cmd});
            p.waitFor();
            System.out.println("谈话数据备份完成");
        } catch (Exception e) {
            System.err.println("备份失败: " + e.getMessage());
        }
    }
}
