package com.lasopro.ministore.desktop;

/**
 *
 * @author williams
 */
import com.lasopro.ministore.util.Util;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class MapListTextPane extends JTextPane {

    private List<Map<String, Object>> data;
    private int selectedIndex = -1;

    private final SimpleAttributeSet normalStyle = new SimpleAttributeSet();
    private final SimpleAttributeSet selectedStyle = new SimpleAttributeSet();
    private Formatter formater;
    private int headerLines = 0;
    private int footerLines = 0;
    
    private List<ActionListener> actionListener;

    public MapListTextPane() {
        
        this.data = new ArrayList<>();
        
        actionListener = new ArrayList<>();
        setEditable(false);
        setOpaque(true);
        setBackground(Color.WHITE);
        //setFont(new Font("Monospaced", Font.PLAIN, 15));

        StyleConstants.setBackground(normalStyle, Color.WHITE);
        StyleConstants.setBackground(selectedStyle, new Color(200, 230, 255)); // celeste claro

        // Detectar cambio de línea por mouse o teclado
        addCaretListener(this::onCaretUpdate);

        // Doble click
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    fireActionListener();
                }
            }
        });

        // Enter
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    fireActionListener();
                }
            }
        });
    }

    /* ================================
       API PUBLICA
       ================================ */

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
        selectedIndex = -1;
        setFormatter(formater);
        setText("");

        if (data == null || data.isEmpty()) return;
        
        if(selectedIndex < 0) selectedIndex = 0;
        
        String header = formater.header();
        String footer = formater.footer();
        headerLines = countLines(header);
        footerLines = countLines(footer);
        StringBuilder sb = new StringBuilder();
        sb.append(header).append("\n\n");
        for (Map<String, Object> map : data) {
            sb.append(formater.format(map)).append("\n");
        }
        sb.append(footer);
        setText(sb.toString());
        try {
        highlightLine(selectedIndex, getTotalLines());    
        } catch (Exception e) {
        }
        
        
    }
    
    public List<Map<String, Object>> getData(){
        return this.data;
    }
    
    
    public void setFormatter(Formatter formatter){
        this.formater  =formatter;
    }
   
    

    public Map<String, Object> getSelectedElement() {
        if (data == null || selectedIndex < 0 || selectedIndex >= data.size()) {
            return null;
        }
        return data.get(selectedIndex);
    }

    /* ================================
       LOGICA INTERNA
       ================================ */

    private void onCaretUpdate(CaretEvent e) {
        try {
            int caretPos = e.getDot();
            int line = getLineOfOffset(caretPos);
            int totalLines = getTotalLines();
            highlightLine(line,totalLines);
        } catch (BadLocationException ignored) {}
    }

    private void highlightLine(int line, int totalLines) {
        if (data == null || line < 0) {
            return;
        }

        try {
            long st = Calendar.getInstance().getTimeInMillis();
            StyledDocument doc = getStyledDocument();

            int dl = doc.getLength();

            if (dl <= 0) {
                return;
            }

            doc.setCharacterAttributes(
                    0,
                    doc.getLength(),
                    normalStyle,
                    true
            );
            
            if (line > headerLines && line <= (totalLines-footerLines-2)) {
                int start = getLineStartOffset(line);
                int end = getLineEndOffset(line);

                doc.setCharacterAttributes(
                        start,
                        end - start,
                        selectedStyle,
                        false
                );
                selectedIndex = line - headerLines-2;
            }
        } catch (Exception ignored) {}
    }

    private int getTotalLines() throws BadLocationException{
        Element root = getDocument().getDefaultRootElement();
        return root.getElementCount();
    }

    private int getLineOfOffset(int offset) throws BadLocationException {
        Element root = getDocument().getDefaultRootElement();
        return root.getElementIndex(offset);
    }

    private int getLineStartOffset(int line) throws BadLocationException {
        Element root = getDocument().getDefaultRootElement();
        return root.getElement(line).getStartOffset();
    }

    private int getLineEndOffset(int line) throws BadLocationException {
        Element root = getDocument().getDefaultRootElement();
        return root.getElement(line).getEndOffset();
    }
    
    public static interface Formatter{
        public String format(Map<String,Object> info);
        public String header();
        public String footer();
    }
    
    
    public void addActionListener(ActionListener listener){
        actionListener.add(listener);
    }
    
    public void fireActionListener(){
        for(ActionListener l : actionListener){
            l.actionPerformed(new ActionEvent(this, 0, "select"));
        }
    }
    
    public static int countLines(String texto) {
        if (texto == null || texto.isEmpty()) {
            return 0;
        }
        return texto.split("\\R", -1).length - 1;
    } 

    
}
