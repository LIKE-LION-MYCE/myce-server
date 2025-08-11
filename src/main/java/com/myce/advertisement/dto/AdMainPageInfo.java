package com.myce.advertisement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AdMainPageInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long bannerId;
    private Long locationId;
    private String bannerImageUrl;
    private String linkUrl;

}
