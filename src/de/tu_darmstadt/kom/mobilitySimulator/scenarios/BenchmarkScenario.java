package de.tu_darmstadt.kom.mobilitySimulator.scenarios;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import de.tu_darmstadt.kom.mobilitySimulator.agent.role.FireFighterRole;
import de.tu_darmstadt.kom.mobilitySimulator.agent.role.PedestrianRole;
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
import de.tu_darmstadt.kom.mobilitySimulator.output.AgentDensityEvaluationOutput;
import de.tu_darmstadt.kom.mobilitySimulator.output.CycleDurationEHCachSizeOutput;
import de.tu_darmstadt.kom.mobilitySimulator.output.GenericFileOutput;
import de.tu_darmstadt.kom.mobilitySimulator.output.SMFOutput;

public class BenchmarkScenario extends AbstractScenario {

	/** Total number of agents (nodes) on the map. */
	private int numberOfAgents = 1;

	/** Total simulation length (in seconds). */
	private int simulationLength = 60 * 60; // In seconds

	/** Used map file. Also defines how large the map is. */
	private String mapFile = "map19.png";
	// private String mapFile = "frankfurt.png";

	private final File MAPS = new File("maps");
	private final File TRACES = new File("output\\"
			+ BenchmarkScenario.class.getName());

	private String prefix;

	private int fileIndex;

	@Override
	protected OutputInterface getOutput() {

		OutputInterface outputHelper = null;

		prefix = numberOfAgents + "agents_" + simulationLength + "sec";
		fileIndex = 0;

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
				fileIndex++;
				outputFile = new File(newTrace, prefix + "trace(" + fileIndex
						+ ").txt");
			}
			outputHelper = new GenericFileOutput(outputFile, false, 1000);
			new Thread((GenericFileOutput) outputHelper);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		outputHelper = new AgentDensityEvaluationOutput(newTrace, fileIndex,
				prefix, outputHelper);

		outputHelper = new CycleDurationEHCachSizeOutput(newTrace, fileIndex,
				prefix, outputHelper);

		outputHelper = new SMFOutput(newTrace, fileIndex, prefix, outputHelper);

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

	@Override
	public void preRun() {

		// /*
		// * init agents
		// */
		// AbstractAgent a = null;
		//
		// // FR distribution
		// int firstResponders = (int) (numberOfAgents * frPercentage);
		//
		// int x, y, obstacle, axis;
		//
		// for (int i = 0; i < firstResponders; i++) {
		//
		// axis = Scheduler.rand.nextInt(4);
		// do {
		// switch (axis) {
		// case 0:
		// x = 0;
		// y = Scheduler.rand.nextInt(DiscreteMap.sizeY);
		// break;
		//
		// case 1:
		// x = Scheduler.rand.nextInt(DiscreteMap.sizeX);
		// y = 0;
		// break;
		//
		// case 2:
		// x = DiscreteMap.sizeX - 1;
		// y = Scheduler.rand.nextInt(DiscreteMap.sizeY);
		// break;
		//
		// case 3:
		// x = Scheduler.rand.nextInt(DiscreteMap.sizeY);
		// y = DiscreteMap.sizeY - 1;
		// break;
		//
		// default:
		// x = y = 0;
		// System.out.println("Something wrong");
		// break;
		// }
		//
		// obstacle = DiscreteMap.getInstance().getObstacles()[y][x];
		// } while (obstacle > 100);
		//
		// a = Scheduler.agentRepository.createAgent(x, y);
		//
		// a.setRole(new FRRole(a));
		//
		// try {
		// Scheduler.agentRepository.put(a);
		// } catch (Exception e) {
		// }
		// }
		//
		// // // Random distribution on streets (ObstacleValue < 100)
		// for (int i = firstResponders; i < numberOfAgents; i++) {
		//
		// do {
		// x = Scheduler.rand.nextInt(DiscreteMap.sizeX);
		// y = Scheduler.rand.nextInt(DiscreteMap.sizeY);
		// obstacle = DiscreteMap.getInstance().getObstacles()[y][x];
		// } while (obstacle > 100);
		//
		// a = Scheduler.agentRepository.createAgent(x, y);
		// a.setRole(new FleeRole(a));
		//
		// // random
		// /*
		// * int roleDistribution = Scheduler.rand.nextInt(20); if
		// * (roleDistribution == 0) // Every 20. agent does not move
		// * a.setRole(new VictimRole(a)); else if (roleDistribution == 1) //
		// * Every 20. agent is a first // responder a.setRole(new FRRole(a));
		// * else a.setRole(new FleeRole(a));
		// */
		//
		// try {
		// Scheduler.agentRepository.put(a);
		// } catch (Exception e) {
		// }
		// }

		// AbstractAgent f1 = Scheduler.agentRepository.createAgent(0, 0);
		// f1.setRole(new FireEngineRole(f1, 6));
		// Scheduler.agentRepository.put(f1);
		// AbstractAgent f2 = Scheduler.agentRepository.createAgent(
		// DiscreteMap.sizeX - 1, DiscreteMap.sizeY - 1);
		// f2.setRole(new FireEngineRole(f2, 6));
		// Scheduler.agentRepository.put(f2);
		//
		// Scheduler.agentRepository.executePut();
		// System.out.println(numberOfAgents + " agents initialized.");
		//
		// LinkedRTreeMapEvent ev = new LinkedRTreeMapEvent(400, 400, 150,
		// 0.7f);
		// Scheduler.mapEventRepository.put(ev.getId(), ev);
		// Scheduler.mapEventRepository.executePut();
		//
		// f1.getMessageQueue().add("Fight fire:" + ev.getId());
		// f2.getMessageQueue().add("Fight fire:" + ev.getId());

		AbstractAgent a = null;
		int x, y, obstacle;
		for (int i = 0; i < numberOfAgents; i++) {

			do {
				x = Scheduler.rand.nextInt(DiscreteMap.sizeX);
				y = Scheduler.rand.nextInt(DiscreteMap.sizeY);
				obstacle = DiscreteMap.getInstance().getObstacles()[y][x];
			} while (obstacle > 100);

			a = Scheduler.agentRepository.createAgent(x, y);

			a.setRole(new FireFighterRole(a));
			a.setRole(new PedestrianRole(a));

			try {
				Scheduler.agentRepository.put(a);
			} catch (Exception e) {
			}
		}

		Scheduler.agentRepository.executePut();
		System.out.println(numberOfAgents + " agents initialized.");
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

	public BenchmarkScenario() {
		System.err.println("SIMREIHE GESTARTET, Consolenausgabe deaktiviert.");

		// Generic File Output
		if (!TRACES.exists())
			if (!TRACES.mkdir())
				System.err.println("Cannot create folder "
						+ TRACES.getAbsolutePath());

		File resultFile = new File(TRACES, "result.txt");
		try {
			System.setOut(new PrintStream(
					new FileOutputStream(resultFile, true)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		numberOfAgents = 70;
		System.out.println("####### agents,70 - percentage,0.2 ##########");
		System.err.println("####### agents,70 - percentage,0.2 ##########");
		run();

		// numberOfAgents = 200;
		// System.out
		// .println("\n\n####### agents,200 - percentage,0.2 ##########");
		// System.err.println("####### agents,200 - percentage,0.2 ##########");
		// run();

		// numberOfAgents = 500;
		// System.out
		// .println("\n\n####### agents,500 - percentage,0.2 ##########");
		// System.err.println("####### agents,500 - percentage,0.2 ##########");
		// run();

		// numberOfAgents = 800;
		// System.out
		// .println("\n\n####### agents,800 - percentage,0.2 ##########");
		// System.err.println("####### agents,800 - percentage,0.2 ##########");
		// run();

		// numberOfAgents = 1000;
		// System.out
		// .println("\n\n####### agents,1000 - percentage,0.2 ##########");
		// System.err.println("####### agents,1000 - percentage,0.2 ##########");
		// run();

		// numberOfAgents = 1500;
		// System.out
		// .println("\n\n####### agents,1500 - percentage,0.2 ##########");
		// System.err.println("####### agents,1500 - percentage,0.2 ##########");
		// run();

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

		System.err.println("Fertig mit allem");

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		new BenchmarkScenario();

	}

}
