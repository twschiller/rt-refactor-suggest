package edu.washington.cs.rtrefactor.eval.transform;

import edu.washington.cs.rtrefactor.eval.ImageTransform;
import edu.washington.cs.rtrefactor.eval.QuickColor;
import edu.washington.cs.rtrefactor.eval.QuickPicture;
import edu.washington.cs.rtrefactor.eval.transform.PhotographicImageTransform.PhotoMode;

public strictfp class NewImageTransform implements ImageTransform{
	private int picId;
	private QuickColor idColor;

	public NewImageTransform(int picId, QuickColor idColor) {
		this.picId = picId;
		this.idColor = idColor;
	}

	@Override
	public QuickPicture transform(QuickPicture old) {
		// TODO Eval: delete the body of this method
		// return null


				// Development Task idea:
		// 1: Go through each pixel and blend it with the average of the 4 diagonal directions, 
		//      preserving the original alpha
		QuickPicture result = new QuickPicture(old.getWidth(), old.getHeight());
		for(int x=0; x<old.getWidth(); x++) {
			for(int y=0; y<old.getHeight(); y++) {
				QuickColor newColor = new QuickColor(0,0,0, old.getColor(x, y).getAlpha());
				QuickColor upperLeft = CartoonifyImageTransform.getSafeColor(old, x-1, y-1);
				QuickColor upperRight = CartoonifyImageTransform.getSafeColor(old, x+1, y-1);
				QuickColor lowerLeft = CartoonifyImageTransform.getSafeColor(old, x-1, y+1);
				QuickColor lowerRight = CartoonifyImageTransform.getSafeColor(old, x+1, y+1);
				newColor.setRed((upperLeft.getRed() + upperRight.getRed()
						+ lowerLeft.getRed() + lowerRight.getRed())/4);
				newColor.setGreen((upperLeft.getGreen() + upperRight.getGreen()
						+ lowerLeft.getGreen() + lowerRight.getGreen())/4);
				newColor.setBlue((upperLeft.getBlue() + upperRight.getBlue()
						+ lowerLeft.getBlue() + lowerRight.getBlue())/4);
				result.setColor(x, y, newColor);
			}
		}

		// 2: Write the picId in Binary in the lowerleft corner of the image (specify number format exactly)
		String binary = Integer.toBinaryString(picId);
		PhotoMode[] digits = new PhotoMode[binary.length()];
		for(int i=0; i<digits.length; i++)
		{
			if(binary.charAt(i) == '1') {
				digits[i] = PhotoMode.SECOND;
			} else {
				digits[i] = PhotoMode.FIRST;
			}
		}

		PhotographicImageTransform imgTrans = new PhotographicImageTransform(digits, idColor, 5, 
				old.getHeight()-30);

		return imgTrans.transform(result);
	}

}
