package com.sme.pm.service;

import com.sme.pm.entity.DataStandard;

import java.util.List;
import java.util.Map;

public interface IDataStandardService {
    List<DataStandard> getAll();

    DataStandard getById(Long id);

    List<DataStandard> getByType(String type);

    DataStandard create(DataStandard dataStandard);

    DataStandard update(Long id, DataStandard dataStandard);

    void delete(Long id);

    Map<String, Object> getDetail(Long id);

    void share(Long id, Map<String, Object> shareParams);
}
