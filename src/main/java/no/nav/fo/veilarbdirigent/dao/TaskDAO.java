package no.nav.fo.veilarbdirigent.dao;

import io.vavr.collection.List;
import no.nav.fo.veilarbdirigent.coreapi.Status;
import no.nav.fo.veilarbdirigent.coreapi.Task;
import no.nav.sbl.sql.SqlUtils;
import no.nav.sbl.sql.where.WhereClause;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TaskDAO {
    private static final String TASK_TABLE = "task";
    private final JdbcTemplate jdbc;

    public TaskDAO(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public void setStateForTask(Task task, Status status) {
        SqlUtils.update(jdbc, TASK_TABLE)
                .set("status", status.name())
                .whereEquals("id", task.getId())
                .execute();
    }

    @Transactional
    public void insert(List<Task> tasks) {
        tasks.forEach(this::insert);
    }

    private void insert(Task task) {
        SqlUtils.insert(jdbc, TASK_TABLE)
                .value("id", task.getId())
                .value("type", task.getType())
                .value("status", task.getStatus().name())
                .value("data", task.getData())
                .execute();
    }

    public List<Task> fetchTasks() {
        WhereClause wc = WhereClause.equals("status", Status.FAILED.name())
                .or(WhereClause.equals("status", Status.PENDING.name()));

        return List.ofAll(SqlUtils.select(jdbc, TASK_TABLE, TaskDAO::toTask)
                .column("*")
                .where(wc)
                .executeToList());
    }

    private static Task toTask(ResultSet rs) throws SQLException {
        return Task.builder()
                .id(rs.getString("id"))
                .type(rs.getString("type"))
                .status(Status.valueOf(rs.getString("status")))
                .created(rs.getTimestamp("created").toLocalDateTime())
                .attempts(rs.getInt("attempts"))
                .nextAttempt(Utils.readTimestamp(rs, "next_attempt"))
                .lastAttempt(Utils.readTimestamp(rs, "last_attempt"))
                .data(rs.getString("data"))
                .error(rs.getString("error"))
                .build();
    }
}
