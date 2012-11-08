package de.tu_darmstadt.kom.mobilitySimulator.output;

import de.tu_darmstadt.kom.mobilitySimulator.core.OutputInterface;

public class DummyOutput implements OutputInterface {

	@Override
	public void agentStatusChanged(int agentId, String status, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void agentPositionChanged(int agentID, int x, int y) {
		// TODO Auto-generated method stub

	}

	@Override
	public void agentVelocityChanged(int agentID, int velocity) {
		// TODO Auto-generated method stub

	}

	@Override
	public void agentCreated(int agentID, int x, int y) {
		// TODO Auto-generated method stub

	}
	
@Override
	public void agentActivated(int id, boolean b) {
		// TODO Auto-generated method stub
		
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
	public void close() {
		// TODO Auto-generated method stub

	}

}
