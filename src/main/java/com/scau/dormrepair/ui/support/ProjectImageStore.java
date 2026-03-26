package com.scau.dormrepair.ui.support;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * 演示阶段把图片复制到项目 pics 目录，模拟图片落库前的本地存储。
 */
public final class ProjectImageStore {

    public static final int MAX_IMAGE_COUNT = 5;
    public static final long MAX_IMAGE_SIZE_BYTES = 8L * 1024 * 1024;

    private static final String PROJECT_PICS_DIR = "pics";
    private static final DateTimeFormatter FILE_PREFIX_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmssSSS");
    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(".png", ".jpg", ".jpeg", ".webp", ".bmp", ".gif");

    private ProjectImageStore() {
    }

    public static List<String> copyImagesToProject(List<File> sourceFiles) {
        if (sourceFiles == null || sourceFiles.isEmpty()) {
            return List.of();
        }

        List<File> normalizedFiles = normalizeFiles(sourceFiles);
        validateImageFiles(normalizedFiles);

        Path picsDirectory = resolveProjectRoot().resolve(PROJECT_PICS_DIR);
        ensureDirectory(picsDirectory);

        List<String> storedPaths = new ArrayList<>();
        for (File sourceFile : normalizedFiles) {
            Path sourcePath = sourceFile.toPath();
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

    public static void validateImageFiles(List<File> sourceFiles) {
        if (sourceFiles == null || sourceFiles.isEmpty()) {
            return;
        }

        if (sourceFiles.size() > MAX_IMAGE_COUNT) {
            throw new IllegalStateException("最多只能上传 " + MAX_IMAGE_COUNT + " 张图片。");
        }

        for (File sourceFile : sourceFiles) {
            if (sourceFile == null) {
                throw new IllegalStateException("图片文件不能为空。");
            }

            Path sourcePath = sourceFile.toPath();
            if (!Files.isRegularFile(sourcePath)) {
                throw new IllegalStateException("图片文件不存在，无法提交：" + sourceFile.getName());
            }

            String extension = extractExtension(sourceFile.getName()).toLowerCase(Locale.ROOT);
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new IllegalStateException("仅支持 png、jpg、jpeg、webp、bmp、gif 图片格式。");
            }

            try {
                if (Files.size(sourcePath) > MAX_IMAGE_SIZE_BYTES) {
                    throw new IllegalStateException("单张图片不能超过 8MB：" + sourceFile.getName());
                }
            } catch (IOException exception) {
                throw new IllegalStateException("读取图片大小失败：" + sourceFile.getName(), exception);
            }
        }
    }

    private static List<File> normalizeFiles(List<File> sourceFiles) {
        Set<Path> distinctPaths = new LinkedHashSet<>();
        List<File> normalizedFiles = new ArrayList<>();
        for (File sourceFile : sourceFiles) {
            if (sourceFile == null) {
                continue;
            }
            Path absolutePath = sourceFile.toPath().toAbsolutePath().normalize();
            if (distinctPaths.add(absolutePath)) {
                normalizedFiles.add(absolutePath.toFile());
            }
        }
        return normalizedFiles;
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
