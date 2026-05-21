package com.sme.pm.dto;

import lombok.Data;

@Data
public class DictCodeDTO {
    private String code;
    private String name;
    private String nameEn;
    private String nameZh;
    private Integer sortOrder;
    private String extra;

    public DictCodeDTO() {}

    public DictCodeDTO(String code, String name, String nameEn, String nameZh, Integer sortOrder, String extra) {
        this.code = code;
        this.name = name;
        this.nameEn = nameEn;
        this.nameZh = nameZh;
        this.sortOrder = sortOrder;
        this.extra = extra;
    }
}
