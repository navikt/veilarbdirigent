package no.nav.fo.veilarbdirigent.dao;

import lombok.SneakyThrows;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Utils {

    @SneakyThrows
    public static LocalDateTime readTimestamp(ResultSet rs, String name) {
        Timestamp timestamp = rs.getTimestamp(name);
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}
