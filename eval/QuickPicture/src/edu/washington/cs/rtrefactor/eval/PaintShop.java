package edu.washington.cs.rtrefactor.eval;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import edu.washington.cs.rtrefactor.eval.transform.CartoonifyImageTransform;
import edu.washington.cs.rtrefactor.eval.transform.CartoonifyImageTransform.CartoonStyle;
import edu.washington.cs.rtrefactor.eval.transform.CinematicImageTransform;
import edu.washington.cs.rtrefactor.eval.transform.NewImageTransform;
import edu.washington.cs.rtrefactor.eval.transform.PhotographicImageTransform;
import edu.washington.cs.rtrefactor.eval.transform.PhotographicImageTransform.PhotoMode;
import edu.washington.cs.rtrefactor.eval.transform.RandomSceneGenerator;
import edu.washington.cs.rtrefactor.eval.transform.UnderwaterTransform;
import edu.washington.cs.rtrefactor.eval.transform.VisionImageTransform;
import edu.washington.cs.rtrefactor.eval.util.ImageUtil;

/**
 * The GUI for the image transformations
 * 
 * ImageTransforms should never directly access this class.
 * 
 * @author Todd Schiller
 *
 */
@SuppressWarnings("serial")
public class PaintShop extends JFrame{

        private final JPanel transformPanel;
        private final MainImage image;
        private final JMenuBar menuBar;
        private final JMenu fileMenu;
        
        private final JFileChooser fc = new JFileChooser();

        
        private void addTransform(String name, final ImageTransform transform){
                JButton button = new JButton(name);
                button.addActionListener(new ActionListener(){
                        @Override
                        public void actionPerformed(ActionEvent e) {
                                image.update(transform);
                        }
                });
                transformPanel.add(button);
        }

        private void addTransforms(){
                addTransform("Flip", new ImageUtil.Flip());
                addTransform("Cartoon", new CartoonifyImageTransform(CartoonStyle.EXPERIMENTAL));
		addTransform("Shrink", new ImageUtil.ShrinkImage(2));
                addTransform("Vision", new VisionImageTransform(50, 50, 500));
                addTransform("Underwater", new UnderwaterTransform(false));
                addTransform("New", new NewImageTransform(17, new QuickColor(-400, 400, -400, 0)));
                //TODO:Bugs
                addTransform("Cinematic", new CinematicImageTransform());
                addTransform("Random", new RandomSceneGenerator(new QuickColor(400,-400,400,0), 100, 100, 200000));
        }
        
        private PaintShop() throws IOException{
                setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                setDefaultLookAndFeelDecorated(true);
        
                transformPanel = new JPanel();
                transformPanel.setLayout(new BoxLayout(transformPanel, BoxLayout.Y_AXIS));
                addTransforms();
                
                menuBar = new JMenuBar();
                fileMenu = new JMenu("File");
                menuBar.add(fileMenu);
                
                JMenuItem open = new JMenuItem("Open");
                open.addActionListener(new ActionListener(){
                        @Override
                        public void actionPerformed(ActionEvent e) {
                                if (fc.showOpenDialog(PaintShop.this) == JFileChooser.APPROVE_OPTION){
                                        try {
                                                image.update(QuickPicture.read(fc.getSelectedFile()));
                                        } catch (IOException ex) {
                                                System.err.println(ex.getMessage());
                                                System.exit(1);
                                        }
                                }
                        }
                });
                fileMenu.add(open);
                
                JMenuItem save = new JMenuItem("Save");
                save.addActionListener(new ActionListener(){
                        @Override
                        public void actionPerformed(ActionEvent e) {
                                if (fc.showOpenDialog(PaintShop.this) == JFileChooser.APPROVE_OPTION){
                                        try {
                                                image.image().write(fc.getSelectedFile());
                                        } catch (IOException ex) {
                                                System.err.println(ex.getMessage());
                                                System.exit(1);
                                        }
                                }
                        }
                });
                fileMenu.add(save);
                
                JMenuItem exit = new JMenuItem("Exit");
                exit.addActionListener(new ActionListener(){
                        @Override
                        public void actionPerformed(ActionEvent arg0) {
                                System.exit(0);
                        }
                });
                fileMenu.add(exit);
                
                image = new MainImage(400,400);
                
                getContentPane().setLayout(new BorderLayout());
                        
                getContentPane().add(transformPanel, BorderLayout.LINE_START);
                getContentPane().add(image, BorderLayout.CENTER);
                
                this.setJMenuBar(menuBar);
                setVisible(true);
                
                this.setSize(500, 400);
        }
        
        /**
         * @param args
         * @throws IOException 
         */
        public static void main(String[] args) throws IOException {
                new PaintShop();
        }

}
