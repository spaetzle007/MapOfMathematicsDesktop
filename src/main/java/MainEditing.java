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

	private JPanel selectionMode;
	private JPanel editMode;
	
	//Auswahlmodus
	//LinkedList-Hilfsvariablen
	private LinkedList list;
	//GUI-Komponenten
	private JScrollPane scrollerlist;
	private JList<String> overviewlist;
	private DefaultListModel<String> listhandler;
	private JButton neu, edit, delete, show, sicherung, synchronisieren;
	private JTextField search;
	private JLabel overview;
	//Suche-Hilfsvariablen
	private boolean searchmode;
	private ArrayList<String> searchresults;
	
	//Editiermodus
	//LinkedList-Hilfsvariablen
	private ArrayList<String> actualSubLinks;
	private ArrayList<String> actualEqualLinks;
	private int act;		//Position des bearbeiteten Eintrags
	//GUI-Komponenten
	private JScrollPane scrollertext, scrollerlinks, scrollertestViewport;
	private JTextPane content;
	private JLabel testViewport;
	private boolean testing;
	private JList<ColoredString> links;
	private DefaultListModel<ColoredString> linkshandler;
	private JButton back, showtestViewport;
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
				list = new LinkedList();
		} catch(AccessException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Fehler", 3);
			System.exit(1);
		} catch(LinkedParseException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Fehler", 3);
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
		setContentPane(selectionMode);
		updateSelectionMode();
	}
	/**
	 * Buttons in den beiden Modi: Neuer Eintrag, Eintrag bearbeiten, Eintrag löschen, Alles Anzeigen, Speichern, Zurück
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getSource()==neu) {
			setContentPane(editMode);
			
			linkshandler.clear();
			
			act=list.size();
			list.add(new Linked());
			
			title.setText(list.get(act).getName());
			content.setText(list.get(act).getEditModeRepresentation());
			
			updateEditMode();
		} else if(e.getSource()==edit) {
			editButton();
		} else if(e.getSource()==delete) {
			//String aus markiertem Index gewinnen
			int selectedIndex = overviewlist.getSelectedIndex();
			String del;
			if(searchmode) {
				del=searchresults.get(selectedIndex);
			} else {
				del=list.get(selectedIndex).getName();
			}
			//Eintrag löschen
			list.removeLinked(del);
			
			updateSelectionMode();
		} else if(e.getSource()==show) {
			searchmode=false;
			search.setText("Suche");
			updateSelectionMode();
		} else if(e.getSource()==sicherung) {
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
		} else if(e.getSource()==back) {
			updateEditMode();
			linkshandler.clear();
			comboboxhandler.removeAllElements();
			
			setContentPane(selectionMode);
			
			
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
					formula = new TeXFormula(list.get(act).getJLatexMathRepresentation());
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
		if(!list.get(act).getName().equals(title.getText())) {
			list.setLinkedsName(list.get(act), title.getText());
		}
		list.get(act).setLatexText(new LatexText(content.getText(), 3));
		
		//Infos ausgeben
		title.setText(list.get(act).getName());		
		content.setText(list.get(act).getEditModeRepresentation());
		
		//Linklist aktualisieren
		linkshandler.clear();
		actualEqualLinks=list.getEqualLinks(list.get(act));
		actualSubLinks=list.getSubLinks(list.get(act));
		if(!list.get(act).getName().equals("Mathematik")) {
			linkshandler.addElement(new ColoredString(list.get(act).getSupLink(), (byte)0));
		}
		if(!list.get(act).getName().equals("Mathematik")) {
			for(int i=0; i<actualEqualLinks.size(); i++) {
				linkshandler.addElement(new ColoredString(actualEqualLinks.get(i), (byte)1));
			}
		}
		for(int i=0; i<actualSubLinks.size(); i++) {
			linkshandler.addElement(new ColoredString(actualSubLinks.get(i), (byte)2));
		}
		for(int i=0; i<list.get(act).getLinks().size(); i++) {
			linkshandler.addElement(new ColoredString(list.get(act).getLinks().get(i)));
		}
		//ComboBox aktualisieren
		comboboxhandler.removeAllElements();
		for(int i=0; i<list.size(); i++) {
			comboboxhandler.addElement(list.get(i).getName());
		}
	}
	/**
	 * Hilfsmethode für den über 2 Wege aufrufbaren Edit-Button im Übersichtsmenü
	 */
	private void editButton() {
		int selectedIndex = overviewlist.getSelectedIndex();
		if(searchmode) {
			act=list.search(searchresults.get(selectedIndex));
		} else {
			act=selectedIndex;
		}
		
		setContentPane(editMode);
		
		title.setText(list.get(act).getName());
		content.setText(list.get(act).getStandardRepresentation());;
		
		updateEditMode();
	}

	/**
	 * JFrame erstellen
	 */
	private void createGUIBasics() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setBounds(0, 0, (int)Variables.standardsize.getWidth(), (int)Variables.standardsize.getHeight());
		selectionMode= new JPanel();
		selectionMode.setBackground(new Color(102, 204, 255));
		selectionMode.setBorder(null);
		selectionMode.setLayout(null);
		editMode = new JPanel();
		editMode.setBackground(new Color(102, 204, 255));
		editMode.setBorder(null);
		editMode.setLayout(null);
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
		selectionMode.add(overview);
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
		selectionMode.add(scrollerlist);
		
		//Optionen rechts
		neu = new JButton("Neuer Eintrag");
		neu.setBorder(null);
		neu.setFont(new Font(Variables.fontname, Font.PLAIN, 24));
		neu.setForeground(new Color(255, 255, 255));
		neu.setBackground(new Color(0, 0, 153));
		neu.setBounds((int)Variables.standardsize.getWidth()-Variables.rechterRand+10, 10, Variables.rechterRand-20, 40);
		neu.addActionListener(this);
		selectionMode.add(neu);
		edit = new JButton("Eintrag bearbeiten");
		edit.setBorder(null);
		edit.setFont(new Font(Variables.fontname, Font.PLAIN, 24));
		edit.setForeground(new Color(255, 255, 255));
		edit.setBackground(new Color(0, 0, 153));
		edit.setBounds((int)Variables.standardsize.getWidth()-Variables.rechterRand+10, 60, Variables.rechterRand-20, 40);
		edit.addActionListener(this);
		selectionMode.add(edit);
		delete = new JButton("Eintrag löschen");
		delete.setBorder(null);
		delete.setFont(new Font(Variables.fontname, Font.PLAIN, 24));
		delete.setForeground(new Color(255, 255, 255));
		delete.setBackground(new Color(0, 0, 153));
		delete.setBounds((int)Variables.standardsize.getWidth()-Variables.rechterRand+10, 110, Variables.rechterRand-20, 40);
		delete.addActionListener(this);
		selectionMode.add(delete);
		show = new JButton("Alle anzeigen");
		show.setBorder(null);
		show.setFont(new Font(Variables.fontname, Font.PLAIN, 24));
		show.setForeground(new Color(255, 255, 255));
		show.setBackground(new Color(0, 0, 153));
		show.setBounds((int)Variables.standardsize.getWidth()-Variables.rechterRand+10, 160, Variables.rechterRand-20, 40);
		show.addActionListener(this);
		selectionMode.add(show);
		sicherung=new JButton("Sicherungskopie");
		sicherung.setBorder(null);
		sicherung.setFont(new Font(Variables.fontname, Font.PLAIN, 24));
		sicherung.setForeground(new Color(255, 255, 255));
		sicherung.setBackground(new Color(0, 0, 153));
		sicherung.setBounds((int)Variables.standardsize.getWidth()-Variables.rechterRand+10, 300, Variables.rechterRand-20, 40);
		sicherung.addActionListener(this);
		selectionMode.add(sicherung);
		synchronisieren=new JButton("Dbx-Synchro");
		synchronisieren.setBorder(null);
		synchronisieren.setFont(new Font(Variables.fontname, Font.PLAIN, 24));
		synchronisieren.setForeground(new Color(255, 255, 255));
		synchronisieren.setBackground(new Color(0, 0, 153));
		synchronisieren.setBounds((int)Variables.standardsize.getWidth()-Variables.rechterRand+10, 350, Variables.rechterRand-20, 40);
		synchronisieren.addActionListener(this);
		selectionMode.add(synchronisieren);
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
			}
		});
		selectionMode.add(search);
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
		editMode.add(title);
		content = new JTextPane();
		content.setFont(new Font(Variables.smallfontname, Font.PLAIN, 12));
		scrollertext = new JScrollPane();
		scrollertext.setBorder(null);
		scrollertext.setBounds(10, 60, (int)Variables.standardsize.getWidth()-Variables.rechterRand-10, (int)Variables.standardsize.getHeight()-150);
		scrollertext.setViewportView(content);
		editMode.add(scrollertext);
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
					updateEditMode();
					
					int selectedIndex = links.getSelectedIndex();
					ColoredString col=linkshandler.get(selectedIndex);
					if(col.getType()<(byte)4) {return;}	//Kann nur crosslinks löschen
					LinkedString link = new LinkedString(col.getName(), (byte)(col.getType()-4));
					
					//Link löschen
					list.removeLink(list.get(act), link);
					
					updateEditMode();
				}
			}
			public void keyPressed(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
		});
		
		scrollerlinks = new JScrollPane();
		scrollerlinks.setBorder(null);
		scrollerlinks.setBounds((int)Variables.standardsize.getWidth()-Variables.rechterRand+10, 60, Variables.rechterRand-20, (int)Variables.standardsize.getHeight()-150);
		scrollerlinks.setViewportView(links);
		editMode.add(scrollerlinks);
		
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
			public void keyReleased(KeyEvent e) {	//Verschiedene Linkarten hinzufügen mit verschiedenen Tasten
				if(e.getKeyCode()==KeyEvent.VK_ENTER) {
					int index = linkselection.getSelectedIndex();
					list.addLink(list.get(act), new LinkedString(comboboxhandler.getElementAt(index), (byte)0));
					
					updateEditMode();
				} else if(e.getKeyCode()==KeyEvent.VK_SPACE) {
					int index = linkselection.getSelectedIndex();
					list.get(act).setSupLink(comboboxhandler.getElementAt(index));
					
					updateEditMode();
				}
			}
			public void keyTyped(KeyEvent e) {}
		});
		editMode.add(linkselection);
		
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
		editMode.add(scrollertestViewport);
		
		//Optionen
		back = new JButton("Zurück");
		back.setBorder(null);
		back.setFont(new Font(Variables.fontname, Font.PLAIN, 24));
		back.setForeground(new Color(255, 255, 255));
		back.setBackground(new Color(0, 0, 153));
		back.setBounds((int)Variables.standardsize.getWidth()-Variables.rechterRand+10, (int)Variables.standardsize.getHeight()-80, Variables.rechterRand-20, 40);
		back.addActionListener(this);
		editMode.add(back);
		showtestViewport=new JButton("Testansicht");
		showtestViewport.setFont(new Font(Variables.fontname, Font.PLAIN, 24));
		showtestViewport.setBorder(null);
		showtestViewport.setForeground(new Color(255, 255, 255));
		showtestViewport.setBackground(new Color(0, 0, 153));
		showtestViewport.setBounds(10, (int)Variables.standardsize.getHeight()-80, 200, 40);
		showtestViewport.addActionListener(this);
		editMode.add(showtestViewport);
	}
	
	@Override
	public void windowActivated(WindowEvent arg0) {}
	@Override
	public void windowClosed(WindowEvent arg0) {}
	@Override
	public void windowClosing(WindowEvent arg0) {
		setVisible(false);
		System.out.println(list.convertToXML());
		
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
