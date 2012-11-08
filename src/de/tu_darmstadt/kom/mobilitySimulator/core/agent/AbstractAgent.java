package de.tu_darmstadt.kom.mobilitySimulator.core.agent;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import de.tu_darmstadt.kom.mobilitySimulator.agent.role.EarthquakeVictimRole;
import de.tu_darmstadt.kom.mobilitySimulator.agent.role.NormalRole;
import de.tu_darmstadt.kom.mobilitySimulator.core.scheduler.Scheduler;
import de.tu_darmstadt.kom.mobilitySimulator.filter.FilterFactory;

public abstract class AbstractAgent {

	private static int lastID = 0;

	protected int id;
	protected AbstractMobilityModel mobilityModel;
	protected AbstractMobilityModel previousMobilityModel;
	protected AbstractRole role;

	protected Queue<String> messages;
	protected float velocity;

	protected int resistance;
	protected float maxVelocity;

	protected int maxUp;
	protected int maxDown;

	protected boolean mobileCommunicationEnabled;

	public static final int VISUAL_RANGE = 15;
	public static final int WIFI_RANGE = 40;

	protected Set<AbstractAgent> agentTable;

	private Set<AbstractAgent> tmpAgentSet;

	public AbstractAgent(int x, int y, boolean mobileCommunicationEnabled) {
		// this.id = id;

		this.id = ++lastID;

		this.messages = new LinkedList<String>();
		this.velocity = 1;
		this.maxVelocity = 5;
		this.resistance = 5000;

		this.mobileCommunicationEnabled = mobileCommunicationEnabled;

		// TODO: let this become constructor parameter
		this.maxDown = 500;
		this.maxUp = 500;

		Scheduler.getInstance().getOutput().agentCreated(id, x, y);

		// // FIXME Aufruf hier noch nicht möglich, da noch nicht alle Parameter
		// // initialisiert wurden, die in die Textdatei geschrieben werden
		// sollen.
		// Scheduler.getInstance().getFileOutput().addKeyValuePair("create",
		// "true").addKeyValuePair("MobileCommunication",
		// mobileCommunicationEnabled ? "true" : "false").writeEntry(this);

		agentTable = new HashSet<AbstractAgent>();
		tmpAgentSet = new HashSet<AbstractAgent>();
	}

	public int getId() {
		return this.id;
	}

	public void behave() {
		if (mobileCommunicationEnabled
				&& EarthquakeVictimRole.getKnown().size() != EarthquakeVictimRole.totalVictims
						.size())
			broadcastVictimTable();
		if (role != null)
			role.behave();
	}

	public void setRole(AbstractRole role) {
		this.role = role;
		Scheduler.getInstance().getOutput()
				.agentStatusChanged(id, "role", role.getName());
	}

	public AbstractRole getRole() {
		return role;
	}

	public Queue<String> getMessageQueue() {
		return messages;
	}

	/**
	 * Retrieves, but does not remove, the head of the message queue, or returns
	 * null if the queue is empty.
	 * 
	 * @return the head of the message queue, or null if the queue is empty
	 */
	public String peekMessage() {
		return messages.peek();
	}

	/**
	 * Retrieves and removes the head of the message queue, or returns null if
	 * the queue is empty.
	 * 
	 * @return the head of the message queue, or null if the queue is empty
	 */
	public String pollMessage() {
		return messages.poll();
	}

	public boolean hasMessage() {
		return messages.size() > 0;
	}

	public void addMessage(String message) {
		messages.add(message);
	}

	/**
	 * Returns the number of elements in the messages collection. If the
	 * collection contains more than Integer.MAX_VALUE elements, returns
	 * Integer.MAX_VALUE.
	 * 
	 * @return the number of elements in the message collection
	 */
	public int getMessagesSize() {
		return messages.size();
	}

	/**
	 * Prints all messages to the console
	 */
	public void printAllMessages() {
		for (String msg : messages) {
			System.out.println(msg);
		}
	}

	public AbstractMobilityModel getMobilityModel() {
		return mobilityModel;
	}

	/**
	 * Sets a new MobilityModel and drops the old one.
	 * 
	 * @param mobilityModel
	 */
	public void setMobilityModel(AbstractMobilityModel mobilityModel) {
		this.mobilityModel = mobilityModel;
		this.previousMobilityModel = null;
		Scheduler
				.getInstance()
				.getOutput()
				.agentStatusChanged(id, "mobilityModel",
						mobilityModel.getName());
	}

	/**
	 * Sets a new MobilityModel and stores/drops the old one to support
	 * returning after finishing.
	 * 
	 * @param mobilityModel
	 * @param onCompleteReturnToPreviousModel
	 */
	public void setMobilityModel(AbstractMobilityModel mobilityModel,
			boolean onCompleteReturnToPreviousModel) {

		if (onCompleteReturnToPreviousModel)
			previousMobilityModel = mobilityModel;
		else
			previousMobilityModel = null;

		this.mobilityModel = mobilityModel;
		Scheduler
				.getInstance()
				.getOutput()
				.agentStatusChanged(id, "mobilityModel",
						mobilityModel.getName());
	}

	public abstract int getX();

	public abstract int getY();

	public abstract int getZ();

	public int[] getPos() {
		return new int[] { getX(), getY() };
	}

	public abstract void setX(int xPos);

	public abstract void setY(int yPos);

	public abstract void setZ(int zPos);

	public abstract void setPos(int x, int y, int z);

	public abstract void setPos(int x, int y);

	public abstract void setPos(int[] position);

	public float getVelocity() {
		return velocity;
	}

	public void setVelocity(float velocity) {
		this.velocity = velocity;
		mobilityModel.velocityChanged();
	}

	public boolean isMobileCommunicationEnabled() {
		return mobileCommunicationEnabled;
	}

	public void setMobileCommunicationEnabled(boolean mobComEnabled) {
		this.mobileCommunicationEnabled = mobComEnabled;
	}

	public String getRoleName() {
		return role.getName();
	}

	public float getMaxVelocity() {
		return maxVelocity;
	}

	public void setMaxVelocity(float maxVelocity) {
		this.maxVelocity = maxVelocity;
	}

	public AbstractMobilityModel getPreviousMobilityModel() {
		return previousMobilityModel;
	}

	public void setPreviousMobilityModel(
			AbstractMobilityModel previousMobilityModel) {
		this.previousMobilityModel = previousMobilityModel;
	}

	public int getResistance() {
		return resistance;
	}

	public void setResistance(int resistance) {
		this.resistance = resistance;
	}

	public int getMaxUp() {
		return maxUp;
	}

	public void setMaxUp(int maxUp) {
		this.maxUp = maxUp;
	}

	public int getMaxDown() {
		return maxDown;
	}

	public void setMaxDown(int maxDown) {
		this.maxDown = maxDown;
	}

	/********************************************************************
	 **************************** WIFI STUFF ****************************
	 ********************************************************************/

	public void broadcastVictimTable() {

		Object circle = Scheduler.mapEventRepository.getCircle(this.getX(),
				this.getY(), WIFI_RANGE);
		tmpAgentSet.clear();
		Scheduler.agentRepository.findIntersectingAgents(circle, FilterFactory
				.getFilter(FilterFactory.AGENT_ROLE_FILTER,
						"ALL,mobileCommunication=true"), tmpAgentSet);
		Scheduler.mapEventRepository.recycleShape(circle);
		for (AbstractAgent agent : tmpAgentSet) {
			agent.addVictimsToAgentTable(agentTable);
		}
	}

	public void addVictimsToAgentTable(Set<AbstractAgent> agentTable) {
		this.agentTable.addAll(agentTable);
		if (!(role instanceof EarthquakeVictimRole)
				&& !(role instanceof NormalRole))
			EarthquakeVictimRole.addKnown(agentTable);
	}

	public void addVictimToAgentTable(AbstractAgent agent) {
		this.agentTable.add(agent);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AbstractAgent) {
			return ((AbstractAgent) obj).getId() == this.id;
		} else {
			return obj == this;
		}
	}
}
