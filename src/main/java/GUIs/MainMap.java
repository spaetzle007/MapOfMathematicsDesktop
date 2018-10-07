package GUIs;

import java.awt.EventQueue;
import java.awt.Point;

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

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.DeleteErrorException;
import com.spaetzle007.MapOfMathematicsLibraries.Linked;
import com.spaetzle007.MapOfMathematicsLibraries.LinkedList;

import GUIs.MainMap.Node;

/*
Language:
Topic = Node (e.g. Analysis is Node)
Connection between Nodes = Vertex (e.g. Connection between Integral and Differential is Vertex)
Linked corresponds to Node
Connection/Suplink corresponds to Vertex
The JFrame is the MapJFrame and should be referenced as such
supLvl: Level that is being drawn FROM
SubLvl: Level that is being drawn TO

This is Map Version 1 which is supposed to look like a branching tree or mindmap
Other Possible versions are nested circles and grid lines

To-do List:
- Add Vertex
- Conversion from LinkedList to LevelList (without positions, but absolute hierarchy) (Jonas?)
- Implementation of supNode.getsubNodes()											  (Jonas?)
- Is static class used correctly?													  (Jonas?)
- Navigation features
- ....
See Code for details
*/

public class MainMap extends JFrame implements ActionListener {
	
	//Benoetigte Daten fuer erzeugen der Map
	private MainViewport viewport;
	private LinkedList links;
	private Linked actual;
	
	//Angezeigte Elemente auf Map
	private ArrayList<JButton> Nodes;
	//private ArrayList<???> Vertex
	private JTextField search;
	private JButton back;
	
	//Ist static class hier richtig um das runnable DisplayMapJFrame auszulagern?
	public static class DisplayMapJFrame implements Runnable {
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
	  }
	
	public class Level{
		ArrayList<Node> Nodes;
		int level;
		
		//Is this Constructor even needed?
		Level(ArrayList<Node> Nodes,int level){
			this.Nodes = Nodes;
			this.level = level;
		}
	}
	
	public class Node extends Point{
		Point nodePos;
		String nodeName;
		
		Node (Point nodePos,String nodeName){
			this.nodePos = nodePos;
			this.nodeName = nodeName;
		}
	}
	
	/**
	 * Main-Methode zum Starten der Ansicht
	 */
	
	public static void main(String[] args) {
		DisplayMapJFrame DisplayMap = new DisplayMapJFrame();
		EventQueue.invokeLater(DisplayMap);
		
	}
	
	//Konstruktor von MainMap()
	@SuppressWarnings("null")
	public MainMap() {
		
		//Creates new Viewport to extract LinkedList and ActualLink
		viewport = new MainViewport();
		String CurrentLinked = viewport.getLinked().getName();
		LinkedList LinkedList = viewport.getLinkedList();
		
		//Convert LinkedList to ArrayList<Level>
		//(Should take place here in the future)
		ArrayList<Level> LevelList = null;
		
		//Sets the variables of the JFrame MainMap
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, (int) Hilfsklassen.Variables.standardsize.getWidth(), (int)Hilfsklassen.Variables.standardsize.getHeight()); 	//Format: 1920x1080
		
		//Origin determines the coordinate of the Mathematik node
		//VERY complicted way just to initialize Node "Mathematik"
		Point origin = new Point(0,0);
		Node Mathematik = new Node(origin,"Mathematik");
		ArrayList<Node> NodesL0 = new ArrayList<Node>();
		NodesL0.add(Mathematik);
		Level L0 = new Level(NodesL0,0);
		LevelList.set(0, L0);
		
		
		//L0dist is distance from origin to first nodes
		double L0dist = 40;
		double L0size = 30;
		JPanel MapPanel = new JPanel();
		MapPanel.setLayout(null);
		
		//for every level draw level
		for (Level supLvl : LevelList) {
				drawLevel(supLvl,L0dist,L0size);
		}
		
		setContentPane(MapPanel);
		setTitle("MapOfMathematics");
	}
	
	//supLvl already exists
	//drawLevel draws the subLvl originating FROM the supLvl
	public void drawLevel(Level supLvl, double L0dist, double L0size) {
		
		//calculate radius and size of vertex/node by L0dist, L0size and level
		double radius = Math.round(Math.pow(2/3, supLvl.level)*L0dist);
		double size = Math.round(Math.pow(2/3, supLvl.level)*L0size);
		
		//for every Node in supLvl draw Subnodes
		for (Node supNode : supLvl.Nodes) {
			drawAllSubnodes(supNode,supLvl.level,radius,size);
		}
	}
	
	//Draws all Subnodes of a given Supnode
	public void drawAllSubnodes(Node supNode,int supLvl,double radius, double size) {

		//This Subnodes ArrayList should be initialized by a ArrayList<Node> Subnodes = supnode.getSubnodes()
		//as soon as implemented (if not already implemented??)
		ArrayList<Node> subNodes = null;
		
		//Increment angle between Nodes and set Maxangle between subnodes
		int length = subNodes.size();
		double maxangle = 1*Math.PI;
		double deltaAngle = maxangle/length;
		
	
		
		//for every node in subNodes draw the Subnode
		for (Node subNode : subNodes) {
			double angle =+ deltaAngle;
			drawSubnode(subNode.nodeName,radius,angle,supNode.nodePos,(int) size);
		}
		
	}
	
	//Draws a Subnode as a JButton and enables its visibility according to specifications
	public void drawSubnode(String NodeName, double radius, double angle, Point SupNodePos,int size) {
		//Add difference to old position
		double deltax = Math.cos(angle)*radius;
		double deltay = Math.sin(angle)*radius;
		
		int xpos = (int) Math.round(SupNodePos.getX() + deltax);
		int ypos = (int) Math.round(SupNodePos.getY() + deltay);
		
		//Create Buttton with corresponding coordinates, size and name
		JButton Node = new JButton();
//		Node.setVisible(true); needed?
		Node.setText(NodeName);
		Node.setBounds(xpos, ypos, size, size);
		
		//Add Node to Global ArrayList of Nodes
		Nodes.add(Node);
	}
	
	
	//Perform Action according to Action Event
	public void actionPerformed(ActionEvent e) {
		
	}
}