package com.cutec.collect.controller;


import com.cutec.common.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.cutec.collect.service.TaskService;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@RestController
public class TaskController {


    private final ConcurrentHashMap<String, ReentrantLock> lockMap = new ConcurrentHashMap<>();


    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }


    
    @GetMapping("/ops/{type}")
    public Result<String> updateFCInfo(@PathVariable("type") String type) {
        ReentrantLock lock = lockMap.computeIfAbsent(type, k -> new ReentrantLock());
        if (lock.tryLock()) {
            try {
                switch (type) {
                    case "fix" -> taskService.pushFixTask();
                    case "f" -> taskService.updateFileTask();
                    case "nfo" -> taskService.nfoTask();
                    case "sukebei" -> taskService.collectSukebei();
                }
                return new Result<>("");
            } catch (Exception e) {
                log.error("{}", e.getMessage());
                return new Result<>(e.getMessage());
            } finally {
                lock.unlock();
                lockMap.computeIfPresent(type, (key, value) -> value.hasQueuedThreads() ? value : null);
            }
        } else {
            return new Result<>("wait");
        }
    }
}
