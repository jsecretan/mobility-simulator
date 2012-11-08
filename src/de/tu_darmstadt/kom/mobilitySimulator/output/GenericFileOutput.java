package de.tu_darmstadt.kom.mobilitySimulator.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import de.tu_darmstadt.kom.mobilitySimulator.core.OutputInterface;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.SchedulerHookInterface;

public class GenericFileOutput implements OutputInterface,
		SchedulerHookInterface, Runnable {

	File outputFile;
	Object lock;

	private String outputPath;
	private Writer fw;
	private boolean writerEnabled;
	private boolean compressed;

	private Map<Integer, Map<String, String>> agentBuffer;

	private Map<String, String> lastLoadedAgentMap;
	private int lastLoadedAgentMapID;

	private Queue<String> optionalParameters;
	private long time;

	private StringBuilder sb;
	private int sbCapacity;
	private boolean closed;

	public GenericFileOutput(File outputFile, boolean logEvents)
			throws IOException {
		init(outputFile, logEvents, false, 2000);
	}

	public GenericFileOutput(File outputFile, boolean logEvents,
			int bufferLength) throws IOException {
		init(outputFile, logEvents, false, bufferLength);
	}

	public GenericFileOutput(File outputFile, boolean logEvents,
			boolean compressed, int bufferLength) throws IOException {
		init(outputFile, logEvents, compressed, bufferLength);
	}

	private void init(File outputFile, boolean logEvents, boolean compressed,
			int bufferLength) throws IOException {

		System.out.println("Use as output: " + this.getClass().toString());

		closed = false;
		lock = new Object();

		this.outputFile = outputFile;

		this.compressed = compressed;

		writerEnabled = false;

		optionalParameters = new LinkedList<String>();

		// FIXME: Compressed FileWriter not implemented
		try {
			fw = new FileWriter(outputFile);
			fw.write("#time;agent;x;y;z;velocity;options\n");
		} catch (IOException e) {
			writerEnabled = false;
			throw e;
		}

		agentBuffer = new HashMap<Integer, Map<String, String>>();

		sbCapacity = bufferLength;
		sb = new StringBuilder(bufferLength);

		Scheduler.getInstance().register(this);
	}

	@Override
	public void run() {
		while (!closed) {
			if (sb.capacity() - sb.length() < 100) {
				StringBuilder sbCopy;
				synchronized (lock) {
					sbCopy = sb;
					sb = new StringBuilder(sbCapacity);
				}

				try {
					fw.write(sbCopy.toString());
				} catch (IOException e) {
					// TODO: handle exception
					e.printStackTrace();
					if (fw != null)
						try {
							fw.close();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
				}

			} else {
				Thread.yield();
			}
		}
	}

	public void close() {
		closed = true;
		if (fw != null)
			try {
				synchronized (lock) {
					fw.write(sb.toString());
				}
				fw.close();
			} catch (IOException e) {
			}
	}

	public void flushToBuffer(long time) {
		synchronized (lock) {
			lastLoadedAgentMapID = 0;

			for (Entry<Integer, Map<String, String>> agent : agentBuffer
					.entrySet()) {
				int id = agent.getKey();

				sb.append(time + ";" + id + ";");

				if (agent.getValue().containsKey("position")) {
					sb.append(agent.getValue().remove("position") + ";");

					if (agent.getValue().containsKey("velocity")) {
						sb.append(agent.getValue().remove("velocity"));
					} else {
						AbstractAgent a;
						a = (AbstractAgent) Scheduler.agentRepository.get(id);

						if (a == null) {
							a = (AbstractAgent) Scheduler.agentRepository
									.getPassiveAgents().get(id);

							if (a == null)
								a = (AbstractAgent) Scheduler.agentRepository
										.getPutQueue().get(id);
						}

						if (a == null)
							sb.append(0);
						else
							sb.append(a.getVelocity());

					}
				} else {
					agent.getValue().remove("velocity");
					AbstractAgent a;

					a = (AbstractAgent) Scheduler.agentRepository.get(id);

					if (a == null)
						a = (AbstractAgent) Scheduler.agentRepository
								.getPassiveAgents().get(id);
					if (a == null)
						a = (AbstractAgent) Scheduler.agentRepository
								.getPutQueue().get(id);

					if (a == null) {
						sb.append("0;0;0;0");
					} else
						sb.append(a.getX() + ";" + a.getY() + ";" + a.getZ()
								+ ";" + a.getVelocity());
				}

				if (agent.getValue().size() > 0) {
					sb.append(";[");
					boolean first = true;
					for (Entry<String, String> optionalParam : agent.getValue()
							.entrySet()) {
						if (first)
							first = false;
						else
							sb.append(";");
						sb.append(optionalParam.getKey() + "="
								+ optionalParam.getValue());
					}
					sb.append("]");
				}
				sb.append("\n");
			}

			agentBuffer.clear();
		}
	}

	// public void write(String strg) {
	// if (writerEnabled) {
	// try {
	// fw.write(strg);
	// } catch (Exception e) {
	// // TODO: handle exception
	// e.printStackTrace();
	// if (fw != null)
	// try {
	// fw.close();
	// } catch (IOException e1) {
	// // TODO Auto-generated catch block
	// e1.printStackTrace();
	// }
	// }
	// }
	// }

	// public OutputInterface addKeyValuePair(String key, String value) {
	// optionalParameters.add(key + "=" + value);
	// return this;
	// }

	public void writeEntry(AbstractAgent agent) {
		if (writerEnabled) {

			StringBuilder sb = new StringBuilder();

			sb.append(Scheduler.getInstance().getSimulationTime() + ";");
			sb.append(agent.getId() + ";");
			sb.append(agent.getX() + ";");
			sb.append(agent.getY() + ";");
			sb.append(agent.getZ() + ";");
			sb.append(agent.getVelocity() + ";");

			if (!optionalParameters.isEmpty()) {
				sb.append('[');
				boolean first = true;
				while (!optionalParameters.isEmpty()) {
					if (!first)
						sb.append(',');
					else
						first = false;
					sb.append(optionalParameters.poll());
				}
				sb.append(']');
			}

			sb.append('\n');

			try {
				fw.write(sb.toString());

			} catch (Exception e) {
				// TODO: handle exception
				e.printStackTrace();
				if (fw != null)
					try {
						fw.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			}
		}
	}

	public String getOutputPath() {
		return outputPath;
	}

	// public void setOutputPath(String outputPath) throws IOException {
	// this.outputPath = outputPath;
	// writerEnabled = true;
	//
	// optionalParameters = new LinkedList<String>();
	//
	// // TODO: Compressed FileWriter not handled yet. Implement
	// try {
	// fw = new FileWriter(this.outputPath);
	// fw.write("#time;agent;x;y;z;velocity;options\n");
	// } catch (IOException e) {
	// writerEnabled = false;
	// throw e;
	// }
	// }

	@Override
	public void agentStatusChanged(int agentId, String status, String value) {
		putToAgentBuffer(agentId, status, value);
	}

	@Override
	public void agentPositionChanged(int agentID, int x, int y) {
		putToAgentBuffer(agentID, "position", x + ";" + y + ";0");
	}

	@Override
	public void agentVelocityChanged(int agentID, int velocity) {
		putToAgentBuffer(agentID, "velocity", velocity + "");
	}

	@Override
	public void agentCreated(int agentID, int x, int y) {
		putToAgentBuffer(agentID, "created", "true");
	}

	@Override
	public void agentActivated(int agentID, boolean activated) {
		if (activated) {
			putToAgentBuffer(agentID, "activated", "true");
		} else {
			putToAgentBuffer(agentID, "activated", "false");
		}

	}

	private void putToAgentBuffer(int id, String key, String value) {
		synchronized (lock) {
			if (lastLoadedAgentMapID != id) {
				Map<String, String> agentMap;
				if (!agentBuffer.containsKey(id))
					lastLoadedAgentMap = new HashMap<String, String>();
				else
					lastLoadedAgentMap = agentBuffer.get(id);
				lastLoadedAgentMapID = id;
			}
			lastLoadedAgentMap.put(key, value);
			agentBuffer.put(id, lastLoadedAgentMap);
		}
	}

	@Override
	public void eventCreated(int eventID, int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void eventStatusChanged(int eventID, String status, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void eventPositionChanged(int eventID, int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postCycle() {
		time = Scheduler.getInstance().getSimulationTime();
		Scheduler.getInstance().getExecutor().execute(new Runnable() {
			@Override
			public void run() {
				flushToBuffer(time);
			}
		});
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
	public void preRun() {
	}
}
