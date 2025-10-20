package com.websiteElectronics.websiteElectronics.Services.Impl;

import com.websiteElectronics.websiteElectronics.Dtos.CategoriesDto;
import com.websiteElectronics.websiteElectronics.Entities.Categories;

import com.websiteElectronics.websiteElectronics.Exceptions.NotFoundId;
import com.websiteElectronics.websiteElectronics.Mappers.CategoriesMapper;
import com.websiteElectronics.websiteElectronics.Repositories.CategoriesRepository;
import com.websiteElectronics.websiteElectronics.Services.CategoriesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoriesServiceImpl implements CategoriesService {

    private static final Logger logger = LoggerFactory.getLogger(CategoriesServiceImpl.class);

    private final CategoriesRepository categoriesRepository;

    @Autowired
    public CategoriesServiceImpl(CategoriesRepository categoriesRepository) {
        this.categoriesRepository = categoriesRepository;
    }

    private Categories findId(int id) {
        return categoriesRepository.findById(id)
                .orElseThrow(() -> {
                    logger.debug("Could not find category with id: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find category with id: " + id);
                });
    }

    @Override
    public CategoriesDto createCategory(CategoriesDto categoryDto) {
        Categories category = CategoriesMapper.toEntity(categoryDto);
        Categories saved = categoriesRepository.save(category);
        return CategoriesMapper.toDto(saved);
    }

    @Override
    public CategoriesDto updateCategory(int id, CategoriesDto categoryDto) {

        Categories category = findId(id);
        category.setName(categoryDto.getName());
        category.setDescription(categoryDto.getDescription());
        category.setParentId(categoryDto.getParentId());
        Categories updated = categoriesRepository.save(category);
        return CategoriesMapper.toDto(updated);
    }

    @Override
    public void deleteCategory(int id) {
        if (!categoriesRepository.existsById(id)) {
            throw new NotFoundId("Category not found with id: " + id);
        }
        categoriesRepository.deleteById(id);
    }


    @Override
    public CategoriesDto getCategoryById(int id) {
        Categories category = findId(id);
        return CategoriesMapper.toDto(category);
    }

    @Override
    public List<CategoriesDto> getAllCategories() {
        return categoriesRepository.findAll().parallelStream()
                .map(CategoriesMapper::toDto)
                .toList();
    }
}
