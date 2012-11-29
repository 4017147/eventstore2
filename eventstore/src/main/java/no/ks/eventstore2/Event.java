package no.ks.eventstore2;

import org.joda.time.DateTime;

public abstract class Event {

    protected String aggregateId;

    protected DateTime created;

    public String getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public DateTime getCreated() {
        return created;
    }

    public void setCreated(DateTime created) {
        this.created = created;
    }
}