package de.tu_darmstadt.kom.mobilitySimulator.core;

public interface OutputInterface {

	public void agentStatusChanged(int agentId, String status, String value);

	public void agentPositionChanged(int agentID, int x, int y);

	public void agentVelocityChanged(int agentID, int velocity);

	public void agentCreated(int agentID, int x, int y);

	public void agentActivated(int id, boolean b);

	public void eventCreated(int eventID, int x, int y);

	public void eventStatusChanged(int eventID, String status, String value);

	public void eventPositionChanged(int eventID, int x, int y);

	public void close();
}
