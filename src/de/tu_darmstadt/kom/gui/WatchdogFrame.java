package de.tu_darmstadt.kom.gui;

import java.awt.Canvas;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import de.tu_darmstadt.kom.mobilitySimulator.core.watchdog.Watchdog;

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
public class WatchdogFrame extends javax.swing.JFrame {
	private JLabel jLabel1;
	private JProgressBar progMemory;

	private long totalMemory;

	private JLabel lblFree;
	private JLabel jLabel3;
	private Canvas cvsState;
	private JLabel jLabel4;
	private JLabel lblCycle;
	private JTextPane txtStatus;
	private JLabel lblTotal;
	private JLabel jLabel2;

	{
		// Set Look & Feel
		try {
			javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager
					.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public WatchdogFrame() {
		super();
		initGUI();
	}

	public void setListeners(String txt) {
		txtStatus.setText(txt);
	}

	public void setTotalMemory(long totalMemory) {
		this.totalMemory = totalMemory;
		lblTotal.setText((totalMemory / 1000000) + "Mb (max "
				+ Runtime.getRuntime().maxMemory() + ")");
		progMemory.setMaximum((int) totalMemory);
	}

	public void setCycle(long cycle) {
		lblCycle.setText(cycle + "");
	}

	public void setFreeMemory(long freeMemory) {
		lblFree.setText((freeMemory / 1000000) + "Mb ("
				+ (int) (freeMemory * 100 / (double) totalMemory) + "%)");
		progMemory.setValue((int) (totalMemory - freeMemory));
	}

	public void setState(short state) {
		switch (state) {
		case Watchdog.MEMORY_NORMAL:
			cvsState.setBackground(Color.GREEN);
			break;
		case Watchdog.MEMORY_WARNING:
			cvsState.setBackground(Color.ORANGE);
			UIManager.put("JProgressBar.selectionBackground", Color.ORANGE);
			break;
		case Watchdog.MEMORY_ALERT:
			cvsState.setBackground(Color.RED);
			break;
		}
	}

	private void initGUI() {
		try {
			getContentPane().setLayout(null);
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			{
				jLabel1 = new JLabel();
				getContentPane().add(jLabel1);
				jLabel1.setText("Memory usage:");
				jLabel1.setBounds(12, 12, 89, 16);
				jLabel1.setOpaque(true);
			}
			{
				progMemory = new JProgressBar();
				getContentPane().add(progMemory);
				progMemory.setBounds(113, 14, 462, 14);
				progMemory.setValue(20);
			}
			{
				jLabel2 = new JLabel();
				getContentPane().add(jLabel2);
				jLabel2.setText("Total Memory:");
				jLabel2.setBounds(12, 41, 85, 16);
			}
			{
				lblTotal = new JLabel();
				getContentPane().add(lblTotal);
				lblTotal.setText("total");
				lblTotal.setBounds(113, 41, 112, 16);
				lblTotal.setFont(new java.awt.Font("Tahoma", 2, 13));
			}
			{
				jLabel3 = new JLabel();
				getContentPane().add(jLabel3);
				jLabel3.setText("Free Memory:");
				jLabel3.setBounds(12, 64, 85, 16);
			}
			{
				lblFree = new JLabel();
				getContentPane().add(lblFree);
				lblFree.setText("free");
				lblFree.setBounds(113, 64, 112, 16);
				lblFree.setFont(new java.awt.Font("Tahoma", 2, 13));
			}
			{
				cvsState = new Canvas();
				getContentPane().add(cvsState);
				cvsState.setBounds(582, 13, 17, 15);
				cvsState.setSize(15, 15);
			}
			{
				txtStatus = new JTextPane();
				getContentPane().add(txtStatus);
				txtStatus.setText("listeners");
				txtStatus.setBounds(230, 43, 345, 450);
				txtStatus.setBackground(new java.awt.Color(240, 240, 240));
				txtStatus
						.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
				txtStatus.setEditable(false);
			}
			{
				jLabel4 = new JLabel();
				getContentPane().add(jLabel4);
				jLabel4.setText("Cycle:");
				jLabel4.setBounds(12, 93, 85, 16);
			}
			{
				lblCycle = new JLabel();
				getContentPane().add(lblCycle);
				lblCycle.setText("Cycle");
				lblCycle.setFont(new java.awt.Font("Tahoma", 2, 13));
				lblCycle.setBounds(113, 93, 112, 16);
			}
			pack();
			this.setSize(627, 551);
		} catch (Exception e) {
			// add your error handling code here
			e.printStackTrace();
		}
	}

}
