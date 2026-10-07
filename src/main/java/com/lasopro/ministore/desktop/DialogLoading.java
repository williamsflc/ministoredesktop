package com.lasopro.ministore.desktop;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

/**
 *
 * @author williams
 */
public class DialogLoading extends JDialog {

    private Timer loadingTimer;
    private final SpinnerIcon loadingIcon = new SpinnerIcon(32);
    private final JLabel iconLabel;

    public DialogLoading(Window owner, String message) {
        super(owner, "Procesando", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        setResizable(false);

        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        iconLabel = new JLabel(loadingIcon, SwingConstants.CENTER);
        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);

        panel.add(iconLabel, BorderLayout.NORTH);
        panel.add(messageLabel, BorderLayout.CENTER);
        setContentPane(panel);
        pack();
        setLocationRelativeTo(owner);
    }

    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            loadingTimer = new Timer(80, e -> {
                loadingIcon.advance();
                iconLabel.repaint();
            });
            loadingTimer.start();
        } else if (loadingTimer != null) {
            loadingTimer.stop();
            loadingTimer = null;
        }
        super.setVisible(visible);
    }

    @Override
    public void dispose() {
        if (loadingTimer != null) {
            loadingTimer.stop();
            loadingTimer = null;
        }
        super.dispose();
    }

    private static final class SpinnerIcon implements Icon {
        private static final Color COLOR = new Color(135, 170, 206);
        private final int size;
        private int angle;

        SpinnerIcon(int size) {
            this.size = size;
        }

        void advance() {
            angle = (angle + 35) % 360;
        }

        @Override
        public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(COLOR);
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawArc(x + 2, y + 2, size - 4, size - 4, angle, 270);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }
}
