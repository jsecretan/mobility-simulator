package de.tu_darmstadt.kom.mobilitySimulator.scenarios;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.swing.SwingUtilities;

import de.tu_darmstadt.kom.gui.MapGuiFrame;
import de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel.WalkToOnGradientMobilityModel;
import de.tu_darmstadt.kom.mobilitySimulator.agent.role.FireEngineRole;
import de.tu_darmstadt.kom.mobilitySimulator.core.OutputInterface;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.map.DiscreteMap;
import de.tu_darmstadt.kom.mobilitySimulator.core.map.MapFactory;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;
import de.tu_darmstadt.kom.mobilitySimulator.core.watchdog.Watchdog;
import de.tu_darmstadt.kom.mobilitySimulator.linkedRTree.LinkedRTreeAgentRepository;
import de.tu_darmstadt.kom.mobilitySimulator.linkedRTree.LinkedRTreeMapEventRepository;
import de.tu_darmstadt.kom.mobilitySimulator.map.EHCacheMapFactory;
import de.tu_darmstadt.kom.mobilitySimulator.output.DummyOutput;

public class Test {

	/** Total number of agents (nodes) on the map. */
	private int numberOfAgents = 2000;

	/** Total simulation length (in seconds). */
	private int simulationLength = 60 * 60 * 3; // In seconds

	/** Used map file. Also defines how large the map is. */
	private String mapFile = "map5.png";
	// private String mapFile = "map_ueberwindung.png";

	private static final File MAPS = new File("maps");
	private static final File TRACES = new File("traces");

	public static void main(String[] args) {

		new Test(args);
	}

	private final static String[] OPTIONS = { "--numberOfAgents",
			"--simulationLength", "--mapFile" };

	private void parse(String arg) {
		String[] option = arg.split("=", 2);
		if (option.length < 2) {
			System.err.println("Not recognized option: " + arg);
			return;
		}

		int found = -1;
		for (int i = 0; i < OPTIONS.length; i++) {
			String o = OPTIONS[i];
			if (option[0].equals(o)) {
				// match
				found = i;
				break;
			}
		}

		switch (found) {

		case 0:
			numberOfAgents = Integer.parseInt(option[1]);
			break;
		case 1:
			simulationLength = Integer.parseInt(option[1]);
			break;
		case 2:
			mapFile = option[1];
			break;
		default:
			System.err.println("Not recognized option: " + arg);
			return;
		}
	}

	private Test(String[] args) {

		System.out
				.println("MobilitySimulator: de.tu-darmstadt.kom.mobilitySimulator TEST");
		Date dt = new Date();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		System.out.println("Date: " + df.format(dt));
		System.out
				.println("_____________________________________________________________");

		// parse parameters
		for (String arg : args)
			parse(arg);

		// Init Output Object
		OutputInterface outputHelper = null;

		outputHelper = new DummyOutput();

		// // Generic File Output
		// if (!TRACES.exists())
		// if (!TRACES.mkdir())
		// System.err.println("Cannot create folder "
		// + TRACES.getAbsolutePath());
		//
		//
		// try {
		// // show map size in filename might be good
		// File outputFile = new File(TRACES, numberOfAgents + "agents_"
		// + simulationLength + "sec.txt");
		// int o = 1;
		//
		// while (!outputFile.createNewFile()) {
		// outputFile = new File(TRACES, numberOfAgents + "agents_"
		// + simulationLength + "sec(" + o + ").txt");
		// o++;
		// }
		// outputHelper = new GenericFileOutput(outputFile, false);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		//
		// int o = 1;
		// while (outputFile.exists()) {
		// outputFile = new FileOutputHelper(TRACES.getAbsolutePath()+
		// numberOfAgents + "agents_"
		// + simulationLength + "sec_" + o + ".txt");
		// o++;
		// }

		/*
		 * INITIALIZE Map
		 */
		DiscreteMap map = null;

		try {
			DiscreteMap.init(new File(MAPS, mapFile).getAbsolutePath());
			map = DiscreteMap.getInstance();
			System.out.println("Map acquired");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(0);
		}

		/*
		 * INITIALIZE Scheduler
		 */
		Scheduler scheduler = Scheduler.getInstance();
		// Set seed
		Scheduler.rand = new Random(123456);
		scheduler.setOutput(outputHelper);

		(new EHCacheMapFactory()).setAsMapFactory();

		/*
		 * INIT AGENTS
		 */
		// Init. AgentRepository
		Scheduler.agentRepository = new LinkedRTreeAgentRepository(
				map.getSizeX(), map.getSizeY(), numberOfAgents, false);

		AbstractAgent a = null;

		// Single Agent
		a = Scheduler.agentRepository.createAgent(0, 0);
		a.setRole(new FireEngineRole(a, 6));
		((WalkToOnGradientMobilityModel) a.getMobilityModel())
				.setDestination(new int[] { map.getSizeX() - 1,
						map.getSizeY() / 2 });
		try {
			Scheduler.agentRepository.put(a.getId(), a);
		} catch (Exception e) {
		}

		// // Low number of agents Agent
		//
		// a = new StaticRTreeAgent(1, 10, map.getSizeY() - 30);
		// a.setRole(new VictimRole(a));
		//
		// StaticRTreeAgent b = new StaticRTreeAgent(2, 10, map.getSizeY() -
		// 10);
		// b.setRole(new FRRole(b));
		//
		// // Scheduler.getInstance().getSimulationIO().writeEntry(a);
		//
		// try {
		// Scheduler.agentRepository.put(a.getId(), a);
		// Scheduler.agentRepository.put(b.getId(), b);
		// } catch (Exception e) {
		// }

		// // Random distribution
		// for (int i = 0; i < numberOfAgents; i++) {
		//
		// a = new StaticRTreeAgent(i, Scheduler.rand.nextInt(map.getSizeX()),
		// Scheduler.rand.nextInt(map.getSizeY()));
		// a.setRole(new PedestrianRole(a));
		// ((WalkToOnGradientMobilityModel) a.getMobilityModel())
		// .setDestination(DiscreteMap.sizeX / 2 - 30,
		// DiscreteMap.sizeY / 2 - 30, 30);
		// try {
		// Scheduler.agentRepository.put(i, a);
		// } catch (Exception e) {
		// }
		// }

		// // FR distribution
		// int firstResponders = numberOfAgents / 20;
		// int victims = numberOfAgents / 20 * 2;
		// // int fleeing = numberOfAgents/20*17;
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
		// y = Scheduler.rand.nextInt(map.getSizeY());
		// break;
		//
		// case 1:
		// x = Scheduler.rand.nextInt(map.getSizeX());
		// y = 0;
		// break;
		//
		// case 2:
		// x = map.getSizeX() - 1;
		// y = Scheduler.rand.nextInt(map.getSizeY());
		// break;
		//
		// case 3:
		// x = Scheduler.rand.nextInt(map.getSizeX());
		// y = map.getSizeY() - 1;
		// break;
		//
		// default:
		// x = y = 0;
		// System.out.println("Something wrong");
		// break;
		// }
		//
		// obstacle = map.getObstacles()[y][x];
		// } while (obstacle > 100);
		//
		// a = Scheduler.agentRepository.createAgent(x, y);
		// ;
		//
		// a.setRole(new FRRole(a));
		//
		// try {
		// Scheduler.agentRepository.put(i, a);
		// } catch (Exception e) {
		// }
		// }
		//
		// // // Random distribution on streets (ObstacleValue < 100)
		// for (int i = firstResponders; i < numberOfAgents; i++) {
		//
		// do {
		// x = Scheduler.rand.nextInt(map.getSizeX());
		// y = Scheduler.rand.nextInt(map.getSizeY());
		// obstacle = map.getObstacles()[y][x];
		// } while (obstacle > 100);
		//
		// a = Scheduler.agentRepository.createAgent(x, y);
		//
		// // not random
		// if (i < firstResponders + victims)
		// // a.setRole(new VictimRole(a));
		// a.setRole(new FireFighterRole(a));
		// else
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
		// Scheduler.agentRepository.put(i, a);
		// } catch (Exception e) {
		// }
		// }

		// // 10x10 Grid
		// int id = 1;
		// for (int i = 0; i < map.getSizeY(); i = i + 10) {
		// for (int j = 0; j < map.getSizeX(); j = j + 10) {
		// a = new StaticRTreeAgent(id, i, j);
		// a.setRole(new PedestrianRole(a));
		// try {
		// Scheduler.agentRepository.put(id, a);
		// } catch (Exception e) {
		// }
		// id++;
		// }
		// }

		Scheduler.agentRepository.executePut();
		System.out.println(numberOfAgents + " agents initialized.");

		/*
		 * MapEvents
		 */

		scheduler.mapEventRepository = new LinkedRTreeMapEventRepository(
				map.getSizeX(), map.getSizeY());
		// StaticRTreeMapEvent ev = new StaticRTreeMapEvent(300, 300, 200,
		// 0.7f);
		// StaticRTreeMapEvent ev = new StaticRTreeMapEvent(160, 160, 25, 0.7f);
		// scheduler.mapEventRepository.put(ev.getId(), ev);

		// ev = new StaticRTreeMapEvent(600, 600, 10, 0.3f);
		// scheduler.mapEventRepository.put(ev.getId(), ev);

		scheduler.mapEventRepository.executePut();

		/*
		 * Test
		 */
		// testOutput();

		/*
		 * GUI
		 */
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				MapGuiFrame gui = new MapGuiFrame();
				gui.setLocationRelativeTo(null);
				gui.setVisible(true);
				gui.setMap(MapFactory.getInstance().getMap(
						new int[] { 700, 400, 100 }, 50, 50, true));
			}
		});

		/*
		 * SIMULATION
		 */

		scheduler.setSimulationLenght(simulationLength);
		scheduler.setSimulationSpeed(0);
		scheduler.setSecondsPerCycle(1);

		System.out.println("Time to be simulated: "
				+ scheduler.getSimulationLenght() + " sec"); // we might
		// approximate
		// to minutes

		// Thread errorProducer = new ThreadExceptionProvoker();
		// errorProducer.start();

		Watchdog.getInstance();

		scheduler.run();

		scheduler.getOutput().close();
	}

	private void testOutput() {
		// Test.printArrayForMatlab("obstacles", DiscreteMap.getInstance()
		// .getObstacles());

		// Test.printArrayForMatlab("event",
		// ((AbstractMapEvent) Scheduler.mapEventRepository.get(1))
		// .getImpactMap());

		// Test.printArrayForMatlab("overlayOhne", Scheduler.getInstance()
		// .getMapCache().getMap(new int[] { 120, 130 }));

		// Test.printArrayForMatlab("overlayMit", Scheduler.getInstance()
		// .getMapCache().getMapIncludingEvent(new int[] { 120, 130 }, 1));

		// printArrayForMatlabToFile("obstacles", DiscreteMap.getInstance()
		// .getObstacles());

		printArray(
				"bl",
				MapFactory.getInstance().getMap(
						new int[] { DiscreteMap.sizeX - 1,
								DiscreteMap.sizeY / 2 }, 50, 50, true));

		System.exit(0);

		// int[][] specialMap = Scheduler.getInstance().getMapCache()
		// .getMap(new int[] { 700, 400, 100 }, 40, 50, true);
		//
		// MapGuiFrame gui = new MapGuiFrame();
		// gui.setMap(specialMap);
		// gui.setLocationRelativeTo(null);
		// gui.setVisible(true);

		while (true) {
			Thread.yield();
		}

		// printArrayForMatlabToFile("with_overlay", specialMap);

		// System.exit(0);

	}

	private void printArray(String name, short[][] arr) {
		System.out.println(name + ":");
		for (int y = 0; y < arr.length; y++) {
			for (int x = 0; x < arr[y].length; x++) {
				System.out.print(arr[y][x] + "\t");
			}
			System.out.println("");
		}
	}

	private void printArray(String name, int[][] arr) {
		System.out.println(name + ":");
		for (int y = 0; y < arr.length; y++) {
			for (int x = 0; x < arr[y].length; x++) {
				System.out.print(arr[y][x] + "\t");
			}
			System.out.println("");
		}
	}

	private static void printArrayForMatlab(String name, short[][] arr) {

		boolean firstY = true, firstX = true;

		System.out.print(name + " = flipud(rot90([");
		for (int y = 0; y < DiscreteMap.getInstance().getObstacles().length; y++) {

			if (!firstY)
				System.out.print(" ; ");

			for (int x = 0; x < DiscreteMap.getInstance().getObstacles()[y].length; x++) {
				if (!firstX)
					System.out.print(" ");
				System.out.print(arr[y][x]);
				firstX = false;
			}

			firstX = true;
			firstY = false;
		}
		System.out.print("]));\n");
	}

	private static void printArrayForMatlab(String name, int[][] arr) {

		boolean firstY = true, firstX = true;

		System.out.print(name + " = flipud(rot90([");
		for (int y = 0; y < DiscreteMap.getInstance().getObstacles().length; y++) {

			if (!firstY)
				System.out.print(" ; ");

			for (int x = 0; x < DiscreteMap.getInstance().getObstacles()[y].length; x++) {
				if (!firstX)
					System.out.print(" ");
				System.out.print(arr[y][x]);
				firstX = false;
			}

			firstX = true;
			firstY = false;
		}
		System.out.print("]));\n");
	}

	private static void printArrayForMatlabToFile(String name, int[][] arr) {

		boolean firstY = true, firstX = true;

		try {
			FileWriter fo = new FileWriter(new File("matlabExport\\" + name
					+ ".txt"));

			// fo.write(name + " = flipud(rot90([");
			for (int y = 0; y < DiscreteMap.getInstance().getObstacles().length; y++) {

				if (!firstY)
					fo.write("\n");

				for (int x = 0; x < DiscreteMap.getInstance().getObstacles()[y].length; x++) {
					if (!firstX)
						fo.write(" ");
					fo.write(arr[y][x] + "");
					firstX = false;
				}

				firstX = true;
				firstY = false;
			}
			// fo.write("]));\n");
			fo.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}

	private static void printArrayForMatlabToFile(String name, short[][] arr) {

		boolean firstY = true, firstX = true;

		try {
			FileWriter fo = new FileWriter(new File("matlabExport\\" + name
					+ ".txt"));

			// fo.write(name + " = flipud(rot90([");
			for (int y = 0; y < DiscreteMap.getInstance().getObstacles().length; y++) {

				if (!firstY)
					fo.write("\n");

				for (int x = 0; x < DiscreteMap.getInstance().getObstacles()[y].length; x++) {
					if (!firstX)
						fo.write(" ");
					fo.write(arr[y][x] + "");
					firstX = false;
				}

				firstX = true;
				firstY = false;
			}
			// fo.write("]));\n");
			fo.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
}
