package GUIs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.SwingConstants;

import org.scilab.forge.jlatexmath.ParseException;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import com.spaetzle007.MapOfMathematicsLibraries.AccessException;
import com.spaetzle007.MapOfMathematicsLibraries.DataHandler;
import com.spaetzle007.MapOfMathematicsLibraries.Linked;
import com.spaetzle007.MapOfMathematicsLibraries.LinkedList;
import com.spaetzle007.MapOfMathematicsLibraries.LinkedParseException;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class MainViewport extends JPanel {
		//Hilfsvariablen für LinkedList
		private LinkedList links;
		private Linked actual;
		private ArrayList<String> actualEqualLinks;
		private ArrayList<String> actualSubLinks;
		
		//Hilfsvariablen für Suche
		boolean searchmode;
		private ArrayList<String> searchresults;
		
		//GUI-Komponenten
		private JLabel viewport, headerlist, headerviewport;
		private JScrollPane listscroller, viewportscroller;
		private JList<Hilfsklassen.ColoredString> list;
		private DefaultListModel<Hilfsklassen.ColoredString> listhandler;
		private JTextField search;
		private JButton back;
		private GridBagLayout layout;

		/**
		 * Main-Methode zum Starten der Ansicht
		 */
		public static void main(String[] args) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						JFrame rahmen=new JFrame();
						rahmen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
						rahmen.setBounds(0, 0, (int)Hilfsklassen.Variables.standardsize.getWidth(), (int)Hilfsklassen.Variables.standardsize.getHeight()); 	//Format: 1920x1080
						
						MainViewport frame = new MainViewport();
						rahmen.setContentPane(frame);
						rahmen.setTitle("MapOfMathematics");
						rahmen.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
		
		/**Editing
		 * Konstruktor zur Erzeugung und Initialisierung des GUI
		 */
		public MainViewport() {
			//LinkedList-Variablen initialisieren
			try {
				links= new LinkedList(DataHandler.cutLast(MainEditing.class.getProtectionDomain().getCodeSource().getLocation().getPath())/*+File.seperator*/+"/MOM.xml");
			} catch(AccessException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(), "Fehler", 3);
				System.exit(1);
			} catch(LinkedParseException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(), "Fehler", 3);
			}
			actual = links.get(links.search("Mathematik"));
			
			//Searchmode-Variablen initialisieren
			searchmode=false;
			searchresults=new ArrayList<String>();
			
			//GUI erstellen
			createGUI();
			
			//install fonts
			try {
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new BufferedInputStream(getClass().getResourceAsStream("/Laksaman.ttf"))));
				ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new BufferedInputStream(getClass().getResourceAsStream("/Laksaman-Bold.ttf"))));
				ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new BufferedInputStream(getClass().getResourceAsStream("/Laksaman-Italic.ttf"))));
				ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new BufferedInputStream(getClass().getResourceAsStream("/Laksaman-BoldItalic.ttf"))));
			} catch(FontFormatException e) {
				JOptionPane.showMessageDialog(null, "Font-Format fehlerhaft", "Fehler", 3);
				Hilfsklassen.Variables.fontname="Dialog";
			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, "Fonts nicht verfügbar", "Fehler", 3);	
				Hilfsklassen.Variables.fontname="Dialog";
			}
			
			//GUI-Inhalt auf Anfangseinstellungen
			update();
		}
		/**
		 * Ausgabe aktualisieren
		 */
		private void update() {
			//Überschrift
			headerviewport.setText(actual.getName());	
					
			//LatexText
			TeXFormula  formula = null;
			try {
				formula = new TeXFormula(actual.getJLatexMathRepresentation());
			} catch (ParseException f) {
				formula=new TeXFormula("\\textbf{Ungültige LaTeX-Eingabe!}");
			}
			TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 17);
			BufferedImage img = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
			icon.paintIcon(new JLabel(), img.getGraphics(), 0, 0);
			viewport.setIcon(icon);
			
			//list aktualisieren
			listhandler.clear();
			actualEqualLinks=links.getEqualLinks(actual);
			actualSubLinks=links.getSubLinks(actual);
			if(searchmode) {
				for(int i=0; i<searchresults.size(); i++) {
					listhandler.addElement(new Hilfsklassen.ColoredString(searchresults.get(i), (byte)3));
				}
			} else {
				if(!actual.getName().equals("Mathematik")) {
					listhandler.addElement(new Hilfsklassen.ColoredString(actual.getSupLink(), (byte)0));
				}
				if(!actual.getName().equals("Mathematik")) {
					for(int i=0; i<actualEqualLinks.size(); i++) {
						listhandler.addElement(new Hilfsklassen.ColoredString(actualEqualLinks.get(i), (byte)1));
					}
				}
				for(int i=0; i<actualSubLinks.size(); i++) {
					listhandler.addElement(new Hilfsklassen.ColoredString(actualSubLinks.get(i), (byte)2));
				}
				for(int i=0; i<actual.getLinks().size(); i++) {
					listhandler.addElement(new Hilfsklassen.ColoredString(actual.getLinks().get(i)));
				}
			}
		}

		/**
		 * GUI erstellen
		 */
		private void createGUI() {
			setBackground(new Color(255, 204, 153));
			setBorder(null);
			layout=new GridBagLayout();
			setLayout(layout);
			
			GridBagConstraints c=new GridBagConstraints();
			//Position in Grid: gridx und gridy
			//Größe des Fensters: gridwidth: immer 1; gridy; immer 1, bis auf viewportscroller(3)
			//Ausfüllmodus: Immer horizontal, außer bei viewportscroller(both)
			//Rand: insets für Rand auf allen 4 Seiten immer 10
			//Ausrichtung(anchor)
			//Ausfüllrechte (weightx, weighty)
			c.anchor=GridBagConstraints.FIRST_LINE_START;
			c.insets=new Insets(5,5,5,5);
			
			c.weightx=1.0;
			//Viewport initialisieren
			headerviewport = new JLabel();
			headerviewport.setFont(new Font(Hilfsklassen.Variables.fontname, Font.BOLD, 36));
			headerviewport.setHorizontalAlignment(SwingConstants.CENTER);
			c.gridx=0; c.gridy=0; c.fill=GridBagConstraints.HORIZONTAL; c.weighty=0.0;
			add(headerviewport, c);
			
			viewport = new JLabel();
			viewport.setBorder(null);
			viewport.setVerticalAlignment(SwingConstants.TOP);
			viewport.setHorizontalAlignment(SwingConstants.LEFT);
			
			viewportscroller= new JScrollPane();
			viewportscroller.setBorder(null);
			viewportscroller.setViewportView(viewport);
			c.gridx=0; c.gridy=1; c.gridheight=3; c.fill=GridBagConstraints.BOTH; c.weighty=1.0;
			add(viewportscroller, c);
			
			c.weightx=0.0;
			//list initialisieren
			headerlist = new JLabel("Verknüpfte Themen");
			headerlist.setFont(new Font(Hilfsklassen.Variables.fontname, Font.BOLD, 20));
			headerlist.setHorizontalAlignment(SwingConstants.CENTER);
			headerlist.setVerticalAlignment(SwingConstants.CENTER);
			headerlist.setMinimumSize(new Dimension(Hilfsklassen.Variables.rechterRand, (int)headerlist.getPreferredSize().getHeight()));
			headerlist.setPreferredSize(new Dimension(Hilfsklassen.Variables.rechterRand, (int)headerlist.getPreferredSize().getHeight()));
			headerlist.setMaximumSize(new Dimension(Hilfsklassen.Variables.rechterRand, (int)headerlist.getPreferredSize().getHeight()));
			c.gridx=1; c.gridy=0; c.gridheight=1; c.fill=GridBagConstraints.HORIZONTAL; c.weighty=0.0;
			add(headerlist, c);
			
			list = new JList<Hilfsklassen.ColoredString>();
			list.setBorder(null);
			list.setFont(new Font(Hilfsklassen.Variables.fontname, Font.PLAIN, 12));
			list.setBackground(new Color(255, 204, 153));
			list.setCellRenderer(new DefaultListCellRenderer() {		//Inhalte passend färben
				public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
					Component c=super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					for(int i=0; i<Hilfsklassen.ColoredString.colors.length; i++ ) {
						if(value instanceof Hilfsklassen.ColoredString && ((Hilfsklassen.ColoredString) value).getType()==(byte)i) {
							setBackground(Hilfsklassen.ColoredString.colors[i]);
						}
					}
					return c;
					
				}
			});
			list.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount()==2) {					//Mit Doppelclick neuen Eintrag auswählen
						JList<String> treffer = (JList<String>)e.getSource();
						int index = treffer.locationToIndex(e.getPoint());
						
						actual=links.get(links.search(listhandler.get(index).getName()));
						
						searchmode=false;
						update();
					}
				}
			});
			listhandler=new DefaultListModel<Hilfsklassen.ColoredString>();
			list.setModel(listhandler);
			listscroller= new JScrollPane();
			listscroller.setBorder(null);
			listscroller.setViewportView(list);
			listscroller.setMinimumSize(new Dimension(Hilfsklassen.Variables.rechterRand, (int)listscroller.getPreferredSize().getHeight()));
			listscroller.setPreferredSize(new Dimension(Hilfsklassen.Variables.rechterRand, (int)listscroller.getPreferredSize().getHeight()));
			listscroller.setMaximumSize(new Dimension(Hilfsklassen.Variables.rechterRand, (int)listscroller.getPreferredSize().getHeight()));
			c.gridx=1; c.gridy=1; c.weighty=1.0; c.fill=GridBagConstraints.BOTH;
			add(listscroller, c);

			//Such-Feld initialisieren
			search = new JTextField("Suche");
			search.setHorizontalAlignment(SwingConstants.LEFT);
			search.setBorder(null);
			search.setFont(new Font(Hilfsklassen.Variables.fontname, Font.PLAIN, 18));
			search.setBackground(Color.decode("#FFB366"));
			search.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					search.setText("");
				}
			});
			search.addKeyListener(new KeyListener() {
				public void keyTyped(KeyEvent e) {}
				public void keyReleased(KeyEvent e) {}
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==KeyEvent.VK_ENTER) {			//Suchen mit "ENTER"
						searchmode=true;
						
						listhandler.clear();
						searchresults.clear();
						for(int i=0; i<links.size(); i++) {
							if(links.get(i).getName().contains(search.getText())) {
								searchresults.add(links.get(i).getName());
							}
						}
						
						update();
						search.setText("Suche");
					}
				}
			});
			search.setMinimumSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
			search.setPreferredSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
			search.setMaximumSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
			c.gridx=1; c.gridy=2; c.weighty=0.0; c.fill=GridBagConstraints.HORIZONTAL;
			add(search, c);
			
			//Zurück-Button
			back=new JButton("Zurück");
			back.setBorder(null);
			back.setFont(new Font(Hilfsklassen.Variables.fontname, Font.PLAIN, 24));
			back.setBackground(Color.decode("#FFB366"));
			back.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					actual=links.get(links.search(actual.getSupLink()));
					update();
				}
			});
			back.setMinimumSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
			back.setPreferredSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
			back.setMaximumSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
			c.gridx=1; c.gridy=3; c.weighty=0.0;
			add(back, c);
		}
		public Linked getLinked() {return actual;}
		public LinkedList getLinkedList() {return links;}
}
