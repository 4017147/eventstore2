package no.ks.eventstore2.projection;

import akka.actor.ActorRef;
import akka.dispatch.Futures;
import no.ks.eventstore2.eventstore.Subscription;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

import java.util.concurrent.Callable;

import static akka.dispatch.Futures.future;

@Subscriber("Form")
public class FutureCallProjection extends Projection {

    public FutureCallProjection(ActorRef eventstoreConnection) {
        super(eventstoreConnection);
    }

    public Future<String> getString(){
        ExecutionContext ec = getContext().system().dispatcher();
        return Futures.<String>future(() -> "OK", ec);
    }

    public Future<String> getFailure(){
        ExecutionContext ec = getContext().system().dispatcher();
        return Futures.<String>future(() -> { throw new RuntimeException("Failing"); }, ec);
    }

    public int getInt(final int test){
        return test;
    }
}

