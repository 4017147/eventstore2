package no.ks.eventstore2.saga;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

public abstract class SqlSagaRepository extends SagaRepository {
	private static Logger log = LoggerFactory.getLogger(SagaDatasourceRepository.class);

	private JdbcTemplate template;

	public SqlSagaRepository(DataSource dataSource) {
		template = new JdbcTemplate(dataSource);
	}

	@Override
	public void saveState(String sagaStateId, String sagaid, byte state) {
		log.debug("Saving state {} sagaid {} state "+ state, sagaStateId, sagaid);
		int i = template.queryForObject("select count(0) from saga where id = ? and clazz = ?", new Object[]{sagaid, sagaStateId}, Integer.class );
		if(i > 0) {
			template.update(getUpdateSagaSql(), state, sagaid, sagaStateId);
		} else {
			template.update(getInsertSagaSql(), new Object[]{sagaid, sagaStateId, state});
		}
	}

	@Override
	public byte getState(String sagaStateId, String sagaid) {
		int result = 0;
		try{
			result = template.queryForObject(getSelectStateSql(), new Object[]{sagaid, sagaStateId}, Integer.class);
		} catch (EmptyResultDataAccessException e){

		}
		if(result > Byte.MAX_VALUE) {
			throw new RuntimeException("Failed to convert to byte " + result);
		}
		log.debug("Loading state from repository for clz " + sagaStateId + " sagaid " + sagaid + " state " + result);
		return (byte) result;
	}

	public void readAllStatesToNewRepository(final SagaRepository repository){
		final List<State> list = new ArrayList<State>();
		template.query("select state,id,clazz from Saga",new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet resultSet) throws SQLException {
				list.add(new State(resultSet.getString("clazz"),resultSet.getString("id"),(byte)resultSet.getInt("state")));
			}
		});
		repository.saveStates(list);
	}

	@Override
	public void doBackup(String backupdir, String backupfilename) {

	}

	@Override
	public long loadLatestJournalID(String aggregate) {
		return 0;
	}

	@Override
	public void saveLatestJournalId(String aggregate, long latestJournalId) {

	}

	@Override
	public void saveStates(List<State> list) {
		for (State state : list) {
			saveState(state.getSagaStateId(), state.getId(), state.getState());
		}
	}

	@Override
	public void close() {

	}

	@Override
	public void open() {

	}

	protected abstract String getUpdateSagaSql();

	protected abstract String getInsertSagaSql();

	protected abstract String getSelectStateSql();

}
