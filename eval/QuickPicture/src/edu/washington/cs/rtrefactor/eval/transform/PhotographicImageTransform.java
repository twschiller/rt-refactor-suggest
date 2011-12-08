package edu.washington.cs.rtrefactor.eval.transform;
import edu.washington.cs.rtrefactor.eval.QuickColor;
import edu.washington.cs.rtrefactor.eval.QuickPicture;

public class PhotographicImageTransform implements ImageTransform{

	public enum PhotoMode  { FIRST, SECOND}

	public static final int OFFX = 4;
	public static final int OFFY = 5;
	public static final int SIZE = 10;
	public static final int SPACE = 4;

	private PhotoMode[] myModes;

	public PhotographicImageTransform(PhotoMode[] modes)
	{
		myModes = modes;
	}
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
				for(int y = OFFY; y<OFFY+2*SIZE; y++) {
					setSafeColor(result, OFFX + cOff, y, new QuickColor(0,0,0,0));
					setSafeColor(result, OFFX+SIZE + cOff, y, new QuickColor(0,0,0,0));
				}
				for(int x = OFFX; x<OFFX+SIZE; x++) {
					setSafeColor(result, x+cOff, OFFY , new QuickColor(0,0,0,0));
					setSafeColor(result, x + cOff, OFFY+2*SIZE, new QuickColor(0,0,0,0));
				}
			} else {
				for(int y = OFFY; y<OFFY+2*SIZE; y++) {
					setSafeColor(result, OFFX+(SIZE/2)+cOff, y, new QuickColor(0,0,0,0));
				}
			}
		}



		return result;
	}

	public void setSafeColor(QuickPicture p, int r, int c, QuickColor newColor) {
		if(r < 0 || r >= p.getHeight())
			return;

		if(c < 0 || c >= p.getWidth())
			return;

		p.setColor(r, c, new QuickColor(newColor.getRed(), newColor.getGreen(), newColor.getBlue(), 
				p.getColor(r, c).getAlpha()));

	}

}
