package Model;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageShape implements MoveableElement {

    private int x1;
    private int y1;
    private final int group = 0;
    private BufferedImage currentImage;
    private int frameWidth;
    private int frameHeight;

    public ImageShape(int x1, int y1, File selectedFile, int width, int height) {
        this(x1, y1, loadAndScaleImage(selectedFile, width, height));
    }

    public ImageShape(int x1, int y1, BufferedImage image, int width, int height) {
        this(x1, y1, scaleImage(image, width, height));
    }

    private ImageShape(int x1, int y1, BufferedImage preparedImage) {
        this.x1 = x1;
        this.y1 = y1;
        this.currentImage = preparedImage;
        this.frameWidth = preparedImage == null ? 0 : preparedImage.getWidth();
        this.frameHeight = preparedImage == null ? 0 : preparedImage.getHeight();
    }

    private static BufferedImage loadAndScaleImage(File selectedFile, int width, int height) {
        try {
            BufferedImage loaded = ImageIO.read(selectedFile);
            if (loaded == null) {
                throw new IllegalArgumentException("Failed to decode image from file.");
            }
            return scaleImage(loaded, width, height);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Failed to load image from file.", e);
        }
    }

    private static BufferedImage scaleImage(BufferedImage source, int width, int height) {
        if (source == null) {
            return null;
        }

        int targetWidth = (width <= 0) ? source.getWidth() : width;
        int targetHeight = (height <= 0) ? source.getHeight() : height;

        int imageType = source.getColorModel() != null && source.getColorModel().hasAlpha()
                ? BufferedImage.TYPE_INT_ARGB
                : BufferedImage.TYPE_INT_RGB;

        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, imageType);
        Graphics2D g2 = scaled.createGraphics();
        g2.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        g2.dispose();
        return scaled;
    }

    @Override
    public int getX1() {
        return x1;
    }

    @Override
    public int getY1() {
        return y1;
    }

    @Override
    public int getX2() {
        return x1 + frameWidth;
    }

    @Override
    public int getY2() {
        return y1 + frameHeight;
    }

    @Override
    public int getGroup() {
        return group;
    }

    @Override
    public boolean isPointInside(int x, int y) {
        return x >= x1 && x <= (x1 + frameWidth) && y >= y1 && y <= (y1 + frameHeight);
    }

    @Override
    public void displace(int dx, int dy) {
        this.x1 += dx;
        this.y1 += dy;
    }

    public void rotateClockwise90() {
        rotate90(true);
    }

    public void rotateCounterClockwise90() {
        rotate90(false);
    }

    private void rotate90(boolean clockwise) {
        if (currentImage == null) {
            return;
        }

        BufferedImage source = currentImage;
        int newWidth = source.getHeight();
        int newHeight = source.getWidth();
        int imageType = source.getColorModel() != null && source.getColorModel().hasAlpha()
                ? BufferedImage.TYPE_INT_ARGB
                : BufferedImage.TYPE_INT_RGB;
        BufferedImage rotated = new BufferedImage(newWidth, newHeight, imageType);

        for (int y = 0; y < source.getHeight(); y++) {
            for (int x = 0; x < source.getWidth(); x++) {
                int argb = source.getRGB(x, y);
                int destX;
                int destY;

                if (clockwise) {
                    destX = source.getHeight() - 1 - y;
                    destY = x;
                } else {
                    destX = y;
                    destY = source.getWidth() - 1 - x;
                }

                rotated.setRGB(destX, destY, argb);
            }
        }

        currentImage = rotated;
        frameWidth = rotated.getWidth();
        frameHeight = rotated.getHeight();
    }


    public BufferedImage copyRegion(int x, int y, int width, int height) {
        if (currentImage == null || width <= 0 || height <= 0) {
            return null;
        }

        int localX = Math.max(0, x - x1);
        int localY = Math.max(0, y - y1);
        int maxWidth = Math.min(width, frameWidth - localX);
        int maxHeight = Math.min(height, frameHeight - localY);

        if (maxWidth <= 0 || maxHeight <= 0) {
            return null;
        }

        int imageType = currentImage.getColorModel() != null && currentImage.getColorModel().hasAlpha()
                ? BufferedImage.TYPE_INT_ARGB
                : BufferedImage.TYPE_INT_RGB;
        BufferedImage copied = new BufferedImage(maxWidth, maxHeight, imageType);
        Graphics2D g2 = copied.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(currentImage, 0, 0, maxWidth, maxHeight, localX, localY, localX + maxWidth, localY + maxHeight, null);
        g2.dispose();
        return copied;
    }

    public boolean pasteImage(BufferedImage clipboardImage, int x, int y) {
        if (currentImage == null || clipboardImage == null) {
            return false;
        }

        boolean pasteMatchesFullFrame = clipboardImage.getWidth() == frameWidth
                && clipboardImage.getHeight() == frameHeight;

        int localX = pasteMatchesFullFrame ? 0 : x - x1;
        int localY = pasteMatchesFullFrame ? 0 : y - y1;
        if (localX >= frameWidth || localY >= frameHeight) {
            return false;
        }

        int drawX = Math.max(0, localX);
        int drawY = Math.max(0, localY);
        int sourceX = drawX - localX;
        int sourceY = drawY - localY;
        int drawWidth = Math.min(clipboardImage.getWidth() - sourceX, frameWidth - drawX);
        int drawHeight = Math.min(clipboardImage.getHeight() - sourceY, frameHeight - drawY);

        if (drawWidth <= 0 || drawHeight <= 0) {
            return false;
        }

        Graphics2D g2 = currentImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(clipboardImage,
                drawX, drawY, drawX + drawWidth, drawY + drawHeight,
                sourceX, sourceY, sourceX + drawWidth, sourceY + drawHeight,
                null);
        g2.dispose();
        return true;
    }


    public java.awt.Color getPixelColorAt(int x, int y) {
        if (currentImage == null) {
            return null;
        }

        int localX = x - x1;
        int localY = y - y1;
        if (localX < 0 || localY < 0 || localX >= frameWidth || localY >= frameHeight) {
            return null;
        }

        return new java.awt.Color(currentImage.getRGB(localX, localY), true);
    }

    public BufferedImage copyCurrentImage() {
        if (currentImage == null) {
            return null;
        }

        int imageType = currentImage.getColorModel() != null && currentImage.getColorModel().hasAlpha()
                ? BufferedImage.TYPE_INT_ARGB
                : BufferedImage.TYPE_INT_RGB;
        BufferedImage copy = new BufferedImage(currentImage.getWidth(), currentImage.getHeight(), imageType);
        Graphics2D g2 = copy.createGraphics();
        g2.drawImage(currentImage, 0, 0, null);
        g2.dispose();
        return copy;
    }

    public void replaceImage(BufferedImage image) {
        if (image == null) {
            return;
        }
        currentImage = image;
        frameWidth = image.getWidth();
        frameHeight = image.getHeight();
    }

    @Override
    public void draw(Graphics2D g) {
        if (currentImage == null) {
            return;
        }
        g.drawImage(currentImage, x1, y1, null);
    }
}
