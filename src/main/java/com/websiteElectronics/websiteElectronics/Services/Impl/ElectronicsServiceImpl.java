package com.websiteElectronics.websiteElectronics.Services.Impl;

import com.websiteElectronics.websiteElectronics.Dtos.ProductsDto;
import com.websiteElectronics.websiteElectronics.Entities.Products;
import com.websiteElectronics.websiteElectronics.Exceptions.NotFoundId;
import com.websiteElectronics.websiteElectronics.Mappers.ElectronicsMapper;
import com.websiteElectronics.websiteElectronics.Repositories.ElectronicsRepositorys;
import com.websiteElectronics.websiteElectronics.Services.ElectronicsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Service
public class ElectronicsServiceImpl implements ElectronicsService {

    private static final Logger logger = LoggerFactory.getLogger(ElectronicsServiceImpl.class);

    private final ElectronicsRepositorys electronicsRepositorys;

    @Autowired
    public ElectronicsServiceImpl(ElectronicsRepositorys electronicsRepositorys) {
        this.electronicsRepositorys = electronicsRepositorys;
    }

    private Products findId(int id) {
        return electronicsRepositorys.findById(id)
                .orElseThrow(() -> {
                    logger.debug("Could not find electronics with id: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find electronics with id: " + id);
                });
    }

    @Override
    public List<ProductsDto> getAllElectronics() {
        List<Products> lstElectronics = electronicsRepositorys.findAll();
        return lstElectronics.parallelStream().map(ElectronicsMapper::mapToDto).toList();
    }

    @Override
    public ProductsDto getElectronicsById(int id) {
        Products electronics = findId(id);
        return ElectronicsMapper.mapToDto(electronics);
    }

    @Override
    public ProductsDto createElectronics(ProductsDto electronicsDto) {
        Products electronics = ElectronicsMapper.mapToEntity(electronicsDto);
        Products savedElectronics = electronicsRepositorys.save(electronics);
        return ElectronicsMapper.mapToDto(savedElectronics);
    }


    @Override
    public ProductsDto updateElectronics(int id, ProductsDto electronicsDto) {
        Products electronics = findId(id);
        electronics.setName(electronicsDto.getName());
        electronics.setDescription(electronicsDto.getDescription());
        electronics.setPrice(electronicsDto.getPrice());
        electronics.setQuantity(electronicsDto.getQuantity());
        electronics.setImageUrl(electronicsDto.getImageUrl());
        electronics.setCategory(electronicsDto.getCategory());
        electronics.setSupplier(electronicsDto.getSupplier());
        Products savedElectronics = electronicsRepositorys.save(electronics);
        return ElectronicsMapper.mapToDto(savedElectronics);
    }

    @Override
    public void deleteElectronics(int id) {
        if (!electronicsRepositorys.existsById(id)) {
            throw new NotFoundId("Product not found with id: " + id);
        }
        electronicsRepositorys.deleteById(id);
    }
}
