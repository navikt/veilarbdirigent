package no.nav.fo.veilarbdirigent.dao;

import io.vavr.collection.List;
import no.nav.fo.veilarbdirigent.core.Status;
import no.nav.fo.veilarbdirigent.core.Task;
import no.nav.sbl.sql.SQLFunction;
import no.nav.sbl.sql.SqlUtils;
import no.nav.sbl.sql.where.WhereClause;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.ResultSet;

public class TaskDAO {

    private final DataSource ds;
    private final JdbcTemplate jdbc;

    public TaskDAO(DataSource ds, JdbcTemplate jdbc) {
        this.ds = ds;
        this.jdbc = jdbc;
    }

    public void setStateForTask(Task task, Status status) {
        SqlUtils.update(jdbc, "task")
                .set("status", status)
                .whereEquals("id", task.getId())
                .execute();
    }

    public List<Task> fetchTasks() {
        WhereClause wc = WhereClause.equals("status", Status.FAILED)
                .or(WhereClause.equals("status", Status.PENDING));

        java.util.List<Task> tasks = SqlUtils.select(ds, "task", (SQLFunction<ResultSet, Task>) resultSet -> new Task() {
        })
                .column("*")
                .where(wc)
                .executeToList();

        return List.ofAll(tasks);
    }
}
