package com.syscom.fep.frmcommon.gui.util;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.commons.io.FilenameUtils;

import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.CleanPathUtil;

/**
 * 
 * @author Richard
 *
 */
public class ImageUtil {
	private static final LogHelper logger = new LogHelper();

	private ImageUtil() {}

	public static Image obtainImage(String path) {
		URL url = ImageUtil.class.getClassLoader().getResource(path);
		return Toolkit.getDefaultToolkit().createImage(url);
	}

	public static ImageIcon obtainImageIcon(String path) {
		try {
			URL url = ImageUtil.class.getClassLoader().getResource(path);
			ImageIcon image = new ImageIcon(url);
			return image;
		} catch (Exception ex) {
			logger.exceptionMsg(ex, ex.getMessage());
			return new ImageIcon();
		}
	}

	public static ImageIcon obtainImageIcon(String path, int width, int heigth) {
		ImageIcon image = obtainImageIcon(path);
		image.setImage(image.getImage().getScaledInstance(width, heigth, Image.SCALE_SMOOTH));
		return image;
	}

	public static void bufferedImageToFile(BufferedImage image, String filename) throws IOException {
		ImageIO.write(image, FilenameUtils.getExtension(filename), new File(CleanPathUtil.cleanString(filename)));
	}

	public static BufferedImage scaleBufferedImage(String filename, double axisFactorX, double axisFactorY) throws IOException {
		return scaleBufferedImage(ImageIO.read(new File(CleanPathUtil.cleanString(filename))), axisFactorX, axisFactorY);
	}

	public static BufferedImage scaleBufferedImage(BufferedImage image, double axisFactorX, double axisFactorY) {
		AffineTransform tx = new AffineTransform();
		tx.scale(axisFactorX, axisFactorY);
		AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
		return op.filter(image, null);
	}
}
