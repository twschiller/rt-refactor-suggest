package edu.washington.cs.rtrefactor.eval.transform;

import edu.washington.cs.rtrefactor.eval.QuickPicture;

public class Flip implements ImageTransform{

	@Override
	public QuickPicture transform(QuickPicture old) {
		QuickPicture res = new QuickPicture(old.getWidth(), old.getHeight());
		
		for (int r = 0; r < old.getHeight(); r++){
			for (int c = 0; c < old.getWidth(); c++){
				res.setRed(c, old.getHeight() - r - 1, old.getRed(c, r));
				res.setGreen(c, old.getHeight() - r - 1, old.getGreen(c, r));
				res.setBlue(c, old.getHeight() - r - 1, old.getBlue(c, r));
				res.setAlpha(c, old.getHeight() - r - 1, old.getAlpha(c, r));
			}
		}
		return res;
	}

}
