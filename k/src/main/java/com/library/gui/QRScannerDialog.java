package com.library.gui;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicReference;

public class QRScannerDialog extends JDialog {
    private volatile boolean scanning = true;
    private AtomicReference<String> result = new AtomicReference<>(null);

    public QRScannerDialog(JFrame parent) {
        super(parent, "Scan QR Code", true);
        setLayout(new BorderLayout());
        setSize(500, 400);
        setLocationRelativeTo(parent);

        Webcam webcam = Webcam.getDefault();
        if (webcam == null) {
            JOptionPane.showMessageDialog(this, "No webcam detected!", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
            return;
        }
        if (webcam.isOpen()) {
            webcam.close();
        }
        webcam.setViewSize(new java.awt.Dimension(320, 240));
        WebcamPanel panel = new WebcamPanel(webcam);
        panel.setFPSDisplayed(true);
        add(panel, BorderLayout.CENTER);

        JLabel infoLabel = new JLabel("Align the QR code in front of the camera");
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(infoLabel, BorderLayout.SOUTH);

        new Thread(() -> {
            while (scanning) {
                BufferedImage image = webcam.getImage();
                if (image == null) continue;
                LuminanceSource source = new BufferedImageLuminanceSource(image);
                BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                try {
                    Result qrResult = new MultiFormatReader().decode(bitmap);
                    if (qrResult != null) {
                        result.set(qrResult.getText());
                        scanning = false;
                        break;
                    }
                } catch (NotFoundException e) {
                    // No QR code found in this frame
                }
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            }
            webcam.close();
            SwingUtilities.invokeLater(() -> {
                dispose();
            });
        }).start();
    }

    public String getScannedText() {
        return result.get();
    }
} 