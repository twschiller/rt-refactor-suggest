package edu.washington.cs.rtrefactor.eval.tests;

import java.io.File;

import org.junit.Before;

import edu.washington.cs.rtrefactor.eval.QuickPicture;

/**
 * Hidden tests (not shown to the user during the evaluation)
 * @author Todd Schiller
 */
public class HiddenTests {

	private QuickPicture astronaut;
	private QuickPicture puppy;
	
	@Before
	public void setUp() throws Exception {
		astronaut = QuickPicture.read(new File(Common.IMAGE_DIR, "Astronaut_small.jpg"));
		puppy = QuickPicture.read(new File(Common.IMAGE_DIR, "Puppy_small.jpg"));
	}
}
