package de.tu_darmstadt.kom.mobilitySimulator.output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import de.tu_darmstadt.kom.mobilitySimulator.core.OutputInterface;
import de.tu_darmstadt.kom.mobilitySimulator.core.map.MapFactory;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.SchedulerHookInterface;
import de.tu_darmstadt.kom.mobilitySimulator.map.EHCacheMapFactory;

public class CycleDurationEHCachSizeOutput implements OutputInterface,
		SchedulerHookInterface {

	private boolean forward;
	private OutputInterface output;

	ArrayList<int[]> times;
	private File outputPath;
	private int fileIndex;
	private String prefix;

	public CycleDurationEHCachSizeOutput(File outputPath, int fileIndex,
			String prefix) {
		init(outputPath, fileIndex, prefix);
		output = null;
		forward = false;
	}

	public CycleDurationEHCachSizeOutput(File outputFile, int fileIndex,
			String prefix, OutputInterface output) {
		init(outputFile, fileIndex, prefix);
		this.output = output;
		forward = true;
	}

	private void init(File outputPath, int fileIndex, String prefix) {
		this.fileIndex = fileIndex;
		this.prefix = prefix;
		System.out.println("Use as output: " + this.getClass().toString());
		times = new ArrayList<int[]>();
		this.outputPath = outputPath;
		Scheduler.getInstance().register(this);
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
				fw = new FileWriter(new File(outputPath, prefix
						+ "cycleDuration.txt"));
			else
				fw = new FileWriter(new File(outputPath, prefix
						+ "cycleDuration(" + fileIndex + ").txt"));

			int i = 0;
			for (Iterator<int[]> iterator = times.iterator(); iterator
					.hasNext(); i++) {
				int[] data = iterator.next();
				fw.write(i + "\t" + data[0] + "\t" + data[1] + "\t" + data[2]
						+ "\t" + data[3] + "\n");
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
		times.add(new int[] {
				(int) Scheduler.getInstance().getExactCycleDuration(),
				(int) ((EHCacheMapFactory) MapFactory.getInstance()).getSize(),
				(int) ((EHCacheMapFactory) MapFactory.getInstance()).getHits(),
				(int) ((EHCacheMapFactory) MapFactory.getInstance())
						.getMisses() });
	}

	@Override
	public void postSimulation() {
	}

	@Override
	public void preCycle() {
	}

	@Override
	public void postCycle() {
		times.add(new int[] {
				(int) Scheduler.getInstance().getExactCycleDuration(),
				(int) ((EHCacheMapFactory) MapFactory.getInstance()).getSize(),
				(int) ((EHCacheMapFactory) MapFactory.getInstance()).getHits(),
				(int) ((EHCacheMapFactory) MapFactory.getInstance())
						.getMisses() });
	}

	@Override
	public void preRun() {
	}

}
