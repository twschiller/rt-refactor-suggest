package edu.washington.cs.rtrefactor.eval.tests;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import edu.washington.cs.rtrefactor.eval.QuickPicture;
import edu.washington.cs.rtrefactor.eval.transform.CartoonifyImageTransform;
import edu.washington.cs.rtrefactor.eval.transform.CartoonifyImageTransform.CartoonStyle;
import edu.washington.cs.rtrefactor.eval.transform.UnderwaterTransform;

//TODO EVAL: delete this class

/**
 * Hidden tests (not shown to the user during the evaluation)
 * @author Todd Schiller
 */
public class HiddenTests {
	private QuickPicture ghost;

	@Before
	public void setUp() throws Exception {
		 ghost = QuickPicture.read(new File(Common.IMAGE_DIR, "ghost_try.png"));
	}

	@Test
	public void testUnderwater() {     
		Common.testTransform(ghost, new UnderwaterTransform(true), Common.tryRead("Ghost_foo.png"));
	}

	@Test
	public void testCartoon() {
		Common.testTransform(ghost, new CartoonifyImageTransform(CartoonStyle.RETRO), Common.tryRead("Ghost_bar.png"));
	}
        
}
