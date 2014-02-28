package no.ks.eventstore2.saga;

import java.util.List;

public abstract class SagaRepository {

	public abstract void saveState(Class<? extends Saga> clz, String sagaid, byte state);

    public abstract byte getState(Class<? extends Saga> clz, String sagaid);

    public abstract void close();

    public abstract void open();
    public abstract void readAllStatesToNewRepository(final SagaRepository repository);

    public abstract void doBackup(String backupdir, String backupfilename);

    public abstract String loadLatestJournalID(String aggregate);

    public abstract void saveLatestJournalId(String aggregate, String latestJournalId);

    public abstract void saveStates(List<State> list);
}