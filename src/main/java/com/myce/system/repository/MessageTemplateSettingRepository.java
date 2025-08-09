package com.myce.system.repository;

import com.myce.system.entity.MessageTemplateSetting;
import com.myce.system.entity.type.ChannelType;
import com.myce.system.entity.type.MessageTemplateCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageTemplateSettingRepository extends JpaRepository<MessageTemplateSetting, Long> {

    Optional<MessageTemplateSetting> findByCodeAndChannelType(MessageTemplateCode code,  ChannelType channelType);

}
