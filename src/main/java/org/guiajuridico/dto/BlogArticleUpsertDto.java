package org.guiajuridico.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BlogArticleUpsertDto {
    private String title;
    private String subtitle;
    private String slug;
    private String imagePath;
    private String content;
    private Boolean published;
    private String category;
    private String subcategory;
}
