package no.nav.veilarbdirigent.repository;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import no.nav.sbl.sql.SqlUtils;
import no.nav.sbl.sql.UpdateQuery;
import no.nav.sbl.sql.order.OrderClause;
import no.nav.sbl.sql.where.WhereClause;
import no.nav.veilarbdirigent.repository.domain.Status;
import no.nav.veilarbdirigent.repository.domain.Task;
import no.nav.veilarbdirigent.repository.domain.TaskType;
import no.nav.veilarbdirigent.utils.SerializerUtils;
import no.nav.veilarbdirigent.utils.TimeUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class TaskRepository {
    private static final String TASK_TABLE = "task";
    private final JdbcTemplate jdbc;

    public TaskRepository(JdbcTemplate jdbc) {
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
                .value("data", task.getJsonData())
                .execute();
    }

    public List<Task> fetchTasksReadyForExecution(int limit) {
        WhereClause statusCondition = WhereClause.equals("status", Status.FAILED.name())
                .or(WhereClause.equals("status", Status.PENDING.name()));
        WhereClause timeCondition  = WhereClause.lteq("next_attempt", Timestamp.valueOf(LocalDateTime.now()));
        WhereClause wc = timeCondition.and(statusCondition);
        OrderClause or = OrderClause.asc("created");

        return SqlUtils.select(jdbc, TASK_TABLE, TaskRepository::toTask)
                .column("*")
                .where(wc)
                .orderBy(or)
                .limit(limit)
                .executeToList();
    }

    public List<Task> fetchAllFailedTasks() {
        return SqlUtils.select(jdbc, TASK_TABLE, TaskRepository::toTask)
                .column("*")
                .where(WhereClause.equals("status", Status.FAILED.name()))
                .executeToList();
    }

    public Map<String, Integer> fetchStatusnumbers() {
        Tuple2<String, Integer> result = SqlUtils.select(jdbc, TASK_TABLE, TaskRepository::toStatusnumbers)
                .column("status")
                .column("count(*) as num")
                .groupBy("status")
                .execute();

        if (result == null) {
            return HashMap.empty();
        }

        return HashMap.ofEntries(result);
    }

    public int runNow(String taskId) {
        return SqlUtils.update(jdbc, TASK_TABLE)
                .whereEquals("id", taskId)
                .set("attempts", 1)
                .set("next_attempt", Timestamp.valueOf(LocalDateTime.now()))
            .execute();
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
                .jsonData(rs.getString("data"))
                .jsonResult(rs.getString("result"))
                .error(rs.getString("error"))
                .build();
    }

    private static Tuple2<String, Integer> toStatusnumbers(ResultSet rs) throws SQLException {
        return Tuple.of(rs.getString("status"), rs.getInt("num"));
    }

    public int setStatusForTask(Task task, Status status) {
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
            query.set("result", task.getJsonResult());
        }

        return query.execute();
    }
}
