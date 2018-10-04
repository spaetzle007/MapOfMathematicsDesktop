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

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.DeleteErrorException;
import com.spaetzle007.MapOfMathematicsLibraries.AccessException;
import com.spaetzle007.MapOfMathematicsLibraries.LatexText;
import com.spaetzle007.MapOfMathematicsLibraries.Linked;
import com.spaetzle007.MapOfMathematicsLibraries.LinkedList;
import com.spaetzle007.MapOfMathematicsLibraries.LinkedParseException;
import com.spaetzle007.MapOfMathematicsLibraries.LinkedString;

import javax.swing.SwingConstants;

import org.scilab.forge.jlatexmath.ParseException;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

import javax.swing.ListSelectionModel;
import javax.swing.JComboBox;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;

public class MainEditing extends JFrame implements ActionListener, WindowListener {

private JPanel contentPane;
	
	
	//Auswahlmodus
	//LinkedList-Hilfsvariablen
	private LinkedList list;
	//GUI-Komponenten
	private JScrollPane scrollerlist;
	private JList<String> overviewlist;
	private DefaultListModel<String> listhandler;
	private JButton neu, edit, delete, show, updateContent, synchronisieren;
	private JTextField search;
	private JLabel overview;
	//Suche-Hilfsvariablen
	private boolean searchmode;
	private ArrayList<String> searchresults;
	
	//Editiermodus
	//LinkedList-Hilfsvariablen
	private ArrayList<String> actualSubLinks;
	private ArrayList<String> actualEqualLinks;
	private Linked actual;		//Bearbeiteter Eintrag
	private Linked before;		//Speicherung des Eintrags vor Editieren
	private int actualpos;		//Position des bearbeiteten Eintrags
	private boolean neuer;		//Erstelle neuen Eintrag
	//GUI-Komponenten
	private JScrollPane scrollertext, scrollerlinks, scrollertestViewport;
	private JTextPane content;
	private JLabel testViewport;
	private boolean testing;
	private JList<ColoredString> links;
	private DefaultListModel<ColoredString> linkshandler;
	private JButton save, back, showtestViewport;
	private JTextField title;
	private JComboBox<String> linkselection;
	private DefaultComboBoxModel<String> comboboxhandler;
	
	/**
	 * Main-Methode zum Starten des Editiermodus
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainEditing frame = new MainEditing();
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
	public MainEditing() {
		//Grundeinstellungen
		try {
			try {
				list = new LinkedList();
			} catch (LinkedParseException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(), "Fehler", 3);
			}
		} catch(AccessException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Fehler", 3);
			System.exit(1);
			
		}

		searchresults=new ArrayList<String>();
		searchmode=false;
		
		//create GUI
		createGUIBasics();
		createGUISelectionMode();
		createGUIEditMode();
		

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
		
		//Modus einstellen
		selectionMode(true);
		editMode(false);
		updateSelectionMode();
		
	}
	/**
	 * GUI-Komponenten des Auswahlmodus ansteuern
	 */
	private void selectionMode(boolean b) {
		overview.setVisible(b);
		scrollerlist.setVisible(b);
		neu.setVisible(b);
		edit.setVisible(b);
		delete.setVisible(b);
		search.setVisible(b);
		show.setVisible(b);
		updateContent.setVisible(b);
		synchronisieren.setVisible(b);
	}
	/**
	 * GUI-Komponenten des Editiermodus ansteuern
	 */
	private void editMode(boolean b) {
		title.setVisible(b);
		scrollertext.setVisible(b);
		scrollerlinks.setVisible(b);
		save.setVisible(b);
		back.setVisible(b);
		linkselection.setVisible(b);
		quitTestView();
	}
	/**
	 * Hilfsmethode zum Verlassen der Testansicht
	 */
	private void quitTestView() {
		showtestViewport.setText("Testansicht");
		scrollertestViewport.setVisible(false);
		testing=false;
	}
	/**
	 * Buttons in den beiden Modi: Neuer Eintrag, Eintrag bearbeiten, Eintrag löschen, Alles Anzeigen, Speichern, Zurück
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==neu) {
			selectionMode(false);
			editMode(true);
			
			linkshandler.clear();
			
			actualpos=list.size();
			actual = new Linked();
			before = new Linked();
			neuer=true;
			
			title.setText("");
			content.setText("");
			
			updateEditMode();
			updateComboBox();
		} else if(e.getSource()==edit) {
			editButton();
		} else if(e.getSource()==delete) {
			int selectedIndex = overviewlist.getSelectedIndex();

			String del;
			if(searchmode) {
				del=searchresults.get(selectedIndex);
			} else {
				del=list.get(selectedIndex).getName();
			}
			//Verknüpfungen löschen
			for(int i=0; i<list.size(); i++) {
				for(int j=0; j<list.get(i).getLinks().size(); j++) {
					if(del.equals(list.get(i).getLinks().get(j))) {
						list.get(i).removeLink(j);
					}
				}
			}
			list.remove(del);
			
			updateSelectionMode();
		} else if(e.getSource()==show) {
			searchmode=false;
			updateSelectionMode();
		} else if(e.getSource()==updateContent) {
			list.sicherungskopie();
		} else if(e.getSource()==synchronisieren) {
			try {
				list.saveList();
			} catch (DeleteErrorException e1) {
				JOptionPane.showMessageDialog(null, "MOM.xml verschwunden! Entwickler alarmieren!", "Fehler", 3);
			} catch (DbxException e1) {
				JOptionPane.showMessageDialog(null, "Dropbox-Synchronisierung nicht möglich! Keine Internetverbindung", "Fehler", 3);
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(null, "Dropbox-Synchronisierung nicht möglich! Keine Internetverbindung", "Fehler", 3);
			}
		} else if(e.getSource()==save) {
			//Daten aus GUI einlesen
			actual.setName(title.getText());
			actual.setLatexText(new LatexText(content.getText(), 3));
			
			//Bedingung für Abspeichern: Eintrag hat Namen
			if(!actual.getName().equals("") || actual.getName() != null) {
				if(neuer) {
					list.add(new Linked(actual));
				}
				list.set(actualpos, actual);
			} else {
				return;
			}
			//Bei Namensänderung Namen auch in Links ändern
			list.changeLinksName(before, actual);
			before = list.get(actualpos);
			
			//Bei Löschen/Hinzufügen von Links diese wieder bijektiv machen
			
			//list.calculateLinkss(actual);
		} else if(e.getSource()==back) {
			updateEditMode();
			editMode(false);
			selectionMode(true);
			
			searchmode=false;
			updateSelectionMode();
		} else if(e.getSource()==showtestViewport) {
			if(testing) {		//Umstellen auf Editiermodus
				showtestViewport.setText("Testansicht");
				scrollertestViewport.setVisible(false);
				scrollertext.setVisible(true);
				testing=false;
			} else {			//Umstellen auf Ansichtsmodus
				testViewport.setText("");
				showtestViewport.setText("Weiter editieren");
				scrollertext.setVisible(false);
				scrollertestViewport.setVisible(true);
				testing=true;
				
				TeXFormula  formula = null;
				try {
					formula = new TeXFormula(actual.getJLatexMathRepresentation());
				} catch (ParseException f) {
					formula=new TeXFormula("\\textbf{Ungültige LaTeX-Eingabe!}");
				}
						
				TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 17);
				BufferedImage img = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_4BYTE_ABGR);
				icon.paintIcon(new JLabel(), img.getGraphics(), 0, 0);
				testViewport.setIcon(icon);
			}
			
		}
	}
	
	/**
	 * Hilfsmethode: Liste der Einträge im Auswahlmodus aktualisieren
	 */
	private void updateSelectionMode() {
		listhandler.clear();
		if(searchmode) {
			for(int i=0; i<searchresults.size(); i++) {
				listhandler.addElement(searchresults.get(i));
			}
		} else {
			for(int i=0; i<list.size(); i++) {
				listhandler.addElement(list.get(i).getName());
			}
		}
	}
	
	/**
	 * Hilfsmethode: Im Editiermodus Daten aus GUI auslesen und neu anzeigen
	 */
	private void updateEditMode() {
		//Daten aus GUI einlesen
		actual.setName(title.getText());
		actual.setLatexText(new LatexText(content.getText(), 3));
		
		//Infos ausgeben
		title.setText(actual.getName());		
		content.setText(actual.getEditModeRepresentation());
		
		//Bedingung für Abspeichern: Eintrag hat Namen
		if(!actual.getName().equals("") && actual.getName() != null) {
			if(neuer) {
				list.add(new Linked());
			}
			list.set(actualpos, actual);
		} else {
			return;
		}
		//Bei Namensänderung Namen auch in Links ändern
		list.changeLinksName(before, actual);
		before = list.get(actualpos);
		
		//Bei Löschen/Hinzufügen von Links diese wieder bijektiv machen
		
		//list.calculateLinkss(actual);
		
		//Links neu anzeigen
		linkshandler.clear();
		actualEqualLinks=list.getEqualLinks(actual);
		actualSubLinks=list.getSubLinks(actual);
		if(!actual.getName().equals("Mathematik")) {
			linkshandler.addElement(new ColoredString(actual.getSupLink(), (byte)0));
		}
		if(!actual.getName().equals("Mathematik")) {
			for(int i=0; i<actualEqualLinks.size(); i++) {
				linkshandler.addElement(new ColoredString(actualEqualLinks.get(i), (byte)1));
			}
		}
		for(int i=0; i<actualSubLinks.size(); i++) {
			linkshandler.addElement(new ColoredString(actualSubLinks.get(i), (byte)2));
		}
		for(int i=0; i<actual.getLinks().size(); i++) {
			linkshandler.addElement(new ColoredString(actual.getLinks().get(i)));
		}
	}
	/**
	 * Einträge der ComboBox aktualisieren
	 * (Nur bei Starten des Editiermodus nötig)
	 */
	private void updateComboBox() {
		comboboxhandler.removeAllElements();
		for(int i=0; i<list.size(); i++) {
			comboboxhandler.addElement(list.get(i).getName());
		}
	}
	/**
	 * Hilfsmethode für den über 2 Wege aufrufbaren Edit-Button im Übersichtsmenü
	 */
	private void editButton() {
		neuer=false;
		
		//Überprüfen: Existiert ausgewählter Eintrag?
		int selectedIndex = overviewlist.getSelectedIndex();
		if(searchmode) {
			actualpos=list.search(searchresults.get(selectedIndex));
		} else {
			actualpos=selectedIndex;
		}
		if(actualpos>=0) {
			actual = list.get(actualpos);
			before=new Linked(list.get(actualpos));
			linkshandler.clear();
			
			selectionMode(false);
			editMode(true);
			
			title.setText(actual.getName());
			content.setText(actual.getText());;
			
			updateEditMode();
			updateComboBox();
		}
	}
	private void viewportSearch() {
		searchmode=true;
		searchresults.clear();
		listhandler.clear();
		for(int i=0; i<list.size(); i++) {
			if(list.get(i).getName().contains(search.getText())) {
				searchresults.add(list.get(i).getName());
			}
		}
		updateSelectionMode();
	}
	/**
	 * Hilfsmethode zum Eintrag in LinkedList löschen
	 * Speichert
	 */
	private void deleteInLinkedList() {		
		int selectedIndex = links.getSelectedIndex();
		ColoredString con = linkshandler.get(selectedIndex);
		if(linkshandler.get(selectedIndex).getType()!=(byte)2) {return;}	//Kann nur crosslinks löschen
		//Link löschen
		actual.removeLink(con.getName());
		
		updateEditMode();
		
		//Bijektion des crosslinks löschen
		for(int j=0; j<list.get(list.search(con.getName())).getLinks().size(); j++) {		//Iteration über Linkss von "con"	
			if(list.get(list.search(con.getName())).getLinks().get(j).equals(actual.getName())) {	//Links hat gleicher Name wie "actual"
				list.get(list.search(con.getName())).removeLink(j);
				break;
			}
		}
		
		updateEditMode();
	}
	/**
	 * Hilfsmethode zum crosslink in LinkedList hinzufügen
	 * Speichert
	 * Keine Neuberechnungen nötig
	 */
	private void addCrossName() {		
		updateEditMode();
		int index = linkselection.getSelectedIndex();
		
		//Bedingungen: Noch nicht in Liste enthalten und nicht Name des eigenen Eintrags
		if(!actual.getLinks().contains(comboboxhandler.getElementAt(index)) && !comboboxhandler.getElementAt(index).equals(actual.getName())) {	//Link noch nicht enthalten & Link ist nicht eigener Name
			actual.addLink(new LinkedString(comboboxhandler.getElementAt(index), (byte)0));
			list.get(list.search(comboboxhandler.getElementAt(index))).addLink(new LinkedString(actual.getName(), (byte)0));
		}
		
		updateEditMode();
	}
	/**
	 * Hilfsmethode zum SupName ändern
	 * Speichert nicht
	 */
	private void addSupLink() {
		updateEditMode();
		int index = linkselection.getSelectedIndex();
		actual.setSupLink(comboboxhandler.getElementAt(index));
		
		updateEditMode();
	}
	/**
	 * JFrame erstellen
	 */
	private void createGUIBasics() {
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setResizable(false);
			setBounds(0, 0, (int)Variables.standardsize.getWidth(), (int)Variables.standardsize.getHeight());
			contentPane = new JPanel();
			contentPane.setBackground(new Color(102, 204, 255));
			contentPane.setBorder(null);
			contentPane.setLayout(null);
			setContentPane(contentPane);
			getContentPane().setLayout(null);
			addWindowListener(this);
			setTitle("MapOfMathematics Editiersoftware");
	}
	/**
	 * GUI für SelectionMode erstellen
	 */
	private void createGUISelectionMode() {
			overview = new JLabel("Übersicht");
			overview.setFont(new Font(Variables.fontname, Font.BOLD, 36));
			overview.setBorder(null);
			overview.setBackground(new Color(204, 255, 255));
			overview.setHorizontalAlignment(SwingConstants.CENTER);
			overview.setBounds(10,10,(int)Variables.standardsize.getWidth()-Variables.rechterRand-10, 60);
			contentPane.add(overview);
			//Viewport mit Eintragsliste
			overviewlist = new JList<String>();
			overviewlist.setFont(new Font(Variables.smallfontname, Font.BOLD, 12));
			overviewlist.setBackground(new Color(255, 255, 255));
			overviewlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			listhandler=new DefaultListModel<String>();
			overviewlist.setModel(listhandler);
			overviewlist.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if(e.getClickCount()==2) {
						editButton();
					}
				}
			});
			scrollerlist = new JScrollPane();
			scrollerlist.setBounds(10, 70, (int)Variables.standardsize.getWidth()-Variables.rechterRand-10, (int)Variables.standardsize.getHeight()-110);
			scrollerlist.setViewportView(overviewlist);
			contentPane.add(scrollerlist);
			
			//Optionen rechts
			neu = new JButton("Neuer Eintrag");
			neu.setBorder(null);
			neu.setFont(new Font(Variables.fontname, Font.PLAIN, 24));
			neu.setForeground(new Color(255, 255, 255));
			neu.setBackground(new Color(0, 0, 153));
			neu.setBounds((int)Variables.standardsize.getWidth()-Variables.rechterRand+10, 10, Variables.rechterRand-20, 40);
			neu.addActionListener(this);
			contentPane.add(neu);
			edit = new JButton("Eintrag bearbeiten");
			edit.setBorder(null);
			edit.setFont(new Font(Variables.fontname, Font.PLAIN, 24));
			edit.setForeground(new Color(255, 255, 255));
			edit.setBackground(new Color(0, 0, 153));
			edit.setBounds((int)Variables.standardsize.getWidth()-Variables.rechterRand+10, 60, Variables.rechterRand-20, 40);
			edit.addActionListener(this);
			contentPane.add(edit);
			delete = new JButton("Eintrag löschen");
			delete.setBorder(null);
			delete.setFont(new Font(Variables.fontname, Font.PLAIN, 24));
			delete.setForeground(new Color(255, 255, 255));
			delete.setBackground(new Color(0, 0, 153));
			delete.setBounds((int)Variables.standardsize.getWidth()-Variables.rechterRand+10, 110, Variables.rechterRand-20, 40);
			delete.addActionListener(this);
			contentPane.add(delete);
			show = new JButton("Alle anzeigen");
			show.setBorder(null);
			show.setFont(new Font(Variables.fontname, Font.PLAIN, 24));
			show.setForeground(new Color(255, 255, 255));
			show.setBackground(new Color(0, 0, 153));
			show.setBounds((int)Variables.standardsize.getWidth()-Variables.rechterRand+10, 160, Variables.rechterRand-20, 40);
			show.addActionListener(this);
			contentPane.add(show);
			updateContent=new JButton("Sicherungskopie");
			updateContent.setBorder(null);
			updateContent.setFont(new Font(Variables.fontname, Font.PLAIN, 24));
			updateContent.setForeground(new Color(255, 255, 255));
			updateContent.setBackground(new Color(0, 0, 153));
			updateContent.setBounds((int)Variables.standardsize.getWidth()-Variables.rechterRand+10, 300, Variables.rechterRand-20, 40);
			updateContent.addActionListener(this);
			contentPane.add(updateContent);
			synchronisieren=new JButton("Dbx-Synchro");
			synchronisieren.setBorder(null);
			synchronisieren.setFont(new Font(Variables.fontname, Font.PLAIN, 24));
			synchronisieren.setForeground(new Color(255, 255, 255));
			synchronisieren.setBackground(new Color(0, 0, 153));
			synchronisieren.setBounds((int)Variables.standardsize.getWidth()-Variables.rechterRand+10, 350, Variables.rechterRand-20, 40);
			synchronisieren.addActionListener(this);
			contentPane.add(synchronisieren);
			//Such-Einstellungen
			search = new JTextField("Suche");
			search.setBorder(null);
			search.setFont(new Font(Variables.fontname, Font.PLAIN, 18));
			search.setHorizontalAlignment(SwingConstants.LEFT);
			search.setForeground(new Color(255, 255, 255));
			search.setBackground(new Color(0, 0, 153));
			search.setBounds((int)Variables.standardsize.getWidth()-Variables.rechterRand+10, 1000, Variables.rechterRand-20, 40);
			search.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					search.setText("");
				}
			});
			search.addKeyListener(new KeyListener() {			//Mit Enter suchen
				public void keyTyped(KeyEvent e) {}
				public void keyReleased(KeyEvent e) {}
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode()==KeyEvent.VK_ENTER) {
						viewportSearch();
					}
				}
			});
			contentPane.add(search);
	}
	/**
	 * GUI für EditMode erstellen
	 */
	private void createGUIEditMode() {
		//Inhalte
		title = new JTextField();
		title.setBorder(null);
		title.setFont(new Font(Variables.fontname, Font.PLAIN, 24));
		title.setBackground(new Color(102, 204, 255));
		title.setBounds(10,10, (int)Variables.standardsize.getWidth()-Variables.rechterRand-10, 40);
		contentPane.add(title);
		content = new JTextPane();
		content.setFont(new Font(Variables.smallfontname, Font.PLAIN, 12));
		scrollertext = new JScrollPane();
		scrollertext.setBorder(null);
		scrollertext.setBounds(10, 60, (int)Variables.standardsize.getWidth()-Variables.rechterRand-10, (int)Variables.standardsize.getHeight()-150);
		scrollertext.setViewportView(content);
		contentPane.add(scrollertext);
		//Link-Liste
		links = new JList<ColoredString>();
		linkshandler = new DefaultListModel<ColoredString>();
		links.setModel(linkshandler);
		links.setFont(new Font(Variables.fontname, Font.PLAIN, 12));
		links.setBackground(new Color(102, 204, 255));
		links.setCellRenderer(new DefaultListCellRenderer() {		//Inhalte passend färben
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
		links.addKeyListener(new KeyListener() {		//Einträge in list löschen
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_DELETE) {
					deleteInLinkedList();
				}
			}
			public void keyPressed(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
		});
		
		scrollerlinks = new JScrollPane();
		scrollerlinks.setBorder(null);
		scrollerlinks.setBounds((int)Variables.standardsize.getWidth()-Variables.rechterRand+10, 60, Variables.rechterRand-20, (int)Variables.standardsize.getHeight()-150);
		scrollerlinks.setViewportView(links);
		contentPane.add(scrollerlinks);
		
		comboboxhandler=new DefaultComboBoxModel<String>();
		for(int i=0; i<list.size(); i++) {comboboxhandler.addElement(list.get(i).getName());}
		linkselection = new JComboBox<String>();
		linkselection.setBorder(null);
		linkselection.setFont(new Font(Variables.smallfontname, Font.PLAIN, 12));
		linkselection.setMaximumRowCount(54);
		linkselection.setBackground(new Color(204, 255, 255));
		linkselection.setModel(comboboxhandler);
		linkselection.setEditable(false);
		linkselection.setBounds((int)Variables.standardsize.getWidth()-Variables.rechterRand+10, 10, Variables.rechterRand-20, 40);
		linkselection.addKeyListener(new KeyListener() {		//Einträge in list hinzufügen(Crosslinks)
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_ENTER) {
					addCrossName();
				} else if(e.getKeyCode()==KeyEvent.VK_SPACE) {
					addSupLink();
				}
			}
			public void keyTyped(KeyEvent e) {}
		});
		contentPane.add(linkselection);
		
		//Test-Viewport
		testing=false;
		
		testViewport = new JLabel();
		testViewport.setVerticalAlignment(SwingConstants.TOP);
		testViewport.setHorizontalAlignment(SwingConstants.LEFT);
		testViewport.setBounds(10, 60, 780, 830);
		
		scrollertestViewport=new JScrollPane();
		scrollertestViewport.setFont(new Font(Variables.fontname, Font.PLAIN, 12));
		scrollertestViewport.setBorder(null);
		scrollertestViewport.setBounds(10, 60, (int)Variables.standardsize.getWidth()-Variables.rechterRand-10, (int)Variables.standardsize.getHeight()-150);
		scrollertestViewport.setViewportView(testViewport);
		scrollertestViewport.setVisible(false);
		contentPane.add(scrollertestViewport);
		
		//Optionen
		save = new JButton("Speichern");
		save.setFont(new Font(Variables.fontname, Font.PLAIN, 24));
		save.setBorder(null);
		save.setForeground(new Color(255, 255, 255));
		save.setBackground(new Color(0, 0, 153));
		save.setBounds(10, (int)Variables.standardsize.getHeight()-80, 200, 40);
		save.addActionListener(this);
		contentPane.add(save);
		back = new JButton("Zurück");
		back.setBorder(null);
		back.setFont(new Font(Variables.fontname, Font.PLAIN, 24));
		back.setForeground(new Color(255, 255, 255));
		back.setBackground(new Color(0, 0, 153));
		back.setBounds((int)Variables.standardsize.getWidth()-Variables.rechterRand+10, (int)Variables.standardsize.getHeight()-80, Variables.rechterRand-20, 40);
		back.addActionListener(this);
		contentPane.add(back);
		showtestViewport=new JButton("Testansicht");
		showtestViewport.setFont(new Font(Variables.fontname, Font.PLAIN, 24));
		showtestViewport.setBorder(null);
		showtestViewport.setForeground(new Color(255, 255, 255));
		showtestViewport.setBackground(new Color(0, 0, 153));
		showtestViewport.setBounds(220, (int)Variables.standardsize.getHeight()-80, 200, 40);
		showtestViewport.addActionListener(this);
		contentPane.add(showtestViewport);
	}
	
	@Override
	public void windowActivated(WindowEvent arg0) {}
	@Override
	public void windowClosed(WindowEvent arg0) {}
	@Override
	public void windowClosing(WindowEvent arg0) {
		setVisible(false);
		
		try {
			list.saveList();
		} catch (DeleteErrorException e) {
			JOptionPane.showMessageDialog(null, "MOM.xml ist verschwunden! Entwickler alarmieren!",  "Fehler", 3);
		} catch (DbxException | IOException e) {
			JOptionPane.showMessageDialog(null, "Dropbox-Synchronisierung nicht möglich! Keine Internetverbindung", "Fehler", 3);
		}
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
