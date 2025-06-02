package com.pfc.thindesk.email;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Service
public class EmailService {

    @Value("${spring.mail.username}")
    private String sender; // E-mail do remetente configurado no application.properties

    @Value("${api.web.domain}")
    private String dominio; // Domínio da aplicação (usado nos links nos templates)

    private final JavaMailSender emailSender; // Responsável por enviar os e-mails
    private final SpringTemplateEngine emailTemplateEngine; // Engine Thymeleaf para processar templates de e-mail

    // Construtor com injeção de dependências
    public EmailService(
            JavaMailSender emailSender,
            @Qualifier("emailTemplateEngine") SpringTemplateEngine emailTemplateEngine
    ) {
        this.emailSender = emailSender;
        this.emailTemplateEngine = emailTemplateEngine;
    }

    // Envia um e-mail com HTML gerado por template Thymeleaf e anexo opcional
    public void enviarEmail(String subject, String receiver, EmailTemplate template, Context context, Optional<MultipartFile> file) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(sender);         // Define remetente
            helper.setTo(receiver);         // Define destinatário
            helper.setSubject(subject);     // Define o assunto do e-mail

            // Adiciona variáveis padrão ao contexto do template
            context.setVariable("receiver", receiver);
            context.setVariable("subject", subject);
            context.setVariable("domain", dominio);

            // Processa o template com o contexto e gera o HTML
            String htmlBody = "";
            try {
                htmlBody = emailTemplateEngine.process(template.getName(), context);
            } catch (Exception e) {
                e.printStackTrace(); // Imprime erros de processamento do template
            }

            helper.setText(htmlBody, true); // Define o corpo do e-mail como HTML

            // Se houver anexo, converte e adiciona ao e-mail
            file.ifPresent(attachment -> {
                try {
                    String fileName = attachment.getOriginalFilename();
                    File tempFile = convertMultipartFileToFile(attachment);
                    if (fileName != null) {
                        helper.addAttachment(fileName, tempFile); // Adiciona anexo
                        tempFile.deleteOnExit(); // Deleta o arquivo temporário depois
                    }
                } catch (Exception e) {
                    throw new MailSendException("Erro ao anexar arquivo no email!", e);
                }
            });

            emailSender.send(mimeMessage); // Envia o e-mail
            System.out.println("Email enviado!");
        } catch (Exception e) {
            e.printStackTrace();
            throw new MailSendException("Erro ao enviar o email!", e);
        }
    }

    // Converte MultipartFile (do Spring) para File físico temporário (para anexar ao e-mail)
    private File convertMultipartFileToFile(MultipartFile file) throws IOException {
        Path tempFilePath = Files.createTempFile("attachment_", ".tmp");
        File tempFile = tempFilePath.toFile();
        file.transferTo(tempFile);
        return tempFile;
    }
}

