package com.websiteElectronics.websiteElectronics.Services.Impl;

import com.websiteElectronics.websiteElectronics.Dtos.CustomersDto;
import com.websiteElectronics.websiteElectronics.Entities.Customers;
import com.websiteElectronics.websiteElectronics.Exceptions.NotFoundId;
import com.websiteElectronics.websiteElectronics.Mappers.CustomersMapper;
import com.websiteElectronics.websiteElectronics.Repositories.CustomersRepository;
import com.websiteElectronics.websiteElectronics.Services.CustomersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomersServiceImpl implements CustomersService {

    private static final Logger logger = LoggerFactory.getLogger(CustomersServiceImpl.class);

    private final CustomersRepository customersRepository;

    @Autowired
    public CustomersServiceImpl(CustomersRepository customersRepository) {
        this.customersRepository = customersRepository;
    }

    private Customers findId(int id) {
        return customersRepository.findById(id)
                .orElseThrow(() -> {
                   logger.debug("Could not find customer with id: {}", id);
                   return new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find customer with id: " + id);
                });
    }

    @Override
    public CustomersDto createCustomer(CustomersDto customerDto) {
        Customers customer = CustomersMapper.toEntity(customerDto);
        Customers saved = customersRepository.save(customer);
        return CustomersMapper.toDto(saved);
    }

    @Override
    public CustomersDto updateCustomer(int id, CustomersDto customerDto) {

        Customers customer = findId(id);
        customer.setFirstName(customerDto.getFirstName());
        customer.setLastName(customerDto.getLastName());
        customer.setEmail(customerDto.getEmail());
        customer.setPassword(customerDto.getPassword());
        customer.setPhoneNumber(customerDto.getPhoneNumber());
        customer.setAddress(customerDto.getAddress());
        customer.setCity(customerDto.getCity());
        customer.setState(customerDto.getState());
        customer.setZipCode(customerDto.getZipCore());
        customer.setCountry(customerDto.getCountry());
        Customers updated = customersRepository.save(customer);
        return CustomersMapper.toDto(updated);
    }

    @Override
    public void deleteCustomer(int id) {
        if (!customersRepository.existsById(id)) {
            throw new NotFoundId("Customer not found with id: " + id);
        }
        customersRepository.deleteById(id);
    }

    @Override
    public CustomersDto getCustomerById(int id) {
        Customers customer = findId(id);
        return CustomersMapper.toDto(customer);
    }

    @Override
    public List<CustomersDto> getAllCustomers() {
        return customersRepository.findAll().parallelStream()
                .map(CustomersMapper::toDto)
                .toList();
    }

    @Override
    public CustomersDto getCustomerByEmail(String email) {
        Customers customer = customersRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.debug("Could not find customer with email: {}", email);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find customer with email: " + email);
                });
        return CustomersMapper.toDto(customer);
    }
}
