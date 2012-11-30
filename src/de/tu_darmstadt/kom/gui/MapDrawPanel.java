package de.tu_darmstadt.kom.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.tu_darmstadt.kom.linkedRTree.Rectangle;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractRole;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;
import de.tu_darmstadt.kom.mobilitySimulator.core.mapEvent.MapEventTypeInterface;

@Deprecated
public class MapDrawPanel extends javax.swing.JPanel implements
		ComponentListener {

	private static final long serialVersionUID = -2851691913621160244L;

	private boolean active;

	protected int offset;
	// FIXME: only one argument for size -> only square maps allowed!
	protected int size;
	private int border;

	int radius = 0;
	AbstractAgent activeA = null;
	Rectangle rectangle;
	int thickness = 0;
	Color agentColor = Color.BLACK;
	float[][] overlayMap = null;
	short[][] obstacleMap = null;
	float heatMapMaxValue = 2000;

	int sizeX, sizeY;

	HashMap<AbstractRole, Color> colorsAgents;
	HashMap<MapEventTypeInterface, Color> colorsEvents;

	private boolean first = true;

	{
		// Set Look & Feel
		try {
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager
					.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public MapDrawPanel() {
		super();
		active = false;
	}

	public void activate(int sizeX, int sizeY) {
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		addComponentListener(this);
		calculateOffset();

	}

	public void activate() {
		this.sizeX = getWidth();
		this.sizeY = getHeight();
		addComponentListener(this);
		calculateOffset();
	}

	@Override
	protected void paintComponent(Graphics g) {

		if (first) {
			first = false;
			System.err.println("PaintThread: \t"
					+ Thread.currentThread().getName());
		}

		super.paintComponent(g);

		Graphics2D g2 = (Graphics2D) g;

		// if (overlayMap != null) {
		// for (int i = 0; i < overlayMap.length; i++) {
		// for (int j = 0; j < overlayMap[i].length; j++) {
		// if (obstacleMap == null) {
		// g2.setPaint(getHeatmapColorFromValue(overlayMap[i][j]));
		// } else {
		// g2.setPaint(getHeatmapColorFromValue(overlayMap[i][j]
		// + obstacleMap[i][j]));
		// }
		// g2.fillRect(j * offset, i * offset, offset, offset);
		// }
		// }
		// }

		if (obstacleMap != null) {
			for (int i = 0; i < obstacleMap.length; i++) {
				for (int j = 0; j < obstacleMap[i].length; j++) {

					if (obstacleMap[i][j] == 0)
						g2.setPaint(Color.GRAY);
					else
						g2.setPaint(Color.LIGHT_GRAY);

					g2.fillRect(j * offset, i * offset, offset, offset);
				}
			}
		}

		if (active) {

			Map<Integer, AbstractAgent> agents;
			try {
				agents = Scheduler.agentRepository;

				Collection<? extends AbstractAgent> agentsCollection = agents
						.values();
				for (AbstractAgent a : agentsCollection) {
					if (a.getRole().getName().equals("FRRole"))
						g2.setPaint(Color.BLUE);
					else if (a.getRole().getName().equals("VictimRole")) {
						g2.setPaint(Color.RED);
					} else if (a.getRole().getName().equals("FleeRole")) {
						g2.setPaint(Color.YELLOW);
					} else
						g2.setPaint(Color.BLACK);

					g2.fillOval(a.getX() * offset, a.getY() * offset,
							thickness, thickness);
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Repaints the complete Map
	 */
	protected void repaintMap() {
		// Repaint the complete Map
		if (getParent() != null) {
			getParent()
					.repaint(getLocation().x, getLocation().y,
							getLocation().x + getWidth(),
							getLocation().y + getHeight());
		}
	}

	private Color getHeatmapColorFromValue(double val) {

		if (val == -1)
			return Color.BLACK;

		val = 1.0 - (val / heatMapMaxValue);

		Color c = Color.getHSBColor((float) val * 240f / 360f, 1, 1);
		return c;
	}

	public void setRectangle(Rectangle rectangle) {
		this.rectangle = rectangle;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public void setActiveA(AbstractAgent activeA) {
		this.activeA = activeA;
	}

	public void setThickness(int thickness) {
		this.thickness = thickness;
	}

	public void setAgentColor(Color agentColor) {
		this.agentColor = agentColor;
	}

	public void setOverlayMap(float[][] overlayMap) {
		this.overlayMap = overlayMap;
	}

	public void setHeatMapMaxValue(float heatMapMaxValue) {
		this.heatMapMaxValue = heatMapMaxValue;
	}

	public float getHeatMapMaxValue() {
		return heatMapMaxValue;
	}

	public void setObstacleMap(short[][] obstacleMap) {
		this.obstacleMap = obstacleMap;
	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentResized(ComponentEvent e) {
		calculateOffset();
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}

	private void calculateOffset() {
		int offsetX = (getSize().width - 2) / this.sizeX;
		int offsetY = (getSize().height - 2) / this.sizeY;

		offset = offsetX < offsetY ? offsetX : offsetY;
		if (offset <= 0)
			offset = 1;

		border = offset * sizeX;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

}