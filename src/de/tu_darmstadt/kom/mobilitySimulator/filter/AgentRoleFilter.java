package de.tu_darmstadt.kom.mobilitySimulator.filter;

import de.tu_darmstadt.kom.linkedRTree.FilterInterface;
import de.tu_darmstadt.kom.mobilitySimulator.agent.role.EarthquakeVictimRole;
import de.tu_darmstadt.kom.mobilitySimulator.agent.role.FirstResponderInterface;
import de.tu_darmstadt.kom.mobilitySimulator.agent.role.FleeRole;
import de.tu_darmstadt.kom.mobilitySimulator.agent.role.NormalRole;
import de.tu_darmstadt.kom.mobilitySimulator.agent.role.VictimRole;
import de.tu_darmstadt.kom.mobilitySimulator.core.agent.AbstractAgent;

public class AgentRoleFilter implements FilterInterface {

	private String[] attributes;

	public AgentRoleFilter(String attributeString) {
		attributes = attributeString.split(",");
	}

	@Override
	public boolean filter(Object obj) {
		boolean valid = true;

		if (obj instanceof AbstractAgent) {

			if (attributes[0].equals("ALL") && attributes.length > 1) {
				if (attributes[1].equals("mobileCommunication=true")) {
					return ((AbstractAgent) obj).isMobileCommunicationEnabled();
				} else {
					valid = false;
				}
			} else if (attributes[0].equals("FirstResponder")) {
				return ((AbstractAgent) obj).getRole() instanceof FirstResponderInterface;
			}

			// Check for rolename
			if (!((AbstractAgent) obj).getRole().getName()
					.equals(attributes[0])) {
				valid = false;
			} else {
				// Check for other attributes
				if (attributes.length > 1) {
					for (int i = 0; i < attributes.length; i++) {
						// TODO use reflection to generalize
						if (valid && attributes[i].equals("rescued=false")) {
							if (obj instanceof VictimRole
									&& ((VictimRole) ((AbstractAgent) obj)
											.getRole()).isRescued())
								valid = false;
						} else if (attributes[i]
								.equals("onWayToCollectionPoint=false")) {
							if (obj instanceof FleeRole
									&& ((FleeRole) ((AbstractAgent) obj)
											.getRole())
											.isOnWayToCollectionPoint())
								valid = false;
						} else if (attributes[i]
								.equals("noCollectionPointSet=true")) {
							if (obj instanceof NormalRole
									&& ((NormalRole) ((AbstractAgent) obj)
											.getRole()).isCollectionPointSet())
								valid = false;
						} else if (attributes[i].equals("unknown")) {
							return !EarthquakeVictimRole.getKnown().contains(
									obj);
						}

					}
				}
			}

		} else
			return false;

		return valid;
	}
}
