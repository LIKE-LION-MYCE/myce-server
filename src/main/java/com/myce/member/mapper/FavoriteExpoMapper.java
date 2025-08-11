package com.myce.member.mapper;

import com.myce.member.dto.expo.FavoriteExpoResponse;
import com.myce.member.entity.Favorite;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class FavoriteExpoMapper {

    public List<FavoriteExpoResponse> toResponseDtoList(List<Favorite> favorites) {
        return favorites.stream()
                .map(favorite -> FavoriteExpoResponse.builder()
                        .expoId(favorite.getExpo().getId())
                        .title(favorite.getExpo().getTitle())
                        .thumbnailUrl(favorite.getExpo().getThumbnailUrl())
                        .startDate(favorite.getExpo().getStartDate())
                        .endDate(favorite.getExpo().getEndDate())
                        .location(favorite.getExpo().getLocation())
                        .locationDetail(favorite.getExpo().getLocationDetail())
                        .build())
                .collect(Collectors.toList());
    }
}