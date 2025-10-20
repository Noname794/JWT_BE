package com.websiteElectronics.websiteElectronics.Services.Impl;

import com.websiteElectronics.websiteElectronics.Dtos.ReviewsDto;
import com.websiteElectronics.websiteElectronics.Entities.Customers;
import com.websiteElectronics.websiteElectronics.Entities.Products;
import com.websiteElectronics.websiteElectronics.Entities.Reviews;
import com.websiteElectronics.websiteElectronics.Exceptions.NotFoundId;
import com.websiteElectronics.websiteElectronics.Mappers.ReviewsMapper;
import com.websiteElectronics.websiteElectronics.Repositories.CustomersRepository;
import com.websiteElectronics.websiteElectronics.Repositories.ElectronicsRepositorys;
import com.websiteElectronics.websiteElectronics.Repositories.ReviewsRepository;
import com.websiteElectronics.websiteElectronics.Services.ReviewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ReviewsServiceImpl implements ReviewsService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewsServiceImpl.class);

    private final ReviewsRepository reviewsRepository;

    private final ReviewsMapper reviewsMapper;

    private final CustomersRepository customersRepository;

    private final ElectronicsRepositorys electronicsRepositorys;

    @Autowired
    public ReviewsServiceImpl(ReviewsRepository reviewsRepository, ReviewsMapper reviewsMapper, CustomersRepository customersRepository, ElectronicsRepositorys electronicsRepositorys) {
        this.reviewsRepository = reviewsRepository;
        this.reviewsMapper = reviewsMapper;
        this.customersRepository = customersRepository;
        this.electronicsRepositorys = electronicsRepositorys;
    }

    private Reviews findId(int id){
        return reviewsRepository.findById(id)
                .orElseThrow(() -> {
                   logger.debug("Could not find review with id: {}", id);
                   return new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find review with id: " + id);
                });
    }

    @Override
    public ReviewsDto createReview(ReviewsDto dto) {
        Reviews entity = reviewsMapper.toEntity(dto);
        entity.setReviewDate(new Date());
        Products product = electronicsRepositorys.findById(dto.getProductId())
            .orElseThrow(() -> {
                logger.debug("Product not found with id: {}", dto.getProductId());
                return new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id: " + dto.getProductId());
            });
        Customers customer = customersRepository.findById(dto.getCustomerId())
            .orElseThrow(() -> {
                logger.debug("Customer not found with id: {}", dto.getCustomerId());
                return new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found with id: " + dto.getCustomerId());
            });
        entity.setProduct(product);
        entity.setCustomer(customer);
        Reviews saved = reviewsRepository.save(entity);
        return reviewsMapper.toDto(saved);
    }

    @Override
    public ReviewsDto getReviewById(int id) {
        Reviews review = findId(id);
        return reviewsMapper.toDto(review);
    }

    @Override
    public List<ReviewsDto> getAllReviews() {
        return reviewsRepository.findAll().parallelStream()
            .map(reviewsMapper::toDto)
            .toList();
    }

    @Override
    public ReviewsDto updateReview(int id, ReviewsDto dto) {
        Reviews review = findId(id);
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());

        Reviews updated = reviewsRepository.save(review);
        return reviewsMapper.toDto(updated);
    }

    @Override
    public void deleteReview(int id) {
        if (!reviewsRepository.existsById(id)) {
            throw new NotFoundId("Review not found with id: " + id);
        }
        reviewsRepository.deleteById(id);
    }
}
