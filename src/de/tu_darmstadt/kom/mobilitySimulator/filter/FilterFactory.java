package de.tu_darmstadt.kom.mobilitySimulator.filter;

import java.util.HashMap;
import java.util.Map;

import de.tu_darmstadt.kom.linkedRTree.FilterInterface;

public class FilterFactory {

	public static final int AGENT_ROLE_FILTER = 0;
	public static final int EVENT_TYPE_FILTER = 1;

	private static Map<String, AgentRoleFilter> agentRoleFilter = new HashMap<String, AgentRoleFilter>();
	private static Map<String, EventTypeFilter> eventTypeFilter = new HashMap<String, EventTypeFilter>();

	public static FilterInterface getFilter(int filterType, String attribute) {
		switch (filterType) {
		case AGENT_ROLE_FILTER:
			if (agentRoleFilter.containsKey(attribute)) {
				return agentRoleFilter.get(attribute);
			} else {
				AgentRoleFilter f = new AgentRoleFilter(attribute);
				agentRoleFilter.put(attribute, f);
				return f;
			}

		case EVENT_TYPE_FILTER:
			if (eventTypeFilter.containsKey(attribute)) {
				return eventTypeFilter.get(attribute);
			} else {
				EventTypeFilter f = new EventTypeFilter(attribute);
				eventTypeFilter.put(attribute, f);
				return f;
			}

		default:
			return null;
		}
	}

}
