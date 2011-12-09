package edu.washington.cs.rtrefactor.eval.transform;
import edu.washington.cs.rtrefactor.eval.ImageTransform;
import edu.washington.cs.rtrefactor.eval.QuickColor;
import edu.washington.cs.rtrefactor.eval.QuickPicture;

public class PhotographicImageTransform implements ImageTransform{

	public enum PhotoMode  { FIRST, SECOND}

	public static final int SIZE = 10;
	public static final int SPACE = 5;
	
	private QuickColor myColor;
	private int offX;
	private int offY;

	private PhotoMode[] myModes;
	
	/**
	 * Initialize the transformation with the necessary data to use.
	 * @param modes  The list of modes indicating the sequence of annotations to add to the image
	 * @param qc The main color to use when transforming the image
	 */
	public PhotographicImageTransform(PhotoMode[] modes, QuickColor qc, int offX, int offY)
	{
		myModes = modes;
		myColor = qc;
		this.offX = offX;
		this.offY = offY;
	}
	
	/**
	 * Transforms the picture.
	 *   
	 * More specifically, applies a sequence of 
	 * transformations to the image which encode the information stored in the modes
	 * array into the pixels of the image, where it can be later retrieved by 
	 * computer vision procedures.
	 */
	@Override
	public QuickPicture transform(QuickPicture old) {
		QuickPicture result = new QuickPicture(old.getWidth(), old.getHeight());
		for(int r=0; r<old.getHeight(); r++)
			for(int c=0; c<old.getWidth(); c++)
				result.setColor(c, r, old.getColor(c, r));

		
		for(int i=0; i<myModes.length; i++)
		{
			PhotoMode myMode = myModes[i];
			int cOff = (SPACE+SIZE) *i;
			if(myMode.equals(PhotoMode.FIRST)) {
				//vertical
				for(int y = offY; y<offY+2*SIZE; y++) {
					setSafeColor(result, offX + cOff, y, myColor);
					setSafeColor(result, offX+SIZE + cOff, y, myColor);
				}
				//horizontal
				for(int x = offX; x<offX+SIZE; x++) {
					setSafeColor(result, x+cOff, offY , myColor);
					setSafeColor(result, x + cOff, offY+2*SIZE, myColor);
				}
			} else {
				//vertical
				for(int y = offY; y<offY+2*SIZE; y++) {
					setSafeColor(result, offX+(SIZE/2)+cOff, y, myColor);
				}
			}
		}



		return result;
	}

	private void setSafeColor(QuickPicture p, int c, int r, QuickColor newColor) {
		if(c < 0 || c >= p.getWidth())
			return;

		if(r < 0 || r >= p.getHeight())
			return;

		p.setColor(c, r, new QuickColor(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), 
				p.getColor(c, r).getAlpha()));

	}

}
