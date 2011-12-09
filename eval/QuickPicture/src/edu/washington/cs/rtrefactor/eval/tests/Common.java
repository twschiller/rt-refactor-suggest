package edu.washington.cs.rtrefactor.eval.tests;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;
import edu.washington.cs.rtrefactor.eval.QuickPicture;
import edu.washington.cs.rtrefactor.eval.ImageTransform;

/**
 * Common testing functions
 * @author Todd Schiller
 */
public class Common {

	public static File IMAGE_DIR = new File("/home/tws/projects/rt-refactor-suggest/eval/QuickPicture/img");
	
	/**
	 * Read a picture, or fail with {@link Assert#fail(String)}
	 * @param file the picture to read
	 * @return the picture, or null
	 */
	public static QuickPicture tryRead(String file){
		try {
			return QuickPicture.read(new File(IMAGE_DIR,file));
		} catch (IOException e) {
			Assert.fail("Error reading file " + file);
			return null;
		}
	}
	
	/**
	 * Test that applying <code>transform</code> to <code>original</code> results in <code>target</code>
	 * @param original the original image
	 * @param transform the transformation under test
	 * @param target the target image
	 * @throws AssertionFailedError iff the two pictures are not equal
	 */
	public static void testTransform(QuickPicture original, ImageTransform transform, QuickPicture target){
		assertEquals(target,transform.transform(original));
	}
	
	/**
	 * Assert that two {@link QuickPicture}s are equal.
	 * @param expected the expected image
	 * @param actual the actual image
	 * @throws AssertionFailedError iff the two pictures are not equal
	 */
	public static void assertEquals(QuickPicture expected, QuickPicture actual){
		Assert.assertEquals("Unexpected image width", expected.getWidth(), actual.getWidth());
		Assert.assertEquals("Unexpected image height", expected.getHeight(), actual.getHeight());
		
		for (int r = 0; r < expected.getHeight(); r++){
			for (int c = 0; c < expected.getWidth(); c++){
				Assert.assertEquals("Unexpected color at x:" + c + " y:" + r, expected.getColor(c, r), actual.getColor(c, r));
			}
		}		
	}

}
