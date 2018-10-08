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
import com.spaetzle007.MapOfMathematicsLibraries.DataHandler;
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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

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
	private JScrollPane scrollertext, scrollerlinks;
	private JTextPane content;
	private JLabel testViewport;
	private boolean testing;
	private JList<Hilfsklassen.ColoredString> links;
	private DefaultListModel<Hilfsklassen.ColoredString> linkshandler;
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
				list = new LinkedList(DataHandler.cutLast(MainEditing.class.getProtectionDomain().getCodeSource().getLocation().getPath())/*+File.seperator*/+"/MOM.xml");
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
		} catch(FontFormatException e) {
			JOptionPane.showMessageDialog(null, "Font-Format fehlerhaft", "Fehler", 3);
			Hilfsklassen.Variables.fontname="Dialog";
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Fonts nicht verfügbar", "Fehler", 3);	
			Hilfsklassen.Variables.fontname="Dialog";
		}
		
		//Modus einstellen
		setContentPane(selectionMode);
		update();
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
			
			update();
		} else if(e.getSource()==edit) {
			int selectedIndex = overviewlist.getSelectedIndex();
			if(searchmode) {
				act=list.search(searchresults.get(selectedIndex));
			} else {
				act=selectedIndex;
			}
			
			setContentPane(editMode);
			
			title.setText(list.get(act).getName());
			content.setText(list.get(act).getStandardRepresentation());;
			
			update();
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
			
			update();
		} else if(e.getSource()==show) {
			searchmode=false;
			search.setText("Suche");
			update();
		} else if(e.getSource()==sicherung) {
			try {
				list.sicherungskopie();
			} catch(AccessException f) {
				JOptionPane.showMessageDialog(null,  f.getMessage(), "Fehler", 3);
			}
		} else if(e.getSource()==synchronisieren) {
			try {
				list.saveList();
			} catch (AccessException f) {
				JOptionPane.showMessageDialog(null,  f.getMessage(), "Fehler", 3);
			}
		} else if(e.getSource()==back) {
			update();
			linkshandler.clear();
			comboboxhandler.removeAllElements();
			
			setContentPane(selectionMode);
			
			searchmode=false;
			update();
		} else if(e.getSource()==showtestViewport) {
			if(testing) {		//Umstellen auf Editiermodus
				showtestViewport.setText("Testansicht");
				scrollertext.setViewportView(content);
				testing=false;
			} else {			//Umstellen auf Ansichtsmodus
				testViewport.setText("");
				showtestViewport.setText("Weiter editieren");
				scrollertext.setViewportView(testViewport);
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
	 * Hilfsmethode: Inhalte ein- und ausgeben
	 * Fallunterscheidung, welches Fenster geöffnet ist
	 */
	private void update() {
		if(getContentPane().equals(selectionMode)) {
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
			
			
		} else if(getContentPane().equals(editMode)){
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
				linkshandler.addElement(new Hilfsklassen.ColoredString(list.get(act).getSupLink(), (byte)0));
			}
			if(!list.get(act).getName().equals("Mathematik")) {
				for(int i=0; i<actualEqualLinks.size(); i++) {
					linkshandler.addElement(new Hilfsklassen.ColoredString(actualEqualLinks.get(i), (byte)1));
				}
			}
			for(int i=0; i<actualSubLinks.size(); i++) {
				linkshandler.addElement(new Hilfsklassen.ColoredString(actualSubLinks.get(i), (byte)2));
			}
			for(int i=0; i<list.get(act).getLinks().size(); i++) {
				linkshandler.addElement(new Hilfsklassen.ColoredString(list.get(act).getLinks().get(i)));
			}
			//ComboBox aktualisieren
			comboboxhandler.removeAllElements();
			for(int i=0; i<list.size(); i++) {
				comboboxhandler.addElement(list.get(i).getName());
			}
		}
	}


	/**
	 * JFrame erstellen
	 */
	private void createGUIBasics() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, (int)Hilfsklassen.Variables.standardsize.getWidth(), (int)Hilfsklassen.Variables.standardsize.getHeight());
		selectionMode= new JPanel(new GridBagLayout());
		selectionMode.setBackground(new Color(102, 204, 255));
		selectionMode.setBorder(null);
		editMode = new JPanel(new GridBagLayout());
		editMode.setBackground(new Color(102, 204, 255));
		editMode.setBorder(null);
		addWindowListener(this);
		setTitle("MapOfMathematics Editiersoftware");
	}
	/**
	 * GUI für SelectionMode erstellen
	 */
	private void createGUISelectionMode() {
		GridBagConstraints c =new GridBagConstraints();
		c.insets=new Insets(5,5,5,5);
		c.anchor=GridBagConstraints.FIRST_LINE_START;
		
		c.weightx=1.0; 
		overview = new JLabel("Übersicht");
		overview.setFont(new Font(Hilfsklassen.Variables.fontname, Font.BOLD, 36));
		overview.setBorder(null);
		overview.setBackground(new Color(204, 255, 255));
		overview.setHorizontalAlignment(SwingConstants.CENTER);
		c.gridx=0; c.gridy=0; c.fill=GridBagConstraints.HORIZONTAL; c.gridwidth=2;
		selectionMode.add(overview, c);
		//Viewport mit Eintragsliste
		overviewlist = new JList<String>();
		overviewlist.setFont(new Font(Hilfsklassen.Variables.smallfontname, Font.BOLD, 12));
		overviewlist.setBackground(new Color(255, 255, 255));
		overviewlist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listhandler=new DefaultListModel<String>();
		overviewlist.setModel(listhandler);
		overviewlist.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount()==2) {
					ActionEvent event=new ActionEvent(edit, ActionEvent.ACTION_PERFORMED, "Edit");
					for(ActionListener listener: edit.getActionListeners()) {
						listener.actionPerformed(event);
					}
				}
			}
		});
		scrollerlist = new JScrollPane();
		scrollerlist.setViewportView(overviewlist);
		c.gridx=0; c.gridy=1; c.fill=GridBagConstraints.BOTH; c.weighty=1.0; c.gridheight=7; c.gridwidth=1;
		selectionMode.add(scrollerlist, c);
		
		//Optionen rechts
		c.weightx=0.0; c.weighty=0.0; c.fill=GridBagConstraints.HORIZONTAL; c.gridheight=1;
		neu = new JButton("Neuer Eintrag");
		neu.setBorder(null);
		neu.setFont(new Font(Hilfsklassen.Variables.fontname, Font.PLAIN, 24));
		neu.setForeground(new Color(255, 255, 255));
		neu.setBackground(new Color(0, 0, 153));
		neu.addActionListener(this);
		neu.setMinimumSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
		neu.setPreferredSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
		neu.setMaximumSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
		c.gridx=1; c.gridy=1;
		selectionMode.add(neu, c);
		edit = new JButton("Eintrag bearbeiten");
		edit.setBorder(null);
		edit.setFont(new Font(Hilfsklassen.Variables.fontname, Font.PLAIN, 24));
		edit.setForeground(new Color(255, 255, 255));
		edit.setBackground(new Color(0, 0, 153));
		edit.addActionListener(this);
		edit.setMinimumSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
		edit.setPreferredSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
		edit.setMaximumSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
		c.gridx=1; c.gridy=2;
		selectionMode.add(edit, c);
		delete = new JButton("Eintrag löschen");
		delete.setBorder(null);
		delete.setFont(new Font(Hilfsklassen.Variables.fontname, Font.PLAIN, 24));
		delete.setForeground(new Color(255, 255, 255));
		delete.setBackground(new Color(0, 0, 153));
		delete.addActionListener(this);
		delete.setMinimumSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
		delete.setPreferredSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
		delete.setMaximumSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
		c.gridx=1; c.gridy=3;
		selectionMode.add(delete, c);
		show = new JButton("Alle anzeigen");
		show.setBorder(null);
		show.setFont(new Font(Hilfsklassen.Variables.fontname, Font.PLAIN, 24));
		show.setForeground(new Color(255, 255, 255));
		show.setBackground(new Color(0, 0, 153));
		show.addActionListener(this);
		show.setMinimumSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
		show.setPreferredSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
		show.setMaximumSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
		c.gridx=1; c.gridy=4;
		selectionMode.add(show, c);
		sicherung=new JButton("Sicherungskopie");
		sicherung.setBorder(null);
		sicherung.setFont(new Font(Hilfsklassen.Variables.fontname, Font.PLAIN, 24));
		sicherung.setForeground(new Color(255, 255, 255));
		sicherung.setBackground(new Color(0, 0, 153));
		sicherung.addActionListener(this);
		sicherung.setMinimumSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
		sicherung.setPreferredSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
		sicherung.setMaximumSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
		c.gridx=1; c.gridy=5;
		selectionMode.add(sicherung, c);
		synchronisieren=new JButton("Dbx-Synchro");
		synchronisieren.setBorder(null);
		synchronisieren.setFont(new Font(Hilfsklassen.Variables.fontname, Font.PLAIN, 24));
		synchronisieren.setForeground(new Color(255, 255, 255));
		synchronisieren.setBackground(new Color(0, 0, 153));
		synchronisieren.addActionListener(this);
		synchronisieren.setMinimumSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
		synchronisieren.setPreferredSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
		synchronisieren.setMaximumSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
		c.gridx=1; c.gridy=6;
		selectionMode.add(synchronisieren, c);
		//Such-Einstellungen
		search = new JTextField("Suche");
		search.setBorder(null);
		search.setFont(new Font(Hilfsklassen.Variables.fontname, Font.PLAIN, 18));
		search.setHorizontalAlignment(SwingConstants.LEFT);
		search.setForeground(new Color(255, 255, 255));
		search.setBackground(new Color(0, 0, 153));
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
					update();
				}
			}
		});
		search.setMinimumSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
		search.setPreferredSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
		search.setMaximumSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
		c.gridx=1; c.gridy=7; c.anchor=GridBagConstraints.LAST_LINE_START; 
		selectionMode.add(search, c);
	}
	/**
	 * GUI für EditMode erstellen
	 */
	private void createGUIEditMode() {
		GridBagConstraints c=new GridBagConstraints();
		c.anchor=GridBagConstraints.FIRST_LINE_START; 
		c.insets=new Insets(5,5,5,5);
		
		c.weightx=1.0; c.weighty=0.0;
		//Inhalte
		title = new JTextField();
		title.setBorder(null);
		title.setFont(new Font(Hilfsklassen.Variables.fontname, Font.PLAIN, 24));
		title.setBackground(new Color(102, 204, 255));
		c.gridx=0; c.gridy=0; c.fill=GridBagConstraints.HORIZONTAL;
		editMode.add(title, c);
		content = new JTextPane();
		content.setFont(new Font(Hilfsklassen.Variables.smallfontname, Font.PLAIN, 12));
		scrollertext = new JScrollPane();
		scrollertext.setBorder(null);
		scrollertext.setViewportView(content);
		c.gridx=0; c.gridy=1; c.fill=GridBagConstraints.BOTH; c.weighty=1.0; c.gridheight=3;
		editMode.add(scrollertext, c);
		c.gridheight=1;
		
		//Test-Viewport
		testing=false;
		testViewport = new JLabel();
		testViewport.setVerticalAlignment(SwingConstants.TOP);
		testViewport.setHorizontalAlignment(SwingConstants.LEFT);
		
		//Link-Liste und Combobox
		//Erkenntnis: Wenn man Größe erzwingen will, muss man minmumSize, maximumSize und preferredSize setzen->In allen Situationen gültig
		c.weightx=0.0; c.weighty=0.0; 
		comboboxhandler=new DefaultComboBoxModel<String>();
		for(int i=0; i<list.size(); i++) {comboboxhandler.addElement(list.get(i).getName());}
		linkselection = new JComboBox<String>();
		linkselection.setBorder(null);
		linkselection.setFont(new Font(Hilfsklassen.Variables.smallfontname, Font.PLAIN, 12));
		linkselection.setMaximumRowCount(54);
		linkselection.setBackground(new Color(204, 255, 255));
		linkselection.setModel(comboboxhandler);
		linkselection.setEditable(false);
		linkselection.addKeyListener(new KeyListener() {		//Einträge in list hinzufügen(Crosslinks)
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {	//Verschiedene Linkarten hinzufügen mit verschiedenen Tasten
				if(e.getKeyCode()==KeyEvent.VK_ENTER) {
					int index = linkselection.getSelectedIndex();
					list.addLink(list.get(act), new LinkedString(comboboxhandler.getElementAt(index), (byte)0));
					
					update();
				} else if(e.getKeyCode()==KeyEvent.VK_SPACE) {
					int index = linkselection.getSelectedIndex();
					list.get(act).setSupLink(comboboxhandler.getElementAt(index));
					
					update();
				}
			}
			public void keyTyped(KeyEvent e) {}
		});
		linkselection.setMinimumSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
		linkselection.setPreferredSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));		
		linkselection.setMaximumSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
		c.gridx=1; c.gridy=0; c.fill=GridBagConstraints.HORIZONTAL;
		editMode.add(linkselection, c);
		
		links = new JList<Hilfsklassen.ColoredString>();
		linkshandler = new DefaultListModel<Hilfsklassen.ColoredString>();
		links.setModel(linkshandler);
		links.setFont(new Font(Hilfsklassen.Variables.fontname, Font.PLAIN, 12));
		links.setBackground(new Color(102, 204, 255));
		links.setCellRenderer(new DefaultListCellRenderer() {		//Inhalte passend färben
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
		links.addKeyListener(new KeyListener() {		//Einträge in list löschen
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_DELETE) {
					update();
					
					int selectedIndex = links.getSelectedIndex();
					Hilfsklassen.ColoredString col=linkshandler.get(selectedIndex);
					if(col.getType()<(byte)4) {return;}	//Kann nur crosslinks löschen
					LinkedString link = new LinkedString(col.getName(), (byte)(col.getType()-4));
					
					//Link löschen
					list.removeLink(list.get(act), link);
					
					update();
				}
			}
			public void keyPressed(KeyEvent e) {}
			public void keyTyped(KeyEvent e) {}
		});
		
		scrollerlinks = new JScrollPane();
		scrollerlinks.setBorder(null);
		scrollerlinks.setViewportView(links);
		scrollerlinks.setMinimumSize(new Dimension(Hilfsklassen.Variables.rechterRand, (int)scrollerlinks.getMinimumSize().getHeight()));
		scrollerlinks.setPreferredSize(new Dimension(Hilfsklassen.Variables.rechterRand, (int)scrollerlinks.getMinimumSize().getHeight()));
		scrollerlinks.setMaximumSize(new Dimension(Hilfsklassen.Variables.rechterRand, (int)scrollerlinks.getMinimumSize().getHeight()));
		
		c.gridx=1; c.gridy=1; c.weighty=1.0; c.fill=GridBagConstraints.BOTH;
		editMode.add(scrollerlinks, c);
		
		//Optionen
		c.weighty=0.0; c.fill=GridBagConstraints.HORIZONTAL;
		showtestViewport=new JButton("Testansicht");
		showtestViewport.setFont(new Font(Hilfsklassen.Variables.fontname, Font.PLAIN, 24));
		showtestViewport.setBorder(null);
		showtestViewport.setForeground(new Color(255, 255, 255));
		showtestViewport.setBackground(new Color(0, 0, 153));
		showtestViewport.addActionListener(this);
		showtestViewport.setMinimumSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
		showtestViewport.setPreferredSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
		showtestViewport.setMaximumSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
		c.gridx=1; c.gridy=2; 
		editMode.add(showtestViewport, c);
		back = new JButton("Zurück");
		back.setBorder(null);
		back.setFont(new Font(Hilfsklassen.Variables.fontname, Font.PLAIN, 24));
		back.setForeground(new Color(255, 255, 255));
		back.setBackground(new Color(0, 0, 153));
		back.addActionListener(this);
		back.setMinimumSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
		back.setPreferredSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
		back.setMaximumSize(new Dimension(Hilfsklassen.Variables.rechterRand, Hilfsklassen.Variables.buttonHeight));
		c.gridx=1; c.gridy=3; 
		editMode.add(back, c);
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
		} catch (AccessException e) {
			JOptionPane.showMessageDialog(null,  e.getMessage(), "Fehler", 3);
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
