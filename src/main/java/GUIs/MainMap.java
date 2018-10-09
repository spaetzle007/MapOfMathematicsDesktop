package GUIs;


import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

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
- Conversion from LinkedList to LevelList (without positions, but absolute hierarchy) 
- Implementation of supNode.getsubNodes()											  
- Navigation features
- ....
See Code for details
*/

public class MainMap extends JFrame implements ActionListener {
	
	//Benoetigte Daten fuer erzeugen der Map
	private MainViewport viewport;
	private LinkedList LinkedList;
	private Linked actual;
	private Point origin;
	
	private int width;
	private int height;
	
	//Swing Component oder Bild -> Google
	//Angezeigte Elemente auf Map
	private ArrayList<JButton> nodeButtons;
	private ArrayList<Level> LevelList;
	private JPanel MapPanel;
	
	//private ArrayList<???> Vertex
	private JTextField search;
	private JButton back;
	
	
	 
//	//Ist static class hier richtig um das runnable DisplayMapJFrame auszulagern?
//	public static class DisplayMapJFrame implements Runnable {
//		
//	  }
	
	public class Level{
		ArrayList<Node> Nodes;
		int level;
		
		
		Level(){
			
		}
		
		//Constructor
		Level(ArrayList<Node> Nodes,int level){
			this.Nodes = Nodes;
			this.level = level;
		}
		
		Level(Node Mathematik){
			ArrayList<Node> NodesL0 = new ArrayList<Node>();
			NodesL0.add(Mathematik);
			this.Nodes = NodesL0;
			this.level = 0;
			
		}
		public ArrayList<String> getlevelNames() {
			ArrayList<String> levelNames = new ArrayList<String>();
			for (Node lvlNode : Nodes) {
				levelNames.add(lvlNode.nodeName);
			}
			return levelNames;
		}
	}

	public class Node extends Point{
		String nodeName;
		Linked Link;
		
		Node (){
			x = (int) origin.getX();
			y = (int) origin.getY();
		}
		
		
		Node (Linked Link){
			x = (int) origin.getX();
			y = (int) origin.getY();
			this.Link = Link;
			this.nodeName = Link.getName();
		}
		
		Node (String name){
			x = (int) origin.getX();
			y = (int) origin.getY();
			this.nodeName = name;
		}
		
		public double getAnglefromPoint(Point p) {
			double deltax = x-p.getX();
			double deltay = y-p.getY();
			double angle = Math.atan2(deltay, deltax);
			return angle;
		}
		
		public int getLevel(ArrayList<Level> LevelList) {
			Boolean nodefound = false;
			int nodeLvl = 0;
				for (Level lvl : LevelList) {
					for (Node lvlNode : lvl.Nodes) {
						if (lvlNode.nodeName.equals(nodeName)) {
							nodefound = true;
							nodeLvl = lvl.level;
							System.out.println("Level of " + nodeName + " is " + lvl.level);
						}
					}
				}
			//Falls Node nicht in LevelList gefunden werden konnte print error message
			if (!nodefound) {
				System.out.println("Node konnte nicht in LevelList gefunden werden");
				JOptionPane.showMessageDialog(null, "Node konnte nicht in LevelList gefunden werden","Fehler", 3);
			}
			return nodeLvl;	
		}

		
		public ArrayList<Node> getSubNodes(LinkedList LinkedList,ArrayList<Level> LevelList){
			ArrayList<Node> subNodes = new ArrayList<Node>();
			int subNodeLevel = getLevel(LevelList)+1;
			//Test Fkt
//			System.out.println("Subnodes: " + LevelList.get(subNodeLevel).getlevelNames());
			
			for (Node lvlNode : LevelList.get(subNodeLevel).Nodes) {
				if (stringContained(LinkedList.getSubLinks(Link),lvlNode.nodeName)) {
					subNodes.add(lvlNode);
				}
			}
			return subNodes;
		}
		
		public Node getSupNode(LinkedList LinkedList,ArrayList<Level> LevelList){
			Node supNode = new Node();
			int subNodeLevel = getLevel(LevelList)+1;
			//Test Fkt
//			System.out.println("Subnodes: " + LevelList.get(subNodeLevel).getlevelNames());
			
			for (Node lvlNode : LevelList.get(subNodeLevel).Nodes) {
				if (stringContained(LinkedList.getSubLinks(Link),lvlNode.nodeName)) {
					supNode = lvlNode;
				}
			}
			return supNode;
			
		}
		
	}
	
	/**
	 * Main-Methode zum Starten der Ansicht
	 */
	
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
		
//		DisplayMapJFrame DisplayMap = new DisplayMapJFrame();
//		EventQueue.invokeLater(DisplayMap);
	}
	
	//Konstruktor von MainMap()
	public MainMap() {
		
		//set global width and height
		width = (int) Hilfsklassen.Variables.standardsize.getWidth();
		height = (int)Hilfsklassen.Variables.standardsize.getHeight();
		origin = new Point((int) (width*0.5),(int) (height*0.5));
//		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//		Graphics2D graphics2D = image.createGraphics();

		
		//Creates new Viewport to extract LinkedList and ActualLink
		viewport = new MainViewport();
		String CurrentLinked = viewport.getLinked().getName();
		LinkedList = viewport.getLinkedList();
		
		//Convert LinkedList to ArrayList<Level>
		//By starting from Mathematik and 
		Node Mathematik = new Node(LinkedList.get(LinkedList.search("Mathematik")));
		LevelList = new ArrayList<Level>();
		LevelList.add(new Level(Mathematik));
		int lvl = 0;
		
		//Initializing as null is bad style
		ArrayList<Node> initialnodeList = new ArrayList<Node>();
		for (String subLink : LinkedList.getSubLinks(Mathematik.Link)) {
			Node subNode = new Node(LinkedList.get(LinkedList.search(subLink)));
			initialnodeList.add(subNode);
		}
		lvl = 1;
		Level Ln = new Level(initialnodeList,lvl);
		System.out.println("Size of first initialnodeList: " + initialnodeList.size());
		System.out.println("Name of first node in initialnodeList: " + initialnodeList.get(0).nodeName);
		LevelList.add(Ln);
		System.out.println("Name of Link of first node in level: " + Ln.Nodes.get(0).Link.getName());
		
		while (checkforSubLinks(LinkedList,Ln)) {
			ArrayList<Node> nodeList = new ArrayList<Node>();
			System.out.println("add level to LevelList");
			System.out.println("True if ln.nodes is empty: " + Ln.Nodes.isEmpty());

			for (Node supNode : Ln.Nodes) {
				for (String subLink : LinkedList.getSubLinks(supNode.Link)) {
					Node subNode = new Node(LinkedList.get(LinkedList.search(subLink)));
					nodeList.add(subNode);
				}
			}
			
			lvl++;
			Ln = new Level(nodeList,lvl);
			System.out.println("Size of nodeList in conversion while loop: " + nodeList.size());
			System.out.println(Ln.getlevelNames());
			LevelList.add(Ln);
		}
		
		//L0dist is distance from origin to first nodes
		double L0dist = 200;
		double L0size = 20;
		MapPanel = new JPanel();
		MapPanel.setLayout(null);
		MapPanel.setBackground(new Color(255, 204, 153));
		nodeButtons = new ArrayList<JButton>();
		
		//Test Button
		int buttonWidth = 200;
		int buttonheight = 100;
		JButton nodeButton = new JButton("nodeName");
		nodeButton.setText("Mathematik");
		nodeButton.setBounds(origin.x - (int) Math.round(buttonWidth*0.5), origin.y - (int) Math.round(buttonheight*0.5), buttonWidth, buttonheight);
		nodeButton.setBackground(Color.decode("#FFB366"));
		MapPanel.add(nodeButton);
		
		System.out.println("Levellist length:" + LevelList.size());
		
		//for every level draw level
		for (Level supLvl : LevelList) {
			if (supLvl != LevelList.get(LevelList.size()-1)) {
				drawLevel(supLvl,L0dist,L0size);
			}
		}
		
		setContentPane(MapPanel);
		setTitle("MapOfMathematics");
		
		//Sets the variables of the JFrame MainMap
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, width, height); 	//Format: 1920x1080
		
		System.out.println("Anzahl nodeButtons: " + nodeButtons.size());
//		for (JButton nodeButton2 : nodeButtons) {
//			System.out.println(nodeButton2.getText());
//			System.out.println(nodeButton2.getX());
//			System.out.println(nodeButton2.getY());
//		}
		
	}

	//supLvl already exists
	//drawLevel draws the subLvl originating FROM the supLvl
	public void drawLevel(Level supLvl, double L0dist, double L0size) {
		//Test
//		System.out.println(supLvl.level);
//		System.out.println(supLvl.Nodes);
		
		//calculate radius and size of vertex/node by L0dist, L0size and level
		double radius = Math.round(Math.pow(2/3., supLvl.level)*L0dist);
		double size = Math.round(Math.pow(2/5., supLvl.level)*L0size);
		double maxangle = Math.pow(2/5., supLvl.level)*Math.PI*2;
		
		//for every Node in supLvl draw Subnodes
		for (Node supNode : supLvl.Nodes) {
			drawAllSubnodes(supNode,radius,size,maxangle);
		}
	}
	
	//Draws all Subnodes of a given Supnode
	public void drawAllSubnodes(Node supNode,double radius, double size,double maxangle) {

		//initializes subNodes as getSubNodes of supNode
		ArrayList<Node> subNodes = supNode.getSubNodes(LinkedList, LevelList);
//		System.out.println("Prints true if no more subNodes: " + subNodes.isEmpty());
//		System.out.println("Xpos of supNode: " + supNode.x);
		
		
		//Increment angle between Nodes and set Maxangle between subnodes
		int length = subNodes.size();
//		double angle = supNode.getAnglefromPoint(supNode.getSupNode(LinkedList, LevelList))-Math.PI/2.;
		double angle = supNode.getAnglefromPoint(supNode.getSupNode(LinkedList, LevelList))-maxangle/2.;
		System.out.println("Base angle for " + supNode.nodeName + " :" + angle*360/(2*Math.PI));
		System.out.println("Subnodes of " + supNode.nodeName + " :" + subNodes);
		
		double deltaAngle = maxangle/(double) (length);
		angle -= deltaAngle/2.;

		
		//for every node in subNodes draw the Subnode
		for (Node subNode : subNodes) {
//			System.out.println(angle);
			angle += deltaAngle;
			drawSubnode(subNode,radius,angle,supNode,(int) size);
		}
	}
	//Bugs:
	//SupNodePos is always default point for some reason
	//Angle is only one of 2 possible values
	
	//Draws a Subnode as a JButton and enables its visibility according to specifications
	public void drawSubnode(Node subNode, double radius, double angle, Node supNode,int size) {
		//Add difference to old position
		double deltax = Math.cos(angle)*radius;
		double deltay = Math.sin(angle)*radius;
		
		subNode.x = (int) Math.round(supNode.getX() + deltax);
		subNode.y=  (int) Math.round(supNode.getY() + deltay);
		
		System.out.println();
		System.out.println("Angle for " + subNode.nodeName + ": " + angle*360/(2*Math.PI));
		System.out.println("subNodeName: " + subNode.nodeName);
		System.out.println("SupNodeName: " + supNode.nodeName);
		System.out.println("Angle from " + subNode.nodeName + " to " + supNode.nodeName + ": " + subNode.getAnglefromPoint(supNode)*360/(2*Math.PI));
		System.out.println("SupNodeXPos: " + supNode.getX());
		System.out.println("SupNodeYPos: " + supNode.getY());
		System.out.println("SubNodeXpos: " + subNode.getX());
		System.out.println("SubNodeYpos: " + subNode.getY());
		
		//Create Buttton with corresponding coordinates, size and name
		//Center of Button is at subNode Position
		int buttonWidth = size*5;
		int buttonheight = size;
		JButton nodeButton = new JButton(subNode.nodeName);
		nodeButton.setText(subNode.nodeName);
		nodeButton.setBounds((int) Math.round(subNode.getX()) - (int) Math.round(buttonWidth*0.5),(int) Math.round(subNode.getY())- (int) Math.round(buttonheight*0.5), buttonWidth, buttonheight);
		nodeButton.setBackground(Color.decode("#FFB366"));
		MapPanel.add(nodeButton);
		System.out.println("Button drawn!");
		
		//Add nodeButton to Global ArrayList of nodeButtons
		nodeButtons.add(nodeButton);
	}
	
	//Checks if any supNodes still even have subNodes
	public boolean checkforSubLinks(LinkedList Data,Level supLevel) {
		Boolean subLinksExist = false;
		for (Node supNode : supLevel.Nodes) {
			if (!Data.getSubLinks(supNode.Link).isEmpty()) {
				subLinksExist = true;
			}
		}
		return subLinksExist;
	}
	
//	public void editList(ArrayList<Level> levelList,Node editNode) {
//		Node nodetoEdit = new Node();
//		Level lvltoEdit = new Level();
//		Boolean edit = false;
//		
//		for (Level level : levelList) {
//			for (Node levelNode : level.Nodes) {
//				if (levelNode.nodeName.equals(editNode.nodeName)) {
//					nodetoEdit = levelNode;
//					lvltoEdit = level;
//					edit = true;
//				}
//			}
//		}
//		if (edit){
//			System.out.println("Edited!");
//			lvltoEdit.Nodes.remove(nodetoEdit);
//			lvltoEdit.Nodes.add(editNode);
//		}
//	}
	
	//Perform Action according to Action Event
	public void actionPerformed(ActionEvent e) {
		
	}
	
	public boolean stringContained(ArrayList<String> stringList, String search) {
		for (String Liststr : stringList) {
			if (Liststr.trim().equals(search)){
				return true;
			}
		}
		return false;
	}
}