package de.tu_darmstadt.kom.mobilitySimulator.core.scheduler;

public interface SchedulerHookInterface {

	public void preSimulation();

	public void postSimulation();

	public void preCycle();

	public void postCycle();

	public void preRun();

}
