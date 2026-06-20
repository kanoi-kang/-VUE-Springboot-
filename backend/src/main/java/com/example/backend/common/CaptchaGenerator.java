package com.example.backend.common;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class CaptchaGenerator {

    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int LINE_COUNT = 5;
    private static final int DOT_COUNT = 20;

    private static Font getFont() {
        String[] fontNames = {"Arial", "Times New Roman", "Helvetica", "SansSerif", "Serif"};
        for (String fontName : fontNames) {
            Font font = new Font(fontName, Font.BOLD, 28);
            if (font != null && !font.getFamily().equals("Dialog")) {
                return font;
            }
        }
        return new Font("SansSerif", Font.BOLD, 28);
    }

    public static CaptchaResult generate() {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        Random random = new Random();

        for (int i = 0; i < LINE_COUNT; i++) {
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = random.nextInt(WIDTH);
            int y2 = random.nextInt(HEIGHT);
            g.setColor(getRandomColor(random, 100, 200));
            g.drawLine(x1, y1, x2, y2);
        }

        for (int i = 0; i < DOT_COUNT; i++) {
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            g.setColor(getRandomColor(random, 50, 250));
            g.fillOval(x, y, 2, 2);
        }

        StringBuilder codeBuilder = new StringBuilder();
        g.setFont(getFont());
        
        for (int i = 0; i < 4; i++) {
            String charStr = String.valueOf(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
            codeBuilder.append(charStr);
            
            int x = 20 + i * 25 + random.nextInt(5);
            int y = 30 + random.nextInt(5);
            
            g.setColor(getRandomColor(random, 50, 150));
            g.drawString(charStr, x, y);
        }

        g.dispose();

        return new CaptchaResult(codeBuilder.toString(), image);
    }

    private static Color getRandomColor(Random random, int min, int max) {
        int r = min + random.nextInt(max - min);
        int g = min + random.nextInt(max - min);
        int b = min + random.nextInt(max - min);
        return new Color(r, g, b);
    }

    public static class CaptchaResult {
        private final String code;
        private final BufferedImage image;

        public CaptchaResult(String code, BufferedImage image) {
            this.code = code;
            this.image = image;
        }

        public String getCode() {
            return code;
        }

        public BufferedImage getImage() {
            return image;
        }
    }
}