package edu.washington.cs.rtrefactor.eval;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import edu.washington.cs.rtrefactor.eval.transform.Blocky;
import edu.washington.cs.rtrefactor.eval.transform.Blur;
import edu.washington.cs.rtrefactor.eval.transform.Colorize;
import edu.washington.cs.rtrefactor.eval.transform.Flip;
import edu.washington.cs.rtrefactor.eval.transform.Alpha;
import edu.washington.cs.rtrefactor.eval.transform.ImageTransform;

@SuppressWarnings("serial")
public class PaintShop extends JFrame{

	private final JPanel transformPanel;
	private final MainImage image;
	
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

	private PaintShop() throws IOException{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setDefaultLookAndFeelDecorated(true);
	
		transformPanel = new JPanel();
		transformPanel.setLayout(new BoxLayout(transformPanel, BoxLayout.Y_AXIS));
		addTransform("Flip", new Flip());
		addTransform("Casper", new Alpha.Ghost25());
		addTransform("Casper++", new Alpha.Ghost50());
		addTransform("Referee", new Colorize.Zebra());
		addTransform("Blur", new Blur());
		addTransform("Blockify", new Blocky());
		
		image = new MainImage(QuickPicture.read(new File("/home/tws/Astronaut.jpg")),400,400);
		
		getContentPane().setLayout(new BorderLayout());
			
		getContentPane().add(transformPanel, BorderLayout.LINE_START);
		getContentPane().add(image, BorderLayout.CENTER);
		
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
