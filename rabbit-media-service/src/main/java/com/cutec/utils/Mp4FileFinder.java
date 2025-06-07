package com.cutec.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class Mp4FileFinder {

    public static List<Path> findMp4Files(String folderPath) {
        try (Stream<Path> filePathStream = Files.walk(Paths.get(folderPath))) {
            return filePathStream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".mp4"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.info("{}", e.getMessage());
            return List.of(); // 如果出现异常，返回一个空列表
        }
    }

}

