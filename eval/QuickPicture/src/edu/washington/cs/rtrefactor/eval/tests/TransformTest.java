package edu.washington.cs.rtrefactor.eval.tests;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import edu.washington.cs.rtrefactor.eval.QuickColor;
import edu.washington.cs.rtrefactor.eval.QuickPicture;
import edu.washington.cs.rtrefactor.eval.transform.CartoonifyImageTransform;
import edu.washington.cs.rtrefactor.eval.transform.CinematicImageTransform;
import edu.washington.cs.rtrefactor.eval.transform.NewImageTransform;
import edu.washington.cs.rtrefactor.eval.transform.UnderwaterTransform;
import edu.washington.cs.rtrefactor.eval.transform.VisionImageTransform;
import edu.washington.cs.rtrefactor.eval.transform.CartoonifyImageTransform.CartoonStyle;
import edu.washington.cs.rtrefactor.eval.util.ImageUtil;

public class TransformTest {

    private QuickPicture astronaut;
    private QuickPicture puppy;
    private QuickPicture ghost;

    @Before
    public void setUp() throws Exception {
        astronaut = QuickPicture.read(new File(Common.IMAGE_DIR, "Astronaut.jpg"));
        puppy = QuickPicture.read(new File(Common.IMAGE_DIR, "Puppy.jpg"));
        ghost = QuickPicture.read(new File(Common.IMAGE_DIR, "Ghost.png"));
    }

    /**
     * This test must pass at the end of the development task
     */
    @Test
    public void testNewTransform() {
        Common.testTransform(astronaut, new NewImageTransform(17, new QuickColor(-400, 400, -400, 0)), Common.tryRead("Astronaut_new.png"));
        Common.testTransform(puppy, new NewImageTransform(10, new QuickColor(-400, 400, 400, 0)), Common.tryRead("Puppy_new.png"));
    }

    /**
     * This test must pass at the end of the maintenance task
     */
    @Test
    public void testShrink() {
        Common.testTransform(ghost, new ImageUtil.ShrinkImage(2), Common.tryRead("Ghost_shrink.png"));
    }

    @Test
    public void testUnderwater() {
        Common.testTransform(puppy, new UnderwaterTransform(false), Common.tryRead("Puppy_underwater.png"));            
        Common.testTransform(astronaut, new UnderwaterTransform(false), Common.tryRead("Astronaut_underwater.png"));
    }

    @Test
    public void testFlip() {
        Common.testTransform(astronaut, new ImageUtil.Flip(), Common.tryRead("Astronaut_flip.png"));
    }

    @Test
    public void testCartoon() {
        Common.testTransform(puppy,  new CartoonifyImageTransform(CartoonStyle.EXPERIMENTAL), Common.tryRead("Puppy_cartoon.png"));
    }


    @Test
    public void testVision() {
        Common.testTransform(astronaut, new VisionImageTransform(50, 50, 500), Common.tryRead("Astronaut_vision.png"));
    }

    @Test
    public void testMovie() {
        Common.testTransform(puppy,  new CinematicImageTransform(), Common.tryRead("Puppy_movie.png"));
    }
}
