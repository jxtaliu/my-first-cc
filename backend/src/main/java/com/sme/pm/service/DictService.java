package com.sme.pm.service;

import com.sme.pm.dto.DictCodeDTO;
import com.sme.pm.entity.DictType;

import java.util.List;

public interface DictService {
    List<DictType> getAllTypes();

    List<DictCodeDTO> getCodesByType(String type);

    DictCodeDTO getCode(String type, String code);

    void refreshCache();
}
