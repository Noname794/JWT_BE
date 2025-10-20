package com.websiteElectronics.websiteElectronics.Services;

import com.websiteElectronics.websiteElectronics.Dtos.ReviewsDto;
import java.util.List;

public interface ReviewsService {
    ReviewsDto createReview(ReviewsDto dto);
    ReviewsDto getReviewById(int id);
    List<ReviewsDto> getAllReviews();
    ReviewsDto updateReview(int id, ReviewsDto dto);
    void deleteReview(int id);
}
