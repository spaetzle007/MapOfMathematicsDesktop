package GUIs;

import java.awt.EventQueue;

import javax.swing.JFrame;

public class MainMap extends JFrame {
	private MainViewport viewport;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainMap frame = new MainMap();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public MainMap() {
		viewport=new MainViewport();
	}
}
