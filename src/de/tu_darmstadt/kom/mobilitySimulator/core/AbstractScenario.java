package de.tu_darmstadt.kom.mobilitySimulator.core;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.swing.SwingUtilities;

import de.tu_darmstadt.kom.gui.MapGuiFrame;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgentRepository;
import de.tu_darmstadt.kom.mobilitySimulator.core.map.DiscreteMap;
import de.tu_darmstadt.kom.mobilitySimulator.core.map.MapFactory;
import de.tu_darmstadt.kom.mobilitySimulator.core.mapEvent.AbstractMapEventRepository;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.SchedulerHookInterface;
import de.tu_darmstadt.kom.mobilitySimulator.core.watchdog.Watchdog;
import de.tu_darmstadt.kom.mobilitySimulator.output.DummyOutput;

public abstract class AbstractScenario implements SchedulerHookInterface {

	protected abstract String getScenarioName();

	protected abstract OutputInterface getOutput();

	/**
	 * The absolute or relative path to the mapfile including the filename and
	 * extension
	 * 
	 * @return
	 */
	protected abstract String getMapFile();

	protected abstract MapFactory getMapFactory();

	protected abstract AbstractAgentRepository getAgentRepository();

	protected abstract AbstractMapEventRepository getMapEventRepository();

	protected abstract int getSimulationLenght();

	protected abstract int getSecondsPerCycle();

	protected abstract float getSimulationSpeed();

	MapGuiFrame gui;

	public void run(int seed) {
		System.out
				.println("MobilitySimulator: de.tu-darmstadt.kom.mobilitySimulator.scenario "
						+ getScenarioName());
		Date dt = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		System.out.println("Date: " + df.format(dt));
		System.out
				.println("_____________________________________________________________");

		/*
		 * set seed
		 */
		Scheduler.rand = new Random(seed);
		System.out.println("Seed: " + seed);

		/*
		 * load map
		 */
		DiscreteMap map = null;
		try {
			DiscreteMap.init(getMapFile());
			map = DiscreteMap.getInstance();
			System.out.println("Map acquired");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(0);
		}

		/*
		 * Set output
		 */
		OutputInterface output = getOutput();
		if (output == null)
			output = new DummyOutput();

		/*
		 * init Scheduler
		 */
		Scheduler scheduler = Scheduler.getInstance();

		scheduler.setOutput(output);

		getMapFactory().setAsMapFactory();

		Scheduler.agentRepository = getAgentRepository();
		Scheduler.mapEventRepository = getMapEventRepository();

		scheduler.setSimulationLenght(getSimulationLenght());
		scheduler.setSimulationSpeed(getSimulationSpeed());
		scheduler.setSecondsPerCycle(getSecondsPerCycle());
		System.out.println("Time to be simulated: "
				+ scheduler.getSimulationLenght() + " sec");

		scheduler.register(this);

		/*
		 * Show GUI
		 */

		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				gui = new MapGuiFrame();
				gui.setLocationRelativeTo(null);
				gui.setVisible(true);
			}
		});

		/*
		 * Start Watchdog
		 */
		Watchdog.getInstance();

		/*
		 * Run simulation
		 */
		scheduler.run();

		if (gui != null)
			gui.setVisible(false);
		else
			System.err.println("GUI MISS!!!");
		
		scheduler.getOutput().close();
	}
}
