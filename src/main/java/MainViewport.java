import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.SwingConstants;

import org.scilab.forge.jlatexmath.ParseException;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import com.spaetzle007.MapOfMathematicsLibraries.AccessException;
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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class MainViewport extends JFrame {
		//Hilfsvariablen für LinkedList
		private LinkedList links;
		private Linked actual;
		private ArrayList<String> actualEqualLinks;
		private ArrayList<String> actualSubLinks;
		
		//Hilfsvariablen für Suche
		boolean searchmode;
		private ArrayList<String> searchresults;
		
		//GUI-Komponenten
		private JPanel contentPane;
		private JLabel viewport, headerlist, headerviewport;
		private JScrollPane listscroller, viewportscroller;
		private JList<ColoredString> list;
		private DefaultListModel<ColoredString> listhandler;
		private JTextField search;
		private JButton back;

		/**
		 * Main-Methode zum Starten der Ansicht
		 */
		public static void main(String[] args) {
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					try {
						MainViewport frame = new MainViewport();
						frame.setVisible(true);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		}
		
		/**
		 * Konstruktor zur Erzeugung und Initialisierung des GUI
		 */
		public MainViewport() {
			//LinkedList-Variablen initialisieren
			try {
				links = new LinkedList();
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
			createGUIBasics();
			createGUI();
			
			//install fonts
			try {
				GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
				ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new BufferedInputStream(getClass().getResourceAsStream("/Laksaman.ttf"))));
				ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new BufferedInputStream(getClass().getResourceAsStream("/Laksaman-Bold.ttf"))));
				ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new BufferedInputStream(getClass().getResourceAsStream("/Laksaman-Italic.ttf"))));
				ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, new BufferedInputStream(getClass().getResourceAsStream("/Laksaman-BoldItalic.ttf"))));
			} catch (IOException|FontFormatException e) {
				JOptionPane.showMessageDialog(null, "Fonts nicht verfügbar", "Fehler", 3);	
				Variables.fontname="Dialog";
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
					
			//Text
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
					listhandler.addElement(new ColoredString(searchresults.get(i), (byte)3));
				}
			} else {
				if(!actual.getName().equals("Mathematik")) {
					listhandler.addElement(new ColoredString(actual.getSupLink(), (byte)0));
				}
				if(!actual.getName().equals("Mathematik")) {
					for(int i=0; i<actualEqualLinks.size(); i++) {
						listhandler.addElement(new ColoredString(actualEqualLinks.get(i), (byte)1));
					}
				}
				for(int i=0; i<actualSubLinks.size(); i++) {
					listhandler.addElement(new ColoredString(actualSubLinks.get(i), (byte)2));
				}
				for(int i=0; i<actual.getLinks().size(); i++) {
					listhandler.addElement(new ColoredString(actual.getLinks().get(i)));
				}
			}
		}
		/**
		 * JFrame erstellen
		 */
		private void createGUIBasics() {
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setBounds(0, 0, (int)Variables.standardsize.getWidth(), (int)Variables.standardsize.getHeight());								//Format: 1920x1080
			setResizable(false);
			contentPane = new JPanel();
			contentPane.setBackground(new Color(255, 204, 153));
			contentPane.setBorder(null);
			setContentPane(contentPane);
			setTitle("MapOfMathematics");
			contentPane.setLayout(null);
		}
		/**
		 * GUI erstellen
		 */
		private void createGUI() {
			//Viewport initialisieren
			headerviewport = new JLabel();
			headerviewport.setFont(new Font(Variables.fontname, Font.BOLD, 36));
			headerviewport.setHorizontalAlignment(SwingConstants.CENTER);
			headerviewport.setBounds(10, 10, (int)Variables.standardsize.getWidth()-20, 60);
			contentPane.add(headerviewport);
			
			viewport = new JLabel();
			viewport.setBorder(null);
			viewport.setVerticalAlignment(SwingConstants.TOP);
			viewport.setHorizontalAlignment(SwingConstants.LEFT);
			
			viewportscroller= new JScrollPane();
			viewportscroller.setBorder(null);
			viewportscroller.setBounds(10, 70, (int)Variables.standardsize.getWidth()-Variables.rechterRand-10,(int)Variables.standardsize.getHeight()-110);
			viewportscroller.setViewportView(viewport);
			contentPane.add(viewportscroller);
			
			//list initialisieren
			headerlist = new JLabel("Verknüpfte Themen");
			headerlist.setFont(new Font(Variables.fontname, Font.BOLD, 20));
			headerlist.setHorizontalAlignment(SwingConstants.CENTER);
			headerlist.setBounds((int)Variables.standardsize.getWidth()-Variables.rechterRand+10, 70, Variables.rechterRand-20, 40); //810; 280
			contentPane.add(headerlist);
			
			list = new JList<ColoredString>();
			list.setBorder(null);
			list.setFont(new Font(Variables.fontname, Font.PLAIN, 12));
			list.setBackground(new Color(255, 204, 153));
			list.setCellRenderer(new DefaultListCellRenderer() {		//Inhalte passend färben
				public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
					Component c=super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
					for(int i=0; i<ColoredString.colors.length; i++ ) {
						if(value instanceof ColoredString && ((ColoredString) value).getType()==(byte)i) {
							setBackground(ColoredString.colors[i]);
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
						if(searchmode) {
							actual=links.get(links.search(searchresults.get(index)));
						} else {
							actual=links.get(links.search(listhandler.get(index).getName()));
						}
						searchmode=false;
		
						update();
					}
				}
			});
			listhandler=new DefaultListModel<ColoredString>();
			list.setModel(listhandler);
			listscroller= new JScrollPane();
			listscroller.setBorder(null);
			listscroller.setBounds((int)Variables.standardsize.getWidth()-Variables.rechterRand+10, 110, Variables.rechterRand-20, 830); //810, 280
			listscroller.setViewportView(list);
			contentPane.add(listscroller);

			//Such-Feld initialisieren
			search = new JTextField("Suche");
			search.setHorizontalAlignment(SwingConstants.LEFT);
			search.setBorder(null);
			search.setFont(new Font(Variables.fontname, Font.PLAIN, 18));
			search.setBackground(Color.decode("#FFB366"));
			search.setBounds((int)Variables.standardsize.getWidth()-Variables.rechterRand+10, 950, Variables.rechterRand-20, 40);
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
			contentPane.add(search);
			
			//Zurück-Button
			back=new JButton("Zurück");
			back.setBorder(null);
			back.setFont(new Font(Variables.fontname, Font.PLAIN, 24));
			back.setBackground(Color.decode("#FFB366"));
			back.setBounds((int)Variables.standardsize.getWidth()-Variables.rechterRand+10, 1000, Variables.rechterRand-20, 40);
			back.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					actual=links.get(links.search(actual.getSupLink()));
					update();
				}
			});
			contentPane.add(back);
		}
}
