package org.guiajuridico.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class BlogArticleSummaryDto {
    private Integer id;
    private String title;
    private String subtitle;
    private String slug;
    private String imagePath;
    private Boolean published;
    private String category;
    private String subcategory;
    private Timestamp createdAt;
}
