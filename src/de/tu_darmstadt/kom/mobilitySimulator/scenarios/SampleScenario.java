package de.tu_darmstadt.kom.mobilitySimulator.scenarios;

import java.io.File;

import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.SchedulerHookInterface;

public class SampleScenario implements SchedulerHookInterface {

	/** Total number of agents (nodes) on the map. */
	private int numberOfAgents = 2000;

	/** Total simulation length (in seconds). */
	private int simulationLength = 60 * 60 * 3; // In seconds

	/** Used map file. */
	private String mapFile = "map5.png";

	/** path to map repository */
	private static final File MAPS = new File("maps");
	/** output path for tracefile */
	private static final File TRACES = new File("traces");

	public static void main(String[] args) {

	}

	@Override
	public void preSimulation() {
		// TODO Auto-generated method stub

	}

	@Override
	public void postSimulation() {
		// TODO Auto-generated method stub

	}

	@Override
	public void preCycle() {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCycle() {
		// TODO Auto-generated method stub

	}

	@Override
	public void preRun() {
		// TODO Auto-generated method stub

	}

}
