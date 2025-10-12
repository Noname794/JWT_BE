package com.websiteElectronics.websiteElectronics.Services;

import com.websiteElectronics.websiteElectronics.Dtos.ShoppingCartDto;

import java.util.List;

public interface ShoppingCartService {
    ShoppingCartDto createShoppingCart(ShoppingCartDto shoppingCartDto);
    ShoppingCartDto updateShoppingCart(int id, ShoppingCartDto shoppingCartDto);
    ShoppingCartDto getShoppingCartById(int id);
    List<ShoppingCartDto> getAllShoppingCarts();
    void deleteShoppingCart(int id);
    List<ShoppingCartDto> getShoppingCartByCustomerId(int customerId);
}
