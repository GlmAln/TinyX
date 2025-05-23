package com.epita.controller.contracts;

import jakarta.ws.rs.core.MediaType;
import lombok.Getter;

import java.io.InputStream;

/**
 * Represents file data for handling file uploads or downloads.
 * Encapsulates the input stream, content type, and file name of a file.
 */
@Getter
public class FileData {

    private final InputStream inputStream;
    private final String contentType;
    private final String fileName;

    /**
     * Constructs a new FileData instance.
     *
     * @param inputStream the input stream of the file.
     * @param contentType the content type of the file (defaults to "application/octet-stream" if null).
     * @param fileName    the name of the file (defaults to "file" if null).
     */
    public FileData(InputStream inputStream, String contentType, String fileName) {
        this.inputStream = inputStream;
        this.contentType = contentType != null ? contentType : MediaType.APPLICATION_OCTET_STREAM;
        this.fileName = fileName != null ? fileName : "file";
    }
}
