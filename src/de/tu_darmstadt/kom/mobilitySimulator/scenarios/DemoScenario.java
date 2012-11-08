package de.tu_darmstadt.kom.mobilitySimulator.scenarios;

import java.io.File;

import de.tu_darmstadt.kom.mobilitySimulator.agent.role.AmbulanceRole;
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
import de.tu_darmstadt.kom.mobilitySimulator.linkedRTree.LinkedRTreeAgentRepository;
import de.tu_darmstadt.kom.mobilitySimulator.linkedRTree.LinkedRTreeCollapsedBuildingMapEvent;
import de.tu_darmstadt.kom.mobilitySimulator.linkedRTree.LinkedRTreeFireMapEvent;
import de.tu_darmstadt.kom.mobilitySimulator.linkedRTree.LinkedRTreeMapEventRepository;
import de.tu_darmstadt.kom.mobilitySimulator.map.EHCacheMapFactory;

public class DemoScenario extends AbstractScenario {

	/** Total number of agents (nodes) on the map. */
	private int numberOfAgents = 1;

	/** Total simulation length (in seconds). */
	private int simulationLength = 60 * 60; // In seconds

	/** Used map file. Also defines how large the map is. */
	private String mapFile = "bsp.png";
	// private String mapFile = "frankfurt.png";

	private final File MAPS = new File("maps");
	private final File TRACES = new File("output\\"
			+ DemoScenario.class.getName());

	private int rescueCounter;

	private int fireCounter;

	@Override
	protected OutputInterface getOutput() {

		OutputInterface outputHelper = null;
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
				DiscreteMap.sizeY, numberOfAgents);
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
		return "Test Scenario";
	}

	public DemoScenario() {

		fireCounter = 0;
		rescueCounter = 0;

		// System.err.println("SIMREIHE GESTARTET, Consolenausgabe deaktiviert.");
		// File resultFile = new File("result.txt");
		// try {
		// System.setOut(new PrintStream(
		// new FileOutputStream(resultFile, true)));
		// } catch (FileNotFoundException e) {
		// e.printStackTrace();
		// }

		numberOfAgents = 700;
		System.out.println("####### agents,70 - percentage,0.2 ##########");
		System.err.println("####### agents,70 - percentage,0.2 ##########");
		run();

		System.exit(0);

		// numberOfAgents = 200;
		// System.out
		// .println("\n\n####### agents,200 - percentage,0.2 ##########");
		// System.err.println("####### agents,200 - percentage,0.2 ##########");
		// run();
		//
		// numberOfAgents = 500;
		// System.out
		// .println("\n\n####### agents,500 - percentage,0.2 ##########");
		// System.err.println("####### agents,500 - percentage,0.2 ##########");
		// run();
		//
		// numberOfAgents = 1000;
		// System.out
		// .println("\n\n####### agents,1000 - percentage,0.2 ##########");
		// System.err.println("####### agents,1000 - percentage,0.2 ##########");
		// run();
		//
		// numberOfAgents = 2000;
		// System.out
		// .println("\n\n####### agents,2000 - percentage,0.2 ##########");
		// System.err
		// .println("\n\n####### agents,2000 - percentage,0.2 ##########");
		// run();
		//
		// numberOfAgents = 5000;
		// System.out
		// .println("\n\n####### agents,5000 - percentage,0.2 ##########");
		// System.err.println("####### agents,5000 - percentage,0.2 ##########");
		// run();
		//
		// numberOfAgents = 10000;
		// System.out
		// .println("\n\n####### agents,10000 - percentage,0.2 ##########");
		// System.err.println("####### agents,10000 - percentage,0.2 ##########");
		// run();
		//
		// numberOfAgents = 15000;
		// System.out
		// .println("\n\n####### agents,15000 - percentage,0.2 ##########");
		// System.err.println("####### agents,15000 - percentage,0.2 ##########");
		// run();
		//
		// numberOfAgents = 20000;
		// System.out
		// .println("\n\n####### agents,20000 - percentage,0.2 ##########");
		// System.err.println("####### agents,20000 - percentage,0.2 ##########");
		// run();
		//
		// numberOfAgents = 25000;
		// System.out
		// .println("\n\n####### agents,25000 - percentage,0.2 ##########");
		// System.err.println("####### agents,25000 - percentage,0.2 ##########");
		// run();

		System.err.println("Fertig mit allem");

	}

	@Override
	public void preRun() {

		Scheduler.getInstance().setSimulationSpeed(0.5f);

		/*
		 * Agents
		 */
		AbstractAgent a = null;

		// Normal Agents
		for (int i = 0; i < numberOfAgents; i++) {
			a = createAgentOnMap();
			a.setRole(new NormalRole(a));
			// a.setRole(new EarthquakeVictimRole(a));
			Scheduler.agentRepository.put(a);
			// AmbulanceRole.reportedVictims.add(a);
		}

		// Ambulances
		for (int i = 0; i < 3; i++) {
			a = createAgentAtHospital();
			a.setRole(new AmbulanceRole(a));
			Scheduler.agentRepository.put(a);
		}

		// // FireEngines
		// {
		// a = createAgentOnBorder();
		// a.setRole(new FireEngineRole(a, 6));
		// Scheduler.agentRepository.put(a);
		//
		// a = createAgentOnBorder();
		// a.setRole(new FireEngineRole(a, 6));
		// Scheduler.agentRepository.put(a);
		//
		// a = createAgentOnBorder();
		// a.setRole(new FireEngineRole(a, 6));
		// Scheduler.agentRepository.put(a);
		//
		// a = createAgentOnBorder();
		// a.setRole(new FireEngineRole(a, 6));
		// Scheduler.agentRepository.put(a);
		//
		// a = createAgentOnBorder();
		// a.setRole(new FireEngineRole(a, 6));
		// Scheduler.agentRepository.put(a);
		// }
		//
		// // RescueVehicles
		// {
		// a = createAgentOnBorder();
		// a.setRole(new RescueVehicleRole(a, 6));
		// Scheduler.agentRepository.put(a);
		//
		// a = createAgentOnBorder();
		// a.setRole(new RescueVehicleRole(a, 6));
		// Scheduler.agentRepository.put(a);
		//
		// a = createAgentOnBorder();
		// a.setRole(new RescueVehicleRole(a, 6));
		// Scheduler.agentRepository.put(a);
		//
		// a = createAgentOnBorder();
		// a.setRole(new RescueVehicleRole(a, 6));
		// Scheduler.agentRepository.put(a);
		//
		// a = createAgentOnBorder();
		// a.setRole(new RescueVehicleRole(a, 6));
		// Scheduler.agentRepository.put(a);
		// }

		Scheduler.agentRepository.executePut();
		System.out.println(numberOfAgents + " agents initialized.");

		/*
		 * Events
		 */
		AbstractMapEvent e;

		// Fires
		e = new LinkedRTreeFireMapEvent(208, 14, 30, 0.3f);
		Scheduler.mapEventRepository.put(e.getId(), e);
		FireEngineRole.reportedFires.add(e);

		// Collapsed Buildings
		e = new LinkedRTreeCollapsedBuildingMapEvent(152, 174, 15, 0f);
		Scheduler.mapEventRepository.put(e.getId(), e);
		RescueVehicleRole.reportedEvents.add(e);

		Scheduler.mapEventRepository.executePut();

	}

	@Override
	public void preCycle() {

		AbstractAgent a;

		rescueCounter++;
		fireCounter++;

		// 1 rescue vehicle every 2 min
		if (rescueCounter == 120 && fireCounter < 3601) {
			rescueCounter = 0;
			a = createAgentOnBorder();
			a.setRole(new RescueVehicleRole(a, 6));
			Scheduler.agentRepository.put(a);
		}

		// 6 Firefighters at 3min
		if (fireCounter == 180) {
			for (int i = 0; i < 3; i++) {
				a = createAgentOnBorder();
				a.setRole(new FireEngineRole(a, 6));
				Scheduler.agentRepository.put(a);
			}
		}
	}

	private AbstractAgent createAgentAtHospital() {
		return Scheduler.agentRepository.createAgent(399, 454);
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

		new DemoScenario();

	}

}
