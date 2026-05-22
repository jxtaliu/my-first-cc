package com.sme.pm.dto;

import lombok.Data;

@Data
public class TaskAttachmentDTO {
    private Long taskId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String mimeType;
}
