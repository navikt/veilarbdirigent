package no.nav.veilarbdirigent;

import lombok.SneakyThrows;
import no.nav.veilarbdirigent.repository.domain.Task;
import no.nav.veilarbdirigent.repository.domain.TaskStatus;
import no.nav.veilarbdirigent.repository.domain.TaskType;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestUtils {

    public static TaskType TASK_TYPE = new TaskType("mock");

    public static Task createTask(String id, String data) {
        return Task
                .builder()
                .id(id)
                .type(TASK_TYPE)
                .taskStatus(TaskStatus.PENDING)
                .jsonData(data)
                .build();
    }

    @SneakyThrows
    public static String readTestResourceFile(String fileName) {
        URL fileUrl = TestUtils.class.getClassLoader().getResource(fileName);
        Path resPath = Paths.get(fileUrl.toURI());
        return Files.readString(resPath);
    }

}
