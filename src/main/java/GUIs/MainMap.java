package GUIs;


import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.awt.event.*;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;

import java.awt.EventQueue;
import java.awt.event.ActionListener;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.DeleteErrorException;
import com.spaetzle007.MapOfMathematicsLibraries.Linked;
import com.spaetzle007.MapOfMathematicsLibraries.LinkedList;


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
- Navigation features
- ....
See Code for details
*/

public class MainMap extends JFrame implements ActionListener {
	//Benoetigte Daten fuer erzeugen der Map
	private MainViewport viewport;
	private LinkedList LinkedList;
	private Linked currentLinked;
	
	//Setting dimensions, starting point of map, Node distance and size
	private int width;
	private int height;
	private Point origin;
	private int L0dist;
	private int L0size;
	private double globalAngle;
	private Boolean debugging;
	
	//Displayed Swing components on Map
	private ArrayList<JButton> nodeButtons;
	private ArrayList<Level> LevelList;
	private JPanel MapPanel;
//	private ArrayList<???> Vertex
	private JTextField search;
	private JButton back;
	
	public class Level{
		ArrayList<Node> Nodes;
		int level;
		
		//Constructors
		Level(){
		}
		
		Level(ArrayList<Node> Nodes,int level){
			this.Nodes = Nodes;
			this.level = level;
		}
		
		//Should only be used for origin and name should be "Mathematik"
		Level(String Name){
			ArrayList<Node> NodesL0 = new ArrayList<Node>();
			Node Mathematik = new Node(LinkedList.get(LinkedList.search(Name)));
			NodesL0.add(Mathematik);
			this.Nodes = NodesL0;
			this.level = 0;
		}
		
		//returns an ArrayList of the Names of every Node in level
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
		
		public int getLevel() {
			Boolean nodefound = false;
			int nodeLvl = 0;
				for (Level lvl : LevelList) {
					for (Node lvlNode : lvl.Nodes) {
						if (lvlNode.nodeName.equals(nodeName)) {
							nodefound = true;
							nodeLvl = lvl.level;
						}
					}
				}
			//Falls Node nicht in LevelList gefunden werden konnte print error message
			if (!nodefound) {
				System.out.println(nodeName + " konnte nicht in LevelList gefunden werden");
				JOptionPane.showMessageDialog(null, "Node konnte nicht in LevelList gefunden werden","Fehler", 3);
			}
			return nodeLvl;	
		}
		
		public ArrayList<Node> getSubNodes(){
			ArrayList<Node> subNodes = new ArrayList<Node>();
			int subNodeLevel = getLevel()+1;
			
			if (subNodeLevel < LevelList.size()) {
				Level subLvl = LevelList.get(subNodeLevel);
				for (Node lvlNode : subLvl.Nodes) {
					if (lvlNode.Link.getSupLink().equals(nodeName)) {
						subNodes.add(lvlNode);
					}
				}
			}
			return subNodes;
		}
		
		public Node getSupNode(){
			Node supNode = new Node();
			int supNodeLevel = getLevel()-1;
			
			if (supNodeLevel >= 0) {
				Level supLvl = LevelList.get(supNodeLevel);
				for (Node lvlNode : supLvl.Nodes) {
					if (lvlNode.nodeName.equals(Link.getSupLink())) {
						supNode = lvlNode;
					}
				}
			}
			return supNode;
		}
	}

	//Main-Methode zum Starten der Map Ansicht
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
		public void run() {
			try {
				//Create MapJFrame and make it visible
				MainMap MapJFrame = new MainMap();
				MapJFrame.setVisible(true);
				
			} catch (Exception e) {
				e.printStackTrace();
				}
			}
		});
	}
	
	//Konstruktor von MainMap()
	public MainMap() {
		//Setting dimensions, starting point of map, Node distance and size
		 width  = (int) Hilfsklassen.Variables.standardsize.getWidth();
		 height = (int)Hilfsklassen.Variables.standardsize.getHeight();
		 origin = new Point((int) (width*0.5),(int) (height*0.5));
		 L0dist = 200;
		 L0size = 20;
		 globalAngle = 2*Math.PI;
		 debugging = true;
		
		//Creates new Viewport to extract LinkedList and ActualLink
		viewport = new MainViewport();
		LinkedList = viewport.getLinkedList();
		currentLinked = viewport.getLinked();
		
		//Convert LinkedList to ArrayList<Level>
		LevelList = convertLinkedListToLevelList("Mathematik");

		//Set MapPanel configurations
		MapPanel = new JPanel();
		MapPanel.setLayout(null);
		MapPanel.setBackground(new Color(255, 204, 153));
		nodeButtons = new ArrayList<JButton>();
		
		//Draws the Mathematik node
		drawButton(origin,"Mathematik",L0size);
		
		//for every level draw level
		for (Level supLvl : LevelList) {
				drawLevel(supLvl);
		}
		
		//Sets the variables of the JFrame MainMap
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, width, height); 	//Format: 1920x1080
		
		//Finalize JFrame Content
		setContentPane(MapPanel);
		setTitle("MapOfMathematics");

		//Output number of nodes
		System.out.println();
		System.out.println("Anzahl nodeButtons: " + nodeButtons.size());
	}

	//Draws the Nodes in the subLvl originating FROM the supLvl
	public void drawLevel(Level supLvl) {
		if (checkforSubLinks(supLvl)) {
			//for every Node in supLvl draw Subnodes
			for (Node supNode : supLvl.Nodes) {
				drawAllSubnodes(supNode);
			}
		}
	}
	
	//Draws all Subnodes of a given Supnode
	public void drawAllSubnodes(Node supNode) {
		ArrayList<Node> subNodes = supNode.getSubNodes();
		
		double maxangle = Math.pow(2/5., supNode.getLevel())*globalAngle;
		double length = subNodes.size();
		double deltaAngle = maxangle/length;
		
		double angle = supNode.getAnglefromPoint(supNode.getSupNode());
		angle -= (deltaAngle + maxangle)/2.;
		
		for (Node subNode : subNodes) {
			angle += deltaAngle;
			drawSubnode(subNode,angle);
		}
	}
	
	//Calculates Coordinates from angle and draws JButton
	public void drawSubnode(Node subNode,double angle) {
		Node supNode = subNode.getSupNode();
		int radius 		= (int) Math.round(Math.pow(2/3., supNode.getLevel())*L0dist);
		int size 		= (int) Math.round(Math.pow(2/5., supNode.getLevel())*L0size);

		setNodeCoordinates(subNode,supNode,radius,angle);
		drawButton(subNode,subNode.nodeName,size);
		
		if (debugging) {
			printDebuggingStats(subNode,supNode);
		}
	}
	
	//Perform Action according to Action Event
	public void actionPerformed(ActionEvent e) {
		
	}
	
	
	//Converts LinkedList to a List of Levels with the starting Linked being originName
	public ArrayList<Level> convertLinkedListToLevelList(String originName){
		LevelList = new ArrayList<Level>();
		Level nthLevel = new Level(originName);
		LevelList.add(nthLevel);
		
		while (checkforSubLinks(nthLevel)) {
			ArrayList<Node> nodeList = new ArrayList<Node>();

			for (Node supNode : nthLevel.Nodes) {
				for (String subLink : LinkedList.getSubLinks(supNode.Link)) {
					Node subNode = new Node(LinkedList.get(LinkedList.search(subLink)));
					nodeList.add(subNode);
				}
			}

			nthLevel = new Level(nodeList,LevelList.size());
			System.out.println("Size of nodeList in conversion while loop: " + nodeList.size());
			System.out.println(nthLevel.getlevelNames());
			LevelList.add(nthLevel);
		}
		return LevelList;
	}
	
	
	//Draw a JButton with the Coordinates at the center of the Button
	public void drawButton(Point center,String Name,int size) {
		int buttonWidth = size*5;
		int buttonheight = size;
		JButton nodeButton = new JButton(Name);
		nodeButton.setText(Name);
		nodeButton.setBounds((int) Math.round(center.getX() - buttonWidth*0.5),(int) Math.round(center.getY() - buttonheight*0.5), buttonWidth, buttonheight);
		nodeButton.setBackground(Color.decode("#FFB366"));
		MapPanel.add(nodeButton);
		
		//Add nodeButton to Global ArrayList of nodeButtons
		nodeButtons.add(nodeButton);
	}
		
	//Calculates subNodes Coordinates from input

	public void setNodeCoordinates(Node subNode,Node supNode,int radius,double angle) {
		double deltax = Math.cos(angle)*radius;
		double deltay = Math.sin(angle)*radius;
		
		subNode.x = (int) Math.round(supNode.getX() + deltax);
		subNode.y=  (int) Math.round(supNode.getY() + deltay);
	}
	
	
	//Checks if any supNodes still even have subNodes
	public boolean checkforSubLinks(Level supLevel) {
		Boolean subLinksExist = false;
		for (Node supNode : supLevel.Nodes) {
			if (!LinkedList.getSubLinks(supNode.Link).isEmpty()) {
				subLinksExist = true;
			}
		}
		return subLinksExist;
	}
	
	
	//Prints a list of Stats for debugging
	public void printDebuggingStats(Node subNode,Node supNode) {
		System.out.println();
		System.out.println("SubNodeName: " + subNode.nodeName);
		System.out.println("SupNodeName: " + supNode.nodeName);
		System.out.println("Angle from \"" + subNode.nodeName + "\" to \"" + supNode.nodeName + "\": " + subNode.getAnglefromPoint(supNode)*360/(2*Math.PI));
		System.out.println("SupNodeXPos: " + (int) supNode.getX());
		System.out.println("SupNodeYPos: " + (int) supNode.getY());
		System.out.println("SubNodeXpos: " + (int) subNode.getX());
		System.out.println("SubNodeYpos: " + (int) subNode.getY());
	}}