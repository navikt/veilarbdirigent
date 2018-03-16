package no.nav.fo.veilarbdirigent.core.outgoingmessage;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.Tuple;
import io.vavr.collection.List;
import io.vavr.collection.Map;
import io.vavr.control.Either;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.function.Function;

@Slf4j
public class OutgoingMessageDefinitionLoader {
    private static Map<String, String> taskcontent;
    private static final ObjectMapper mapper = new ObjectMapper();

    public static <T extends OutgoingMessageDefinition> Option<T> get(String name, Class<T> type) {
        if (taskcontent == null) {
            taskcontent = readAllFiles();
        }

        return taskcontent
                .get(name)
                .onEmpty(() -> log.warn("Requested '" + name + "', but taskdefinition does not exist."))
                .flatMap((content) -> readvalue(content, type).toOption());
    }

    private static <T> Either<Exception, T> readvalue(String content, Class<T> type) {
        try {
            return Either.right(mapper.readValue(content, type));
        } catch (Exception e) {
            log.error("Parsing content", e);
            return Either.left(e);
        }
    }

    private static Map<String, String> readAllFiles() {
        return Option.of(OutgoingMessageDefinitionLoader.class.getClassLoader().getResource("taskdefinitions"))
                .map(URL::getFile)
                .map(File::new)
                .map(OutgoingMessageDefinitionLoader::listFiles)
                .getOrElse(List.empty())
                .map((file) -> Tuple.of(findName(file), readFile(file).getOrElse((String) null)))
                .toMap(Function.identity());
    }

    private static String findName(File file) {
        String filenameWithExtension = file.getName();
        int extensionIndex = filenameWithExtension.lastIndexOf(".");

        return filenameWithExtension.substring(0, extensionIndex);
    }

    private static Either<Exception, String> readFile(File file) {
        try {
            return Either.right(new String(Files.readAllBytes(Paths.get(file.getAbsolutePath()))));
        } catch (Exception e) {
            log.error("Reading file", e);
            return Either.left(e);
        }
    }

    private static List<File> listFiles(File directory) {
        return Option.of(directory.listFiles())
                .map(List::of)
                .getOrElse(List.empty())
                .flatMap((File file) -> {
                    if (file.isFile()) {
                        return List.of(file);
                    } else {
                        return listFiles(directory);
                    }
                });
    }
}
