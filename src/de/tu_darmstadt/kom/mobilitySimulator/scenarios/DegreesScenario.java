package de.tu_darmstadt.kom.mobilitySimulator.scenarios;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.sf.ehcache.CacheManager;

import de.tu_darmstadt.kom.linkedRTree.Rectangle;
import de.tu_darmstadt.kom.mobilitySimulator.agent.role.AmbulanceRole;
import de.tu_darmstadt.kom.mobilitySimulator.agent.role.EarthquakeVictimRole;
import de.tu_darmstadt.kom.mobilitySimulator.agent.role.FireEngineRole;
import de.tu_darmstadt.kom.mobilitySimulator.agent.role.NormalRole;
import de.tu_darmstadt.kom.mobilitySimulator.agent.role.RescueVehicleRole;
import de.tu_darmstadt.kom.mobilitySimulator.core.AbstractScenario;
import de.tu_darmstadt.kom.mobilitySimulator.core.OutputInterface;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgentRepository;
import de.tu_darmstadt.kom.mobilitySimulator.core.map.DiscreteMap;
import de.tu_darmstadt.kom.mobilitySimulator.core.map.MapFactory;
import de.tu_darmstadt.kom.mobilitySimulator.core.mapEvent.AbstractMapEvent;
import de.tu_darmstadt.kom.mobilitySimulator.core.mapEvent.AbstractMapEventRepository;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;
import de.tu_darmstadt.kom.mobilitySimulator.core.watchdog.Watchdog;
import de.tu_darmstadt.kom.mobilitySimulator.linkedRTree.LinkedRTreeAgentRepository;
import de.tu_darmstadt.kom.mobilitySimulator.linkedRTree.LinkedRTreeCollapsedBuildingMapEvent;
import de.tu_darmstadt.kom.mobilitySimulator.linkedRTree.LinkedRTreeFireMapEvent;
import de.tu_darmstadt.kom.mobilitySimulator.linkedRTree.LinkedRTreeMapEventRepository;
import de.tu_darmstadt.kom.mobilitySimulator.map.EHCacheMapFactory;
import de.tu_darmstadt.kom.mobilitySimulator.output.AgentDensityEvaluationOutput;
import de.tu_darmstadt.kom.mobilitySimulator.output.GenericFileOutput;
import de.tu_darmstadt.kom.mobilitySimulator.output.VictimOutput;

public class DegreesScenario extends AbstractScenario {

	private int initialSeed = 123456;
	/** Do multiple simulations with different seeds after each other */
	private boolean doMultipleRuns = false;
	private int amountOfSimulationRuns = 20;

	/** Total number of agents (nodes) on the map. */
	private int numberOfAgents = 200;
	private int percentageOfVictims = 10;

	private int percentageOfMobileComEnabledAgents = 50;

	/** Total simulation length (in seconds). */
	private int simulationLength = 60 * 60 * 3; // In seconds
	private float simulationSpeed = Scheduler.MAX_SPEED;

	/** Used map file. Also defines how large the map is. */
	private String mapFile = "frankfurt3.png";
	// private String mapFile = "frankfurt.png";

	private final File MAPS = new File("maps");
	private final File TRACES = new File("output/"+ ScenesScenario.class.getName());

	private Map<Rectangle, Integer> agentBuckets;

	@Override
	protected OutputInterface getOutput() {

		OutputInterface outputHelper = null;

		String prefix = numberOfAgents + "agents_" + simulationLength + "sec";
		int index = 0;

		File newTrace = new File(TRACES, prefix);

		// Generic File Output
		if (!TRACES.exists())
			if (!TRACES.mkdir())
				System.err.println("Cannot create folder "
						+ TRACES.getAbsolutePath());

		// Generic File Output
		if (!newTrace.exists())
			if (!newTrace.mkdir())
				System.err.println("Cannot create folder "
						+ newTrace.getAbsolutePath());

		System.out.println("Output Folder: " + newTrace);

		prefix = "";

		try {
			// show map size in filename might be good
			File outputFile = new File(newTrace, prefix + "trace.txt");

			while (!outputFile.createNewFile()) {
				index++;
				outputFile = new File(newTrace, prefix + "trace(" + index
						+ ").txt");
			}
			new Thread((GenericFileOutput) outputHelper);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Collection<String> specialAgents = new ArrayList<String>();
		// TODO: Should we use this for a special role?
		specialAgents.add("EarthquakeVictimRole");
		specialAgents.add("NormalRole");
		
		outputHelper = new AgentDensityEvaluationOutput(newTrace, index,
				prefix, specialAgents);

		outputHelper = new VictimOutput(newTrace, index, prefix, outputHelper);

		return outputHelper;
	}

	@Override
	protected String getMapFile() {
		return new File(MAPS, mapFile).getAbsolutePath();
	}

	@Override
	protected MapFactory getMapFactory() {
		return new EHCacheMapFactory();
	}

	@Override
	protected AbstractAgentRepository getAgentRepository() {
		return new LinkedRTreeAgentRepository(DiscreteMap.sizeX,
				DiscreteMap.sizeY, numberOfAgents, true);
	}

	@Override
	protected AbstractMapEventRepository getMapEventRepository() {
		return new LinkedRTreeMapEventRepository(DiscreteMap.sizeX,
				DiscreteMap.sizeY);
	}

	@Override
	protected int getSimulationLenght() {
		return simulationLength;
	}

	@Override
	protected int getSecondsPerCycle() {
		return 1;
	}

	@Override
	protected String getScenarioName() {
		return "Wireless Degree Separation Scenario";
	}

	@Override
	protected float getSimulationSpeed() {
		return simulationSpeed;
	}

	public DegreesScenario() {

		// Setup cache
		CacheManager.getInstance().addCache("largeMemoryNoDisk");

		initBuckets();

		/**********************
		 * PERSONAL CODE HERE *
		 **********************/

		doMultipleRuns = false;

		simulationLength = 60 * 60*3;

		percentageOfMobileComEnabledAgents = 50;

		simulationSpeed = 0.5f;

		numberOfAgents = 800;

		amountOfSimulationRuns = 20;

		if (doMultipleRuns) {
			System.err
					.println("Simulation started.");

			String prefix = numberOfAgents + "agents_" + simulationLength
					+ "sec";
			int index = 0;

			File newTrace = new File(TRACES, prefix);

			// Generic File Output
			if (!TRACES.exists())
				if (!TRACES.mkdir())
					System.err.println("Cannot create folder "
							+ TRACES.getAbsolutePath());

			// Generic File Output
			if (!newTrace.exists())
				if (!newTrace.mkdir())
					System.err.println("Cannot create folder "
							+ newTrace.getAbsolutePath());

			try {
				File resultFile = new File(newTrace, prefix + "_consoleLog.txt");

				while (!resultFile.createNewFile()) {
					index++;
					resultFile = new File(newTrace, prefix + "_consoleLog("
							+ index + ").txt");
				}
				System.setOut(new PrintStream(new FileOutputStream(resultFile,
						true), false));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.println("####### agents," + numberOfAgents
				+ "; run,1 - MobComEnabledPercentage,"
				+ percentageOfMobileComEnabledAgents + "; seed," + initialSeed
				+ " ##########");
		System.err.println("####### agents," + numberOfAgents
				+ "; run,1 - MobComEnabledPercentage,"
				+ percentageOfMobileComEnabledAgents + "; seed," + initialSeed
				+ " ##########");
		run(initialSeed);

		if (doMultipleRuns) {

			for (int i = 2; i <= amountOfSimulationRuns; i++) {

				// Reset Victim stuff;
				EarthquakeVictimRole.getKnown().clear();
				EarthquakeVictimRole.inRescueProc.clear();
				EarthquakeVictimRole.toRescue.clear();
				EarthquakeVictimRole.SECURED = 0;
				EarthquakeVictimRole.totalVictims.clear();
				Watchdog.clearInstance();
				Scheduler.clearInstance();
				// MapFactory.clearInstance();
				Runtime.getRuntime().gc();
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Watchdog.clearInstance();

				int seed = Scheduler.rand.nextInt();

				System.out.println("####### agents," + numberOfAgents
						+ "; run," + i + " - MobComEnabledPercentage,"
						+ percentageOfMobileComEnabledAgents + "; seed," + seed
						+ " ##########");
				System.err.println("####### agents," + numberOfAgents
						+ "; run," + i + " - MobComEnabledPercentage,"
						+ percentageOfMobileComEnabledAgents + "; seed," + seed
						+ " ##########");

				run(seed);
			}
		}

		System.out.println("All simulations terminated");

		System.exit(0);

	}

	@Override
	public void preRun() {

		// Scheduler.getInstance().setSimulationSpeed(0.5f);

		/*
		 * Agents
		 */
		AbstractAgent a = null;

		// Normal Agents
		for (int i = 0; i < numberOfAgents; i++) {
			a = createAgentInGreen((Scheduler.rand.nextInt(100)) < percentageOfMobileComEnabledAgents);
			// a = createAgentOnMap();
			// a = createAgentInBucket();

			if (Scheduler.rand.nextInt(100) < percentageOfVictims)
				a.setRole(new EarthquakeVictimRole(a));
			else
				a.setRole(new NormalRole(a));

			Scheduler.agentRepository.put(a);
			// AmbulanceRole.reportedVictims.add(a);
		}



		Scheduler.agentRepository.executePut();
		System.out.println(numberOfAgents + " agents initialized.");

		/*
		 * Events
		 */
		AbstractMapEvent e;

		// Fires
		e = new LinkedRTreeFireMapEvent(160, 310, 30, 0.3f);
		Scheduler.mapEventRepository.put(e.getId(), e);
		FireEngineRole.reportedFires.add(e);

		e = new LinkedRTreeFireMapEvent(540, 340, 30, 0.3f);
		Scheduler.mapEventRepository.put(e.getId(), e);
		FireEngineRole.reportedFires.add(e);

		e = new LinkedRTreeFireMapEvent(900, 700, 30, 0.3f);
		Scheduler.mapEventRepository.put(e.getId(), e);
		FireEngineRole.reportedFires.add(e);

		e = new LinkedRTreeFireMapEvent(1030, 190, 30, 0.3f);
		Scheduler.mapEventRepository.put(e.getId(), e);
		FireEngineRole.reportedFires.add(e);

		// Collapsed Buildings

		e = new LinkedRTreeCollapsedBuildingMapEvent(440, 55, 15, 0f);
		Scheduler.mapEventRepository.put(e.getId(), e);
		RescueVehicleRole.reportedEvents.add(e);

		e = new LinkedRTreeCollapsedBuildingMapEvent(370, 240, 15, 0f);
		Scheduler.mapEventRepository.put(e.getId(), e);
		RescueVehicleRole.reportedEvents.add(e);

		e = new LinkedRTreeCollapsedBuildingMapEvent(200, 560, 15, 0f);
		Scheduler.mapEventRepository.put(e.getId(), e);
		RescueVehicleRole.reportedEvents.add(e);

		e = new LinkedRTreeCollapsedBuildingMapEvent(445, 680, 15, 0f);
		Scheduler.mapEventRepository.put(e.getId(), e);
		RescueVehicleRole.reportedEvents.add(e);

		e = new LinkedRTreeCollapsedBuildingMapEvent(540, 560, 15, 0f);
		Scheduler.mapEventRepository.put(e.getId(), e);
		RescueVehicleRole.reportedEvents.add(e);

		e = new LinkedRTreeCollapsedBuildingMapEvent(790, 115, 15, 0f);
		Scheduler.mapEventRepository.put(e.getId(), e);
		RescueVehicleRole.reportedEvents.add(e);

		e = new LinkedRTreeCollapsedBuildingMapEvent(895, 355, 15, 0f);
		Scheduler.mapEventRepository.put(e.getId(), e);
		RescueVehicleRole.reportedEvents.add(e);

		e = new LinkedRTreeCollapsedBuildingMapEvent(790, 75, 15, 0f);
		Scheduler.mapEventRepository.put(e.getId(), e);
		RescueVehicleRole.reportedEvents.add(e);

		Scheduler.mapEventRepository.executePut();

	}

	@Override
	public void preCycle() {

	}

	private void initBuckets() {
		agentBuckets = new HashMap<Rectangle, Integer>();

		agentBuckets.put(new Rectangle(0, 0, 370, 230), 0);
		agentBuckets.put(new Rectangle(400, 0, 370, 230), 0);
		agentBuckets.put(new Rectangle(0, 816, 370, 230), 0);
		agentBuckets.put(new Rectangle(400, 816, 370, 230), 0);
	}

	private AbstractAgent createAgentAtHospital() {
		return Scheduler.agentRepository.createAgent(AmbulanceRole.HOSPITAL_X,
				AmbulanceRole.HOSPITAL_Y);
	}

	private AbstractAgent createAgentOnMap() {
		int x, y, obstacle;
		do {
			x = Scheduler.rand.nextInt(DiscreteMap.sizeX);
			y = Scheduler.rand.nextInt(DiscreteMap.sizeY);
			obstacle = DiscreteMap.getInstance().getObstacles()[y][x];
		} while (obstacle > 100);

		return Scheduler.agentRepository.createAgent(x, y);
	}

	private AbstractAgent createAgentInBucket() {
		int x, y, obstacle;
		boolean bucket;
		do {
			bucket = false;
			x = Scheduler.rand.nextInt(DiscreteMap.sizeX);
			y = Scheduler.rand.nextInt(DiscreteMap.sizeY);
			for (Rectangle b : agentBuckets.keySet()) {
				if (b.containsPoint(x, y)) {
					bucket = true;
					agentBuckets.put(b, agentBuckets.get(b) + 1);
					break;
				}
			}
			obstacle = DiscreteMap.getInstance().getObstacles()[y][x];
		} while (!bucket || obstacle > 100);

		return Scheduler.agentRepository.createAgent(x, y);
	}

	private AbstractAgent createAgentInGreen(boolean mobileComEnabled) {
		int x, y, percent, obstacle;
		do {
			x = Scheduler.rand.nextInt(DiscreteMap.sizeX);
			y = Scheduler.rand.nextInt(DiscreteMap.sizeY);
			percent = Scheduler.rand.nextInt(100) + 1;
			// obstacle = DiscreteMap.getInstance().getObstacles()[y][x];
		} while (percent > DiscreteMap.getInstance().getAdditionalCahnnel()[y][x]);
		return Scheduler.agentRepository.createAgent(x, y, mobileComEnabled);
	}

	private AbstractAgent createAgentOnBorder() {
		int x, y, obstacle, axis;

		axis = Scheduler.rand.nextInt(4);
		do {
			switch (axis) {
			case 0:
				x = 0;
				y = Scheduler.rand.nextInt(DiscreteMap.sizeY);
				break;

			case 1:
				x = Scheduler.rand.nextInt(DiscreteMap.sizeX);
				y = 0;
				break;

			case 2:
				x = DiscreteMap.sizeX - 1;
				y = Scheduler.rand.nextInt(DiscreteMap.sizeY);
				break;

			case 3:
				x = Scheduler.rand.nextInt(DiscreteMap.sizeY);
				y = DiscreteMap.sizeY - 1;
				break;

			default:
				x = y = 0;
				System.out.println("Something wrong");
				break;
			}

			obstacle = DiscreteMap.getInstance().getObstacles()[y][x];
		} while (obstacle > 100);

		return Scheduler.agentRepository.createAgent(x, y);
	}

	@Override
	public void preSimulation() {
	}

	@Override
	public void postSimulation() {
	}

	@Override
	public void postCycle() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		new DegreesScenario();

	}

}
