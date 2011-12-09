package edu.washington.cs.rtrefactor.eval;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;


/**
 * Display for the image being edited.
 * 
 * ImageTransforms should never access this class.
 *  
 * @author Todd Schiller
 */
@SuppressWarnings("serial")
public class MainImage extends JPanel{

	private QuickPicture image;
	
	protected MainImage(int width, int height) {
		super();
		this.image = null;
		this.setSize(width, height);
		this.setMinimumSize(new Dimension(width,height));
	}
	
	protected MainImage(QuickPicture image) {
		super();
		this.image = image;
		this.setSize(image.getWidth(), image.getHeight());
		this.setMinimumSize(new Dimension(image.getWidth(), image.getHeight()));
	}
	
	/**
	 * Update the display with <code>image</code>, repainting if necessary
	 * @param image the new image
	 */
	public void update(QuickPicture image){
		if (this.image != image){
			this.image = image;
			repaint();
		}
	}	
	
	protected QuickPicture image(){
		return image;
	}
	
	/**
	 * Update the display using the given <code>transform</code>
	 * @param transform the transform
	 */
	public void update(ImageTransform transform){
		update(transform.transform(image));
	}	

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		// draw the image, if there is one.	
		if (image != null){
			g.drawImage((Image) image.getPaintable(), 0, 0, null);
		}		
	}
	
}
