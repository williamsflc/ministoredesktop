package com.lasopro.ministore.desktop;

import com.lasopro.ministore.util.Util;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * JTextField con sugerencias tipo autocomplete.
 * - Coincidencia EXACTA por codigo -> dispara ActionEvent
 * - Coincidencias parciales por nombre -> muestra popup con sugerencias
 * - Búsqueda case-insensitive e ignora tildes
 * - Navegación con flechas y Enter
 */
public class SuggestTextField extends JTextField {

    private List<Map<String, Object>> values = new ArrayList<>();

    private final JPopupMenu popup = new JPopupMenu();
    private final JList<Map<String, Object>> suggestionList = new JList<>();
    private DefaultListModel<Map<String, Object>> listModel = new DefaultListModel<>();

    private Map<String, Object> selected = null;

    public SuggestTextField() {
        super();
        initUI();
        initListeners();
    }

    /**
     * Carga los valores base para las sugerencias.
     * @param list
     */
    public void setValues(List<Map<String, Object>> list) {
        this.values = (list != null) ? list : new ArrayList<>();
        this.values.forEach((m)->{
            String precio = "";
            try {
                precio = Util.formatDecimal(((Number) m.get("precio_venta")).doubleValue());
            } catch (Exception e) {
                MinistoreDesktop.log.err("Error con el precio de venta del producto: "+m.get("codigo")+"/"+m.get("nombre"), e);
            }
            
            String detalle = Util.lpad((String)m.get("codigo"),15,' ') + " " + m.get("nombre") + " - " + precio;
            
            try {
                Double stock = ((Number)m.get("stock")).doubleValue();
                
                if(stock > 0){
                    detalle = Util.lpad(stock+"",7,' ') + " | " + detalle;
                }else{
                    detalle = "AGOTADO | "+detalle;
                }
                
            } catch (Exception e) {
            }
            
            if(detalle.length() > 110){
                detalle = detalle.substring(0, 110);
            }
            m.put("detalle1",detalle);
            m.put("normalized",Util.normalize((String)m.get("nombre")+" "+m.get("descripcion") +" "+m.get("codigo")));
        });
        
    }

    private void initUI() {
        suggestionList.setModel(listModel);
        suggestionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        suggestionList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Map) {
                    setText((String)((Map) value).get("detalle1"));
                }
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(suggestionList);
        scroll.setBorder(null);
        popup.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        popup.add(scroll);
    }

    private void initListeners() {
        // Detectar escritura
        getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateSuggestions(); }
            public void removeUpdate(DocumentEvent e) { updateSuggestions(); }
            public void changedUpdate(DocumentEvent e) { updateSuggestions(); }
        });

        // Teclas de navegación
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!popup.isVisible()) return;

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DOWN:
                        moveSelection(1);
                        e.consume();
                        break;
                    case KeyEvent.VK_UP:
                        moveSelection(-1);
                        e.consume();
                        break;
                    case KeyEvent.VK_ENTER:
                        acceptSelection();
                        e.consume();
                        break;
                    case KeyEvent.VK_ESCAPE:
                        popup.setVisible(false);
                        break;
                }
            }
        });

        // Click con mouse
        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    acceptSelection();
                }
            }
        });
        suggestionList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!popup.isVisible()) return;

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_ENTER:
                        acceptSelection();
                        e.consume();
                        break;
                }
            }
        });
    }

    private void updateSuggestions() {
        String text = getText();
        if (text == null || text.trim().isEmpty() || text.length() <3) {
            popup.setVisible(false);
            return;
        }

        String normalizedInput = Util.normalize(text);

        // 2. Buscar coincidencias por nombre (contains)
        listModel = new DefaultListModel();
        for (Map<String, Object> m : values) {
            String normalized = (String)m.get("normalized");
            if (normalized != null) {
                if (normalized.contains(normalizedInput)) {
                    listModel.addElement(m);
                }
            }
        }

        if (!listModel.isEmpty()) {
            this.suggestionList.setModel(listModel);
            suggestionList.setSelectedIndex(0);
            showPopup();
        } else {
            popup.setVisible(false);
        }
    }

    private void showPopup() {
        if (popup.isVisible()) return;
        popup.setPopupSize(getWidth(), Math.min(150, listModel.getSize() * 24)+50);
        popup.show(this, 0, getHeight());
        requestFocusInWindow();
    }

    private void moveSelection(int delta) {
        int index = suggestionList.getSelectedIndex();
        int size = listModel.getSize();
        int newIndex = Math.max(0, Math.min(size - 1, index + delta));
        suggestionList.setSelectedIndex(newIndex);
        suggestionList.ensureIndexIsVisible(newIndex);
    }

    private void acceptSelection() {
        selected = suggestionList.getSelectedValue();
        if (selected != null) {
            setText(String.valueOf(selected.get("codigo")));
            popup.setVisible(false);
            fireActionEvent(String.valueOf(selected.get("codigo")));
        }
    }

    /**
     * Dispara ActionEvent estándar de JTextField
     */
    private void fireActionEvent(String codigo) {
        ActionEvent evt = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, codigo);
        for (ActionListener l : getActionListeners()) {
            l.actionPerformed(evt);
        }
    }


    public Map<String, Object> getSelected(){
        return selected;
    }

    
}
