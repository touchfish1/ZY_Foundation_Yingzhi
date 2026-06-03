package com.zhangyuan.modules.system;

import com.zhangyuan.modules.system.domain.SystemSetting;
import com.zhangyuan.modules.system.dto.BatchUpdateRequest;
import com.zhangyuan.modules.system.dto.SettingResponse;
import com.zhangyuan.modules.system.dto.UpdateSettingRequest;
import com.zhangyuan.modules.system.repository.SystemSettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SystemSettingService {

    private final SystemSettingRepository repository;

    public SystemSettingService(SystemSettingRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<SettingResponse> listSettings() {
        return repository.findAllByOrderBySettingKeyAsc().stream()
                .map(s -> new SettingResponse(s.getSettingKey(), s.getSettingValue()))
                .toList();
    }

    @Transactional(readOnly = true)
    public Map<String, String> getPublicSettings() {
        Map<String, String> map = new LinkedHashMap<>();
        for (SystemSetting s : repository.findAllByOrderBySettingKeyAsc()) {
            map.put(s.getSettingKey(), s.getSettingValue());
        }
        return map;
    }

    @Transactional
    public SettingResponse updateSetting(String key, UpdateSettingRequest request) {
        SystemSetting setting = repository.findBySettingKey(key)
                .orElseGet(() -> repository.save(new SystemSetting(key, null)));
        setting.updateValue(request.value());
        return new SettingResponse(setting.getSettingKey(), setting.getSettingValue());
    }

    @Transactional
    public List<SettingResponse> batchUpdate(BatchUpdateRequest request) {
        for (Map.Entry<String, String> entry : request.settings().entrySet()) {
            SystemSetting setting = repository.findBySettingKey(entry.getKey())
                    .orElseGet(() -> repository.save(new SystemSetting(entry.getKey(), null)));
            setting.updateValue(entry.getValue());
        }
        return listSettings();
    }
}
