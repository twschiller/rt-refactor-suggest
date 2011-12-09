package edu.washington.cs.rtrefactor.eval.transform;

import edu.washington.cs.rtrefactor.eval.ImageTransform;
import edu.washington.cs.rtrefactor.eval.QuickColor;
import edu.washington.cs.rtrefactor.eval.QuickPicture;

public class NewImageTransform implements ImageTransform{
	private int picId;
	private QuickColor annotationColor;
	
	public NewImageTransform(int picId, QuickColor annotationColor) {
		this.picId = picId;
		this.annotationColor = annotationColor;
	}
	@Override
	public QuickPicture transform(QuickPicture old) {
		// Development Task idea:
		// 1: Go through each pixel and blend it with the average of the 4 diagonal directions, 
		//	preserving the original alpha
		// 2: Write the picId in Binary in the lowerleft corner of the image (specify number format exactly)
		return null;
	}
	
}
