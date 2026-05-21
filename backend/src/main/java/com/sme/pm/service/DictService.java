package com.sme.pm.service;

import com.sme.pm.dto.DictCodeDTO;
import com.sme.pm.entity.DictCode;
import com.sme.pm.entity.DictType;

import java.util.List;

public interface DictService {
    List<DictType> getAllTypes();

    List<DictCodeDTO> getCodesByType(String type);

    DictCodeDTO getCode(String type, String code);

    void refreshCache();

    // DictType CRUD
    DictType getTypeById(Long id);

    DictType createType(DictType dictType);

    DictType updateType(DictType dictType);

    void deleteType(Long id);

    // DictCode CRUD
    List<DictCode> getItemsByTypeId(Long dictTypeId);

    List<DictCode> getAllItems();

    DictCode getItemById(Long id);

    DictCode createItem(DictCode dictCode);

    DictCode updateItem(DictCode dictCode);

    void deleteItem(Long id);
}
