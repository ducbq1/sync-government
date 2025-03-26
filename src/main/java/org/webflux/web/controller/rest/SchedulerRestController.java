package org.webflux.web.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.webflux.enumerate.Status;
import org.webflux.model.Toast;
import org.webflux.schedule.PausableTask;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/scheduler")
public class SchedulerRestController {

    private final ScheduledExecutorService scheduler;
    private final PausableTask task;
    private ScheduledFuture<?> scheduledFuture;


    @Autowired
    public SchedulerRestController(ScheduledExecutorService scheduler, PausableTask task) {
        this.scheduler = scheduler;
        this.task = task;
    }

    @PostMapping("/start")
    public ResponseEntity<Toast> startTask() {
        if (Objects.isNull(scheduledFuture) || scheduledFuture.isCancelled()) {
            scheduledFuture = scheduler.scheduleWithFixedDelay(task, 0, 60, TimeUnit.SECONDS);
            return ResponseEntity.ok(new Toast(Status.Success, "Thông báo", "Task started"));
        }
        return ResponseEntity.ok(new Toast(Status.Warning, "Thông báo", "Task started"));
    }

    @GetMapping(path = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @ResponseBody
    public Mono<Map<Long, Double>> events() {
        if (Objects.nonNull(scheduledFuture) && !scheduledFuture.isCancelled()) {
            return Mono.just(task.getValueProgress());
        } else {
            return Mono.just(Map.of(0L, 0D));
        }
    }

    @PostMapping("/pause")
    public ResponseEntity<Toast> pauseTask() {
        task.pause();
        return ResponseEntity.ok(new Toast(Status.Success, "Thông báo", "Task paused"));
    }

    @PostMapping("/resume")
    public ResponseEntity<Toast> resumeTask() {
        task.resume();
        return ResponseEntity.ok(new Toast(Status.Success, "Thông báo", "Task resumed"));
    }

    @PostMapping("/stop")
    public ResponseEntity<Toast> stopTask() {
        task.setValueTest(0);
        if (Objects.nonNull(scheduledFuture) && !scheduledFuture.isCancelled()) {
            scheduledFuture.cancel(true);
            return ResponseEntity.ok(new Toast(Status.Success, "Thông báo", "Task stopped"));
        }
        return ResponseEntity.ok(new Toast(Status.Warning, "Thông báo", "Task is not running"));
    }

    @PostMapping("/shutdown")
    public void shutdownTask() {
        // Shut down the scheduler
        scheduler.shutdown();

        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
                if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("Scheduler did not terminate");
                }
            }
        } catch (InterruptedException ex) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
