package de.tu_darmstadt.kom.mobilitySimulator.core.scheduler;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.tu_darmstadt.kom.mobilitySimulator.core.OutputInterface;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgentRepository;
import de.tu_darmstadt.kom.mobilitySimulator.core.mapEvent.AbstractMapEvent;
import de.tu_darmstadt.kom.mobilitySimulator.core.mapEvent.AbstractMapEventRepository;

public class Scheduler implements TerminationListener {

	private static int count = 0;
	private int id;

	public final static int MAX_SPEED = 0;
	public final static int REAL_TIME = 1;

	public static final boolean MULTITHREADING = false;

	private static volatile Scheduler instance;

	public static AbstractAgentRepository agentRepository;

	public static AbstractMapEventRepository mapEventRepository;

	private Executor executor;

	public static Random rand = new Random();

	private Set<SchedulerHookInterface> listeners;

	private volatile int openTasks;

	private int cores;

	@Deprecated
	public static final int CLOCK_RATE = 1;

	/** Seconds that are simulated during one cycle */
	private int secondsPerCycle;

	/** Scaling factor for simulation time of one cycle */
	private float simulationSpeed;

	/** Time available for one cycle (seconds) */
	private int simulationTimeOffset;

	private boolean simSpeedDelayed;

	/** Amount of total cycles */
	private long totalCycles;

	protected boolean simulating;

	protected OutputInterface output;

	/** Simulation length in seconds */
	protected long simulationLenght;

	/** Elapsed simulation time in seconds */
	protected long timestamp;

	private long cycle;

	private volatile boolean interrupted;
	private long exactCycleDuration;

	public static Scheduler getInstance() {

		if (instance == null) {
			instance = new Scheduler();
		}
		return instance;
	}

	public static void clearInstance() {
		if (instance != null) {
			System.out.println("Cleared Scheduler (" + instance.id + ")");
			System.err.println("Cleared Scheduler (" + instance.id + ")");
			instance = null;
		}
	}

	private Scheduler() {
		count++;
		id = count;

		System.err.println("created Scheduler (" + id + ")");
		System.out.println("created Scheduler (" + id + ")");

		interrupted = false;
		simulationLenght = 0;
		timestamp = 0;
		simulationSpeed = MAX_SPEED;
		secondsPerCycle = 1;
		simulating = false;
		simSpeedDelayed = false;
		listeners = new HashSet<SchedulerHookInterface>();
		openTasks = 0;
		cores = Runtime.getRuntime().availableProcessors();
		executor = Executors.newFixedThreadPool(cores);
	}

	public int getId() {
		return id;
	}

	public void run() {

		if (!simulating) {

			simulating = true;

			// Call preRun() on all listeners
			for (SchedulerHookInterface listener : listeners) {
				listener.preRun();
			}

			// Check if all parameters are initialized
			if (agentRepository != null && mapEventRepository != null
					&& secondsPerCycle > 0 && simulationLenght > 0
					&& output != null) {

				// (0) Initialize simulation environnment

				long simStartTime, simEndTime, cycleStartTime, cycleStopTime;

				if (MULTITHREADING)
					System.out
							.println("Multithreading for agent calls enabled. "
									+ cores + " core(s) available.");
				else
					System.out
							.println("Multithreading for agent calls disabled.");

				// Calculate amount of required cycles
				totalCycles = (long) Math.ceil(simulationLenght
						/ secondsPerCycle);

				timestamp = 0;

				double abschnitt = simulationLenght / 100.0;
				double status = 0;
				int countPercent = 0;

				// Call preSimulation() on all listeners
				for (SchedulerHookInterface listener : listeners) {
					listener.preSimulation();
				}

				// Start simulation
				System.out.println("Start Simulation");
				simStartTime = System.currentTimeMillis();
				for (cycle = 1; cycle <= totalCycles && !interrupted; cycle++) {

					if (Thread.interrupted()) {
						System.out.println("Simulation aborded.");
						return;
					}

					cycleStartTime = System.currentTimeMillis();

					// Progress indication
					if (cycle * secondsPerCycle > status) {
						if (countPercent % 10 == 0)
							System.out
									.print("\n" + (countPercent / 10) + "0% ");
						System.out.print(".");
						status += abschnitt;
						countPercent++;
					}

					timestamp = cycle * secondsPerCycle;

					// Call preCycle() on all listeners
					for (SchedulerHookInterface listener : listeners) {
						listener.preCycle();
					}

					// (1) Call map events
					for (Iterator<AbstractMapEvent> iterator = mapEventRepository
							.values().iterator(); iterator.hasNext()
							&& !interrupted;) {
						iterator.next().clock();
					}

					// (2) Call agent behavior
					if (!MULTITHREADING) {
						for (Iterator iterator = agentRepository.values()
								.iterator(); iterator.hasNext() && !interrupted;) {
							AbstractAgent agent = (AbstractAgent) iterator
									.next();
							agent.behave();
						}
					} else {
						// int maxParallelAgents = (int) Math.ceil(cores / 2.0);
						for (int i = 0; i < agentRepository
								.maxSpatialIndependentAgentSubsets(); i++) {
							openTasks++;
							Collection<AbstractAgent> cA = agentRepository
									.spatialIndependentAgentSubset(i);
							executor.execute(new AgentRunnable(cA.iterator(),
									this));
						}
						while (openTasks != 0) {
							Thread.yield();
						}

					}

					cycleStopTime = System.currentTimeMillis();

					exactCycleDuration = cycleStopTime - cycleStartTime;
					if (exactCycleDuration < simulationTimeOffset) {
						try {
							Thread.sleep(simulationTimeOffset - cycleStopTime
									+ cycleStartTime);
						} catch (InterruptedException e) {
							cycle = totalCycles + 1;
						}
						simSpeedDelayed = false;
					} else {
						if (!simSpeedDelayed) {
							System.out
									.print("Sim is running slower than speed requirement.");
							simSpeedDelayed = true;
						}
					}

					// Call postCycle() on all listeners
					for (SchedulerHookInterface listener : listeners) {
						listener.postCycle();
					}

				}
				simEndTime = System.currentTimeMillis();

				// Call postSimulation() on all listeners
				for (SchedulerHookInterface listener : listeners) {
					listener.postSimulation();
				}

				System.out
						.println("\n ---------------------------------------\n"
								+ "Simulation Terminated.");
				System.out.println("Simulation duration: "
						+ (simEndTime - simStartTime) + "ms | "
						+ (simEndTime - simStartTime) / 1000 + "s | "
						+ (simEndTime - simStartTime) / 60000 + "min | "
						+ (simEndTime - simStartTime) / 3600000f + "h");
				System.out.println("");
				simulating = false;

			} else {
				simulating = false;
				System.err.println("Simulation not started!\n"
						+ "Some scheduler parameters are not initialized.\n "
						+ "Initialize:\n \tsimulationLenght\n "
						+ "\tmapEventRepository\n " + "\tAgentRepository\n "
						+ "\tsecondsPerCicle+\n" + "\toutputFile\n");
				System.exit(0);
			}
		}
	}

	public void interrupt() {
		if (simulating) {
			interrupted = true;
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"EEE, d MMM yyyy HH:mm:ss Z");
			System.out.println("\n! Simulation interrupted by User at:"
					+ dateFormat.format(new Date()) + " !");

		}
	}

	public long getExactCycleDuration() {
		return exactCycleDuration;
	}

	public void setOutput(OutputInterface file) {
		output = file;
	}

	public OutputInterface getOutput() {
		return output;
	}

	/**
	 * <p>
	 * Sets the time in seconds that are simulated during one simulation cycle.
	 * </p>
	 * <p>
	 * <i>This is NOT the running speed.</i>
	 * </p>
	 * 
	 * @param secondsPerCicle
	 *            <p>
	 *            time in seconds that are simulated during one simulation cycle
	 *            </p>
	 */
	public void setSecondsPerCycle(int secondsPerCicle) {
		if (!simulating && secondsPerCicle > 0) {
			this.secondsPerCycle = secondsPerCicle;
			calcOffset();
		}
	}

	/**
	 * Sets the speed of the simulation.
	 * <ul>
	 * <li>0 - Max speed <i>(No time between the execution of a simulation
	 * cycle)</i></li>
	 * <li>1 - Realtime <i>(A simulation cycle takes as long as the seconds that
	 * are simulated during this cycle)</i></li>
	 * <li>0 < simSpeed < 1 - Simulation runs faster</li>
	 * <li>1 < simSpeed < Float.MAX_VALUE - Simulation runs slower</li>
	 * </ul>
	 * 
	 * @param simSpeed
	 *            time
	 */
	public void setSimulationSpeed(float simSpeed) {
		this.simulationSpeed = simSpeed;
		calcOffset();
	}

	/**
	 * <p>
	 * Returns the time in seconds that are simulated during one simulation
	 * cycle.
	 * </p>
	 * <p>
	 * <i>This is NOT the running speed.</i>
	 * </p>
	 * 
	 * @return <p>
	 *         time in seconds that are simulated during one simulation cycle
	 *         </p>
	 */
	public int getSecondsPerCycle() {
		return secondsPerCycle;
	}

	/**
	 * Returns the speed of the simulation.
	 * <ul>
	 * <li>0 - Max speed <i>(No time between the execution of a simulation
	 * cycle)</i></li>
	 * <li>1 - Realtime <i>(A simulation cycle takes as long as the seconds that
	 * are simulated during this cycle)</i></li>
	 * <li>0 < simSpeed < 1 - Simulation runs faster</li>
	 * <li>1 < simSpeed <= Float.MAX_VALUE - Simulation runs slower</li>
	 * </ul>
	 * 
	 * @param simSpeed
	 *            time
	 */
	public float getSimulationSpeed() {
		return simulationSpeed;
	}

	private void calcOffset() {
		simulationTimeOffset = (int) ((simulationSpeed * secondsPerCycle) * 1000);
	}

	/**
	 * 
	 * @param simulationLenght
	 *            Total simulation Length in seconds
	 */
	public void setSimulationLenght(long simulationLenght) {
		if (!simulating)
			this.simulationLenght = simulationLenght;
	}

	/**
	 * 
	 * @return Total simulation length in seconds
	 */
	public long getSimulationLenght() {
		return simulationLenght;
	}

	public boolean isSimulating() {
		return simulating;
	}

	@Override
	public void terminated(Object subject) {
		// System.out.println("OpenTask: " + openTasks);
		synchronized (this) {
			if (openTasks > 0)
				openTasks--;
		}
	}

	public long getSimulationTime() {
		return timestamp;
	}

	public boolean isSimSpeedDelayed() {
		return simSpeedDelayed;
	}

	public int getProgress() {
		if (simulating && cycle > 0)
			return (int) ((cycle * secondsPerCycle * 100.f) / (float) (simulationLenght));
		return 0;
	}

	/**
	 * Returns the actual cycle
	 * 
	 * @return the actual cycle
	 */
	public long getCycle() {
		return cycle;
	}

	public boolean register(SchedulerHookInterface listener) {
		return listeners.add(listener);
	}

	public boolean unregister(SchedulerHookInterface listener) {
		return listeners.remove(listener);
	}

	public Executor getExecutor() {
		return executor;
	}

	public long getTotalCycles() {
		return totalCycles;
	}

}
