package com.epita.common.api.request;

import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

/**
 * Represents a request for creating a post.
 * This class is used to handle form data for posts, including text, reply ID, repost ID, and media.
 */
public class PostsRequest {

    /**
     * The text content of the post.
     * This field is optional and can be null.
     */
    @RestForm("text")
    public String text;

    /**
     * The ID of the post being replied to.
     * This field is optional and can be null.
     */
    @RestForm("replyId")
    public String replyId;

    /**
     * The ID of the post being reposted.
     * This field is optional and can be null.
     */
    @RestForm("repostId")
    public String repostId;

    /**
     * The media file associated with the post.
     * This field is optional and can be null.
     * It is represented as a binary file upload.
     */
    @RestForm("media")
    @Schema(type = SchemaType.STRING, format = "binary", implementation = FileUpload.class)
    public FileUpload media;
}