package no.ks.eventstore2.saga;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Inbox;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import akka.testkit.TestKit;
import com.typesafe.config.ConfigFactory;
import eventstore.Messages;
import no.ks.eventstore2.formProcessorProject.FormParsed;
import no.ks.eventstore2.formProcessorProject.FormReceived;
import org.joda.time.DateTime;
import org.junit.Test;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;


public class TiemoutSagaTest extends TestKit{

    static ActorSystem _system = ActorSystem.create("TestSys", ConfigFactory
            .load().getConfig("TestSys"));
    private SagaInMemoryRepository sagaInMemoryRepository;

    public TiemoutSagaTest() {
        super(_system);
        sagaInMemoryRepository = new SagaInMemoryRepository();
    }

    @Test
    public void schedulesAwake() {
        final ActorRef commandDispatcher = super.testActor();
        final Props sagaprop = Props.create(SagaTimerUt.class, "sagaid", commandDispatcher, sagaInMemoryRepository);

        final TestActorRef<SagaTimerUt> ref = TestActorRef.create(_system, sagaprop, super.testActor(), "sagatimerut");
        final SagaTimerUt saga = ref.underlyingActor();
        ref.tell(new FormParsed("1"), super.testActor());
        expectMsgClass(Messages.ScheduleAwake.class);
        ref.tell("awake", super.testActor());
        assertEquals(Saga.STATE_FINISHED, saga.getState());
    }

    @Test
    public void schedulesAwakeInTheFuture() throws TimeoutException {
        final ActorRef commandDispatcher = super.testActor();
        final Props sagaprop = Props.create(SagaTimerUt.class, "sagaid", commandDispatcher, sagaInMemoryRepository);


        final TestActorRef<SagaTimerUt> ref = TestActorRef.create(_system, sagaprop, super.testActor(), "sagatimerut");
        final SagaTimerUt saga = ref.underlyingActor();
        ref.tell(new FormReceived("1"), super.testActor());

        final Messages.ScheduleAwake receive = (Messages.ScheduleAwake) receiveOne(Duration.create(3, TimeUnit.SECONDS));

        assertTrue("Correct timeout value", receive.getAwake().getSeconds() - DateTime.now().plusSeconds(120).toDate().getTime()/1000 < 5);
    }

}
