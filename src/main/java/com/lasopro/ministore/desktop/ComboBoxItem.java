package com.lasopro.ministore.desktop;

import java.util.Objects;

/**
 *
 * @author williams
 */
public class ComboBoxItem {
    
    private Object id;
    private Object value;
    private String label;

    public ComboBoxItem(Object id, Object value, String label) {
        this.id = id;
        this.value = value;
        this.label = label;
    }
    
    public ComboBoxItem(Object id) {
        this.id = id;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ComboBoxItem other = (ComboBoxItem) obj;
        return Objects.equals(this.id, other.id);
    }


    @Override
    public String toString() {
        return label;
    }
    
    
    
}
