package com.websiteElectronics.websiteElectronics.Mappers;

import com.websiteElectronics.websiteElectronics.Dtos.CustomersDto;
import com.websiteElectronics.websiteElectronics.Entities.Customers;

public class CustomersMapper {
    public static CustomersDto toDto(Customers customer) {
        if (customer == null) return null;
        return new CustomersDto(
                customer.getId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getPassword(),
                customer.getPhoneNumber(),
                customer.getAddress(),
                customer.getCity(),
                customer.getState(),
                customer.getZipCode(),
                customer.getCountry()
        );
    }

    public static Customers toEntity(CustomersDto dto) {
        if (dto == null) return null;
        Customers entity = new Customers();
        entity.setId(dto.getId());
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmail(dto.getEmail());
        entity.setPassword(dto.getPassword());
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setAddress(dto.getAddress());
        entity.setCity(dto.getCity());
        entity.setState(dto.getState());
        entity.setZipCode(dto.getZipCore());
        entity.setCountry(dto.getCountry());
        return entity;
    }
}
