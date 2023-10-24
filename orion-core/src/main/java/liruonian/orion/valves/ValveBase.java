package liruonian.orion.valves;

import liruonian.orion.EventBus;
import liruonian.orion.Valve;

public abstract class ValveBase implements Valve {

    private EventBus eventBus;

    public ValveBase(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    protected EventBus getEventBus() {
        return this.eventBus;
    }
}
