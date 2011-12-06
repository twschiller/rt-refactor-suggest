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

	public int getRed(int x, int y){
		Color color = new Color(raw.getRGB(x, y));
		return color.getRed();
	}
	
	public int getBlue(int x, int y){
		Color color = new Color(raw.getRGB(x, y));
		return color.getBlue();
	}
	
	public int getGreen(int x, int y){
		Color color = new Color(raw.getRGB(x, y));
		return color.getGreen();
	}
	
	public int getAlpha(int x, int y){
		Color color = new Color(raw.getRGB(x, y));
		return color.getAlpha();
	}
	
	public void setRed(int x, int y, int r){
		Color color = new Color(raw.getRGB(x, y));
		raw.setRGB(x, y, new Color(r, color.getGreen(), color.getBlue(),color.getAlpha()).getRGB());
	}
	
	public void setBlue(int x, int y, int b){
		Color color = new Color(raw.getRGB(x, y));
		raw.setRGB(x, y, new Color(color.getRed(), color.getGreen(), b, color.getAlpha()).getRGB());
	}
	
	public void setGreen(int x, int y, int g){
		Color color = new Color(raw.getRGB(x, y));
		raw.setRGB(x, y, new Color(color.getRed(), g, color.getBlue(), color.getAlpha()).getRGB());
	}
	
	public void setAlpha(int x, int y, int a){
		Color color = new Color(raw.getRGB(x, y));
		raw.setRGB(x, y, new Color(color.getRed(), color.getGreen(),  color.getBlue(), a).getRGB());
	}
	
	/**
	 * Do not call, or modify the visibility of this method.
	 * @return an image suitable for painting to the screen
	 */
	protected Image getPaintable(){
		return raw;
	}
}
