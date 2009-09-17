package engine;

import unclassified.EventBus;

public class Engine {
	private final ProfileRegistry registry;
	private final ActionManager actMgr;
	private final TouchpointManager tpMgr;
	private final EventBus bus;

	public Engine(ProfileRegistry registry, ActionManager actMgr, TouchpointManager tpMgr, EventBus bus) {
		this.registry = registry;
		this.actMgr = actMgr;
		this.tpMgr = tpMgr;
		this.bus = bus;
	}

	public String toString() {
		return "Engine(registry=" + registry + ", actionManager=" + actMgr + ", touchpointManager=" + tpMgr + ", eventBus=" + bus + ')';
	}

}
