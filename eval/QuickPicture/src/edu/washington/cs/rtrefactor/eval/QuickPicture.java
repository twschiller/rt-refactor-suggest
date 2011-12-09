package edu.washington.cs.rtrefactor.eval;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/** 
 * A picture implementation which uses non-standard color values
 * 
 * @author Travis Mandel
 * @author Todd Schiller
 *
 */
public class QuickPicture extends java.awt.Image {

	private BufferedImage raw;

	/**
	 * Create a QuickPicture with the specified width and height
	 * 
	 * @param width The image width
	 * @param height The image height
	 */
	public QuickPicture(int width, int height){
		raw = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}

	private QuickPicture(BufferedImage rhs){
		raw = new BufferedImage(rhs.getWidth(), rhs.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = raw.getGraphics();
		g.drawImage(rhs, 0, 0, null);
	}

	/**
	 * Image transformations should not call this method
	 */
	public static QuickPicture read(File input) throws IOException{
		return new QuickPicture(ImageIO.read(input));
	}

	/**
	 * Image transformations should not call this method
	 */
	public void write(File input) throws IOException{
		ImageIO.write(raw, "PNG", input);
	}

	/**
	 * Gets the width of the image in pixels
	 * @return the width of the image in pixels
	 */
	public int getWidth() {
		return raw.getWidth();
	}

	/**
	 * Get the height of the image in pixels
	 * @return the height of the image in pixels
	 */
	public int getHeight() {
		return raw.getHeight();
	}

	/**
	 * Image transformations should not call this method
	 */
	@Override
	public Graphics getGraphics() {
		return raw.getGraphics();
	}

	/**
	 * Image transformations should not call this method
	 */
	@Override
	public int getHeight(ImageObserver observer) {
		return getHeight();
	}

	/**
	 * Image transformations should not call this method
	 */
	@Override
	public Object getProperty(String name, ImageObserver observer) {
		return raw.getProperty(name, observer);
	}

	/**
	 * returns <code>null</code>
	 * 
	 * Image transformations should not call this method
	 * 
	 * @return returns <code>null</code>
	 */
	@Override
	public ImageProducer getSource() {
		return null;
	}

	@Override
	/**
	 * Image transformations should not call this method
	 */
	public int getWidth(ImageObserver observer) {
		return getWidth();
	}

	/**
	 * Get the QuickColor of the pixel at the specified location 
	 * @param x The x-coordinated (column) of the pixel
	 * @param y The y-coordinate (row) of the pixel
	 * @return The QuickColor corresponding to the pixel.  This QuickColor is only a copy of the 
	 *              true color, and modifications will not impact the image.
	 */
	public QuickColor getColor(int x, int y){
		if(x < 0 || x >= raw.getWidth() || y< 0 || y >= raw.getHeight())
			return null;
		Color rawColor = new Color(raw.getRGB(x, y), true);

		return  new QuickColor(fromRGBVal(rawColor.getRed()), fromRGBVal(rawColor.getGreen()), 
				fromRGBVal(rawColor.getBlue()), fromRGBVal(rawColor.getAlpha()));
	}

	/**
	 * Set the QuickColor of the pixel at the specified location 
	 * @param x The x-coordinated (column) of the pixel
	 * @param y The y-coordinate (row) of the pixel
	 * @param color The new color to use for the pixel.  This will be copied, further changes to the
	 *              QuickColor will not affect the picture.
	 */
	public void setColor(int x, int y, QuickColor color) {
		Color rawColor  = new Color(toRGBVal(color.getRed()), toRGBVal(color.getGreen()), 
				toRGBVal(color.getBlue()), toRGBVal(color.getAlpha()));
		raw.setRGB(x, y, rawColor.getRGB());
	}

	private int fromRGBVal(int val) {
		return  (val * 4) - 500;
	}

	private int toRGBVal(int val) {
		int newVal = (val + 500) / 4;
		if(newVal < 0){
			return 0;
		}else if (newVal > 255){
			return 255;
		}else{
			return newVal;
		}
	}

	/**
	 * Do not call, or modify the visibility of this method.
	 * @return an image suitable for painting to the screen
	 */
	 public Image getPaintable(){
		 return raw;
	 }
}
