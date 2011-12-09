package edu.washington.cs.rtrefactor.eval;


public interface ImageTransform {

	/**
	 * Transform an image
	 * @param old the original image
	 * @return the new image
	 */
	public QuickPicture transform(QuickPicture old);
	
}
