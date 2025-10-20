package com.websiteElectronics.websiteElectronics.Services.Impl;

import com.websiteElectronics.websiteElectronics.Dtos.ShoppingCartDto;
import com.websiteElectronics.websiteElectronics.Entities.Customers;
import com.websiteElectronics.websiteElectronics.Entities.Products;
import com.websiteElectronics.websiteElectronics.Entities.ShoppingCart;
import com.websiteElectronics.websiteElectronics.Exceptions.NotFoundId;
import com.websiteElectronics.websiteElectronics.Mappers.ShoppingCartMapper;
import com.websiteElectronics.websiteElectronics.Repositories.CustomersRepository;
import com.websiteElectronics.websiteElectronics.Repositories.ElectronicsRepositorys;
import com.websiteElectronics.websiteElectronics.Repositories.ShoppingCartRepository;
import com.websiteElectronics.websiteElectronics.Services.ShoppingCartService;
import jakarta.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private static final Logger logger = LoggerFactory.getLogger(ShoppingCartServiceImpl.class);

    private final ShoppingCartRepository shoppingCartRepository;


    private final CustomersRepository customersRepository;


    private final ElectronicsRepositorys electronicsRepositorys;

    @Autowired
    public ShoppingCartServiceImpl(ShoppingCartRepository shoppingCartRepository, CustomersRepository customersRepository, ElectronicsRepositorys electronicsRepositorys) {
        this.shoppingCartRepository = shoppingCartRepository;
        this.customersRepository = customersRepository;
        this.electronicsRepositorys = electronicsRepositorys;
    }

    private ShoppingCart findId(int id) {
        return shoppingCartRepository.findById(id)
                .orElseThrow(() -> {
                    logger.debug("Could not find shopping cart with id: {}", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Could not find shopping cart with id: " + id);
                });
    }

    @Override
    public ShoppingCartDto createShoppingCart(ShoppingCartDto shoppingCartDto) {
        ShoppingCart shoppingCart = ShoppingCartMapper.mapToEntity(shoppingCartDto);

        Customers customer = customersRepository.findById(shoppingCartDto.getCustomerId())
            .orElseThrow(() -> new RuntimeException("Customer not found: " + shoppingCartDto.getCustomerId()));
        Products product = electronicsRepositorys.findById(shoppingCartDto.getProductId())
            .orElseThrow(() -> new RuntimeException("Product not found: " + shoppingCartDto.getProductId()));

        shoppingCart.setCustomer(customer);
        shoppingCart.setProduct(product);

        ShoppingCart savedShoppingCart = shoppingCartRepository.save(shoppingCart);
        return ShoppingCartMapper.mapToDto(savedShoppingCart);
    }

    @Override
    public ShoppingCartDto updateShoppingCart(int id, ShoppingCartDto shoppingCartDto) {
        ShoppingCart findShoppingCart = findId(id);
        Customers customer = customersRepository.findById(shoppingCartDto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found: " + shoppingCartDto.getCustomerId()));
        Products product = electronicsRepositorys.findById(shoppingCartDto.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found: " + shoppingCartDto.getProductId()));
        findShoppingCart.setCustomer(customer);
        findShoppingCart.setProduct(product);
        findShoppingCart.setQuantity(shoppingCartDto.getQuantity());
        findShoppingCart.setCreatedAt(shoppingCartDto.getCreatedAt());
        ShoppingCart savedShoppingCart = shoppingCartRepository.save(findShoppingCart);
        return ShoppingCartMapper.mapToDto(savedShoppingCart);
    }

    @Override
    public ShoppingCartDto getShoppingCartById(int id) {
        ShoppingCart findShoppingCart = findId(id);
        return ShoppingCartMapper.mapToDto(findShoppingCart);
    }

    @Override
    public List<ShoppingCartDto> getAllShoppingCarts() {
        List<ShoppingCart> getAllShoppingCarts = shoppingCartRepository.findAll();
        return getAllShoppingCarts.parallelStream().map(ShoppingCartMapper::mapToDto).toList();
    }

    @Override
    public void deleteShoppingCart(int id) {
        if (!shoppingCartRepository.existsById(id)) {
            throw new NotFoundId("Shopping cart not found with id: " + id);
        }
        shoppingCartRepository.deleteById(id);
    }

    @Override
    public List<ShoppingCartDto> getShoppingCartByCustomerId(int customerId) {
        List<ShoppingCart> carts = shoppingCartRepository.findByCustomerId(customerId);
        if (carts == null) return List.of();

        return carts.parallelStream().map(ShoppingCartMapper::mapToDto).toList();

    }
}
