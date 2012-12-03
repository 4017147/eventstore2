package no.ks.eventstore2.eventstore;

import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.UntypedActorFactory;
import no.ks.eventstore2.json.Adapter;
import no.ks.eventstore2.json.DateTimeTypeConverter;
import org.joda.time.DateTime;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Component
public class EventStoreFactory implements UntypedActorFactory {

	private DataSource ds;
	private List<ActorRef> remoteActors = new ArrayList<ActorRef>();

	public Actor create() {
       return new EventStore(ds, getAdapters(), remoteActors);
    }

    public void setDs(DataSource ds) {
        this.ds = ds;
    }

	public void addRemoteEventStores(ActorRef ref){
		this.remoteActors.add(ref);
	}

    private List<Adapter> getAdapters() {
        List<Adapter> gsonAdapters = new ArrayList<Adapter>();
        Adapter jodaTimeAdapter = new Adapter(DateTime.class, new DateTimeTypeConverter());
        gsonAdapters.add(jodaTimeAdapter);
        return gsonAdapters;
    }
}
