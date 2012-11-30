package de.tu_darmstadt.kom.mobilitySimulator.scenarios;

import java.io.File;
import java.util.Random;

import de.tu_darmstadt.kom.mobilitySimulator.agent.role.SMFRole;
import de.tu_darmstadt.kom.mobilitySimulator.agent.role.WalkRightRole;
import de.tu_darmstadt.kom.mobilitySimulator.core.AbstractScenario;
import de.tu_darmstadt.kom.mobilitySimulator.core.OutputInterface;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgentRepository;
import de.tu_darmstadt.kom.mobilitySimulator.core.map.DiscreteMap;
import de.tu_darmstadt.kom.mobilitySimulator.core.map.MapFactory;
import de.tu_darmstadt.kom.mobilitySimulator.core.mapEvent.AbstractMapEventRepository;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;
import de.tu_darmstadt.kom.mobilitySimulator.linkedRTree.LinkedRTreeAgentRepository;
import de.tu_darmstadt.kom.mobilitySimulator.linkedRTree.LinkedRTreeMapEventRepository;
import de.tu_darmstadt.kom.mobilitySimulator.map.EHCacheMapFactory;
import de.tu_darmstadt.kom.mobilitySimulator.output.SMFOutput;

public class TestScenario extends AbstractScenario {

	// /** Total number of agents (nodes) on the map. */
	// private int numberOfAgents = 20000;

	/** Total simulation length (in seconds). */
	private int simulationLength = 60 * 60 * 1; // In seconds

	/** Used map file. Also defines how large the map is. */
	private String mapFile = "map15.png";
	// private String mapFile = "frankfurt.png";

	private final File MAPS = new File("maps");
	private final File TRACES = new File("output\\"
			+ TestScenario.class.getName());

	@Override
	protected OutputInterface getOutput() {

		if (!TRACES.exists())
			if (!TRACES.mkdir())
				System.err.println("Cannot create folder "
						+ TRACES.getAbsolutePath());

		return new SMFOutput(TRACES, 0, "");
	}

	@Override
	protected float getSimulationSpeed() { return 0.5f; }

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
				DiscreteMap.sizeY, true);
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

	@Override
	public void preRun() {

		Scheduler.rand = new Random(456654);

		for (int i = 0; i < 5; i++) {
			SMFRole.addDestination(new int[] {
					Scheduler.rand.nextInt(DiscreteMap.sizeX),
					Scheduler.rand.nextInt(DiscreteMap.sizeY) });
		}
		/*
		 * init agents
		 */
		AbstractAgent a = null;

		// Single Agent
		a = Scheduler.agentRepository.createAgent(0, DiscreteMap.sizeY / 2);
		a.setRole(new WalkRightRole(a));
		Scheduler.agentRepository.put(a.getId(), a);

		// a = Scheduler.agentRepository.createAgent(10, 10);
		// a.setRole(new SMFRole(a));
		// Scheduler.agentRepository.put(a.getId(), a);
		//
		// a = Scheduler.agentRepository.createAgent(0, 10);
		// a.setRole(new SMFRole(a));
		// Scheduler.agentRepository.put(a.getId(), a);
		//
		// a = Scheduler.agentRepository.createAgent(10, 0);
		// a.setRole(new SMFRole(a));
		// Scheduler.agentRepository.put(a.getId(), a);

		Scheduler.agentRepository.executePut();
		// System.out.println(numberOfAgents + " agents initialized.");

		// AbstractMapEvent event = new LinkedRTreeFireMapEvent(30, 30, 15,
		// 0.2f);
		// Scheduler.mapEventRepository.put(event.getId(), event);
		// Scheduler.mapEventRepository.executePut();
		//
		// a.getMessageQueue().add("Fight fire:" + event.getId());

		// Scheduler.getInstance().setSimulationSpeed(0.5f);
	}

	@Override
	public void preSimulation() {
	}

	@Override
	public void postSimulation() {
	}

	@Override
	public void preCycle() {
	}

	@Override
	public void postCycle() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new TestScenario().run(125);
	}

}
