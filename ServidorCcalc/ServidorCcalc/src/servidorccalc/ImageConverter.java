package servidorccalc;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by francesc on 12/05/16.
 */

public class ImageConverter {
    /**
     * Converts an image to another format
     *
     * @param inputImagePath Path of the source image
     * @param outputImagePath Path of the destination image
     * @param formatName the format to be converted to, one of: jpeg, png,
     * bmp, wbmp, and gif
     * @return true if successful, false otherwise
     * @throws IOException if errors occur during writing
     */
    private int imgWidth;
    private int imgHeight;
    private float colors[][] ;

    public boolean convertFormat(String inputImagePath, String outputImagePath, String formatName) throws IOException {
        FileInputStream inputStream = new FileInputStream(inputImagePath);
        FileOutputStream outputStream = new FileOutputStream(outputImagePath);

        // reads input image from file
        BufferedImage inputImage = ImageIO.read(inputStream);
        imgWidth = inputImage.getWidth();
        imgHeight = inputImage.getHeight();
        //Per fer el fons blanc
        int vType = inputImage.getType();
        if (vType == BufferedImage.TYPE_CUSTOM) // png images have a "custom" type, which causes trouble !
        {
            vType = BufferedImage.TYPE_3BYTE_BGR;
        }
        BufferedImage vThumb = new BufferedImage(imgWidth, imgHeight, vType);
        Graphics2D vG2d = vThumb.createGraphics();
        vG2d.setBackground(Color.white);
        vG2d.clearRect(0,0,imgWidth,imgHeight);
        vG2d.drawImage(inputImage, 0, 0, imgWidth, imgHeight, null);
        //ByteArrayOutputStream vBAOS = new ByteArrayOutputStream();
        boolean result = ImageIO.write(vThumb, formatName, outputStream);

        // needs to close the streams
        outputStream.close();
        inputStream.close();

        return result;
    }
}
