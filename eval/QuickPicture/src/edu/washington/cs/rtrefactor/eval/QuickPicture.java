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

public class QuickPicture extends java.awt.Image {

	private BufferedImage raw;
	
	public QuickPicture(int width, int height){
		raw = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
	}
	
	private QuickPicture(BufferedImage rhs){
		raw = new BufferedImage(rhs.getWidth(), rhs.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = raw.getGraphics();
		g.drawImage(rhs, 0, 0, null);
	}
	
	public static QuickPicture read(File input) throws IOException{
		return new QuickPicture(ImageIO.read(input));
	}
	
	public void write(File input) throws IOException{
		ImageIO.write(raw, "PNG", input);
	}
	
	public int getWidth() {
		return raw.getWidth();
	}
	
	public int getHeight() {
		return raw.getHeight();
	}

	@Override
	public Graphics getGraphics() {
		return raw.getGraphics();
	}

	@Override
	public int getHeight(ImageObserver observer) {
		return getHeight();
	}

	@Override
	public Object getProperty(String name, ImageObserver observer) {
		return raw.getProperty(name, observer);
	}

	/**
	 * returns <code>null</code>
	 * @return returns <code>null</code>
	 */
	@Override
	public ImageProducer getSource() {
		return null;
	}

	@Override
	public int getWidth(ImageObserver observer) {
		return getWidth();
	}
	
	public QuickColor getColor(int x, int y){
		if(x < 0 || x >= raw.getWidth() || y< 0 || y >= raw.getHeight())
			return null;
		Color rawColor = new Color(raw.getRGB(x, y));
		return new QuickColor(fromRGBVal(rawColor.getRed()), fromRGBVal(rawColor.getGreen()), 
				fromRGBVal(rawColor.getBlue()), fromRGBVal(rawColor.getAlpha()));
	}

	public void setColor(int x, int y, QuickColor color) {
		Color rawColor  = new Color(toRGBVal(color.getRed()), toRGBVal(color.getGreen()), 
				toRGBVal(color.getBlue()), toRGBVal(color.getAlpha()));
		raw.setRGB(x, y, rawColor.getRGB());
		//TODO: alpha?
	}
	
	private int fromRGBVal(int val) {
		return  (val * 4) - 500;
	}
	
	private int toRGBVal(int val) {
	
		int newVal =   (val +500) /4;
		if(newVal < 0)
			return 0;
		if(newVal > 255)
			return 255;
		
		return newVal;
	}
	
	/**
	 * Do not call, or modify the visibility of this method.
	 * @return an image suitable for painting to the screen
	 */
	protected Image getPaintable(){
		return raw;
	}
}
