package com.epita.service;

import com.epita.common.utils.ErrorCode;
import com.epita.controller.contracts.FileData;
import com.mongodb.client.MongoClient;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import jakarta.ws.rs.core.MediaType;
import org.bson.Document;
import org.bson.types.ObjectId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Service for handling file storage operations using MongoDB GridFS.
 * Provides methods to store, retrieve, and delete files.
 */
@ApplicationScoped
public class FileStorageService {

    @Inject
    MongoClient mongoClient;

    private final String databaseName = "Epitweet";
    private static final Logger LOG = LoggerFactory.getLogger(PostService.class);

    /**
     * Stores a file in the GridFS storage.
     *
     * @param mediaFile the file to be stored, represented as a `FileUpload` object.
     * @return the ID of the stored file as a string, or null if the operation fails.
     */
    public String storeFile(FileUpload mediaFile) {
        if (mediaFile == null || mediaFile.fileName() == null || mediaFile
                .fileName()
                .isBlank()) {
            LOG.warn("Invalid file upload: file is null or filename is blank");
            return null;
        }
        LOG.info("Storing file: {}", mediaFile.fileName());
        try {
            Path uploadedPath = mediaFile.uploadedFile();
            try (InputStream fileStream = Files.newInputStream(uploadedPath)) {
                GridFSBucket gridFSBucket = GridFSBuckets.create(mongoClient.getDatabase(databaseName));

                GridFSUploadOptions options = new GridFSUploadOptions()
                        .metadata(new Document("contentType", mediaFile.contentType()));

                ObjectId fileObjectId = gridFSBucket.uploadFromStream(
                        mediaFile.fileName(),
                        fileStream,
                        options
                );
                LOG.info("File stored successfully with ID: {}", fileObjectId.toString());
                return fileObjectId.toString();
            }
        } catch (IOException e) {
            LOG.error("Error storing file: {}", e.getMessage());
            ErrorCode.FILE_STORAGE_FAILED.throwException(e.getMessage());
        }
        return null;
    }

    /**
     * Deletes a file from the GridFS storage.
     *
     * @param mediaId the ID of the file to be deleted.
     */
    public void deleteFile(String mediaId) {
        LOG.info("Deleting file with ID: {}", mediaId);
        try {
            GridFSBucket gridFSBucket = GridFSBuckets.create(mongoClient.getDatabase(databaseName));
            ObjectId fileId = new ObjectId(mediaId);
            gridFSBucket.delete(fileId);
            LOG.info("File with ID {} deleted successfully", mediaId);
        } catch (Exception e) {
            LOG.error("Error deleting file with ID {}: {}", mediaId, e.getMessage());
            ErrorCode.FILE_DELETION_FAILED.throwException(mediaId);
        }
    }


    /**
     * Retrieves a file from the GridFS storage.
     *
     * @param mediaId the ID of the file to be retrieved.
     * @return a `FileData` object containing the file's input stream, content type, and file name,
     * or null if the operation fails.
     */
    public FileData getFile(String mediaId) {
        LOG.info("Fetching file with ID: {}", mediaId);
        try {
            GridFSBucket gridFSBucket = GridFSBuckets.create(mongoClient.getDatabase(databaseName));
            ObjectId fileId = new ObjectId(mediaId);
            GridFSDownloadStream downloadStream = gridFSBucket.openDownloadStream(fileId);

            GridFSFile file = downloadStream.getGridFSFile();
            Document metadata = file.getMetadata();

            String contentType = metadata != null ? metadata.getString("contentType") : MediaType.APPLICATION_OCTET_STREAM;
            String fileName = !file
                    .getFilename()
                    .isEmpty() ? file.getFilename() : "file";

            LOG.info("File with ID {} fetched successfully", mediaId);
            return new FileData(downloadStream, contentType, fileName);
        } catch (Exception e) {
            LOG.error("Error fetching file with ID {}: {}", mediaId, e.getMessage());
            ErrorCode.FILE_RETRIEVAL_FAILED.throwException(e.getMessage());
        }
        return null;
    }
}
