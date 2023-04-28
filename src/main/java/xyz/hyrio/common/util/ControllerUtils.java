package xyz.hyrio.common.util;

import jakarta.servlet.http.HttpServletResponse;
import lombok.experimental.UtilityClass;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.springframework.util.StringUtils.hasText;
import static xyz.hyrio.common.util.ObjectUtils.requireHasText;

@UtilityClass
public class ControllerUtils {
    private static void transferInToOut(InputStream in, HttpServletResponse response) throws IOException {
        try (BufferedInputStream bis = new BufferedInputStream(in);
             BufferedOutputStream bos = new BufferedOutputStream(response.getOutputStream())) {
            bis.transferTo(bos);
            bos.flush();
        }
    }

    public static void setFileDownloadHeaders(String filename, long fileSize, HttpServletResponse response) {
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        if (hasText(filename)) {
            response.setHeader(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(filename, StandardCharsets.UTF_8).build().toString());
        }
        if (fileSize > 0) {
            response.addHeader(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize));
        }
    }

    public static void showImage(String filename, InputStream in, HttpServletResponse response) throws IOException {
        response.setContentType(hasText(filename) && filename.toLowerCase().endsWith(".png") ? MediaType.IMAGE_PNG_VALUE : MediaType.IMAGE_JPEG_VALUE);
        transferInToOut(in, response);
    }

    public static void downloadFile(Path srcPath, HttpServletResponse response) throws IOException {
        downloadFile(srcPath, response, srcPath.getFileName().toString());
    }

    public static void downloadFile(Path srcPath, HttpServletResponse response, String filename) throws IOException {
        if (!Files.exists(srcPath)) {
            throw new IOException("srcPath not found: " + srcPath);
        }
        requireHasText(filename, "filename is required");
        if (Files.isDirectory(srcPath)) {
            setFileDownloadHeaders(srcPath.getFileName() + ".zip", -1, response);
            try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
                Files.walkFileTree(srcPath, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        zos.putNextEntry(new ZipEntry(srcPath.relativize(file).toString()));
                        Files.copy(file, zos);
                        zos.closeEntry();
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        zos.putNextEntry(new ZipEntry(srcPath.relativize(dir) + "/"));
                        zos.closeEntry();
                        return FileVisitResult.CONTINUE;
                    }
                });
                zos.flush();
            }
        } else if (Files.isRegularFile(srcPath)) {
            try (BufferedInputStream in = new BufferedInputStream(Files.newInputStream(srcPath))) {
                downloadFile(filename, in, response, Files.size(srcPath));
            }
        } else {
            throw new IOException("file type not supported: " + srcPath);
        }
    }

    public static void downloadFile(String filename, InputStream in, HttpServletResponse response) throws IOException {
        downloadFile(filename, in, response, in.available());
    }

    public static void downloadFile(String filename, InputStream in, HttpServletResponse response, long fileSize) throws IOException {
        setFileDownloadHeaders(filename, fileSize, response);
        transferInToOut(in, response);
    }
}
