package com.ruoyi.web.core.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.ruoyi.common.config.RuoYiConfig;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Swagger2�Ľӿ�����
 * 
 * @author ruoyi
 */
@Configuration
public class SwaggerConfig
{
    /** ϵͳ�������� */
    @Autowired
    private RuoYiConfig ruoyiConfig;
    
    /**
     * �Զ���� OpenAPI ����
     */
    @Bean
    public OpenAPI customOpenApi()
    {
        return new OpenAPI().components(new Components()
            // ������֤������ͷ
            .addSecuritySchemes("apikey", securityScheme()))
            .addSecurityItem(new SecurityRequirement().addList("apikey"))
            .info(getApiInfo());
    }
    
    @Bean
    public SecurityScheme securityScheme()
    {
        return new SecurityScheme()
            .type(SecurityScheme.Type.APIKEY)
            .name("Authorization")
            .in(SecurityScheme.In.HEADER)
            .scheme("Bearer");
    }
    
    /**
     * ����ժҪ��Ϣ
     */
    public Info getApiInfo()
    {
        return new Info()
            // ���ñ���
            .title("���⣺ѧ��̸��̸��ϵͳ_�ӿ��ĵ�")
            // ����
            .description("���������ڹ����������¹�˾����Ա��Ϣ,�������XXX,XXXģ��...")
            // ������Ϣ
            .contact(new Contact().name(ruoyiConfig.getName()))
            // �汾
            .version("�汾��:" + ruoyiConfig.getVersion());
    }
}
