package de.tu_darmstadt.kom.mobilitySimulator.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.tu_darmstadt.kom.mobilitySimulator.core.OutputInterface;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;
import de.tu_darmstadt.kom.mobilitySimulator.core.map.DiscreteMap;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.SchedulerHookInterface;

public class AgentDensityEvaluationOutput implements OutputInterface,
		SchedulerHookInterface {

	private boolean forward;
	private OutputInterface output;

	private Object lock;

	private long[][] densityMap;

	private File outputPath;

	private int fileIndex;

	private boolean useSpecialDensities;

	private Collection<String> roles;
	private Map<String, long[][]> roleDensityMaps;
	private String prefix;

	public AgentDensityEvaluationOutput(File outputPath, int fileIndex,
			String prefix) {
		init(outputPath, fileIndex, prefix);
		output = null;
		forward = false;
	}

	public AgentDensityEvaluationOutput(File outputPath, int fileIndex,
			String prefix, OutputInterface output) {
		init(outputPath, fileIndex, prefix);
		this.output = output;
		forward = true;
	}

	public AgentDensityEvaluationOutput(File outputPath, int fileIndex,
			String prefix, Collection<String> specialAgents) {
		init(outputPath, fileIndex, prefix);
		output = null;
		forward = false;
		initRoleDensities(specialAgents);
	}

	public AgentDensityEvaluationOutput(File outputPath, int fileIndex,
			String prefix, Collection<String> specialAgents,
			OutputInterface output) {
		init(outputPath, fileIndex, prefix);
		this.output = output;
		forward = true;
		initRoleDensities(specialAgents);
	}

	private void init(File outputPath, int fileIndex, String prefix) {
		this.prefix = prefix;
		System.out.println("Use as output: " + this.getClass().toString());
		lock = new Object();
		densityMap = new long[DiscreteMap.sizeY][DiscreteMap.sizeX];
		this.outputPath = outputPath;
		this.fileIndex = fileIndex;
		Scheduler.getInstance().register(this);
		useSpecialDensities = false;
	}

	private void initRoleDensities(Collection<String> specialAgents) {
		useSpecialDensities = true;
		roles = new ArrayList<String>();
		roleDensityMaps = new HashMap<String, long[][]>();
		for (String string : specialAgents) {
			roles.add(string);
			roleDensityMaps.put(string,
					new long[DiscreteMap.sizeY][DiscreteMap.sizeX]);
		}
	}

	@Override
	public void agentStatusChanged(int agentId, String status, String value) {
		if (forward)
			output.agentStatusChanged(agentId, status, value);

	}

	@Override
	public void agentPositionChanged(int agentID, int x, int y) {
		if (forward)
			output.agentPositionChanged(agentID, x, y);
	}

	@Override
	public void agentVelocityChanged(int agentID, int velocity) {
		if (forward)
			output.agentVelocityChanged(agentID, velocity);
	}

	@Override
	public void agentCreated(int agentID, int x, int y) {
		if (forward)
			output.agentCreated(agentID, x, y);
	}

	@Override
	public void agentActivated(int id, boolean b) {
		if (forward)
			output.agentActivated(id, b);
	}

	@Override
	public void eventCreated(int eventID, int x, int y) {
		if (forward)
			output.eventCreated(eventID, x, y);
	}

	@Override
	public void eventStatusChanged(int eventID, String status, String value) {
		if (forward)
			output.eventStatusChanged(eventID, status, value);
	}

	@Override
	public void eventPositionChanged(int eventID, int x, int y) {
		if (forward)
			output.eventPositionChanged(eventID, x, y);
	}

	@Override
	public void close() {
		FileWriter fw = null;
		FileWriter fwObst = null;

		try {
			if (fileIndex != 0) {
				fw = new FileWriter(new File(outputPath, prefix + "density("
						+ fileIndex + ").txt"));
				fwObst = new FileWriter(new File(outputPath, prefix
						+ "obstacles(" + fileIndex + ").txt"));
			} else {
				fw = new FileWriter(
						new File(outputPath, prefix + "density.txt"));
				fwObst = new FileWriter(new File(outputPath, prefix
						+ "obstacles.txt"));
			}

			for (int y = 0; y < densityMap.length; y++) {
				boolean first = true;
				for (int x = 0; x < densityMap[y].length; x++) {
					fw.write(x + "\t" + y + "\t" + densityMap[y][x] + "\n");
					fwObst.write(x + "\t" + y + "\t"
							+ DiscreteMap.getInstance().getObstacles()[y][x]
							+ "\n");
				}
			}
			fw.close();
			fwObst.close();

		} catch (IOException e) {
			if (fw != null)
				try {
					fw.close();
				} catch (IOException e1) {
				}
			e.printStackTrace();
		}

		if (useSpecialDensities) {
			for (Entry<String, long[][]> entry : roleDensityMaps.entrySet()) {
				long[][] densityMap = entry.getValue();
				try {
					if (fileIndex == 0)
						fw = new FileWriter(new File(outputPath, prefix
								+ "density_" + entry.getKey() + ".txt"));
					else
						fw = new FileWriter(new File(outputPath, prefix
								+ "density_" + entry.getKey() + "(" + fileIndex
								+ ").txt"));

					for (int y = 0; y < densityMap.length; y++) {
						boolean first = true;
						for (int x = 0; x < densityMap[y].length; x++) {
							fw.write(x + "\t" + y + "\t" + densityMap[y][x]
									+ "\n");
						}
					}
					fw.close();

				} catch (IOException e) {
					if (fw != null)
						try {
							fw.close();
						} catch (IOException e1) {
						}
					e.printStackTrace();
				}
			}
		}

		if (forward)
			output.close();
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
		Collection<AbstractAgent> agents = Scheduler.agentRepository.values();
		for (AbstractAgent agent : agents) {
			densityMap[agent.getY()][agent.getX()]++;

			if (useSpecialDensities
					&& roles.contains(agent.getRole().getName())) {
				roleDensityMaps.get(agent.getRole().getName())[agent.getY()][agent
						.getX()]++;
			}
		}
	}

	@Override
	public void preRun() {
	}

}
