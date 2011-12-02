package edu.washington.cs.rtrefactor.eval;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JPanel;

import edu.washington.cs.rtrefactor.eval.transform.ImageTransform;

@SuppressWarnings("serial")
public class MainImage extends JPanel{

	private QuickPicture image;
	
	protected MainImage(QuickPicture image, int width, int height) {
		super();
		this.image = image;
		this.setSize(width, height);
		this.setMinimumSize(new Dimension(width,height));
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
