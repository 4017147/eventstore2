package no.ks.eventstore2.projection;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import no.ks.eventstore2.Event;
import no.ks.eventstore2.eventstore.Subscription;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public abstract class Projection extends UntypedActor {

    protected ActorRef eventStore;

    protected Projection() {
        init();
    }

    @Override
    public void preStart(){
        System.out.println(getSelf().path().toString());
        eventStore = getContext().actorFor("akka://default/user/eventStore");
        subscribe(eventStore);
    }

    @Override
    public void onReceive(Object o) throws Exception{
        if (o instanceof Event)
            handleEvent((Event) o);
    }

    public void handleEvent(Event event) {
        Method method = handleEventMap.get(event.getClass());
        if (method != null)
            try {
                method.invoke(this, event);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
    }

    private Map<Class<? extends Event>, Method> handleEventMap = null;

    private void init() {
        handleEventMap = new HashMap<Class<? extends Event>, Method>();
        try {
            Class<? extends Projection> projectionClass = this.getClass();
            ListensTo annotation = projectionClass.getAnnotation(ListensTo.class);
            if (annotation != null) {
                Class[] handledEventClasses = annotation.value();
                for (Class<? extends Event> handledEventClass : handledEventClasses) {
                    Method handleEventMethod = projectionClass.getMethod("handleEvent", handledEventClass);
                    handleEventMap.put(handledEventClass, handleEventMethod);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void subscribe(ActorRef eventStore){
        ListensTo annotation = getClass().getAnnotation(ListensTo.class);
        if (annotation != null)
            for (String aggregate : annotation.aggregates())
                eventStore.tell(new Subscription(aggregate), self());
    }

}










