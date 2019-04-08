package GUIs;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.MouseInputListener;

import java.util.ArrayList;

import com.spaetzle007.MapOfMathematicsLibraries.Linked;
import com.spaetzle007.MapOfMathematicsLibraries.LinkedList;


/*
Terminology (see Documentation for more details)
Topic = Node (e.g. Analysis is Node)
Connection between Nodes = Edge (e.g. Connection between Integral and Differential is Edge)

supLvl: Level that is being drawn FROM
subLvl: Level that is being drawn TO

This is Map Version 1 which is supposed to look like a branching tree or mindmap
Other Possible versions are nested circles and grid lines
*/

public class MainMap extends JFrame{
	//Creates new Viewport to extract LinkedList and ActualLink
	private MainViewport viewport = new MainViewport();
	private LinkedList LinkedList = viewport.getLinkedList();
//	private Linked currentLinked = viewport.getLinked();
	
	//Setting dimensions, starting Vector of map, Node distance and size
	private int width  = (int) Hilfsklassen.Variables.standardsize.getWidth();
	private int height = (int) Hilfsklassen.Variables.standardsize.getHeight();
	private int L0dist = 600;
	private int L0size = 70;
	private double scale = 1;
	private Vector origin = new Vector (new Point((int) (width/2.),(int) (height/2.)));
	private Boolean debugging = false;
	
	//Graph/Tree Node and Edge details
	private ArrayList<Node> globalNodeList = new ArrayList<Node>();
	private ArrayList<Level> levelList = convertLinkedListToLevelList();
	private ArrayList<Edge> treeEdges = new ArrayList<Edge>();
	
	//Displayed Swing components on Map
	private DrawingPanel MapPanel = new DrawingPanel();
	private JButton cleanMapButton;

/**********************************************************************************************************************************/
	

	//Main-Methode zum Starten der Map Ansicht
	public static void main(String[] args) {
		//Why is the Event Queue,runnable, run shit even needed?
//		EventQueue.invokeLater(
//		new Runnable() {
//			public void run() {
				try {
					MainMap MapJFrame = new MainMap();
					MapJFrame.setVisible(true);
				} 
				catch (Exception failedConstruction) {
					failedConstruction.printStackTrace();
					System.out.println("Map Construction failed");
				}
//			}
//		});
	}
	
	//Constructor of the MainMapJFrame
	public MainMap() throws InterruptedException {
		configurePanel();
		configureMapJFrame();
		
		drawInterface();
		drawNodes();
		drawTreeEdges();
		
		cleanUpMap();
	}
	
	//Allows navigation by translating the whole map in direction of drag
	MouseInputListener navigateByDrag = new MouseInputListener() {

		Vector previousPos = new Vector();
		
		//Translates all elements by dragged amount
		public void mouseDragged(MouseEvent mouseDragged) {
			Vector mousePos = new Vector(mouseDragged.getPoint());
			Vector deltaPos = mousePos.add(previousPos.scale(-1));
			
			translate(mousePos,deltaPos);
			previousPos = mousePos;
		}
		
		//Translates all elements by vector deltaPos
		public void translate(Vector mousePos,Vector deltaPos) {
			MapPanel.removeAll();
			
			for (Node Node : globalNodeList) {
				Node.setPos(Node.add(deltaPos));
			}
			drawTreeEdges();
			drawInterface();
		}
		
		//Get current mouse position
		public void mouseMoved(MouseEvent mouseMoved) {
			previousPos.setPos(mouseMoved.getPoint());
		}
		
		//Inherited Methods
		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}
		
		public void mouseClicked(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}
	};
	
	//Allows zooming in the map by scaling all elements on the map using the current mousePos as a pivot
	MouseWheelListener zoomInByScroll = new MouseWheelListener() {
		int logScale = 0;
		
		//Reads the amount turned and invokes zoomIn
		public void mouseWheelMoved(MouseWheelEvent turnEvent) {
//			Vector mousePos = turnEvent.getVector();
			Vector mousePos = new Vector(turnEvent.getPoint());
			logScale += turnEvent.getWheelRotation();
			
			zoomIn(mousePos);
		}
			
		public void zoomIn(Vector mousePos) {
			double scaleNew = Math.pow(10, -logScale/10.);
			MapPanel.removeAll();
			
			for (Node Node : globalNodeList) {
				Vector mousePosToNode = Node.add(mousePos.scale(-1));
//				mousePosToNode = scaleVector(mousePosToNode,scaleNew/scale);
				mousePosToNode = mousePosToNode.scale(scaleNew/scale);
				Node.setScale(scaleNew/scale);
//				Node.setPos(vectorAddition(mousePos,mousePosToNode));
				Node.setPos(mousePos.add(mousePosToNode));
			}
			scale = scaleNew;
			drawTreeEdges();
			drawInterface();
		}
	};
	
	//2-D Vector that can return added and scaled versions of itself
	class Vector{
		double x;
		double y;
		
		Vector(){
			x = 0;
			y = 0;
		}
		
		Vector(Point p){
			x = p.getX();
			y = p.getY();
		}
		
		void setPos(Point p) {
			x = p.getX();
			y = p.getY();
		}
		
		Vector add(Vector b) {
			Vector addedVector = new Vector();
			addedVector.x = x+b.x;
			addedVector.y = y+b.y;
			return addedVector;
		}
		Vector scale(double s) {
			Vector scaledVector = new Vector();
			scaledVector.x = s*x;
			scaledVector.y = s*y;
			return scaledVector;
		}
		
		double length() {
			double lsquared = Math.pow(x, 2)+Math.pow(y, 2);
			return Math.sqrt(lsquared);
		}
		
		Vector unitVector() {
			return this.scale(1/this.length());
		}
	}
	
	//A Node is a Vector with its assosiziated button and Link
	
	//Node class is position, Name and Link of a Topic
	public class Node extends Vector{
		JButton nodeButton;
		String name;
		Linked Link;
		double nodeWidth = L0size*5;
		double nodeHeight = L0size;
		
		Node (Linked Link){
			this.x = origin.x;
			this.y = origin.y;
			this.Link = Link;
			this.name = Link.getName();
		}
		
		//Opens Topic in MainViewport when clicked
		ActionListener openClicked() {
			ActionListener openClicked = new ActionListener() {
				public void actionPerformed(ActionEvent buttonClicked) {
					Linked clickedLinked = Link;
					viewport.setActual(clickedLinked);
					setContentPane(viewport);
					viewport.update();
				}
			};
			return openClicked;
		}
		
		//Draws all subNodes and treeVertices of a given supNode
		public void drawAllSubNodes() {
			ArrayList<Node> subNodes = getSubNodes();
			
			double maxangle = Math.pow(0.4, getlevelNum())*2*Math.PI;
			double length = subNodes.size();
			double deltaAngle = maxangle/length;
			
			double angle = getAngletoVector(getSupNode());
			angle -= (deltaAngle + maxangle)/2.;
			
			for (Node subNode : subNodes) {
				angle += deltaAngle;
				int radius 		= (int) (Math.pow(2/3., getlevelNum())*L0dist);
				int size 		= (int) (Math.pow(3/5., getlevelNum()+1)*L0size);
	
				double deltax = Math.cos(angle)*radius;
				double deltay = Math.sin(angle)*radius;
				
				subNode.nodeWidth = size*5;
				subNode.nodeHeight = size;
				subNode.setPos(new Point ((int) (x + deltax),(int) (y + deltay)));
				
				if (debugging) {
					printDebuggingStats();
				}
			}
		}
		
		//Return the level of the Node starting with 0
		public int getlevelNum() {
			for (Level lvl : levelList) {
				if (lvl.getNodeNames().contains(this.name)) {
					return lvl.num;
				}
			}
			System.out.println(name + " konnte nicht in LevelList gefunden werden");
			return 0;	
		}
		
		//Get all Nodes that are connected by edges (currently only hierarchy)
		public ArrayList<Node> getConNodes(){
			ArrayList<Node> conNodes = getSubNodes();
			conNodes.add(getSupNode());
			
			return conNodes;
		}
		
		//Prints a list of Stats for debugging if enabled
		public void printDebuggingStats() {
			if (debugging) {
				System.out.println();
				System.out.println("NodeName: " + name);
				System.out.println("LevelNum:" + getlevelNum());
				System.out.println("SupNodeName: " + getSupNode().name);
				System.out.println("Angle from \"" + name + "\" to \"" + getSupNode().name + "\": "
						+ getAngletoVector(getSupNode()) * 360 / (2 * Math.PI));
				System.out.println("SupNodeXPos: " + (int) getSupNode().y);
				System.out.println("SupNodeYPos: " + (int) getSupNode().x);
				System.out.println("NodeXpos: " + x);
				System.out.println("NodeYpos: " + y);
			}
		}
		
		public ArrayList<Node> getSubNodes(){
			ArrayList<Node> subNodes = new ArrayList<Node>();
			int subNodeLevel = getlevelNum()+1;
			
			if (subNodeLevel < levelList.size()) {
				Level subLvl = levelList.get(subNodeLevel);
				for (Node lvlNode : subLvl) {
					if (lvlNode.Link.getSupLink().equals(name)) {
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
				Level supLvl = levelList.get(supNodeLevel);
				for (Node lvlNode : supLvl) {
					if (lvlNode.name.equals(Link.getSupLink())) {
						supNode = lvlNode;
					}
				}
			}
			return supNode;
		}
		
		//Create new Button and configure it
		public void drawButton() {
			nodeButton = new JButton(name);
			nodeButton.setText(name);
			nodeButton.setBounds((int) Math.round(x - nodeWidth*0.5)
								,(int) Math.round(y - nodeHeight*0.5)
								,(int) Math.round(nodeWidth)
								,(int) Math.round(nodeHeight));
			nodeButton.setBackground(Color.decode("#FFB366"));
			nodeButton.addActionListener(openClicked());
				
			MapPanel.add(nodeButton);
			}
	
		public void setPos(Point p) {
			this.x = p.getX();
			this.y = p.getY();
			drawButton();
		}
		
		public void setPos(Vector p) {
			this.x = p.x;
			this.y = p.y;
			drawButton();
		}
		
		public void setScale(double scale) {
			nodeWidth  = nodeWidth * scale;
			nodeHeight = nodeHeight * scale;
		}
	
		public double getAngletoVector(Vector p) {
			double deltax = x-p.x;
			double deltay = y-p.y;
			double angle = Math.atan2(deltay, deltax);
			return angle;
		}
	}
	
	//Edge class is two connected Nodes and a Color
	public class Edge {
		Node start;
		Node end;
		Color color;
		
		Edge(Node start,Node end,Color Color){
			this.start = start;
			this.end = end;
			this.color = Color;
		}
		
		public void drawEdge() {
		    treeEdges.add(this);        
		    repaint();
		}
		
	}
	
	//Level class is a List of Nodes and an integer levelNum
	public class Level extends ArrayList<Node>{
		int num;
		
		Level(int level){
			this.num = level;
		}
		
		//Returns true if no Node in level has subNodes
		public boolean isLowest() {
			for (Node supNode : this) {
				if (!LinkedList.getSubLinks(supNode.Link).isEmpty()) {
					return false;
				}
			}
			return true;
		}
		
		public ArrayList<String> getNodeNames() {
			ArrayList<String> NodeNames = new ArrayList<String>();
			for (Node lvlNode : this) {
				NodeNames.add(lvlNode.name);
			}
			return NodeNames;
		}
	}
	
	//drawingPanel provides a canvas to paint lines on
	public class DrawingPanel extends JPanel{	
		public void addLine(Node start,Node end, Color color) {
			treeEdges.add(new Edge(start,end, color));        
		    repaint();
		}
		
		protected void paintComponent(Graphics g) {
		    super.paintComponent(g);
		    for (Edge Edge  : treeEdges) {
		        g.setColor(Edge.color);
		        g.drawLine((int) Edge.start.x,(int) Edge.start.y,(int) Edge.end.x,(int) Edge.end.y);
		    }
		}
	}
	
	//Configures JPanel
	public void configurePanel() {
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
	
	//draw Interface of the Map
	void drawInterface(){
		//Creates cleanMapJButton
		ActionListener cleanMapAction = new ActionListener() {
			public void actionPerformed(ActionEvent buttonClicked) {
				try {
					cleanUpMap();
				} catch (InterruptedException cleanInterrupted) {
					cleanInterrupted.printStackTrace();
					System.out.println("Map Clean up interrupted");
				}
			}
		};

		cleanMapButton = new JButton("Clean Map");
		cleanMapButton.setText("cleanMapJButton");
		cleanMapButton.setBounds(0
				,0
				, 200
				, 30);
		cleanMapButton.setBackground(Color.decode("#FFB366"));
		cleanMapButton.addActionListener(cleanMapAction);
		MapPanel.add(cleanMapButton);
	}
	
	//Draws all Nodes
	public void drawNodes() {
		Node originNode = levelList.get(0).get(0);
		globalNodeList.add(originNode);
		originNode.drawButton();
		
		//Draws the subLevel originating FROM the supLevel
		for (Level supLvl : levelList) {
			if (!supLvl.isLowest()) {
				for (Node supNode : supLvl) {
					supNode.drawAllSubNodes();
				}
			}
		}
	}
	
	//Draws all treeEdges
	public void drawTreeEdges() {
		for (Node Node : globalNodeList) {
			Edge treeEdge = new Edge(Node.getSupNode(),Node,Color.BLACK);
			treeEdge.drawEdge();
		}
	}
		
	//Converts LinkedList to a List of Levels with the starting Linked being originName
	public ArrayList<Level> convertLinkedListToLevelList(){
		//Initialize first Level and LevelList
		Level supLevel = new Level(0);
		supLevel.add(new Node(LinkedList.getOriginLinked()));

		levelList = new ArrayList<Level>();
		levelList.add(supLevel);

		//Construct subLevel originating from the supLevel down to the lowest level
		while (!supLevel.isLowest()) {
			Level subLevel = new Level(levelList.size());
			
			for (Node supNode : supLevel) {
				for (String subLink : LinkedList.getSubLinks(supNode.Link)) {
					Node subNode = new Node(LinkedList.get(LinkedList.search(subLink)));
					globalNodeList.add(subNode);
					subLevel.add(subNode);
				}
			}

			//subLevel becomes supLevel
			levelList.add(subLevel);
			supLevel = subLevel;

			if (debugging) {
				System.out.println("Size of nodeList in conversion while loop: " + subLevel.size());
				System.out.println(subLevel.getNodeNames());
			}
		}
		return levelList;
	}
	
	//Heuristic algorithm to create a map with least possible crossover and maximum spread
	public void cleanUpMap() throws InterruptedException {
		//A particle contains its pos,vel,force vectors
		//It also has an assosoziated Node
		class Particle{
			Node node;
			Vector pos;
			Vector vel;
			Vector force;
			
			Particle(Node node){
				this.node = node;
//				Hier Achtung!
				this.pos = node;
				this.vel = new Vector();
				this.force = new Vector();
			}
			
//			void getDynamics(){
//				System.out.println(node.name);
//				System.out.println("xPos: "  + pos.x);
//				System.out.println("yPos: "  + pos.y);
//				System.out.println("xvel: "  + vel.x);
//				System.out.println("xvel: "  + vel.y);
//				System.out.println("xforce: "  + force.x);
//				System.out.println("yforce: "  + force.y);
//			}
		}
		
		//Initialize fields
		int n = (int) Math.pow(10, 2.5);
		double deltaT = 2/1000.;
		double ks = 9*Math.pow(10, 3)/(L0dist);
		double ke = -Math.pow(10, 1)*(Math.pow(L0dist,2));
		
		//Run through the simulation 3 times for good measure
		for (int k = 0;k < 3;k++) {
			
		ArrayList<Particle> allParticles = new ArrayList<Particle>();
		for (Node node : globalNodeList) {
			allParticles.add(new Particle(node));
		}
		
		//Run Simulation
		for (int i = 0;i < n;i++) {
			for (Particle x : allParticles) {
				//Update Position
				Vector deltaS = x.vel.scale(deltaT);
				x.pos = x.pos.add(deltaS);
				
				//Update Velocity
				Vector deltaV = x.force.scale(deltaT);
				x.vel = x.vel.add(deltaV);
				
				//Update Force
				x.force = new Vector();
				for (Particle xPrime : allParticles) {
					//The vector xToxPrime Vectors (obviously) from x to x'
					//The negative el. constant results in the vector being turned over
					Vector xToxPrime = xPrime.pos.add(x.pos.scale(-1));
					double dist = xToxPrime.length();
					xToxPrime = xToxPrime.unitVector();
					
					//Add electric force if particles are not the same
					if (!x.equals(xPrime) && dist != 0){
						double scalingFactor = Math.pow(2, -xPrime.node.getlevelNum());
						Vector elForce = xToxPrime.scale( ke*scalingFactor*Math.min(5,1/Math.pow(dist, 2)));
						x.force = x.force.add(elForce);
					}
					
					//Add spring force if there is an edge connecting Nodes
					//Get relaxation lenths for Springs
					if (x.node.getConNodes().contains(xPrime.node) && !(x.equals(xPrime))) {
//						double l0 = x.node.getLocation().distance(xPrime.node);
						double l0 = x.node.add(xPrime.node.scale(-1)).length();
						Vector springForce = xToxPrime.scale(ks*(dist-l0));
						x.force = x.force.add(springForce);
					}
				}
				
			}
		}
		//Renew Node Positions on Map
		MapPanel.removeAll();
		for (Node node : globalNodeList) {
			for (Particle x : allParticles) {
				if (x.node.equals(node)) {
					node.setPos(new Point((int) x.pos.x,(int) x.pos.y));
				}
			}
		}
		drawTreeEdges();
		drawInterface();
		System.out.println("Clean up finished");
		}
	}
}