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

import java.util.Collections;
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
            String cacheKey = CACHE_PREFIX + type.getCode();
            List<DictCode> codes = dictCodeMapper.findByTypeCode(type.getCode());
            List<DictCodeDTO> dtos = codes.stream().map(this::toDTO).toList();
            try {
                redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(dtos), CACHE_TTL, TimeUnit.SECONDS);
            } catch (JsonProcessingException e) {
                // Ignore
            }
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
