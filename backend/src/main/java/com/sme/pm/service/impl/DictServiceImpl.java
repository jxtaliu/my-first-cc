package com.sme.pm.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sme.pm.dto.DictCodeDTO;
import com.sme.pm.entity.DictCode;
import com.sme.pm.entity.DictType;
import com.sme.pm.mapper.DictCodeMapper;
import com.sme.pm.mapper.DictTypeMapper;
import com.sme.pm.service.DictService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DictServiceImpl implements DictService {

    private static final String CACHE_PREFIX = "dict:";
    private static final long CACHE_TTL = 3600;

    private final DictTypeMapper dictTypeMapper;
    private final DictCodeMapper dictCodeMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public DictServiceImpl(DictTypeMapper dictTypeMapper, DictCodeMapper dictCodeMapper,
                          StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.dictTypeMapper = dictTypeMapper;
        this.dictCodeMapper = dictCodeMapper;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<DictType> getAllTypes() {
        return dictTypeMapper.selectList(null);
    }

    @Override
    public List<DictCodeDTO> getCodesByType(String type) {
        String cacheKey = CACHE_PREFIX + type;

        try {
            String cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return objectMapper.readValue(cached, new TypeReference<List<DictCodeDTO>>() {});
            }
        } catch (JsonProcessingException e) {
            // Cache miss or error, fallback to DB
        }

        List<DictCode> codes = dictCodeMapper.findByTypeCode(type);
        List<DictCodeDTO> result = codes.stream()
                .map(this::toDTO)
                .toList();

        try {
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(result), CACHE_TTL, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            // Ignore cache write error
        }

        return result;
    }

    @Override
    public DictCodeDTO getCode(String type, String code) {
        DictCode dictCode = dictCodeMapper.findByTypeCodeAndCode(type, code);
        if (dictCode == null) {
            return null;
        }
        return toDTO(dictCode);
    }

    @Override
    public void refreshCache() {
        List<DictType> types = getAllTypes();
        for (DictType type : types) {
            clearTypeCache(type.getCode());
        }
    }

    // DictType CRUD

    @Override
    public DictType getTypeById(Long id) {
        return dictTypeMapper.selectById(id);
    }

    @Override
    public DictType createType(DictType dictType) {
        dictTypeMapper.insert(dictType);
        return dictType;
    }

    @Override
    public DictType updateType(DictType dictType) {
        dictTypeMapper.updateDictType(dictType);
        clearTypeCache(dictTypeMapper.selectById(dictType.getId()).getCode());
        return dictType;
    }

    @Override
    public void deleteType(Long id) {
        DictType type = dictTypeMapper.selectById(id);
        if (type == null) {
            throw new IllegalArgumentException("Dictionary type not found");
        }
        int itemCount = dictTypeMapper.countItemsByTypeId(id);
        if (itemCount > 0) {
            throw new IllegalArgumentException("Cannot delete type with associated items");
        }
        dictTypeMapper.deleteById(id);
        clearTypeCache(type.getCode());
    }

    // DictCode CRUD

    @Override
    public List<DictCode> getItemsByTypeId(Long dictTypeId) {
        return dictCodeMapper.findByDictTypeId(dictTypeId);
    }

    @Override
    public List<DictCode> getAllItems() {
        return dictCodeMapper.selectList(null);
    }

    @Override
    public DictCode getItemById(Long id) {
        return dictCodeMapper.selectById(id);
    }

    @Override
    public DictCode createItem(DictCode dictCode) {
        dictCodeMapper.insertDictCode(dictCode);
        refreshTypeCache(dictCode.getDictTypeId());
        return dictCode;
    }

    @Override
    public DictCode updateItem(DictCode dictCode) {
        dictCodeMapper.updateDictCode(dictCode);
        refreshTypeCache(dictCode.getDictTypeId());
        return dictCode;
    }

    @Override
    public void deleteItem(Long id) {
        DictCode item = dictCodeMapper.selectById(id);
        if (item == null) {
            throw new IllegalArgumentException("Dictionary item not found");
        }
        dictCodeMapper.deleteById(id);
        refreshTypeCache(item.getDictTypeId());
    }

    private void clearTypeCache(String typeCode) {
        try {
            redisTemplate.delete(CACHE_PREFIX + typeCode);
        } catch (Exception e) {
            // Ignore cache clear error
        }
    }

    private void refreshTypeCache(Long dictTypeId) {
        DictType type = dictTypeMapper.selectById(dictTypeId);
        if (type != null) {
            clearTypeCache(type.getCode());
        }
    }

    private DictCodeDTO toDTO(DictCode dictCode) {
        return new DictCodeDTO(
                dictCode.getCode(),
                dictCode.getName(),
                dictCode.getNameEn(),
                dictCode.getNameZh(),
                dictCode.getSortOrder(),
                dictCode.getExtra()
        );
    }
}
