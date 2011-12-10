package edu.washington.cs.rtrefactor.eval.tests;

import java.io.File;
import java.io.IOException;

import edu.washington.cs.rtrefactor.eval.ImageTransform;
import edu.washington.cs.rtrefactor.eval.QuickColor;
import edu.washington.cs.rtrefactor.eval.QuickPicture;
import edu.washington.cs.rtrefactor.eval.transform.CartoonifyImageTransform;
import edu.washington.cs.rtrefactor.eval.transform.CinematicImageTransform;
import edu.washington.cs.rtrefactor.eval.transform.NewImageTransform;
import edu.washington.cs.rtrefactor.eval.transform.UnderwaterTransform;
import edu.washington.cs.rtrefactor.eval.transform.VisionImageTransform;
import edu.washington.cs.rtrefactor.eval.transform.CartoonifyImageTransform.CartoonStyle;
import edu.washington.cs.rtrefactor.eval.util.ImageUtil;

// TODO EVAL: delete this class

public class GenerateTargets {

    private static QuickPicture astronaut;
    private static QuickPicture puppy;
    private static QuickPicture ghost;

    private static void writeTarget(QuickPicture original, ImageTransform transform, String target) throws IOException{
        transform.transform(original).write(new File(Common.IMAGE_DIR, target));
    }


    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        astronaut = QuickPicture.read(new File(Common.IMAGE_DIR, "Astronaut.jpg"));
        puppy = QuickPicture.read(new File(Common.IMAGE_DIR, "Puppy.jpg"));
        ghost = QuickPicture.read(new File(Common.IMAGE_DIR, "Ghost.png"));

        writeTarget(astronaut, new NewImageTransform(17, new QuickColor(-400, 400, -400, 0)), "Astronaut_new.jpg");
        writeTarget(puppy, new NewImageTransform(10, new QuickColor(-400, 400, 400, 0)), "Puppy_new.jpg");

        writeTarget(ghost, new ImageUtil.ShrinkImage(2), "Ghost_shrink.png");

        writeTarget(puppy, new UnderwaterTransform(false), "Puppy_underwater.jpg");            
        writeTarget(astronaut, new UnderwaterTransform(false), "Astronaut_underwater.jpg");

        writeTarget(astronaut, new ImageUtil.Flip(), "Astronaut_flip.jpg");
        writeTarget(puppy,  new CartoonifyImageTransform(CartoonStyle.EXPERIMENTAL), "Puppy_cartoon.jpg");
        writeTarget(astronaut, new VisionImageTransform(50, 50, 500), "Astronaut_vision.jpg");
        writeTarget(puppy,  new CinematicImageTransform(), "Puppy_movie.jpg");

        // HIDDEN TESTS

        writeTarget(ghost, new UnderwaterTransform(true), "Ghost_foo.png");           
        writeTarget(ghost, new CartoonifyImageTransform(CartoonStyle.RETRO), "Ghost_bar.png");
    }
}