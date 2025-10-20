package com.websiteElectronics.websiteElectronics.Mappers;

import com.websiteElectronics.websiteElectronics.Dtos.ShoppingCartDto;
import com.websiteElectronics.websiteElectronics.Entities.ShoppingCart;

public class ShoppingCartMapper {

    public static ShoppingCartDto mapToDto(ShoppingCart shoppingCart) {
        return new ShoppingCartDto(
                shoppingCart.getId(),
                shoppingCart.getCustomer() != null ? shoppingCart.getCustomer().getId() : null,
                shoppingCart.getProduct() != null ? shoppingCart.getProduct().getId() : null,
                shoppingCart.getQuantity(),
                shoppingCart.getCreatedAt()
        );
    }

    public static ShoppingCart mapToEntity(ShoppingCartDto shoppingCartDto) {
        ShoppingCart cart = new ShoppingCart();
        cart.setId(shoppingCartDto.getId());
        cart.setQuantity(shoppingCartDto.getQuantity());
        cart.setCreatedAt(shoppingCartDto.getCreatedAt());
        return cart;
    }
}
