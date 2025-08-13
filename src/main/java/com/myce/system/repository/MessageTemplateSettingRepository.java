package com.myce.system.repository;

import com.myce.system.entity.MessageTemplateSetting;
import com.myce.system.entity.type.ChannelType;
import com.myce.system.entity.type.MessageTemplateCode;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageTemplateSettingRepository extends JpaRepository<MessageTemplateSetting, Long> {

    Page<MessageTemplateSetting> findAll(Pageable pageable);

    Page<MessageTemplateSetting> findAllByNameContains(String name, Pageable pageable);

    Optional<MessageTemplateSetting> findByCodeAndChannelType(MessageTemplateCode code,  ChannelType channelType);

}
