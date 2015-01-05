package du.tools.main.commons.utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class IconUtil {

    public static Image generate(String text, Color foreground, Color background) {
        int w = 100;
        int h = 25;

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = img.createGraphics();

        Font font = new Font(null, Font.PLAIN, 14);
        FontRenderContext context = g2d.getFontRenderContext();
        Rectangle2D bounds = font.getStringBounds(text, context);

        w = (int) bounds.getWidth() + 12;

        if (w < 26) {
            w = 26;
        }

        img = g2d.getDeviceConfiguration().createCompatibleImage(w + 1, h + 1, Transparency.TRANSLUCENT);
        g2d.dispose();
        g2d = img.createGraphics();
        g2d.setStroke(new BasicStroke(1));

        //Background
        g2d.setColor(background);
        g2d.fillRoundRect(0, 0, w, h, 2, 2);

        //Border
        //g2d.setColor(Color.GRAY);
        //g2d.drawRoundRect(0, 0, w, h, 2, 2);

        double x = (w - bounds.getWidth()) / 2;
        double y = (h - bounds.getHeight()) / 2;
        double ascent = -bounds.getY();
        double baseY = y + ascent;

        //Foreground
        g2d.setFont(font);
        g2d.setPaint(foreground);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.drawString(text, (int) x, (int) baseY);
        g2d.dispose();
        return img;
    }

    public static void fileIconSaveAsPng(String file, String path) {
        Icon icon = FileSystemView.getFileSystemView().getSystemIcon(new File(file));
        iconSaveAsPng(icon, path);
    }

    public static boolean iconSaveAsPng(Icon icon, String path) {
        try {
            System.out.println(icon.toString());
            Image img = ((ImageIcon) icon).getImage();
            BufferedImage bi = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = bi.createGraphics();
            bi = g2d.getDeviceConfiguration().createCompatibleImage(bi.getWidth(), bi.getHeight(), Transparency.TRANSLUCENT);
            g2d.dispose();
            g2d = bi.createGraphics();
            g2d.drawImage(img, 0, 0, null);
            g2d.dispose();
            return ImageIO.write(bi, "PNG", new File(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void main(String[] args) throws Exception {
        fileIconSaveAsPng("F:\\WorkSpace\\evnwhp\\evnwhp.parent.iml", "d:\\file.png");
    }
}