package com.myce.system.controller;

import com.myce.system.dto.message.MessageTemplateResponse;
import com.myce.system.dto.message.MessageTemplateListResponse;
import com.myce.system.dto.message.UpdateMessageTemplateRequest;
import com.myce.system.service.message.MessageTemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/settings/message-template")
public class MessageTemplateController {

    private final MessageTemplateService messageTemplateService;

    @GetMapping
    @PreAuthorize("hasAuthority('PLATFORM_ADMIN')")
    public ResponseEntity<MessageTemplateListResponse> getMessageTemplates(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "") String keyword) {
        MessageTemplateListResponse response = messageTemplateService.getAllMessageTemplates(page, keyword);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PLATFORM_ADMIN')")
    public ResponseEntity<MessageTemplateResponse> getMessageTemplate(@PathVariable long id) {
        MessageTemplateResponse response = messageTemplateService.getMessageTemplateById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PLATFORM_ADMIN')")
    public ResponseEntity<MessageTemplateResponse> updateMessageTemplate
            (@PathVariable long id, @RequestBody @Valid UpdateMessageTemplateRequest request) {
        MessageTemplateResponse response = messageTemplateService.updateMessageTemplate(id, request);
        return ResponseEntity.ok(response);
    }
}
