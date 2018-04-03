/*
 * 
 * @author Aayush Gupta
 * @author Jeetendra Ahuja
 * 
 *  
 * This class is responsible for building entire GUI
 * It interacts with AStarCoreLogic to find actual path
 * 
 * When we hit plot button, then it calls core class to get Node map of all cities
 * 
 */

package core2;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;



@SuppressWarnings("serial")
class AStarGUICode extends JFrame implements MouseListener,MouseMotionListener{
	
	static String locFilePath = null;
	static String conFilePath = null;
	static String heuresticChosen = null;
	
	public static final Font fontStyle = new Font(Font.MONOSPACED, Font.BOLD, 15);
	
	static boolean isCityDragged = false;
	
	static AStarGUICode AstarGUIGraphicsObj = null;
	static Graphics coreGraphicObj = null;
	
	AffineTransform at;
	BasicStroke dashes;
	
	Set<String> skipCitySet = new HashSet<String>();
	
	public static Map<String, Node> nodesMap = null;
	
	private static Map<String,Ellipse2D.Double> circles = new LinkedHashMap<String, Ellipse2D.Double>();
	private static final double DIAMETER = 30.0;
	static Color color = Color.gray;
	private static Map<List<String>,Shape> lines = new LinkedHashMap<List<String>,Shape>();
	private String selectedCity = null;
	
	static boolean isFindingPath = false;

	public String startCity = null;
	static String endCity = null;
	
//	Graphics graph

	/* Everything start with J is Swing components
	 * while button, frame are AWT ones..
	 * Swing offers far more functionality than AWT so unless your users
	 * don't have Swing support you should use the Swing ones ie.
	 * 
	 */
	
    public AStarGUICode(){
        this.getInputsAndAddToPanel();
        this.setSize(850,1050); // It sets panel of this size
        
        AstarGUIGraphicsObj = this;
        
        repaint();
        
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        this.setResizable(false); 
        
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
        
    }

    /**
     * main method of execution
     * @param args
     */
    public static void main(String []args){
    	
    	AStarGUICode s=new AStarGUICode();
    	
    	s.setVisible(true); 
    	
    	
    }
    
    /*
     * (non-Javadoc)
     * @see java.awt.Window#paint(java.awt.Graphics)
     * This is called to paint again
     */
    public void paint(Graphics g) {
    	
        super.paint(g);  // fixes the immediate problem ,as suggested!
        
        //set title
        setTitle("A * Visualisation");
        
        // get 2d object
        Graphics2D g2 = (Graphics2D) g;
        
        // a line to separate inputs
        Line2D inputLine1 = new Line2D.Double(0,200,850,200);
        
        g2.draw(inputLine1);
        
        // creates cities map of node if not created yet
        if(locFilePath!=null && locFilePath.length()>0 
    			&& conFilePath!=null && conFilePath.length()>0	&& nodesMap==null)
    		nodesMap = AStarCoreLogic.fileRead(locFilePath, conFilePath);

        // Calling repaint on plot button action, at that time both below maps are ready!
        if( nodesMap!=null && nodesMap.size()>0 ) {
        	plotCity(g2);
        }
        
        // need nodesMap so call after above code
        if(isFindingPath) {
        	isFindingPath = false;
        	if(heuresticChosen==null)
        		heuresticChosen = "Straight Line Distance";
        	// call main code to get path, send graphic object so as to draw on current frame only
        	AStarCoreLogic.traversal(nodesMap, g2, startCity, endCity, skipCitySet, heuresticChosen);
        }
    }
    
    /*
     * 
     * Take user inputs
     */
	public void getInputsAndAddToPanel() {
		JPanel panel=new JPanel();
		
		repaint();
		
		// Default arrow changes to hand cursor
		panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        /*
         * Lets add location file button
         */
		JButton uploadLocFileBtn = createButton("Upload Locations file");
        uploadLocFileBtn.setForeground(Color.WHITE);
        uploadLocFileBtn.setBackground(Color.BLACK);
       
        uploadLocFileBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
         
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
         
                fileChooser.setAcceptAllFileFilterUsed(true);
         
                int rVal = fileChooser.showOpenDialog(null);
                if (rVal == JFileChooser.APPROVE_OPTION) {
                	locFilePath = fileChooser.getSelectedFile().toString();
                }
              }
        });
        uploadLocFileBtn.setBounds(20, 20, 170, 20);
        // Default tooltip behaviour is weird! Let's customize it :)
        changeTooltipBehavior(uploadLocFileBtn, "Upload Locations file");
        panel.add(uploadLocFileBtn);
        
        panel.add(uploadLocFileBtn);
        
        /*
         * Let's add upload connection file button 
         */
        JButton uploadConFileBtn = createButton("Upload Connections file");
        uploadConFileBtn.setForeground(Color.WHITE);
        uploadConFileBtn.setBackground(Color.BLACK);
        uploadConFileBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
         
                // For File
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
         
                fileChooser.setAcceptAllFileFilterUsed(true);
         
                int rVal = fileChooser.showOpenDialog(null);
                
                if (rVal == JFileChooser.APPROVE_OPTION) {
                	conFilePath = fileChooser.getSelectedFile().toString();
                }
              }
            });
        uploadConFileBtn.setBounds(385, 20, 170, 20);
        // Default tooltip behaviour is weird! Let's customize it :)
        changeTooltipBehavior(uploadConFileBtn, "Upload Connections file");
        panel.add(uploadConFileBtn);
        
        /*
         * Lets add plot button now
         */
        JButton plotGraphBtn = new JButton("Plot");
        plotGraphBtn.setBounds(590, 20, 60, 20);
        plotGraphBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				repaint();
			}
		});
        panel.add(plotGraphBtn);
        
        /*
         * Text box for start city
         */
        JTextField startCityTF = new JTextField() {
            public Point getToolTipLocation(MouseEvent e) {
                return new Point(20, -25);
            }
         };
         
        // Default tooltip behaviour is weird! Let's customize it :)
        changeTooltipBehavior(startCityTF, "Enter Start city");
        startCityTF.setBounds(40, 105, 120, 20);
        panel.add(startCityTF, null);
        
        
        /*
         *  Text box for end city
         */
        JTextField endCityTF = new JTextField() {
            public Point getToolTipLocation(MouseEvent e) {
                return new Point(20, -25);
            }
         };
         
        // Default tooltip behaviour is weird! Let's customize it :)
        changeTooltipBehavior(endCityTF, "Enter End city");
        endCityTF.setBounds(185, 105, 120, 20);
        panel.add(endCityTF);
        
        /*
         * Lets add drop down for Heurestic
         */
        JComboBox<String> heureCombo = new JComboBox<String>();
        heureCombo.addItem("Straight Line Distance");
        heureCombo.addItem("Fewest Link");
        
        heureCombo.addActionListener(new ActionListener() {
        	
			@Override public void actionPerformed(ActionEvent e) {
				heuresticChosen = heureCombo.getSelectedItem().toString();
				if(heuresticChosen==null || heuresticChosen=="" || heuresticChosen.length()<1)
					heuresticChosen = "Straight Line Distance"; // set default if not chosen
			}
		});
        heureCombo.setBounds(340, 105, 240, 20);
        panel.add(heureCombo);
        
        
        /**
         * Button to find path
         */
        JButton findPathBtn = createButton("Find Path");
        findPathBtn.setForeground(Color.WHITE);
        findPathBtn.setBackground(Color.BLACK);
        
        findPathBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				startCity = startCityTF.getText();
				endCity = endCityTF.getText();
				isFindingPath = true;
				repaint();
			}
			
		});
        
        findPathBtn.setBounds(615, 105, 100, 20);
        panel.add(findPathBtn);
        
        //------------Added all comp to Panel-----------------//
        
        panel.setLayout(null); // Let's not use default Layout Manager, I want to place buttons wherever I would like to!
        getContentPane().add(panel); 
	}

	/**
	 * @return JButton - A button with desired label
	 * 
	 * We all overrided getToolTipLocation() which is called when you hover over button
	 * so this is a mouse event as passed to method!
	 */
	private JButton createButton(String label) {
		JButton button = new JButton(label) {
            public Point getToolTipLocation(MouseEvent e) {
                return new Point(20, -25);
            }
         };
		return button;
	}

	/**
	 * Change weird default tooltip behavior
	 */
	private void changeTooltipBehavior(Component butt, String tTipText) {
		((JComponent) butt).setToolTipText(tTipText);
		
		ToolTipManager.sharedInstance().setInitialDelay(50);
        ToolTipManager.sharedInstance().setDismissDelay(700);
	}
	
	
	/*
	 * Lets plot cities by the use of map of Nodes created for each cities from core class
	 */
	private void plotCity(Graphics2D g2) {

		for(Entry<String, Node> entry : nodesMap.entrySet()) {

			double x1 = entry.getValue().getXcord();
			double y1 = entry.getValue().getYcord();
			circles.put(entry.getKey(), new Ellipse2D.Double(x1,y1,DIAMETER,DIAMETER));

			// Make skip cities red and don't draw connections!
			if(!skipCitySet.contains(entry.getKey()) ){
				for(Node node : entry.getValue().getConnections()) {

					if(skipCitySet.contains(node.getNode()))
						continue;

					List<String> list = new ArrayList<String>();
					double x2 = node.getXcord();
					double y2 = node.getYcord();

					list.add(entry.getKey());
					list.add(node.node);
					lines.put(list, new Line2D.Double(x1+10,y1+10,x2+10,y2+10));
					g2.setStroke(new BasicStroke(2));
					g2.setColor(color);
					g2.draw(lines.get(list));
				}
				drawCity(g2, entry.getValue(),false);
			}
			else {
				drawCity(g2, entry.getValue(),true);
			}
		}
	}

	/*
	 * Used for mouse events to see whether we have clicked on cities or not
	 */
	public String getCircle(double x, double y) {
	    for (Map.Entry<String, Node> entry : nodesMap.entrySet()) {
	      if(circles!=null && circles.size()>0 && circles.get(entry.getKey()).contains(x,y))
	        return entry.getKey();
	    }
	    return null;
	  }
	
	/*
	 * check there is a line already existing between two cities or not
	 */
	public boolean lineEquals(List<String> list1,String s1, String s2) {
		return (list1.contains(s1) && list1.contains(s2));
	}
	
	/*
	 * Calls above method
	 */
	public boolean isLineExist(String s1, String s2) {
	    for (Map.Entry<List<String>, Shape> entry : lines.entrySet()) {
	    	if(lineEquals(entry.getKey(),s1,s2)) {
	    		return true;
	    	}
	    }
	    return false;
	  }

	

	@Override
	public void mouseClicked(MouseEvent e) {
		
	}
	
	/*
	 * draw all cities from node map created from core logic
	 * skipped cities are drawn red!
	 */
	public void drawCity(Graphics2D graphics, Node currCity, boolean isSkipped) {
		ImageIcon originalIcon = new ImageIcon(getClass().getResource("city.png"));
		
		Ellipse2D.Double cityEllipse = circles.get(currCity.getNode());
		if(isSkipped)
			graphics.setColor(Color.RED);
		else
			graphics.setColor(currCity.getColor());
		graphics.fill(cityEllipse);
		graphics.drawImage(originalIcon.getImage(), (int)cityEllipse.getX()+5, (int)cityEllipse.getY()+5, 20, 20, null);
		
		graphics.setColor(Color.black);
		graphics.setFont(fontStyle);
		graphics.drawString(currCity.getNode(), (float)cityEllipse.getX()+3, (float)cityEllipse.getY());

	}

	/*
	 * It is used to check we have clicked on circle of city or not!
	 */
	public boolean inRange(double x, double y) {
		
		if(nodesMap!=null) {
			Node node = nodesMap.get(selectedCity);
			if(node!=null)
				return (x<node.getXcord()+30 && x>node.getXcord()-30 && y<node.getYcord()+30 && y>node.getYcord()-30);
		}
		else
			return false;
		return false;
			
	}


	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if(circles!=null && circles.size()>0) {
			double x = e.getX();
			double y = e.getY();
			selectedCity = getCircle(x,y);
			if (selectedCity == null)
				isCityDragged = false;
			else if(!isFindingPath) {
					isCityDragged = true;
				if(SwingUtilities.isRightMouseButton(e)) {
					if( !skipCitySet.contains(selectedCity) )
						skipCitySet.add(selectedCity);
					else
						skipCitySet.remove(selectedCity); // city revived again
					repaint();
				}
			}
		}
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		double x=e.getX(),y=e.getY();
		if( !inRange(x, y) && isCityDragged && !isFindingPath 
				&& selectedCity!=null && !skipCitySet.contains(selectedCity)) {
			
			if((e.getX()> 800 || e.getX()< 0) ) {
				x= e.getX()< 0 ?0:800;
			}
			if(e.getY()>1000 || e.getY()<200) {
				y = e.getY()< 200 ?200:1000;

			}
			nodesMap.get(selectedCity).setXcord(x);
			nodesMap.get(selectedCity).setYcord(y);
			isCityDragged = false;
			repaint();
		}
	}

	
	// MouseMotionListener
	@Override
	public void mouseDragged(MouseEvent e) {
		if(isCityDragged && !isFindingPath && selectedCity!=null && 
				!skipCitySet.contains(selectedCity)) {
			if((e.getX()< e.getComponent().getWidth()-10 && e.getX()>= 10) && (e.getY()<e.getComponent().getHeight()-10-200 && e.getY()>=10)) {
				@SuppressWarnings("unused")
				double x = e.getX();
				@SuppressWarnings("unused")
				double y = e.getY();
			}
		}
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// mouse is moving but left button is not pushed down
		// safety thing , may not be required.
		isCityDragged = false;
	}
}





