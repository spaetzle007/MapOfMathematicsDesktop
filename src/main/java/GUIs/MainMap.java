package GUIs;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.EventQueue;
import java.awt.event.ActionListener;
import java.awt.event.WindowListener;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.DeleteErrorException;


//Every JFrame in here is the MapJFrame and should be referenced as such
public class MainMap extends JFrame implements ActionListener, WindowListener {
	private MainViewport viewport;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					//MapJFrame erstellen mit Konstrkutor
					//Sichtbar machen
					MainMap MapJFrame = new MainMap();
					MapJFrame.setVisible(true);
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public MainMap() {
		//Creates new Viewport
		viewport = new MainViewport();
		
		//Sets the variables of the JFrame MainMap
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, (int) Hilfsklassen.Variables.standardsize.getWidth(), (int)Hilfsklassen.Variables.standardsize.getHeight()); 	//Format: 1920x1080
		setContentPane(viewport);
		setTitle("MapOfMathematics");
		
		
	}
	
	//Perform Action according to Action Event
	public void actionPerformed(ActionEvent e) {
		
	}
	
	
	//Block copied from Main Editing to implement abstract methods 
	//inherited by WindowListener
	@Override
	public void windowActivated(WindowEvent arg0) {}
	@Override
	public void windowClosed(WindowEvent arg0) {}
	@Override
	public void windowClosing(WindowEvent arg0) {
		setVisible(false);
	}
	@Override
	public void windowDeactivated(WindowEvent arg0) {}
	@Override
	public void windowDeiconified(WindowEvent arg0) {}
	@Override
	public void windowIconified(WindowEvent arg0) {}
	@Override
	public void windowOpened(WindowEvent arg0) {}
	
}
