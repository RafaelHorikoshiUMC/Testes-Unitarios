package com.pfc.thindesk.email.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

@Configuration
public class EmailConfig {

    // Define um TemplateResolver específico para templates de e-mail (busca em classpath: templates/)
    @Primary
    @Bean
    @Qualifier("emailTemplateResolver")
    public ITemplateResolver emailTemplateResolver() {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/"); // pasta onde estão os templates
        templateResolver.setSuffix(".html"); // extensão dos arquivos
        templateResolver.setTemplateMode("HTML"); // tipo do template (HTML)
        templateResolver.setCharacterEncoding("UTF-8"); // encoding dos arquivos
        templateResolver.setOrder(1); // prioridade se houver múltiplos resolvers
        templateResolver.setCheckExistence(true); // só tenta resolver se o arquivo existir
        return templateResolver;
    }

    // Define o TemplateEngine (motor Thymeleaf) específico para e-mails, usando o resolver acima
    @Bean
    @Qualifier("emailTemplateEngine")
    public SpringTemplateEngine emailTemplateEngine(@Qualifier("emailTemplateResolver") ITemplateResolver templateResolver) {
        SpringTemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(templateResolver); // associa o resolver configurado
        templateEngine.setTemplateEngineMessageSource(emailMessageSource()); // mensagens externas (i18n)
        return templateEngine;
    }

    // Fonte de mensagens usada nos templates de e-mail (para i18n ou textos externos)
    @Bean
    public ResourceBundleMessageSource emailMessageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages"); // procura arquivos messages.properties
        return messageSource;
    }
}


