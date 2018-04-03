/*
 * 
 * @author Aayush Gupta
 * @author Jeetendra Ahuja
 * 
 *  
 * This class is responsible creating Nodes that represents each cities variables like
 * cordinates, color, distance from start city and end city, just to name a few
 * 
 */

package core2;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Node  {
	
	String node = null;
	String previous = null;
	
	private double distancetoEndPoint;
	private double totalHeuristicDistance;
	private double distanceFromStartPoint;
	private List<Node> connections = new ArrayList<Node>();
	private double xcord;
	private double ycord;
	private boolean circleAlreadyMade = false;
	private Color color;
	
	public Node(String node) {
	}
	
	public Node(String node,double xcord, double ycord) {
		this.node = node;
		this.xcord = xcord;
		this.ycord = ycord;
		Random r = new Random();
		color = new Color(r.nextInt(256),r.nextInt(256),r.nextInt(256),50);
	}

	public double getXcord() {
		return xcord;
	}

	public double getYcord() {
		return ycord;
	}

	public void setYcord(double ycord) {
		this.ycord = ycord;
	}
	
	public void setXcord(double xcord) {
		this.xcord = xcord;
	}


	public double getDistanceFromStartPoint() {
		return distanceFromStartPoint;
	}

	public void setDistanceFromStartPoint(double distanceFromStartPoint) {
		this.distanceFromStartPoint = distanceFromStartPoint;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public String getPrevious() {
		return previous;
	}

	public void setPrevious(String previous) {
		this.previous = previous;
	}

	public Double getDistancetoEndPoint() {
		return distancetoEndPoint;
	}

	public void setDistancetoEndPoint(Double distancetoEndPoint) {
		this.distancetoEndPoint = distancetoEndPoint;
	}

	public Double getTotalHeuristicDistance() {
		return totalHeuristicDistance;
	}

	public void setTotalHeuristicDistance(Double totalHeuristicDistance) {
		this.totalHeuristicDistance = totalHeuristicDistance;
	}

	public List<Node> getConnections() {
		return connections;
	}

	public void setConnections(List<Node> connections) {
		this.connections = connections;
	}
	
	public int getConnectionSize() {
		return this.connections.size();
	}
	
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	
	public boolean isCircleAlreadyMade() {
		return circleAlreadyMade;
	}

	public void setCircleAlreadyMade(boolean circleAlreadyMade) {
		this.circleAlreadyMade = circleAlreadyMade;
	}
	

	
}
