package GUIs;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import javax.swing.event.MouseInputListener;

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
twig = Node and corresponding treeVertex

supLvl: Level that is being drawn FROM
subLvl: Level that is being drawn TO

This is Map Version 1 which is supposed to look like a branching tree or mindmap
Other Possible versions are nested circles and grid lines

To-do List:			
- Get MainViewport Article if Node is clicked						  
- Navigation features 
	- zooming in
	- move by dragging mouse
	- use Map space more effectively
		- no stacking
		- no lonely Nodes
- Nicer Aesthetics
- ....
*/

public class MainMap extends JFrame{
	//Creates new Viewport to extract LinkedList and ActualLink
	private MainViewport viewport = new MainViewport();
	private LinkedList LinkedList = viewport.getLinkedList();
	private Linked currentLinked = viewport.getLinked();
	
	//Setting dimensions, starting point of map, Node distance and size
	private int width  = (int) Hilfsklassen.Variables.standardsize.getWidth();
	private int height = (int) Hilfsklassen.Variables.standardsize.getHeight();
	private int L0dist = 200;
	private int L0size = 50;
	private double scale = 1;
	private Point origin = new Point((int) (width/2.),(int) (height/2.));
	private Boolean debugging = true;
	
	//Displayed Swing components on Map
	private ArrayList<Node> globalNodeList = new ArrayList<Node>();
	private ArrayList<Level> LevelList = convertLinkedListToLevelList();
	private ArrayList<Vertex> treeVertices = new ArrayList<Vertex>();
	private DrawPanel MapPanel = new DrawPanel();
	private JTextField search;
	private JButton back;

	
	MouseInputListener navigateByDrag = new MouseInputListener() {
		Point previousPos;
		
		public void mouseDragged(MouseEvent mouseDragged) {
			System.out.println("Mouse dragged!");
			Point mousePos = mouseDragged.getPoint();
			Point deltaPos = vectorAddition(mousePos,scaleVector(previousPos,-1));
			
			translate(mousePos,deltaPos);
			previousPos = mousePos;

		}
		
		public void mouseMoved(MouseEvent mouseMoved) {
			previousPos = mouseMoved.getPoint();
		}
		
		public void translate(Point mousePos,Point deltaPos) {
			MapPanel.removeAll();
			
			for (Node Node : globalNodeList) {
				Node.setPos(vectorAddition(Node,deltaPos));
			}
			drawTreeVertices();
		}
		
		
		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			// TODO Auto-generated method stub
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
		}
	};
		
	//Zooms toward the given mouse Position
	MouseWheelListener zoomInByScroll = new MouseWheelListener() {
//		int previousNotches = 0;
		int logScale = 0;
		
		@Override
		public void mouseWheelMoved(MouseWheelEvent turnEvent) {
			Point mousePos = turnEvent.getPoint();
			logScale += turnEvent.getWheelRotation();
			
			zoomIn(mousePos);
		}
			
		public void zoomIn(Point mousePos) {
			double scaleNew = Math.pow(10, -logScale/10.);
			MapPanel.removeAll();
			
			for (Node Node : globalNodeList) {
				Point mousePosToNode = vectorAddition(Node,scaleVector(mousePos,-1));
				mousePosToNode = scaleVector(mousePosToNode,scaleNew/scale);
				Node.setScale(scaleNew/scale);
				Node.setPos(vectorAddition(mousePos,mousePosToNode));
			}
			System.out.println(scaleNew);
			scale = scaleNew;
			drawTreeVertices();
		}
	};
	
	//Action Listener opens Topic in MainViewport
	ActionListener openClicked(Node clickedNode) {
		ActionListener openClicked = new ActionListener() {
			public void actionPerformed(ActionEvent buttonClicked) {
				Linked clickedLinked = clickedNode.Link;
				viewport.setActual(clickedLinked);
				setContentPane(viewport);
				viewport.update();
			}
		};
		return openClicked;
	}
	
	//drawPanel is needed to draw Lines
	public class DrawPanel extends JPanel{

		public void clearLines() {
			//if other vertices need to be cleared add here
		    treeVertices.clear();
		    repaint();
		}
		
		public void addLine(Node start,Node end, Color color) {
			treeVertices.add(new Vertex(start,end, color));        
		    repaint();
		}
		
		protected void paintComponent(Graphics g) {
		    super.paintComponent(g);
		    for (Vertex vertex  : treeVertices) {
		        g.setColor(vertex.color);
		        g.drawLine(vertex.start.x, vertex.start.y, vertex.end.x, vertex.end.y);
		    }
		}
	}
	
	//Vertex class is two connected Nodes and a Color
	public class Vertex {
		Node start;
		Node end;
		Color color;
		
		Vertex(Node start,Node end,Color Color){
			this.start = start;
			this.end = end;
			this.color = Color;
		}
		
		public void drawVertex() {
		    treeVertices.add(this);        
		    repaint();
		}
	}
	
	//Node class is position, Name and Link of a Topic
	public class Node extends Point{
		JButton nodeButton;
		String nodeName;
		Linked Link;
		double nodeWidth = L0size*5;
		double nodeHeight = L0size;
		
		Node (Linked Link){
			this.x = origin.x;
			this.y = origin.y;
			this.Link = Link;
			this.nodeName = Link.getName();
		}
		
		//Prints a list of Stats for debugging if enabled
		public void printDebuggingStats() {
			System.out.println();
			System.out.println("NodeName: " + nodeName);
			System.out.println("SupNodeName: " + getSupNode().nodeName);
			System.out.println("Angle from \"" + nodeName + "\" to \"" + getSupNode().nodeName + "\": " + getAngletoPoint(getSupNode())*360/(2*Math.PI));
			System.out.println("SupNodeXPos: " + (int) getSupNode().y);
			System.out.println("SupNodeYPos: " + (int) getSupNode().x);
			System.out.println("NodeXpos: " + x);
			System.out.println("NodeYpos: " + y);
		}
		
		//Sets the Coordinates and size of a subNode with given angle to supNode
		public void setNodeProperties(double angleToSupNode) {
			int radius 		= (int) Math.round(Math.pow(2/3., getSupNode().getlevelNum())*L0dist);
			int size 		= (int) Math.round(Math.pow(3/5., getlevelNum())*L0size);
			nodeWidth = size*5;
			nodeHeight = size;
			
			double deltax = Math.cos(angleToSupNode)*radius;
			double deltay = Math.sin(angleToSupNode)*radius;
			
			x = (int) Math.round(getSupNode().x + deltax);
			y = (int) Math.round(getSupNode().y + deltay);
		}
		
		public double getAngletoPoint(Point p) {
			double deltax = x-p.getX();
			double deltay = y-p.getY();
			double angle = Math.atan2(deltay, deltax);
			return angle;
		}
		
		public int getlevelNum() {
			Boolean nodefound = false;
			int nodeLvl = 0;
				for (Level lvl : LevelList) {
					if (lvl.getNodeNames().contains(this.nodeName)) {
						nodeLvl = lvl.levelNum;
						nodefound = true;
					}
				}
				
			if (!nodefound) {
				System.out.println(nodeName + " konnte nicht in LevelList gefunden werden");
			}
			
			return nodeLvl;	
		}
		
		public ArrayList<Node> getSubNodes(){
			ArrayList<Node> subNodes = new ArrayList<Node>();
			int subNodeLevel = getlevelNum()+1;
			
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
			Node supNode = this;
			int supNodeLevel = getlevelNum()-1;
			
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
		
		public void drawButton() {
			//Create new Button and configure it
			nodeButton = new JButton(nodeName);
			nodeButton.setText(nodeName);
			nodeButton.setBounds((int) Math.round(x - nodeWidth*0.5)
								,(int) Math.round(y - nodeHeight*0.5)
								,(int) Math.round(nodeWidth)
								,(int) Math.round(nodeHeight));
			nodeButton.setBackground(Color.decode("#FFB366"));
			nodeButton.addActionListener(openClicked(this));
				
			MapPanel.add(nodeButton);
			}
	
		public void setPos(Point p) {
			this.x = p.x;
			this.y = p.y;
			drawButton();
		}
		
		public void setScale(double scale) {
			nodeWidth  = nodeWidth * scale;
			nodeHeight = nodeHeight * scale;
		}
	}

	//Level class is a List of Nodes and an integer levelNum
	public class Level extends ArrayList<Node>{
		int levelNum;
		
		Level(int level){
			this.levelNum = level;
		}
		
		//Returns true if any Node.Link in supLevel have subLinked
		public boolean isLowest() {
			Boolean isLowest = true;
			for (Node supNode : this) {
				if (!LinkedList.getSubLinks(supNode.Link).isEmpty()) {
					isLowest = false;
				}
			}
			return isLowest;
		}
		
		public ArrayList<String> getNodeNames() {
			ArrayList<String> NodeNames = new ArrayList<String>();
			for (Node lvlNode : this) {
				NodeNames.add(lvlNode.nodeName);
			}
			return NodeNames;
		}
	}
	
	//Constructor of MapJFrame
	public MainMap() {
		configurePanel();
		configureMapJFrame();
		
		drawNodes();
		drawTreeVertices();
	}
	
	//Draws all treeVertices
	public void drawTreeVertices() {
		for (Node Node : globalNodeList) {
			drawVertex(Node);
		}
	}
	
	//Calculates Position of and finally draws Line of Vertex
	public void drawVertex(Node subNode) {
			Vertex treeVertex = new Vertex(subNode.getSupNode(),subNode,Color.BLACK);
			treeVertex.drawVertex();
		}
	
	//Draws all Nodes
	public void drawNodes() {
		Node originNode = LevelList.get(0).get(0);
		globalNodeList.add(originNode);
		originNode.drawButton();
		
		//Draws the subLevel originating FROM the supLevel
		for (Level supLvl : LevelList) {
			if (!supLvl.isLowest()) {
				for (Node supNode : supLvl) {
					drawAllSubNodes(supNode);
				}
			}
		}
	}
	
	//Draws all subNodes and treeVertices of a given supNode
	public void drawAllSubNodes(Node supNode) {
		ArrayList<Node> subNodes = supNode.getSubNodes();
		
		double maxangle = Math.pow(3/5., supNode.getlevelNum())*2*Math.PI;
		double length = subNodes.size();
		double deltaAngle = maxangle/length;
		
		double angle = supNode.getAngletoPoint(supNode.getSupNode());
		angle -= (deltaAngle + maxangle)/2.;
		
		for (Node subNode : subNodes) {
			angle += deltaAngle;
			drawSubnode(subNode,angle);
		}
	}

	//Calculates Position of and finally draws JButton of subNode
	public void drawSubnode(Node subNode,double angle) {
		subNode.setNodeProperties(angle);
		subNode.drawButton();
		
		if (debugging) {
//			subNode.printDebuggingStats();
		}
	}
	
	//Converts LinkedList to a List of Levels with the starting Linked being originName
	public ArrayList<Level> convertLinkedListToLevelList(){
		Level supLevel = new Level(0);
		supLevel.add(new Node(LinkedList.getOriginLinked()));
		
		LevelList = new ArrayList<Level>();
		LevelList.add(supLevel);
		
		while (!supLevel.isLowest()) {
			Level subLevel = new Level(LevelList.size());

			for (Node supNode : supLevel) {
				for (String subLink : LinkedList.getSubLinks(supNode.Link)) {
					Node subNode = new Node(LinkedList.get(LinkedList.search(subLink)));
					globalNodeList.add(subNode);
					subLevel.add(subNode);
				}
			}
			
			LevelList.add(subLevel);
			supLevel = subLevel;
			
			System.out.println("Size of nodeList in conversion while loop: " + subLevel.size());
			System.out.println(subLevel.getNodeNames());
		}
		return LevelList;
	}
	
	//Configures JPanel
	public void configurePanel() {
		MapPanel.addMouseListener(navigateByDrag);
		MapPanel.addMouseMotionListener(navigateByDrag);
		MapPanel.addMouseWheelListener(zoomInByScroll);
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
	
	//Main-Methode zum Starten der Map Ansicht
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
			try {
				MainMap MapJFrame = new MainMap();
				MapJFrame.setVisible(true);
				
			} catch (Exception e) {
				e.printStackTrace();
				}
			}
		});
	}
	
	//scales a vector by a scalar
	public static Point scaleVector(Point a,double scale) {
		Point scaledVector = new Point();
		scaledVector.x = (int) Math.round(a.x*scale);
		scaledVector.y = (int) Math.round(a.y*scale);
		return scaledVector;
		
	}
	
	//adds two vectors together and outputs the result
	public static Point vectorAddition(Point a, Point b) {
		Point c = new Point(a.x+b.x,
							a.y+b.y);
		return c;
	}
	
	//Executes algorithm to make the map with least possible crossover and maximum spread
	public void cleanUpMap() {
		
	}
}