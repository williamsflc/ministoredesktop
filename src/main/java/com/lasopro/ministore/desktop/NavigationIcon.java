package com.lasopro.ministore.desktop;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import javax.swing.Icon;

final class NavigationIcon implements Icon {

    enum Type {
        INVENTORY, SALES, SUPPLIERS, CATEGORIES, CLIENTS, REPORTS, USERS, PROFILE, CONFIG
    }

    private static final int SIZE = 18;
    private static final Color COLOR = new Color(35, 55, 75);
    private final Type type;

    NavigationIcon(Type type) {
        this.type = type;
    }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.translate(x, y);
        g.setColor(COLOR);
        g.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        switch (type) {
            case INVENTORY -> paintInventory(g);
            case SALES -> paintSales(g);
            case SUPPLIERS -> paintSuppliers(g);
            case CATEGORIES -> paintCategories(g);
            case CLIENTS -> paintClients(g);
            case REPORTS -> paintReports(g);
            case USERS -> paintUsers(g);
            case PROFILE -> paintProfile(g);
            case CONFIG -> paintConfig(g);
        }
        g.dispose();
    }

    private void paintInventory(Graphics2D g) {
        g.drawRect(2, 5, 14, 11);
        g.drawLine(2, 8, 16, 8);
        g.drawLine(7, 5, 7, 16);
        g.drawLine(11, 5, 11, 16);
        g.drawLine(6, 2, 12, 2);
        g.drawLine(7, 2, 7, 5);
        g.drawLine(11, 2, 11, 5);
    }

    private void paintSales(Graphics2D g) {
        Path2D cart = new Path2D.Double();
        cart.moveTo(1.5, 3);
        cart.lineTo(4, 3);
        cart.lineTo(6, 11);
        cart.lineTo(14.5, 11);
        cart.lineTo(16, 6);
        cart.lineTo(5, 6);
        g.draw(cart);
        g.drawLine(6, 13, 14, 13);
        g.fillOval(5, 15, 2, 2);
        g.fillOval(13, 15, 2, 2);
    }

    private void paintSuppliers(Graphics2D g) {
        g.drawRect(1, 5, 10, 8);
        Path2D cab = new Path2D.Double();
        cab.moveTo(11, 8);
        cab.lineTo(14, 8);
        cab.lineTo(17, 11);
        cab.lineTo(17, 13);
        cab.lineTo(11, 13);
        cab.closePath();
        g.draw(cab);
        g.fillOval(3, 13, 3, 3);
        g.fillOval(13, 13, 3, 3);
    }

    private void paintCategories(Graphics2D g) {
        g.drawRoundRect(2, 2, 5, 5, 1, 1);
        g.drawRoundRect(11, 2, 5, 5, 1, 1);
        g.drawRoundRect(2, 11, 5, 5, 1, 1);
        g.drawRoundRect(11, 11, 5, 5, 1, 1);
    }

    private void paintClients(Graphics2D g) {
        g.drawRoundRect(1, 3, 16, 12, 2, 2);
        g.drawOval(4, 6, 4, 4);
        g.drawArc(3, 10, 6, 4, 0, 180);
        g.drawLine(11, 7, 15, 7);
        g.drawLine(11, 10, 15, 10);
    }

    private void paintReports(Graphics2D g) {
        g.drawLine(2, 16, 16, 16);
        g.drawRect(3, 10, 3, 6);
        g.drawRect(8, 6, 3, 10);
        g.drawRect(13, 2, 3, 14);
    }

    private void paintUsers(Graphics2D g) {
        g.drawOval(6, 2, 6, 6);
        g.drawArc(3, 8, 12, 8, 0, 180);
        g.drawOval(1, 5, 4, 4);
        g.drawOval(13, 5, 4, 4);
        g.drawArc(0, 9, 6, 6, 45, 90);
        g.drawArc(12, 9, 6, 6, 45, 90);
    }

    private void paintProfile(Graphics2D g) {
        g.drawOval(6, 2, 6, 6);
        g.drawArc(3, 9, 12, 7, 0, 180);
    }

    private void paintConfig(Graphics2D g) {
        g.drawOval(5, 5, 8, 8);
        g.drawOval(8, 8, 2, 2);
        for (int i = 0; i < 8; i++) {
            double angle = Math.PI * i / 4;
            int x1 = (int) Math.round(9 + Math.cos(angle) * 5);
            int y1 = (int) Math.round(9 + Math.sin(angle) * 5);
            int x2 = (int) Math.round(9 + Math.cos(angle) * 7);
            int y2 = (int) Math.round(9 + Math.sin(angle) * 7);
            g.drawLine(x1, y1, x2, y2);
        }
    }

    @Override
    public int getIconWidth() {
        return SIZE;
    }

    @Override
    public int getIconHeight() {
        return SIZE;
    }
}
