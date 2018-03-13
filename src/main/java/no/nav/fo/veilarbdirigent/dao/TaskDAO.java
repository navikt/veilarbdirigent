package no.nav.fo.veilarbdirigent.dao;

import io.vavr.collection.List;
import no.nav.fo.veilarbdirigent.core.Status;
import no.nav.fo.veilarbdirigent.core.Task;
import no.nav.sbl.sql.SqlUtils;
import no.nav.sbl.sql.where.WhereClause;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.ResultSet;

public class TaskDAO {
    private static final String TASK_TABLE = "task";
    private final DataSource ds;
    private final JdbcTemplate jdbc;

    public TaskDAO(DataSource ds, JdbcTemplate jdbc) {
        this.ds = ds;
        this.jdbc = jdbc;
    }

    public void setStateForTask(Task task, Status status) {
        SqlUtils.update(jdbc, TASK_TABLE)
                .set("status", status)
                .whereEquals("id", task.getId())
                .execute();
    }

    public List<Task> fetchTasks() {
        WhereClause wc = WhereClause.equals("status", Status.FAILED.name())
                .or(WhereClause.equals("status", Status.PENDING.name()));

        return List.ofAll(SqlUtils.select(ds, TASK_TABLE, TaskDAO::toTask)
                .column("*")
                .where(wc)
                .executeToList());
    }

    private static Task toTask(ResultSet rs) {
        return Task.builder().build();
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
}
