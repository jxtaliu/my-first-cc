package com.sme.pm.service.impl;

import com.sme.pm.entity.*;
import com.sme.pm.mapper.*;
import com.sme.pm.service.IDataStandardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class DataStandardServiceImpl implements IDataStandardService {

    private final DataStandardMapper dataStandardMapper;
    private final DataStandardEnumItemMapper enumItemMapper;
    private final DataStandardCodeItemMapper codeItemMapper;
    private final DataStandardStringItemMapper stringItemMapper;
    private final DataStandardNumberItemMapper numberItemMapper;

    public DataStandardServiceImpl(DataStandardMapper dataStandardMapper,
                                  DataStandardEnumItemMapper enumItemMapper,
                                  DataStandardCodeItemMapper codeItemMapper,
                                  DataStandardStringItemMapper stringItemMapper,
                                  DataStandardNumberItemMapper numberItemMapper) {
        this.dataStandardMapper = dataStandardMapper;
        this.enumItemMapper = enumItemMapper;
        this.codeItemMapper = codeItemMapper;
        this.stringItemMapper = stringItemMapper;
        this.numberItemMapper = numberItemMapper;
    }

    @Override
    public List<DataStandard> getAll() {
        return dataStandardMapper.findAll();
    }

    @Override
    public DataStandard getById(Long id) {
        return dataStandardMapper.findById(id);
    }

    @Override
    public List<DataStandard> getByType(String type) {
        return dataStandardMapper.findByType(type);
    }

    @Override
    @Transactional
    public DataStandard create(DataStandard dataStandard) {
        dataStandardMapper.insert(dataStandard);
        saveSubItem(dataStandard, dataStandard.getId());
        return dataStandard;
    }

    @Override
    @Transactional
    public DataStandard update(Long id, DataStandard dataStandard) {
        DataStandard existing = dataStandardMapper.findById(id);
        if (existing == null) {
            throw new RuntimeException("Data standard not found");
        }

        existing.setCode(dataStandard.getCode());
        existing.setName(dataStandard.getName());
        existing.setType(dataStandard.getType());
        existing.setDescription(dataStandard.getDescription());
        existing.setOwnerId(dataStandard.getOwnerId());
        existing.setOwnerName(dataStandard.getOwnerName());

        dataStandardMapper.updateById(existing);

        // Delete existing sub-items and recreate
        deleteSubItems(id);
        saveSubItem(dataStandard, id);

        return existing;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        deleteSubItems(id);
        dataStandardMapper.deleteById(id);
    }

    @Override
    public Map<String, Object> getDetail(Long id) {
        DataStandard standard = dataStandardMapper.findById(id);
        if (standard == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> detail = new HashMap<>();
        detail.put("id", standard.getId());
        detail.put("code", standard.getCode());
        detail.put("name", standard.getName());
        detail.put("type", standard.getType());
        detail.put("description", standard.getDescription());
        detail.put("ownerId", standard.getOwnerId());
        detail.put("ownerName", standard.getOwnerName());
        detail.put("createdAt", standard.getCreatedAt());
        detail.put("updatedAt", standard.getUpdatedAt());

        // Load sub-item based on type
        String type = standard.getType();
        if ("ENUM".equals(type)) {
            detail.put("enumItems", enumItemMapper.findByStandardId(id));
        } else if ("CODE".equals(type)) {
            detail.put("codeItem", codeItemMapper.findByStandardId(id));
        } else if ("STRING".equals(type)) {
            detail.put("stringItem", stringItemMapper.findByStandardId(id));
        } else if ("NUMBER".equals(type)) {
            detail.put("numberItem", numberItemMapper.findByStandardId(id));
        }

        return detail;
    }

    @Override
    public void share(Long id, Map<String, Object> shareParams) {
        DataStandard standard = dataStandardMapper.findById(id);
        if (standard == null) {
            throw new RuntimeException("Data standard not found");
        }

        String method = (String) shareParams.get("method");
        if ("feishu".equals(method)) {
            shareToFeishu(standard, shareParams);
        } else if ("email".equals(method)) {
            shareToEmail(standard, shareParams);
        }
    }

    private void saveSubItem(DataStandard dataStandard, Long standardId) {
        String type = dataStandard.getType();

        if ("ENUM".equals(type)) {
            List<DataStandardEnumItem> enumItems = dataStandard.getEnumItems();
            if (enumItems != null && !enumItems.isEmpty()) {
                for (DataStandardEnumItem enumItem : enumItems) {
                    enumItem.setStandardId(standardId);
                    enumItemMapper.insert(enumItem);
                }
            }
        } else if ("CODE".equals(type)) {
            DataStandardCodeItem codeItem = dataStandard.getCodeItem();
            if (codeItem != null) {
                codeItem.setStandardId(standardId);
                codeItemMapper.insert(codeItem);
            }
        } else if ("STRING".equals(type)) {
            DataStandardStringItem stringItem = dataStandard.getStringItem();
            if (stringItem != null) {
                stringItem.setStandardId(standardId);
                stringItemMapper.insert(stringItem);
            }
        } else if ("NUMBER".equals(type)) {
            DataStandardNumberItem numberItem = dataStandard.getNumberItem();
            if (numberItem != null) {
                numberItem.setStandardId(standardId);
                numberItemMapper.insert(numberItem);
            }
        }
    }

    private void deleteSubItems(Long standardId) {
        enumItemMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataStandardEnumItem>()
                .eq("standard_id", standardId));
        codeItemMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataStandardCodeItem>()
                .eq("standard_id", standardId));
        stringItemMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataStandardStringItem>()
                .eq("standard_id", standardId));
        numberItemMapper.delete(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<DataStandardNumberItem>()
                .eq("standard_id", standardId));
    }

    private void shareToFeishu(DataStandard standard, Map<String, Object> shareParams) {
        String webhookUrl = (String) shareParams.get("webhookUrl");
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            throw new RuntimeException("Feishu webhook URL is required");
        }

        Map<String, Object> message = new HashMap<>();
        message.put("msg_type", "text");
        Map<String, Object> content = new HashMap<>();
        content.put("text", buildShareText(standard));
        message.put("content", content);

        // Note: In production, use RestTemplate or WebClient to send HTTP POST request
        System.out.println("Sharing to Feishu: " + webhookUrl + " - " + message);
    }

    private void shareToEmail(DataStandard standard, Map<String, Object> shareParams) {
        String recipient = (String) shareParams.get("recipient");
        if (recipient == null || recipient.isEmpty()) {
            throw new RuntimeException("Email recipient is required");
        }

        // Note: In production, use JavaMailSender to send email
        System.out.println("Sharing to Email: " + recipient + " - " + buildShareText(standard));
    }

    private String buildShareText(DataStandard standard) {
        StringBuilder sb = new StringBuilder();
        sb.append("【数据标准分享】\n");
        sb.append("编码: ").append(standard.getCode()).append("\n");
        sb.append("名称: ").append(standard.getName()).append("\n");
        sb.append("类型: ").append(standard.getType()).append("\n");
        sb.append("描述: ").append(standard.getDescription()).append("\n");
        sb.append("责任人: ").append(standard.getOwnerName()).append("\n");
        return sb.toString();
    }
}
