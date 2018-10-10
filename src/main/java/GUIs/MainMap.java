package GUIs;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.awt.event.*;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.DeleteErrorException;
import com.spaetzle007.MapOfMathematicsLibraries.Linked;
import com.spaetzle007.MapOfMathematicsLibraries.LinkedList;

/*
Language:
Topic = Node (e.g. Analysis is Node)
Connection between Nodes = Vertex (e.g. Connection between Integral and Differential is Vertex)

Linked corresponds to Node
Connection corresponds to Vertex

supLvl: Level that is being drawn FROM
SubLvl: Level that is being drawn TO

This is Map Version 1 which is supposed to look like a branching tree or mindmap
Other Possible versions are nested circles and grid lines

To-do List:
- Add Vertex										  
- Navigation features
- ....
*/

public class MainMap extends JFrame implements ActionListener {
	//Creates new Viewport to extract LinkedList and ActualLink
	private MainViewport viewport = new MainViewport();
	private LinkedList LinkedList = viewport.getLinkedList();
	private Linked currentLinked = viewport.getLinked();
	
	//Setting dimensions, starting point of map, Node distance and size
	private int width  = (int) Hilfsklassen.Variables.standardsize.getWidth();
	private int height = (int)Hilfsklassen.Variables.standardsize.getHeight();
	private Node origin = new Node(new Point((int) (width*0.5),(int) (height*0.5)),"Mathematik");
	private int L0dist = 200;
	private int L0size = 20;
	private double globalAngle = 2*Math.PI;
	private Boolean debugging = true;

	//Displayed Swing components on Map
	private ArrayList<JButton> nodeButtons = new ArrayList<JButton>();
	private ArrayList<Level> LevelList;
	private JPanel MapPanel;
//	private ArrayList<???> Vertex
	private JTextField search;
	private JButton back;
	
	//Node class is position, Name and Link of a Topic
	public class Node extends Point{
		String nodeName;
		Linked Link;
		
		Node(){
			this.x = (int) origin.getX();
			this.y = (int) origin.getY();
		}
		
		Node(Point nodePos,String nodeName){
			this.x = (int) nodePos.getX();
			this.y = (int) nodePos.getY();
			this.nodeName = nodeName;
		}
		
		Node (Linked Link){
			x = (int) origin.getX();
			y = (int) origin.getY();
			this.Link = Link;
			this.nodeName = Link.getName();
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
					for (Node lvlNode : lvl) {
						if (lvlNode.nodeName.equals(nodeName)) {
							nodefound = true;
							nodeLvl = lvl.levelInt;
						}
					}
				}
				
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
				for (Node lvlNode : subLvl) {
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
				for (Node lvlNode : supLvl) {
					if (lvlNode.nodeName.equals(Link.getSupLink())) {
						supNode = lvlNode;
					}
				}
			}
			return supNode;
		}
	}

	//Level class is a List of Nodes and an integer LevelInt
	public class Level extends ArrayList<Node>{
		int levelInt;
		
		//Constructor
		Level(int level){
			this.levelInt = level;
		}
			
		//returns an ArrayList of the Names of every Node in level
		public ArrayList<String> getlevelNames() {
			ArrayList<String> levelNames = new ArrayList<String>();
			for (Node lvlNode : this) {
				levelNames.add(lvlNode.nodeName);
			}
			return levelNames;
		}
	}

	//Main-Methode zum Starten der Map Ansicht
	public static void main(String[] args) {
		EventQueue.invokeLater(new DisplayMapJFrame());
	}

	//Constructor of MapJFrame
	public MainMap() {
		LevelList = convertLinkedListToLevelList("Mathematik");

		configurePanel();
		configureMapJFrame();
		
		drawButton(origin,L0size);
		
		for (Level supLvl : LevelList) {
				drawLevel(supLvl);
		}
	}

	//Draws the subLevel originating FROM the supLevel
	public void drawLevel(Level supLvl) {
		if (checkforSubLinks(supLvl)) {
			for (Node supNode : supLvl) {
				drawAllSubnodes(supNode);
			}
		}
	}
	
	//Draws all subNodes of a given supNode
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
	
	//Calculates Position of and finally draws JButton of subNode
	public void drawSubnode(Node subNode,double angle) {
		Node supNode = subNode.getSupNode();
		int radius 		= (int) Math.round(Math.pow(2/3., supNode.getLevel())*L0dist);
		int size 		= (int) Math.round(Math.pow(2/5., supNode.getLevel())*L0size);

		setNodeCoordinates(subNode,radius,angle);
		drawButton(subNode,size);
		
		if (debugging) {
			printDebuggingStats(subNode,supNode);
		}
	}
	
	//Perform Action according to Action Event
	public void actionPerformed(ActionEvent e) {
		
	}
	
	//Converts LinkedList to a List of Levels with the starting Linked being originName
	public ArrayList<Level> convertLinkedListToLevelList(String originName){
		Level supLevel = new Level(0);
		supLevel.add(new Node(LinkedList.get(LinkedList.search(originName))));
		
		LevelList = new ArrayList<Level>();
		LevelList.add(supLevel);
		
		while (checkforSubLinks(supLevel)) {
			Level subLevel = new Level(LevelList.size());

			for (Node supNode : supLevel) {
				for (String subLink : LinkedList.getSubLinks(supNode.Link)) {
					Node subNode = new Node(LinkedList.get(LinkedList.search(subLink)));
					subLevel.add(subNode);
				}
			}
			LevelList.add(subLevel);
			supLevel = subLevel;
			
			System.out.println("Size of nodeList in conversion while loop: " + subLevel.size());
			System.out.println(subLevel.getlevelNames());

		}
		return LevelList;
	}
	
	//Draw a JButton with the Coordinates at the center of the Button
	public void drawButton(Node centerNode,int size) {
		int buttonWidth = size*5;
		int buttonheight = size;
		JButton nodeButton = new JButton(centerNode.nodeName);
		nodeButton.setText(centerNode.nodeName);
		nodeButton.setBounds((int) Math.round(centerNode.getX() - buttonWidth*0.5),(int) Math.round(centerNode.getY() - buttonheight*0.5), buttonWidth, buttonheight);
		nodeButton.setBackground(Color.decode("#FFB366"));
		MapPanel.add(nodeButton);
		
		//Add nodeButton to Global ArrayList of nodeButtons
		nodeButtons.add(nodeButton);
	}
		
	//Sets the Coordinates of a subNode with given inputs
	public void setNodeCoordinates(Node subNode,int radius,double angle) {
		double deltax = Math.cos(angle)*radius;
		double deltay = Math.sin(angle)*radius;
		
		subNode.x = (int) Math.round(subNode.getSupNode().getX() + deltax);
		subNode.y=  (int) Math.round(subNode.getSupNode().getY() + deltay);
	}
	
	//Configures JPanel
	public void configurePanel() {
		MapPanel = new JPanel();
		MapPanel.setLayout(null);
		MapPanel.setBackground(new Color(255, 204, 153));
	}
	
	//Configures MapJFrame
	public void configureMapJFrame() {
		//Sets the variables of the JFrame MainMap
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, width, height); 	//Format: 1920x1080
		
		//Finalize JFrame Content
		setContentPane(MapPanel);
		setTitle("MapOfMathematics");
	}
	
	//Returns true if any Nodes in supLevel have subNodes
	public boolean checkforSubLinks(Level supLevel) {
		Boolean subLinksExist = false;
		for (Node supNode : supLevel) {
			if (!LinkedList.getSubLinks(supNode.Link).isEmpty()) {
				subLinksExist = true;
			}
		}
		return subLinksExist;
	}
	
	//Runnable creates a MainMap
	public static class DisplayMapJFrame implements Runnable{
			public void run() {
				try {
					MainMap MapJFrame = new MainMap();
					MapJFrame.setVisible(true);
					
				} catch (Exception e) {
					e.printStackTrace();
					}
				}
	}
	
	//Prints a list of Stats for debugging if enabled
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