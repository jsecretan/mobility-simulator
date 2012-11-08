package de.tu_darmstadt.kom.mobilitySimulator.core.watchdog;

public interface WatchdogStateListener {

	public void memoryStateChanged(short state);

	public void reportToWatchdog();

	public String getName();

	public void unregisterFromWatchdog();
}
