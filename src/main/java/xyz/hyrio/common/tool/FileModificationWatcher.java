package xyz.hyrio.common.tool;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static xyz.hyrio.common.util.ObjectUtils.requireHasTextElse;

@Slf4j
public class FileModificationWatcher {
    @Getter private final Path watchedFile;
    @Getter private final Consumer<Path> callback;
    @Getter private final Duration callbackMinInterval;
    @Getter private final String runnerThreadName;
    @Getter private final boolean runCallbackImmediately;

    private final ScheduledExecutorService executor;
    private long lastRunTimestamp = 0L;

    private void run() {
        long now = System.currentTimeMillis();
        if (now - lastRunTimestamp < callbackMinInterval.toMillis()) {
            log.debug("File modification watcher callback is called too frequently, run it later");
            executor.schedule(() -> callback.accept(watchedFile), callbackMinInterval.toMillis(), TimeUnit.MILLISECONDS);
        } else {
            lastRunTimestamp = now;
            callback.accept(watchedFile);
        }
    }

    public FileModificationWatcher(Path watchedFilePath, Consumer<Path> callback, Duration callbackMinInterval, String runnerThreadName, boolean runCallbackImmediately) throws IOException {
        this.callback = callback;
        this.callbackMinInterval = callbackMinInterval;
        this.runnerThreadName = runnerThreadName;
        this.runCallbackImmediately = runCallbackImmediately;
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setName(requireHasTextElse(runnerThreadName, "file-modification-watcher-runner"));
            thread.setDaemon(true);
            return thread;
        });

        if (!Files.exists(watchedFilePath)) {
            log.debug("File {} does not exist, creating it", watchedFilePath);
            Files.createFile(watchedFilePath);
        }
        this.watchedFile = watchedFilePath.toRealPath();

        Path parentFolder = watchedFile.getParent();
        WatchService watcher = FileSystems.getDefault().newWatchService();
        parentFolder.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
        new Thread(() -> {
            while (true) {
                try {
                    WatchKey key = watcher.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY && event.context().toString().equals(watchedFile.getFileName().toString())) {
                            log.debug("File {} has been modified...", watchedFile);
                            executor.execute(this::run);
                        }
                    }
                    key.reset();
                } catch (InterruptedException e) {
                    log.error("Thread interrupted", e);
                }
            }
        }, "file-modification-watcher-thread").start();

        if (runCallbackImmediately) {
            executor.execute(this::run);
        }
        log.info("Watching file modification: {}", watchedFile);
    }

    /**
     * Call before modifying the file manually.
     */
    public void setLastRunTimestampToNow() {
        lastRunTimestamp = System.currentTimeMillis();
    }
}
