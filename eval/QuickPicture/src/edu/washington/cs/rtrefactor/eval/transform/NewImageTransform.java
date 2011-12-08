package edu.washington.cs.rtrefactor.eval.transform;

import edu.washington.cs.rtrefactor.eval.QuickPicture;

public class NewImageTransform implements ImageTransform{
	private int picId;
	public NewImageTransform(int picId) {
		this.picId = picId;
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
