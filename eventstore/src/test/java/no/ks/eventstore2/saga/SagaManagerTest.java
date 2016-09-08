package no.ks.eventstore2.saga;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.testkit.TestActorRef;
import akka.testkit.TestKit;
import com.typesafe.config.ConfigFactory;
import eventstore.Messages;
import no.ks.eventstore2.eventstore.IncompleteSubscriptionPleaseSendNew;
import no.ks.eventstore2.eventstore.Subscription;
import no.ks.eventstore2.formProcessorProject.*;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashMap;

public class SagaManagerTest extends TestKit {

    static ActorSystem _system = ActorSystem.create("TestSys", ConfigFactory
            .load().getConfig("TestSys"));

    private SagaInMemoryRepository sagaInMemoryRepository;


    public SagaManagerTest() {
        super(_system);
        sagaInMemoryRepository = new SagaInMemoryRepository();
    }

    @Test
    public void testIncomingEventWithNewIdIsDispatchedToNewSaga() throws Exception {
        final Props props = getSagaManagerProps();
        final TestActorRef<SagaManager> ref = TestActorRef.create(_system, props, "sagaManager");

        FormReceived formReceived = new FormReceived("1");
        ref.tell(formReceived, super.testActor());
        expectMsgClass(ParseForm.class);
    }

    @Test
    public void testIncomingEventWithExistingIdIsDispatchedToExistingSaga() throws Exception {
        final Props props = getSagaManagerProps();
        final TestActorRef<SagaManager> ref = TestActorRef.create(_system, props, "sagaManager1");
        FormReceived formReceived = new FormReceived("2");
        ref.tell(formReceived, super.testActor());
        expectMsgClass(ParseForm.class);
        FormParsed formParsed = new FormParsed("2");
        ref.tell(formParsed, super.testActor());
        expectMsgClass(DeliverForm.class);
    }

    @Test
    public void testSagaStateSurvivesExceptionAndRestart() throws Exception {
        final Props props = getSagaManagerProps();
        final TestActorRef<SagaManager> ref = TestActorRef.create(_system, props, "sagaManager2");
        FormReceived formReceived = new FormReceived("2");
        ref.tell(formReceived, super.testActor());
        ((HashMap <SagaCompositeId, ActorRef>)ReflectionTestUtils.getField(ref.underlyingActor(), "sagas")).get(new SagaCompositeId(FormProcess.class, "2")).tell("BOOM!", super.testActor());
        expectMsgClass(ParseForm.class);
        FormParsed formParsed = new FormParsed("2");
        ref.tell(formParsed, super.testActor());
        expectMsgClass(DeliverForm.class);
    }

    private Props getSagaManagerProps() {
        final ActorRef testActor = super.testActor();
        return SagaManager.mkProps(_system, testActor, sagaInMemoryRepository, _system.actorOf(Props.create(DummyActor.class)), "no");
    }

    @Test
    public void testIncompleteSubscribeSendsCorrectJournalid() throws Exception {
        final Props props = SagaManager.mkProps(_system, super.testActor(), sagaInMemoryRepository, super.testActor(), "no");
        final TestActorRef<SagaManager> ref = TestActorRef.create(_system, props, "sagaManagerIncomplete");
        expectMsgClass(Messages.Subscription.class);
        expectMsgClass(Messages.Subscription.class);
        FormReceived msg = new FormReceived("3");
        msg.setJournalid("1");
        ref.tell(msg, super.testActor());
        expectMsgClass(ParseForm.class);
        ref.tell(Messages.IncompleteSubscriptionPleaseSendNew.newBuilder().setAggregateType(msg.getAggregateType()).build(),super.testActor());
        expectMsg(Messages.Subscription.newBuilder().setAggregateType(msg.getAggregateType()).setFromJournalId(1).build());
    }
}
