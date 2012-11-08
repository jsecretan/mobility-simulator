package de.tu_darmstadt.kom.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

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
public class MainFrame extends javax.swing.JFrame {

	{
		// Set Look & Feel
		try {
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager
					.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private JScrollBar scrBarSpeed;
	private JButton jButton1;
	private JLabel lblOutputPath;
	private MapDrawPanel mapGui;
	private JLabel lblSpeed;
	private JCheckBox chkGuiEnabled;
	private JTextField txtPath;

	// /**
	// * Auto-generated main method to display this JFrame
	// */
	// public static void main(String[] args) {
	// SwingUtilities.invokeLater(new Runnable() {
	// public void run() {
	// Frame inst = new Frame();
	// inst.setLocationRelativeTo(null);
	// inst.setVisible(true);
	// }
	// });
	// }

	public MainFrame() {
		super();
		initGUI();
	}

	private void initGUI() {
		try {
			BorderLayout thisLayout = new BorderLayout();
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			getContentPane().setLayout(thisLayout);
			{
				final JPanel jPanel1 = new JPanel();
				BoxLayout jPanel1Layout = new BoxLayout(jPanel1,
						javax.swing.BoxLayout.X_AXIS);
				jPanel1.setLayout(jPanel1Layout);
				getContentPane().add(jPanel1, BorderLayout.NORTH);
				{
					scrBarSpeed = new JScrollBar();
					jPanel1.add(scrBarSpeed);
					scrBarSpeed.setOrientation(SwingConstants.HORIZONTAL);
					scrBarSpeed
							.setPreferredSize(new java.awt.Dimension(377, 21));
				}
				{
					lblSpeed = new JLabel();
					jPanel1.add(lblSpeed);
					lblSpeed.setText("Delay:");
					lblSpeed.setPreferredSize(new java.awt.Dimension(85, 16));
				}
				{
					jButton1 = new JButton();
					jPanel1.add(jButton1);
					jButton1.setText("Start Simulation");
				}
				{
					lblOutputPath = new JLabel();
					jPanel1.add(lblOutputPath);
					lblOutputPath.setText(" Output path: ");
				}
				{
					txtPath = new JTextField();
					jPanel1.add(txtPath);
				}
				{
					chkGuiEnabled = new JCheckBox();
					jPanel1.add(chkGuiEnabled);
					chkGuiEnabled.setText("Gui enabled");
					chkGuiEnabled.setSelected(true);
				}
			}
			{
				mapGui = new MapDrawPanel();
				getContentPane().add(mapGui, BorderLayout.CENTER);
			}
			pack();
			this.setPreferredSize(new Dimension(1000, 1000));
			mapGui.activate();
			mapGui.setActive(true);
		} catch (Exception e) {
			// add your error handling code here
			e.printStackTrace();
		}
	}

	public MapDrawPanel getMapGui() {
		return mapGui;
	}

}
