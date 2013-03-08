package de.tu_darmstadt.kom.mobilitySimulator.agent.role;

import de.tu_darmstadt.kom.mobilitySimulator.agent.role.PedestrianRole;
import java.util.Set;
import java.util.HashSet;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;

// Right now, our social networkers are based off of normal pedestrians
// TODO, Maybe we should put this social networking stuff into the abstract agent, or some sort of decorator
public class SocialNetworkerRole extends PedestrianRole {
	// Set of all friends in the network
	private Set<AbstractAgent> allFriends = new HashSet<AbstractAgent>();
	// Set of close friends about whom we wish to know
	private Set<AbstractAgent> closeFriends = new HashSet<AbstractAgent>();

	// Demographic information
	public enum Gender {Male, Female};

	private Gender gender;

	private int age;

	public SocialNetworkerRole(AbstractAgent agent) {
		super(agent);
	}

	public Set<AbstractAgent> getFriends() {
		return allFriends;
	}

}