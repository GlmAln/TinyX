package com.epita.controller.contracts;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

/**
 * Represents a multipart request for creating or updating a post in the Repo-Social service.
 * Encapsulates the text, reply ID, repost ID, and media file associated with the post.
 */
public class PostRequestMultiPart {

    /**
     * The text content of the post.
     */
    @RestForm("text")
    public String text;

    /**
     * The ID of the post being replied to, if applicable.
     */
    @RestForm("replyId")
    public String replyId;

    /**
     * The ID of the post being reposted, if applicable.
     */
    @RestForm("repostId")
    public String repostId;

    /**
     * The media file associated with the post.
     * Represented as a binary file upload.
     */
    @RestForm("media")
    @Schema(type = SchemaType.STRING, format = "binary", implementation = FileUpload.class)
    public FileUpload media;
}
