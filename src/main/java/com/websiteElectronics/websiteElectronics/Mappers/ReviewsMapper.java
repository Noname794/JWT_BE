package com.websiteElectronics.websiteElectronics.Mappers;

import com.websiteElectronics.websiteElectronics.Entities.Reviews;
import com.websiteElectronics.websiteElectronics.Dtos.ReviewsDto;
import org.springframework.stereotype.Component;

@Component
public class ReviewsMapper {
    public ReviewsDto toDto(Reviews entity) {
        if (entity == null) return null;
        ReviewsDto dto = new ReviewsDto();
        dto.setId(entity.getId());
        dto.setRating(entity.getRating());
        dto.setComment(entity.getComment());
        dto.setReviewDate(entity.getReviewDate() != null ? entity.getReviewDate().toString() : null);
        if (entity.getProduct() != null) {
            dto.setProductId(entity.getProduct().getId());
            dto.setProductName(entity.getProduct().getName());
        }
        if (entity.getCustomer() != null) {
            dto.setCustomerId(entity.getCustomer().getId());
            dto.setCustomerName(entity.getCustomer().getFirstName() + " " + entity.getCustomer().getLastName());
        }
        return dto;
    }

    public Reviews toEntity(ReviewsDto dto) {
        if (dto == null) return null;
        Reviews entity = new Reviews();
        entity.setId(dto.getId());
        entity.setRating(dto.getRating());
        entity.setComment(dto.getComment());

        return entity;
    }
}
