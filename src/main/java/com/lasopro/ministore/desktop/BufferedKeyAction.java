
package com.lasopro.ministore.desktop;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 *
 * @author williams
 */
public abstract class BufferedKeyAction implements KeyListener {

    StringBuilder buffer = new StringBuilder();
    boolean ctrlPressed = false;
    int modifierKeyCode;

    public BufferedKeyAction(int modifierKeyCode) {
        this.modifierKeyCode = modifierKeyCode;
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == modifierKeyCode) {
            ctrlPressed = true;
            buffer.setLength(0);
        }

        if (ctrlPressed && (Character.isDigit(e.getKeyChar()))|| e.getKeyChar() == '.') {
            buffer.append(e.getKeyChar());
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == modifierKeyCode) {
            ctrlPressed = false;

            if (buffer.length() > 0) {
                keyBufferedReleased(e,buffer.toString());
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent ke) {
    }
    
    public abstract void keyBufferedReleased(KeyEvent ke, String bufferedKeys);
    

}
