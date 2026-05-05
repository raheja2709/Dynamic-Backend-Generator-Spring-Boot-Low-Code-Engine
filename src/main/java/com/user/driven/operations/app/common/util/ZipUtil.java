package com.user.driven.operations.app.common.util;

import java.io.*;
import java.nio.file.*;
import java.util.zip.*;

public class ZipUtil {

    public static Path zipFolder(Path sourceDir, String zipName) {

        Path zipPath = Paths.get("generated-projects/" + zipName + ".zip");

        try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(zipPath))) {

            Files.walk(sourceDir)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(sourceDir.relativize(path).toString());

                        try {
                            zs.putNextEntry(zipEntry);
                            Files.copy(path, zs);
                            zs.closeEntry();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });

        } catch (IOException e) {
            throw new RuntimeException("ZIP creation failed", e);
        }

        return zipPath;
    }
}