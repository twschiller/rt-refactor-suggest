package edu.washington.cs.rtrefactor.eval.tests;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import edu.washington.cs.rtrefactor.eval.QuickPicture;
import edu.washington.cs.rtrefactor.eval.transform.UnderwaterTransform;

public class TransformTest {
	
	private QuickPicture astronaut;
	private QuickPicture puppy;
	
	@Before
	public void setUp() throws Exception {
		astronaut = QuickPicture.read(new File(Common.IMAGE_DIR, "Astronaut_small.jpg"));
		puppy = QuickPicture.read(new File(Common.IMAGE_DIR, "Puppy_small.jpg"));
	}

	@Test
	public void testUnderwater() {
		Common.testTransform(puppy, new UnderwaterTransform(false), Common.tryRead("Puppy_underwater.jpg"));		
		Common.testTransform(astronaut, new UnderwaterTransform(false), Common.tryRead("Astronaut_underwater.jpg"));
	}
	
	@Test
	public void testNewTransform() {
		fail("Unimplemented");
	}

}
