package no.nav.fo.veilarbdirigent.core.dao;

import io.vavr.collection.List;
import no.nav.fo.veilarbdirigent.core.api.Status;
import no.nav.fo.veilarbdirigent.core.api.Task;
import no.nav.fo.veilarbdirigent.core.api.TaskType;
import no.nav.fo.veilarbdirigent.utils.SerializerUtils;
import no.nav.fo.veilarbdirigent.utils.TimeUtils;
import no.nav.sbl.sql.SqlUtils;
import no.nav.sbl.sql.UpdateQuery;
import no.nav.sbl.sql.where.WhereClause;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class TaskDAO {
    private static final String TASK_TABLE = "task";
    private final JdbcTemplate jdbc;

    public TaskDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public void insert(List<Task> tasks) {
        tasks.forEach(this::insert);
    }

    private void insert(Task task) {
        SqlUtils.insert(jdbc, TASK_TABLE)
                .value("id", task.getId())
                .value("type", task.getType().getType())
                .value("status", task.getStatus().name())
                .value("data", SerializerUtils.serialize(task.getData()))
                .execute();
    }

    public List<Task> fetchTasks() {
        WhereClause statusCondition = WhereClause.equals("status", Status.FAILED.name())
                .or(WhereClause.equals("status", Status.PENDING.name()));
        WhereClause timeCondition  = WhereClause.lteq("next_attempt", Timestamp.valueOf(LocalDateTime.now()));
        WhereClause wc = timeCondition.and(statusCondition);

        return List.ofAll(SqlUtils.select(jdbc, TASK_TABLE, TaskDAO::toTask)
                .column("*")
                .where(wc)
                .executeToList());
    }

    @SuppressWarnings("unchecked")
    private static Task toTask(ResultSet rs) throws SQLException {
        return Task.builder()
                .id(rs.getString("id"))
                .type(new TaskType(rs.getString("type")))
                .status(Status.valueOf(rs.getString("status")))
                .created(rs.getTimestamp("created").toLocalDateTime())
                .attempts(rs.getInt("attempts"))
                .nextAttempt(SerializerUtils.deserialize(rs.getTimestamp("next_attempt")))
                .lastAttempt(SerializerUtils.deserialize(rs.getTimestamp("last_attempt")))
                .data(SerializerUtils.deserialize(rs.getString("data")))
                .result(SerializerUtils.deserialize(rs.getString("result")))
                .error(rs.getString("error"))
                .build();
    }

    public int setStatusForTask(Task<?, ?> task, Status status) {
        LocalDateTime now = LocalDateTime.now();
        UpdateQuery query = SqlUtils.update(jdbc, TASK_TABLE)
                .whereEquals("id", task.getId())
                .set("status", status.name())
                .set("last_attempt", Timestamp.valueOf(now));

        if (status == Status.FAILED) {
            LocalDateTime nextRetry = TimeUtils.exponentialBackoff(task.getAttempts(), now);
            query.set("next_attempt", Timestamp.valueOf(nextRetry));
            query.set("attempts", task.getAttempts() + 1);
            query.set("error", task.getError());
        }
        if (status == Status.OK) {
            query.set("result", SerializerUtils.serialize(task.getResult()));
        }

        return query.execute();
    }
}
