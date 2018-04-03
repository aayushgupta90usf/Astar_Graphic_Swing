/*
 * @author Aayush Gupta
 * @author Jeetendra Ahuja
 *
 *Referred link for core logic
 *http://ieeexplore.ieee.org/document/7863246/keywords
 *
 *This file is responsible for core logic to find path between start and end city
 *
 *It also draws yellow line for all cities visited then draws green line for final path
 *
 */

package core2;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class AStarCoreLogic {
	
	private static Map<String,Node> nodesMap = new HashMap<String, Node>();

	// Hash Map for storing 
	static Map<String,Double> distanceFromEndPoint = new HashMap<String,Double>();
	static Map<String,Double> distanceFromStartPoint = new HashMap<String,Double>();
	
	// Storing node, parent node and get the final path by backtracking
	static Map<String,String> traversalNodeParentMap = new HashMap<String,String>();

	// Open and closed list for storing cities that are still open and that are already visited
	static Map<String,Node> openList = new HashMap<String,Node>();
	static Map<String,Node> closedList = new HashMap<String,Node>();
	
	//List of skipped cities provided by user
	static List<String> skippedList = null;
	
	// FLag to determine the heuristic
	static boolean heuristicStraightLineFlag = true;
	
	//Below variables are used to draw line as traversed and hence used convention dynamic** for it
	static Double dynamicX1Cord;
	static Double dynamicY1Cord;
	static Double dynamicX2Cord;
	static Double dynamicY2Cord;
	static String dynamicCurrNodeToNode;
	static Line2D dynamicLine; 
	
	public static Map<String,Node> fileRead(String locFilePath, String conFilePath) {
		try (Stream<String> lines = Files.lines(Paths.get(locFilePath))) {
			for (String line : (Iterable<String>) lines::iterator){
				// Last line in file is END, skip it
				if(!line.equalsIgnoreCase("END")) {
					String[] arr= line.split(" ");
					// adding coordinates into the list
					
					// adding the list into the map with key as city name
					nodesMap.put(arr[0], new Node(arr[0],Double.valueOf(arr[1]),Double.valueOf(arr[2])+200 ));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try (Stream<String> lines = Files.lines(Paths.get(conFilePath))) {
			for (String line : (Iterable<String>) lines::iterator){
				// Last line in file is END, skip it
				if(!line.equalsIgnoreCase("END")) {
				
					List<Node> list = new ArrayList<Node>();
					String[] arr= line.split(" ");
					
					int i = 0;
					while(i < Double.valueOf(arr[1])) {
						list.add(nodesMap.get(arr[i+2]));
						i++;
					}
					// adding the list into the map with key as city name
					nodesMap.get(arr[0]).setConnections(list);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return nodesMap;
	}
	
	/**
	 * 
	 * This method gets final path and print it in green color
	 * It sets initial variables and calls another method traversingAStar() that handles core logic of A* algo
	 */
	public static void traversal (Map<String,Node> nodesMap, Graphics2D coreGraphicObj, String startCity, String endCity, Set<String> skipCitySet, String heuresticChosen) {
		
		if(!heuresticChosen.equalsIgnoreCase("Straight Line Distance") )
				heuristicStraightLineFlag = false;
		
		skippedList = new ArrayList<String>(skipCitySet);
		
		/**
		 * Remove skipped cities from node
		 * 
		 * create new nodemap to delete skip cities, don't update the received one since it will 
		 * change the actual object and will mess up other things.
		 */
		
		Map<String,Node> newsNodesMap = new LinkedHashMap<String, Node>();
		
		// copy received one into new one.
		newsNodesMap.putAll(nodesMap);
		
		
		Iterator< Map.Entry<String, Node> > iterator = newsNodesMap.entrySet().iterator();
		while(iterator.hasNext()) {
			
			Map.Entry< String, Node > entry = iterator.next();
			List<Node> currConnectedNodesList = entry.getValue().getConnections();
			
			if(skippedList.contains(entry.getKey()) ){
				
				// if fromNode is skipped, delete entry
				iterator.remove();
			}
			else {
				// if skipcity in connected nodes, remove it and update key for new list of nodes.
				Iterator<Node> currConnectedNodesListIter = currConnectedNodesList.iterator();
				while(currConnectedNodesListIter.hasNext()) {
					if(skippedList.contains(currConnectedNodesListIter.next().getNode()) )
						currConnectedNodesListIter.remove();
				}
				// create a node with updated connections
				newsNodesMap.get(entry.getKey()).setConnections(currConnectedNodesList);
				newsNodesMap.put(entry.getKey(), entry.getValue());
			}
		}
		

		// computing end point distance for each city and stored in a hash map	
		for(Map.Entry<String, Node> entry:newsNodesMap.entrySet()) {
			if(heuristicStraightLineFlag)
				distanceFromEndPoint.put(entry.getKey(), calculateDistance(newsNodesMap.get(endCity),entry.getValue()));
			else
				distanceFromEndPoint.put(entry.getKey(), 1d);
		}

		// calling actual method for traversing AStar
		traversingAStar(newsNodesMap, startCity, endCity, coreGraphicObj); 

		List<String> finalPath = new ArrayList<String>();

		// iterating through node parent map from bottom to top to get the actual path 
		String reconstructString = endCity;
		while(!reconstructString.equals(startCity)) {
			
			/*
			 * Also draw final path with different color
			 */
			if(traversalNodeParentMap!=null  && reconstructString!=null
					&& traversalNodeParentMap.get(reconstructString)!=null) {
				coreGraphicObj.setStroke(new BasicStroke(3.5f));
				
				dynamicX1Cord = newsNodesMap.get(traversalNodeParentMap.get(reconstructString)).getXcord();
				dynamicY1Cord = newsNodesMap.get(traversalNodeParentMap.get(reconstructString)).getYcord();
				
				
				dynamicX2Cord = newsNodesMap.get(reconstructString).getXcord();
				dynamicY2Cord = newsNodesMap.get(reconstructString).getYcord();
				
				dynamicLine = new Line2D.Double(dynamicX1Cord+10, dynamicY1Cord+10,
						dynamicX2Cord+10, dynamicY2Cord+10);
				coreGraphicObj.setColor(Color.GREEN);
				coreGraphicObj.draw(dynamicLine);
			}

			// Final path Line Drawing end!
			
			finalPath.add(traversalNodeParentMap.get(reconstructString) + " to "+reconstructString);
			reconstructString = traversalNodeParentMap.get(reconstructString);
			
		}
		// reversing the list to get the actual order list
		Collections.reverse(finalPath);

		System.out.println(finalPath);

	}

	/**
	 * Traversing A star based on heuristic selected
	 * Actual logic is here!
	 */
	public static void traversingAStar(Map<String, Node> newsNodesMap, String startString, String endCity, Graphics2D coreGraphicObj){
		
		Node node = newsNodesMap.get(startString);
		node.setDistanceFromStartPoint(0d);
		node.setDistancetoEndPoint(distanceFromEndPoint.get(startString)); 
		node.setTotalHeuristicDistance(distanceFromEndPoint.get(startString));
		openList.put(startString,node);
		distanceFromStartPoint.put(startString, 0d);

		while(!openList.isEmpty()) {

			String currentNode = calculateShortestFromAll();
			traversalNodeParentMap.put(currentNode, openList.get(currentNode).getPrevious());
			/*
			 * Lets try to draw line for each traversal path
			 */
			
			if(traversalNodeParentMap.get(currentNode)!=null &&
					!traversalNodeParentMap.get(currentNode).isEmpty()) {
				
				coreGraphicObj.setStroke(new BasicStroke(3.5f));
				
				dynamicX1Cord = newsNodesMap.get(currentNode).getXcord();
				dynamicY1Cord = newsNodesMap.get(currentNode).getYcord();
				
				dynamicCurrNodeToNode = traversalNodeParentMap.get(currentNode);
				
				dynamicX2Cord = newsNodesMap.get(dynamicCurrNodeToNode).getXcord();
				dynamicY2Cord = newsNodesMap.get(dynamicCurrNodeToNode).getYcord();
				
				dynamicLine = new Line2D.Double(dynamicX1Cord+10, dynamicY1Cord+10,
						dynamicX2Cord+10, dynamicY2Cord+10);
				coreGraphicObj.setColor(Color.YELLOW);
				coreGraphicObj.draw(dynamicLine);
				
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			
			closedList.put(currentNode, openList.get(currentNode));
			openList.remove(currentNode);

			if(currentNode.equals(endCity)) {
				break;
			}

			if(newsNodesMap.get(currentNode).getConnections() == null) {
				if(openList.containsKey(currentNode)) {
					closedList.put(currentNode, openList.get(currentNode));
					openList.remove(currentNode);
				}
				continue;
			}

			// Iterating through each current node connected city
			for(Node childNode: newsNodesMap.get(currentNode).getConnections()) {
				
				// checking if parent of currentNode is equal to child node and it is in closed List
				if((closedList.containsKey(currentNode) && closedList.get(currentNode).getPrevious()!=null && closedList.get(currentNode).getPrevious().equals(childNode.getNode()))) {
					continue;
				}

				// checking for heuristic and computing start point distance of child node based on the heuristic
				if(heuristicStraightLineFlag)
					distanceFromStartPoint.put(childNode.getNode(), distanceFromStartPoint.get(currentNode)+calculateDistance(newsNodesMap.get(currentNode),newsNodesMap.get(childNode.getNode())));
				else
					distanceFromStartPoint.put(childNode.getNode(), distanceFromStartPoint.get(currentNode)+1);

				// calculating total Distance of child Node
				double totalDistance = distanceFromStartPoint.get(childNode.getNode()) + distanceFromEndPoint.get(childNode.getNode());

				// if open list already contains the child node, then
				// compare the start point distance and store the lease one. 
				if(openList.containsKey(childNode.getNode())) {
					if(distanceFromStartPoint.get(childNode.getNode())>=openList.get(childNode.getNode()).getDistanceFromStartPoint()) {
						distanceFromStartPoint.replace(childNode.getNode(), openList.get(childNode.getNode()).getDistanceFromStartPoint());
						continue;
					}
					openList.get(childNode.getNode()).setDistanceFromStartPoint(distanceFromStartPoint.get(childNode.getNode()));
					openList.get(childNode.getNode()).setPrevious(currentNode);
					traversalNodeParentMap.replace(childNode.getNode(), currentNode);
				}

				// if closed list already contains the child node, then
				// compare the start point distance and store the lease one.
				else if(closedList.containsKey(childNode.getNode())) {
					if(distanceFromStartPoint.get(childNode.getNode())>= closedList.get(childNode.getNode()).getDistanceFromStartPoint()) {
						continue;
					}
					openList.put(childNode.getNode(), closedList.get(childNode.getNode()));
					closedList.remove(childNode.getNode());
					openList.get(childNode.getNode()).setDistanceFromStartPoint(distanceFromStartPoint.get(childNode.getNode()));
					openList.get(childNode.getNode()).setPrevious(currentNode);
					traversalNodeParentMap.replace(childNode.getNode(), currentNode);
				}

				// if node is not in both the open and closed means its the new one
				// then need to create a new Node and store it in open list with all its value
				else {
					Node newNode = newsNodesMap.get(childNode.getNode());
					newNode.setTotalHeuristicDistance(totalDistance);
					newNode.setDistancetoEndPoint(distanceFromEndPoint.get(childNode.getNode()));
					newNode.setDistanceFromStartPoint(distanceFromStartPoint.get(childNode.getNode()));
					newNode.setPrevious(currentNode);
					openList.put(childNode.getNode(),newNode);

				}
			}
		}
	}



	/**
	 * calculating shortest based on total distance from open list
	 * @return shortest city name from open list
	 */
	public static String calculateShortestFromAll() {
		String shortest = null;
		double minimum = Double.MAX_VALUE;

		// iterating through open list map for calculating the shortest
		for(Map.Entry<String, Node> entry:openList.entrySet()) {
			if(entry.getValue().getTotalHeuristicDistance() < minimum) {
				minimum = entry.getValue().getTotalHeuristicDistance();
				shortest = entry.getValue().getNode();
			}
		}
		return shortest;
	}

	/**
	 * calculating the straight line distance between two coordinates
	 * @param list1, list2 are the pair of coordinates(x,y)
	 * @return the straight line distance between the two coordinates
	 */

	private static double calculateDistance(Node node1, Node node2) {
		double x1 = node1.getXcord();
		double y1 = node1.getYcord();
		double x2 = node2.getXcord();
		double y2 = node2.getYcord();
		return Math.sqrt((x2 -x1)*(x2-x1) + (y2-y1)*(y2-y1));
	}

	/*
	 * Setter for private variable
	 */
	public static void setNodeMap(Map<String, Node> nodeMap) {
		nodesMap = nodeMap;
	}
	
	/*
	 * Getter for private variable
	 */
	public static Map<String, Node> getNodeMap() {
		return nodesMap;
	}
}
