package com.epita.repository.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@With
public class StoredPostModel {
    public UUID post_id;
    public LocalDateTime insert_date;
}
