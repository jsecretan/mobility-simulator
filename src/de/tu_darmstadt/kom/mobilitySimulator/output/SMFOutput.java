package de.tu_darmstadt.kom.mobilitySimulator.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import de.tu_darmstadt.kom.mobilitySimulator.agent.mobilityModel.WalkToOnGradientMobilityModel;
import de.tu_darmstadt.kom.mobilitySimulator.core.OutputInterface;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.SchedulerHookInterface;

public class SMFOutput implements OutputInterface, SchedulerHookInterface {

	private boolean forward;
	private OutputInterface output;

	private File outputPath;
	private int fileIndex;
	private String prefix;

	private ArrayList<Float> smf_s;

	public SMFOutput(File outputPath, int fileIndex, String prefix) {
		init(outputPath, fileIndex, prefix);
		output = null;
		forward = false;
	}

	public SMFOutput(File outputFile, int fileIndex, String prefix,
			OutputInterface output) {
		init(outputFile, fileIndex, prefix);
		this.output = output;
		forward = true;
	}

	private void init(File outputPath, int fileIndex, String prefix) {
		smf_s = new ArrayList<Float>();
		this.fileIndex = fileIndex;
		this.prefix = prefix;
		System.out.println("Use as output: " + this.getClass().toString());
		this.outputPath = outputPath;
		Scheduler.getInstance().register(this);

		WalkToOnGradientMobilityModel.setEnableStatistics(true);
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
	public void eventCreated(int eventID, int x, int y) {
		if (forward)
			output.eventCreated(eventID, x, y);
	}

	@Override
	public void agentActivated(int id, boolean b) {
		if (forward)
			output.agentActivated(id, b);
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

		try {

			if (fileIndex == 0)
				fw = new FileWriter(new File(outputPath, prefix + "SMF.txt"));
			else
				fw = new FileWriter(new File(outputPath, prefix + "SMF("
						+ fileIndex + ").txt"));

			for (int i = 0; i < smf_s.size(); i++) {
				fw.write(i + "\t" + smf_s.get(i) + "\n");
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

		if (forward)
			output.close();
	}

	@Override
	public void preSimulation() {
		smf_s.add(WalkToOnGradientMobilityModel.getSMF());
	}

	@Override
	public void postSimulation() {
	}

	@Override
	public void preCycle() {
	}

	@Override
	public void postCycle() {
		smf_s.add(WalkToOnGradientMobilityModel.getSMF());
	}

	@Override
	public void preRun() {
	}

}
