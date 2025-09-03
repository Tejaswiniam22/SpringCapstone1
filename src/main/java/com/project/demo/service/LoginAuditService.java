package com.project.demo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class LoginAuditService {

    private static final Logger log = LoggerFactory.getLogger(LoginAuditService.class);
    private static final String AUDIT_FILE_PATH = "data/user.json";

    private final ObjectMapper objectMapper;

    public LoginAuditService() {
        this.objectMapper = new ObjectMapper();
        ensureDirectoryExists();
    }

    public void saveLoginLog(Map<String, Object> loginInfo) {
        try {
            List<Map<String, Object>> logs = loadExistingLogs();
            logs.add(loginInfo);

            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(AUDIT_FILE_PATH), logs);

            log.info("Login log saved for user: {}", loginInfo.get("username"));
        } catch (Exception e) {
            log.error("Failed to save login log: {}", e.getMessage(), e);
        }
    }

    private List<Map<String, Object>> loadExistingLogs() {
        try {
            File file = new File(AUDIT_FILE_PATH);
            if (file.exists() && file.length() > 0) {
                return objectMapper.readValue(file, new TypeReference<List<Map<String, Object>>>() {});
            }
        } catch (IOException e) {
            log.warn("Could not read existing logs, starting fresh: {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    private void ensureDirectoryExists() {
        File file = new File(AUDIT_FILE_PATH);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
    }
}