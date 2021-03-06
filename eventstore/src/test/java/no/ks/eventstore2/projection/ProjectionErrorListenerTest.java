package no.ks.eventstore2.projection;

import akka.actor.Props;
import akka.testkit.TestActorRef;
import no.ks.eventstore2.testkit.EventStoreTestKit;
import no.ks.eventstore2.util.IdUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ProjectionErrorListenerTest extends EventStoreTestKit {


    private TestActorRef<ProjectionErrorListener> projectionErrorListener;

    @Before
    public void before() {
        projectionErrorListener = TestActorRef.create(actorSystem, Props.create(ProjectionErrorListener.class), IdUtil.createUUID());
    }

    @Test
    public void testAskErrorsReturnsEmptyList() {
        List<ProjectionError> projectionErrors = ProjectionErrorListener.askErrors(projectionErrorListener);
        assertNotNull(projectionErrors);
        assertEquals(0, projectionErrors.size());
    }
}
