package com.scau.dormrepair.ui.support;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 本地演示阶段把图片复制进项目 pics 目录，模拟云端地址落库。
 */
public final class ProjectImageStore {

    private static final String PROJECT_PICS_DIR = "pics";
    private static final DateTimeFormatter FILE_PREFIX_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS");

    private ProjectImageStore() {
    }

    public static List<String> copyImagesToProject(List<File> sourceFiles) {
        if (sourceFiles == null || sourceFiles.isEmpty()) {
            return List.of();
        }

        Path picsDirectory = resolveProjectRoot().resolve(PROJECT_PICS_DIR);
        ensureDirectory(picsDirectory);

        List<String> storedPaths = new ArrayList<>();
        for (File sourceFile : sourceFiles) {
            Path sourcePath = sourceFile.toPath();
            if (!Files.isRegularFile(sourcePath)) {
                throw new IllegalStateException("图片文件不存在，无法提交：" + sourceFile.getName());
            }

            String fileName = buildStoredFileName(sourceFile.getName());
            Path targetPath = picsDirectory.resolve(fileName);
            try {
                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException exception) {
                throw new IllegalStateException("图片复制到项目 pics 目录失败，请检查文件是否被占用。", exception);
            }
            storedPaths.add(PROJECT_PICS_DIR + "/" + fileName);
        }
        return storedPaths;
    }

    private static Path resolveProjectRoot() {
        Path current = Path.of("").toAbsolutePath();
        while (current != null) {
            if (Files.exists(current.resolve("pom.xml")) && Files.exists(current.resolve("src"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("未找到项目根目录，无法写入 pics 目录。");
    }

    private static void ensureDirectory(Path directory) {
        try {
            Files.createDirectories(directory);
        } catch (IOException exception) {
            throw new IllegalStateException("创建 pics 目录失败，请检查项目目录写权限。", exception);
        }
    }

    private static String buildStoredFileName(String originalFileName) {
        String extension = extractExtension(originalFileName);
        String prefix = LocalDateTime.now().format(FILE_PREFIX_FORMATTER);
        String token = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return prefix + "_" + token + extension;
    }

    private static String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex);
    }
}
