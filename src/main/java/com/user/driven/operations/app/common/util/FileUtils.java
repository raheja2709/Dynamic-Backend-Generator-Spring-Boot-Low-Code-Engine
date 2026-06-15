package com.user.driven.operations.app.common.util;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.springframework.stereotype.Component;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Utility class for file-related operations such as compressing project
 * directories into ZIP archives.
 * <p>
 * Used primarily to package generated project files for download.
 * </p>
 * 
 * @author Jatin Raheja
 */
@Component
public class FileUtils {

	/**
	 * Creates a ZIP archive in memory from the specified source directory path.
	 *
	 * @param sourcePath the root directory to zip
	 * @return a byte array containing the zipped contents
	 * @throws IOException if an I/O error occurs while reading files or writing the ZIP archive
	 */
	public byte[] createZipFile(Path sourcePath) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try (ZipArchiveOutputStream zipOut = new ZipArchiveOutputStream(baos)) {
			try (Stream<Path> paths = Files.walk(sourcePath)) {
				paths.filter(Files::isRegularFile).forEach(file -> {
					try {
						String relativePath = sourcePath.relativize(file).toString();
						ZipArchiveEntry entry = new ZipArchiveEntry(relativePath);
						entry.setSize(Files.size(file));
						zipOut.putArchiveEntry(entry);
						Files.copy(file, zipOut);
						zipOut.closeArchiveEntry();
					} catch (IOException e) {
						throw new RuntimeException("Error adding file to zip: " + file, e);
					}
				});
			}
		}

		return baos.toByteArray();
	}
}
