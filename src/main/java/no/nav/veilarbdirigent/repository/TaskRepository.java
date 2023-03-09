package no.nav.veilarbdirigent.repository;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.collection.HashMap;
import io.vavr.collection.Map;
import lombok.RequiredArgsConstructor;

import no.nav.veilarbdirigent.repository.domain.Task;
import no.nav.veilarbdirigent.repository.domain.TaskStatus;
import no.nav.veilarbdirigent.repository.domain.TaskType;
import no.nav.veilarbdirigent.utils.SerializerUtils;
import no.nav.veilarbdirigent.utils.TimeUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TaskRepository {

    public static final String TASK_TABLE = "task";

    private final NamedParameterJdbcTemplate jdbc;

    @Transactional
    public void insert(List<Task> tasks) {
        tasks.forEach(this::insert);
    }

    private void insert(Task task) {
        var params = new MapSqlParameterSource()
                .addValue("id", task.getId())
                .addValue("type", task.getType().getType())
                .addValue("status", task.getTaskStatus().name())
                .addValue("data", task.getJsonData());

        var sql = """
                INSERT INTO TASK (ID, TYPE, STATUS, DATA)
                VALUES (:id, :type, :status, :data)
                """;

        jdbc.update(sql, params);
    }

    public Optional<Task> fetch(String taskId) {
        var params = new MapSqlParameterSource()
                .addValue("id", taskId);

        var sql = """
                SELECT * FROM TASK WHERE ID = :id FETCH FIRST 1 ROW ONLY
                """;

        RowMapper<Task> rowmapper = (rs, rowNum) -> toTask(rs);
        List<Task> taskList = jdbc.query(sql, params, rowmapper);
        if (taskList.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(taskList.get(0));
        }
    }

    public boolean hasTask(String taskId) {
        var params = new MapSqlParameterSource()
                .addValue("id", taskId);

        var sql = """
                SELECT * FROM TASK WHERE ID = :id FETCH FIRST 1 ROW ONLY
                """;

        RowMapper<Task> rowmapper = (rs, rowNum) -> toTask(rs);
        List<Task> tasks = jdbc.query(sql, params, rowmapper);


        return ! tasks.isEmpty();
    }

    public List<Task> fetchTasksReadyForExecution(int limit) {

        var params = new MapSqlParameterSource()
                .addValue("limit", limit)
                .addValue("statuser", List.of(TaskStatus.FAILED.name(), TaskStatus.PENDING.name()));

        var sql = """
                SELECT * FROM TASK WHERE STATUS IN (:statuser)
                AND NEXT_ATTEMPT <= CURRENT_TIMESTAMP
                ORDER BY CREATED
                FETCH FIRST :limit ROWS ONLY
                """;
        RowMapper<Task> rowmapper = (rs, rowNum) -> toTask(rs);
        return jdbc.query(sql, params, rowmapper);
    }

    public List<Task> fetchAllFailedTasks() {
        var params = new MapSqlParameterSource()
                .addValue("status", TaskStatus.FAILED.name());

        var sql = """
                SELECT * FROM TASK WHERE STATUS = :status
                """;
        RowMapper<Task> rowmapper = (rs, rowNum) -> toTask(rs);
        return jdbc.query(sql, params, rowmapper);
    }

    public Map<String, Integer> fetchStatusnumbers() {
        var sql = """
                SELECT STATUS, COUNT(*) AS NUM FROM TASK GROUP BY STATUS
                """;

        RowMapper<Tuple2<String, Integer>> rowMapper = (rs, i) -> toStatusnumbers(rs);

        List<Tuple2<String, Integer>> statusCounts = jdbc.query(sql, rowMapper);


//        Tuple2<String, Integer> result = SqlUtils.select(jdbc, TASK_TABLE, TaskRepository::toStatusnumbers)
//                .column("status")
//                .column("count(*) as num")
//                .groupBy("status")
//                .execute();

        if (statusCounts.isEmpty()) {
            return HashMap.empty();
        }

        return HashMap.ofEntries(statusCounts);
    }

    public int runNow(String taskId) {
        var params = new MapSqlParameterSource()
                .addValue("id", taskId);
        var sql = """
                UPDATE TASK SET ATTEMPTS = ATTEMPTS + 1,
                            NEXT_ATTEMPT = CURRENT_TIMESTAMP
                            WHERE ID = :id
                """;
        return jdbc.update(sql, params);
    }

    @SuppressWarnings("unchecked")
    private static Task toTask(ResultSet rs) throws SQLException {
        return Task.builder()
                .id(rs.getString("id"))
                .type(new TaskType(rs.getString("type")))
                .taskStatus(TaskStatus.valueOf(rs.getString("status")))
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

    public int setStatusForTask(Task task, TaskStatus taskStatus) {
        var params  = new MapSqlParameterSource()
                .addValue("id", task.getId())
                .addValue("status", taskStatus.name());

        var sql = """
                UPDATE TASK SET 
                    STATUS = :status,
                    LAST_ATTEMPT = CURRENT_TIMESTAMP
                WHERE ID = :id    
                """;

        if (taskStatus == TaskStatus.FAILED) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime nextRetry = TimeUtils.exponentialBackoff(task.getAttempts(), now);
            params
                    .addValue("next_attempt", nextRetry)
                    .addValue("error", task.getError());
            sql = """
                UPDATE TASK SET 
                    STATUS = :status,
                    LAST_ATTEMPT = CURRENT_TIMESTAMP,
                    NEXT_ATTEMPT = :next_attempt,
                    ATTEMPTS = ATTEMPTS + 1,
                    ERROR = :error
                WHERE ID = :id    
                """;
        }

        if (taskStatus == TaskStatus.OK) {
            params.addValue("result", task.getJsonResult());
            sql = """
                UPDATE TASK SET 
                    STATUS = :status,
                    LAST_ATTEMPT = CURRENT_TIMESTAMP,
                    RESULT = :result
                WHERE ID = :id    
                """;
        }

        return jdbc.update(sql, params);

/*
        LocalDateTime now = LocalDateTime.now();
        UpdateQuery query = SqlUtils.update(jdbc, TASK_TABLE)
                .whereEquals("id", task.getId())
                .set("status", taskStatus.name())
                .set("last_attempt", Timestamp.valueOf(now));

        if (taskStatus == TaskStatus.FAILED) {
            LocalDateTime nextRetry = TimeUtils.exponentialBackoff(task.getAttempts(), now);
            query.set("next_attempt", Timestamp.valueOf(nextRetry));
            query.set("attempts", task.getAttempts() + 1);
            query.set("error", task.getError());
        }

        if (taskStatus == TaskStatus.OK) {
            query.set("result", task.getJsonResult());
        }

        return query.execute();
        */
    }
}
