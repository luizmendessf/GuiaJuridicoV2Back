package org.guiajuridico.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class BlogArticleDetailDto {
    private Integer id;
    private String title;
    private String subtitle;
    private String slug;
    private String imagePath;
    private String content;
    private Boolean published;
    private String category;
    private String subcategory;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
