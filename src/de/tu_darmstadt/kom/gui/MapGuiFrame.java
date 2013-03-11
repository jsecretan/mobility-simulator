package de.tu_darmstadt.kom.gui;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.tu_darmstadt.kom.linkedRTree.CircleInterface;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.map.DiscreteMap;
import de.tu_darmstadt.kom.mobilitySimulator.core.mapEvent.AbstractMapEvent;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;
import de.tu_darmstadt.kom.mobilitySimulator.linkedRTree.LinkedRTreeCollapsedBuildingMapEvent;
import de.tu_darmstadt.kom.mobilitySimulator.linkedRTree.LinkedRTreeFireMapEvent;
import de.tu_darmstadt.kom.mobilitySimulator.linkedRTree.LinkedRTreeMapEvent;
import de.tu_darmstadt.kom.mobilitySimulator.agent.role.SocialNetworkerRole;

/**
 * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI
 * Builder, which is free for non-commercial use. If Jigloo is being used
 * commercially (ie, by a corporation, company or business for any purpose
 * whatever) then you should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details. Use of Jigloo implies
 * acceptance of these licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN
 * PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR
 * ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
public class MapGuiFrame extends javax.swing.JFrame implements
		ComponentListener, ActionListener {

	// FIXME: remove
	public int[][] specialMap;
	private double heatMapMaxValue;

	private JMenuBar menueBar;
	private JTextField txtAgentSize;
	private JPanel topPanel;
	private JPanel colorPanel;
	private JLabel jLabel2;
	private JPanel optionsPanel;
	private Canvas canvas;
	private JToggleButton cmdLinkToSim;
	private JProgressBar simProgress;
	private JLabel jLabel1;
	private JMenuItem closeMenItem;
	private JMenu fileMenue;

	private Map<String, Color> agentColors;
	private Map<String, JToggleButton> agentButtons;
	private Map<String, Color> eventColors;

	private int offset;
	// FIXME: only one argument for size -> only square maps allowed!
	private int size;
	private int border;

	private boolean showWifiRange;
	private boolean showVisualRange;
	private int wifiRadius = AbstractAgent.WIFI_RANGE;
	private int visualRadius = AbstractAgent.VISUAL_RANGE;

	private int mapWidth, mapHeight;
	private JCheckBox ckbShowWifiRange;
	private JCheckBox ckbShowVisualRange;
	private JButton cmdInterrupt;
	private JLabel lblElapsedTime;
	private JSlider sliderSpeed;
	private JLabel lblSpeed;
	private JPanel jPanel1;
	private JLabel lblactiveAgents;
	private JLabel jLabel4;
	private JTextField txtRefreshSpeed;
	private JLabel jLabel3;

	private Graphics2D g2;

	private BufferedImage obstacleMap;

	private boolean connected;

	private Timer refreshTimer;
	private Timer statusTimer;

	private int agentSize;
	private int refreshSpeed;

	{
		// Set Look & Feel
		try {
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager
					.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Auto-generated main method to display this JFrame
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MapGuiFrame inst = new MapGuiFrame();
				inst.setLocationRelativeTo(null);
				inst.setVisible(true);
			}
		});
	}

	public MapGuiFrame() {
		super();
		agentSize = 4;
		refreshSpeed = 900;
		initGUI();
		g2 = (Graphics2D) canvas.getGraphics();
		agentColors = new HashMap<String, Color>();
		agentColors.put("inactive", new Color(98, 98, 98, 255));
		eventColors = new HashMap<String, Color>();
		agentButtons = new HashMap<String, JToggleButton>();
		refreshTimer = new Timer(0, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				refreshCanvas();
			}
		});
		refreshTimer.setDelay(refreshSpeed);
		statusTimer = new Timer(400, new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Scheduler scheduler = Scheduler.getInstance();
				simProgress.setValue(scheduler.getProgress());
				lblactiveAgents.setText(Scheduler.agentRepository
						.getActiveAgentCount()
						+ "/"
						+ Scheduler.agentRepository.getPassiveAgentCount());

				lblElapsedTime.setText(Math.round(scheduler
						.getSecondsPerCycle() * scheduler.getCycle() / 6f)
						/ 10f + " min");
			}
		});
		statusTimer.start();

		canvas.addComponentListener(this);
		txtRefreshSpeed.addActionListener(this);
		txtAgentSize.addActionListener(this);
	}

	private void initGUI() {
		try {
			BorderLayout thisLayout = new BorderLayout();
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			getContentPane().setLayout(thisLayout);
			{
				optionsPanel = new JPanel();
				BoxLayout colorPanelLayout = new BoxLayout(optionsPanel,
						javax.swing.BoxLayout.Y_AXIS);
				optionsPanel.setLayout(colorPanelLayout);
				getContentPane().add(optionsPanel, BorderLayout.EAST);
				{
					colorPanel = new JPanel();
					BoxLayout colorPanelLayout1 = new BoxLayout(colorPanel,
							javax.swing.BoxLayout.Y_AXIS);
					colorPanel.setLayout(colorPanelLayout1);
					optionsPanel.add(colorPanel);
					{
						jLabel1 = new JLabel();
						colorPanel.add(jLabel1);
						jLabel1.setText("Agents:");
					}
				}
			}
			{
				canvas = new Canvas();
				getContentPane().add(canvas, BorderLayout.CENTER);
			}
			{
				topPanel = new JPanel();
				FlowLayout topPanelLayout = new FlowLayout();
				topPanelLayout.setAlignment(FlowLayout.LEFT);
				topPanelLayout.setHgap(10);
				topPanel.setLayout(topPanelLayout);
				getContentPane().add(topPanel, BorderLayout.NORTH);
				{
					cmdLinkToSim = new JToggleButton();
					topPanel.add(cmdLinkToSim);
					cmdLinkToSim.setText("Enable GUI");
					cmdLinkToSim.setSelected(false);
					cmdLinkToSim.setActionCommand("linkToScheduler");
					cmdLinkToSim.setPreferredSize(new java.awt.Dimension(300,
							22));
					cmdLinkToSim.addActionListener(this);
				}
				{
					jLabel2 = new JLabel();
					topPanel.add(jLabel2);
					jLabel2.setText("Agent Size:");
				}
				{
					txtAgentSize = new JTextField();
					topPanel.add(txtAgentSize);
					txtAgentSize.setText(agentSize + "");
					txtAgentSize.setActionCommand("agentSize");
					txtAgentSize.setText("10");
					txtAgentSize
							.setPreferredSize(new java.awt.Dimension(50, 22));
				}
				{
					jLabel3 = new JLabel();
					topPanel.add(jLabel3);
					jLabel3.setText("GUI Speed:");
				}
				{
					txtRefreshSpeed = new JTextField();
					topPanel.add(txtRefreshSpeed);
					txtRefreshSpeed.setText("" + refreshSpeed);
					txtRefreshSpeed.setPreferredSize(new java.awt.Dimension(50,
							22));
					txtRefreshSpeed.setActionCommand("refreshSpeed");
				}
				{
					ckbShowWifiRange = new JCheckBox();
					topPanel.add(ckbShowWifiRange);
					ckbShowWifiRange.setText("show Wifi range");
				}
				{
					ckbShowVisualRange = new JCheckBox();
					topPanel.add(ckbShowVisualRange);
					ckbShowVisualRange.setText("show Visual range");
				}
				{
					jLabel4 = new JLabel();
					topPanel.add(jLabel4);
					jLabel4.setText("ActiveAgents:");
				}
				{
					lblactiveAgents = new JLabel();
					topPanel.add(lblactiveAgents);
					lblactiveAgents.setText("0");
				}
			}
			{
				jPanel1 = new JPanel();
				BoxLayout jPanel1Layout = new BoxLayout(jPanel1,
						javax.swing.BoxLayout.X_AXIS);
				jPanel1.setLayout(jPanel1Layout);
				getContentPane().add(jPanel1, BorderLayout.SOUTH);
				{
					simProgress = new JProgressBar();
					jPanel1.add(simProgress);
					simProgress
							.setMaximumSize(new java.awt.Dimension(32767, 20));
					simProgress.setMinimumSize(new java.awt.Dimension(10, 20));
					simProgress.setPreferredSize(new java.awt.Dimension(1382,
							15));
					simProgress.setSize(1054, 20);
				}
				{
					lblElapsedTime = new JLabel();
					jPanel1.add(lblElapsedTime);
					lblElapsedTime.setText("0 min");
					lblElapsedTime.setPreferredSize(new java.awt.Dimension(97,
							16));
				}
				{
					sliderSpeed = new JSlider();
					jPanel1.add(sliderSpeed);
					sliderSpeed.setMinimum(-1000);
					sliderSpeed.setMaximum(1000);
					sliderSpeed
							.setValue((int) (-1000f
									* Scheduler.getInstance()
											.getSimulationSpeed() + 1000f));
					sliderSpeed.addChangeListener(new ChangeListener() {
						public void stateChanged(ChangeEvent evt) {
							Scheduler.getInstance().setSimulationSpeed(
									(-sliderSpeed.getValue() / 1000.f) + 1f);
							lblSpeed.setText((int) (-1000f
									* Scheduler.getInstance()
											.getSimulationSpeed() + 1000f)
									+ "");
						}
					});
				}
				{
					lblSpeed = new JLabel();
					jPanel1.add(lblSpeed);
					lblSpeed.setPreferredSize(new java.awt.Dimension(87, 16));
					lblSpeed.setText((int) (-1000f
							* Scheduler.getInstance().getSimulationSpeed() + 1000f)
							+ "");
				}
				{
					cmdInterrupt = new JButton();
					jPanel1.add(cmdInterrupt);
					cmdInterrupt.setText("Interrupt");
					cmdInterrupt.setActionCommand("interrupt");
					cmdInterrupt.addActionListener(this);
				}
			}
			{
				menueBar = new JMenuBar();
				setJMenuBar(menueBar);
				{
					fileMenue = new JMenu();
					menueBar.add(fileMenue);
					fileMenue.setText("File");
					{
						closeMenItem = new JMenuItem();
						fileMenue.add(closeMenItem);
						closeMenItem.setText("Close");
					}
				}
			}
			this.setPreferredSize(new java.awt.Dimension(1400, 1200));
			pack();
			this.setSize(1101, 1020);
		} catch (Exception e) {
			// add your error handling code here
			e.printStackTrace();
		}
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
		canvas.setEnabled(connected);

		if (connected) {
			this.mapWidth = DiscreteMap.getInstance().getSizeX();
			this.mapHeight = DiscreteMap.getInstance().getSizeY();
			calculateOffset();
			refreshTimer.start();
		} else {
			refreshTimer.stop();
		}
	}

	public boolean isConnected() {
		return connected;
	}

	private void refreshCanvas() {

		/*
		 * MAP
		 */
		if (specialMap == null) {
			if (obstacleMap == null) {
				obstacleMap = new BufferedImage(mapWidth * offset, mapHeight
						* offset, BufferedImage.TYPE_INT_RGB);

				int[] obstacles1D = new int[mapWidth * mapHeight * offset
						* offset];

				short[][] obstacles2D = DiscreteMap.getInstance()
						.getObstacles();

				// short[][] obstacles2D = DiscreteMap.getInstance()
				// .getAdditionalCahnnel();

				int innerCounter = 0;
				int color = 0;

				for (int i = 0; i < obstacles2D.length; i++) {

					for (int l = 0; l < offset; l++) {

						for (int j = 0; j < obstacles2D[i].length; j++) {

							for (int k = 0; k < offset; k++) {

								if (obstacles2D[i][j] > 100)
									color = Color.LIGHT_GRAY.getBlue();
								else
									color = Color.GRAY.getBlue();

								// color = obstacles2D[i][j];

								obstacles1D[innerCounter] = (color << 16)
										| (color << 8) | color;
								innerCounter++;

							}
						}
					}
				}

				obstacleMap.setRGB(0, 0, mapWidth * offset, mapHeight * offset,
						obstacles1D, 0, mapWidth * offset);
				g2.drawImage(obstacleMap, 0, 0, canvas);
			} else {
				g2.drawImage(obstacleMap, 0, 0, canvas);
			}
		} else {
			if (obstacleMap == null) {
				obstacleMap = new BufferedImage(mapWidth * offset, mapHeight
						* offset, BufferedImage.TYPE_INT_RGB);

				int[] obstacles1D = new int[mapWidth * mapHeight * offset
						* offset];

				int[][] obstacles2D = specialMap;
				int innerCounter = 0;
				int red, green, blue;

				for (int i = 0; i < obstacles2D.length; i++) {

					for (int l = 0; l < offset; l++) {

						for (int j = 0; j < obstacles2D[i].length; j++) {

							for (int k = 0; k < offset; k++) {

								Color c = getHeatmapColorFromValue(obstacles2D[i][j]);
								red = c.getRed();
								green = c.getGreen();
								blue = c.getBlue();
								obstacles1D[innerCounter] = (red << 16)
										| (green << 8) | blue;
								innerCounter++;

							}
						}
					}
				}

				obstacleMap.setRGB(0, 0, mapWidth * offset, mapHeight * offset,
						obstacles1D, 0, mapWidth * offset);
				g2.drawImage(obstacleMap, 0, 0, canvas);
			} else {
				g2.drawImage(obstacleMap, 0, 0, canvas);
			}
			// return;
		}

		/*
		 * GRID
		 */
		g2.setPaint(new Color(115, 115, 115, 255));
		if (offset > 8) {
			for (int i = 0; i < mapWidth * offset; i = i + offset) {
				g2.drawLine(i, 0, i, mapHeight * offset - 1);
			}
			for (int i = 0; i < mapHeight * offset; i = i + offset) {
				g2.drawLine(0, i, mapWidth * offset - 1, i);
			}
		}

		Composite original = g2.getComposite();

		/*
		 * Events
		 */
		AbstractMapEvent event;
		synchronized (Scheduler.mapEventRepository) {

			for (Iterator<AbstractMapEvent> iterator = Scheduler.mapEventRepository
					.values().iterator(); iterator.hasNext();) {
				event = iterator.next();
				if (event instanceof LinkedRTreeFireMapEvent)
					g2.setPaint(Color.RED);
				else if (event instanceof LinkedRTreeCollapsedBuildingMapEvent)
					g2.setPaint(Color.ORANGE);

				// BasicStroke stroke1
				// = new BasicStroke(2.0f, BasicStroke.CAP_BUTT,
				// BasicStroke.JOIN_MITER);
				// g2.setStroke(stroke1);

				AlphaComposite transparancy1 = AlphaComposite.getInstance(
						AlphaComposite.SRC_OVER, 0.2f);
				AlphaComposite transparancy2 = AlphaComposite.getInstance(
						AlphaComposite.SRC_OVER, 0.1f);

				int rad;

				if (event instanceof LinkedRTreeFireMapEvent) {
					rad = ((LinkedRTreeFireMapEvent) event).getDamageArea()
							.getRadius();
					g2.setComposite(transparancy1);
					g2.fillOval((event.getX() - rad) * offset,
							(event.getY() - rad) * offset, (2 * rad) * offset,
							(2 * rad) * offset);
				}

				g2.setComposite(transparancy2);

				rad = ((CircleInterface) ((LinkedRTreeMapEvent) event)
						.getImpactArea()).getRadius();
				g2.fillOval((event.getX() - rad) * offset, (event.getY() - rad)
						* offset, (2 * rad) * offset, (2 * rad) * offset);

				g2.drawOval((event.getX() - rad) * offset, (event.getY() - rad)
						* offset, (2 * rad) * offset, (2 * rad) * offset);

				g2.setComposite(original);
				g2.drawLine(event.getX() * offset - 4, event.getY() * offset,
						event.getX() * offset + 4, event.getY() * offset);
				g2.drawLine(event.getX() * offset, event.getY() * offset - 4,
						event.getX() * offset, event.getY() * offset + 4);

			}
			g2.setComposite(original);
		}

		/*
		 * Agents
		 */
		AbstractAgent agent;
		synchronized (Scheduler.agentRepository) {

			// Paint passive Agents
			g2.setPaint(agentColors.get("inactive"));
			for (Iterator iterator = Scheduler.agentRepository
					.getPassiveAgents().values().iterator(); iterator.hasNext();) {
				agent = (AbstractAgent) iterator.next();
				g2.fillOval(agent.getX() * offset, agent.getY() * offset,
						agentSize * offset, agentSize * offset);
			}

			showWifiRange = ckbShowWifiRange.isSelected();
			showVisualRange = ckbShowVisualRange.isSelected();

			// Paint active agents
			for (Iterator<AbstractAgent> iterator = Scheduler.agentRepository
					.values().iterator(); iterator.hasNext();) {
				agent = iterator.next();

				if (!agentColors.containsKey(agent.getRoleName())) {
					JToggleButton tgb = new JToggleButton(agent.getRoleName());
					tgb.setBackground(agent.getRole().getPreferedColor());
					tgb.setFont(new java.awt.Font("Tahoma", 1, 13));
					tgb.setForeground(agent.getRole().getPreferedColor());
					colorPanel.add(tgb);
					colorPanel.revalidate();
					agentButtons.put(agent.getRoleName(), tgb);
					agentColors.put(agent.getRoleName(), agent.getRole()
							.getPreferedColor());
				}

				g2.setPaint(agentColors.get(agent.getRoleName()));

				g2.fillOval(agent.getX() * offset, agent.getY() * offset,
						agentSize * offset, agentSize * offset);

				if (showWifiRange && agent.isMobileCommunicationEnabled()) {
					g2.drawOval((agent.getX() - wifiRadius) * offset,
							(agent.getY() - wifiRadius) * offset, wifiRadius
									* 2 * offset, wifiRadius * 2 * offset);
				}

				if (showVisualRange) {
					g2.drawOval((agent.getX() - visualRadius) * offset,
							(agent.getY() - visualRadius) * offset,
							visualRadius * 2 * offset, visualRadius * 2
									* offset);
				}

				// Now draw the friendship connections
				// TODO, Generalize this so we can run other agents in roles besides our own

				SocialNetworkerRole socialNetRole = (SocialNetworkerRole) agent.getRole();


				//System.out.println("Number of friends : "+socialNetRole.getFriends().size());

				for(AbstractAgent friend : socialNetRole.getFriends()) {
					g2.drawLine(agent.getX(), agent.getY(), friend.getX(), friend.getY());
				}
				// /*
				// * Icons
				// */
				// if(agent.getRole() instanceof FireEngineRole ){
				// try {
				// g2.setComposite(AlphaComposite.getInstance(
				// AlphaComposite.SRC_OVER, 0.5f));
				// BufferedImage icon = ImageIO.read(new
				// File("icons\\fireEngine.png"));
				// g2.drawImage(icon, agent.getX()*offset, agent.getY()*offset,
				// this);
				// g2.setComposite(original);
				// } catch (IOException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// }
				// }
			}

		}
	}

	protected void calculateOffset() {
		if (this.mapHeight > 0 && this.mapWidth > 0) {
			int offsetX = (canvas.getSize().width - 2) / this.mapWidth;
			int offsetY = (canvas.getSize().height - 2) / this.mapHeight;

			offset = offsetX < offsetY ? offsetX : offsetY;
			if (offset <= 0)
				offset = 1;
			border = offset * mapWidth;
		}
	}

	private Color getHeatmapColorFromValue(double val) {

		if (val == -1)
			return Color.BLACK;

		val = 1.0 - (val / heatMapMaxValue);

		Color c = Color.getHSBColor((float) val * 240f / 360f, 1, 1);
		return c;
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void componentResized(ComponentEvent e) {
		// Assuming, that canvas is the only component this is bound to.
		// This means that only a resize of canvas calls this method
		calculateOffset();
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("linkToScheduler")) {
			setConnected(cmdLinkToSim.isSelected());
		} else if (e.getActionCommand().equals("agentSize")) {
			if (new Integer("0" + txtAgentSize.getText()) > 0)
				agentSize = new Integer(txtAgentSize.getText());
		} else if (e.getActionCommand().equals("refreshSpeed")) {
			if (new Integer(txtRefreshSpeed.getText()) > 10) {
				refreshSpeed = new Integer(txtRefreshSpeed.getText());
				refreshTimer.setDelay(refreshSpeed);
			}
		} else if (e.getActionCommand().equals("interrupt")) {
			Scheduler.getInstance().interrupt();
		}
	}

	public void setMap(int[][] specialMap) {
		this.specialMap = specialMap;
		double maxVal = 0;

		for (int i = 0; i < specialMap.length; i++) {
			for (int j = 0; j < specialMap[i].length; j++) {
				if (specialMap[i][j] > maxVal)
					maxVal = specialMap[i][j];
			}
		}
		heatMapMaxValue = maxVal - 100;
	}

	@Override
	public void setVisible(boolean b) {
		if (!b) {
			if (refreshTimer != null)
				refreshTimer.stop();
			if (statusTimer != null)
				statusTimer.stop();
			statusTimer = null;
			refreshTimer = null;
		}
		super.setVisible(b);
	}
}
