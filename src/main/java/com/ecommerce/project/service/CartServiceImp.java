package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.ApiException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.CartItem;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.repository.CartItemRepository;
import com.ecommerce.project.repository.CartRepository;
import com.ecommerce.project.repository.ProductRepository;
import com.ecommerce.project.util.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
@Service
public class CartServiceImp implements CartService{

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        // find existing cart or create one
        Cart cart = createCart();

        //retrieve product details
        Product product = productRepository.findById(productId)
                .orElseThrow(()->new ResourceNotFoundException("Product","productId",productId));

        //perform validation
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartid(
                cart.getCartId(),
                productId
        );
        if(cartItem != null){
            throw new ApiException("Product" +product.getProductName() + "already exists in the cart");
        }
        if(product.getQuantity() == 0){
            throw new ApiException(product.getProductName() + " is not available");
        }
        if(product.getQuantity() < quantity){
            throw new ApiException("Please, make an order of the "+product.getProductName()
            + "less than or equal to the quantity" +product.getQuantity());
        }

        //create cart item
        CartItem newCartItem = new CartItem();
        newCartItem.setProduct(product);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());
        //save cart item

        cartItemRepository.save(newCartItem);
        product.setQuantity(product.getQuantity());
        cart.setTotalPrice(cart.getTotalPrice()+(product.getSpecialPrice() * quantity));

        cartRepository.save(cart);
        //return updated cart

        CartDTO cartDto = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();
        Stream<ProductDTO> productDTOStream = cartItems.stream().map(item -> {
            ProductDTO map = modelMapper.map(item.getProduct(),ProductDTO.class);
            map.setQuantity(item.getQuantity());
            return map;
        });

        cartDto.setProducts(productDTOStream.toList());

        return cartDto;
    }

    @Override
    public List<CartDTO> getAllCarts() {
       List<Cart> carts = cartRepository.findAll();

       if(carts.size() == 0){
           throw new ApiException("No cart exists");
       }
       List<CartDTO> cartDTOS = carts.stream()
               .map(cart -> {
                   CartDTO cartDto = modelMapper.map(cart, CartDTO.class);
                   List<ProductDTO> products = cart.getCartItems().stream().map(cartItem->{
                       ProductDTO productDTO = modelMapper.map(cartItem.getProduct(),ProductDTO.class);
                       productDTO.setQuantity(cartItem.getQuantity());
                       return productDTO;
                   }).toList();

                   cartDto.setProducts(products);
                   return cartDto;
               }).toList();
       return cartDTOS;
    }

    @Override
    public CartDTO getCart(String emailId, Long cartId) {

        Cart cart = cartRepository.findCartByEmailAndCartId(emailId,cartId);

        if(cart == null){
            throw new ResourceNotFoundException("Cart","cartId",cartId);
        }


        CartDTO cartDto = modelMapper.map(cart, CartDTO.class);
//        cart.getCartItems().forEach(c->c.getProduct().setQuantity(c.getQuantity()));
        List<ProductDTO> products = cart.getCartItems().stream()
                .map(p -> modelMapper.map(p,ProductDTO.class))
                .toList();
        cartDto.setProducts(products);

        return cartDto;
    }

    @Transactional
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, int quantity) {
        //check if cart exist or not
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        Long cartId = userCart.getCartId();

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));


        //check product quantity
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        if (product.getQuantity() == 0) {
            throw new ApiException(product.getProductName() + " is not available");
        }
        if (product.getQuantity() < quantity) {
            throw new ApiException("Please, make an order of the " + product.getProductName()
                    + "less than or equal to the quantity" + product.getQuantity());
        }

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartid(cartId, productId);
        if (cartItem == null) {
            throw new ApiException("product " + product.getProductName() + " not available in the cart");
        }

        int newQuantity = cartItem.getQuantity() + quantity;

        if (newQuantity < 0) {
            throw new ApiException("product " + product.getProductName() + " quantity can not be negative");
        }

        if (newQuantity <= 0) {
            deleteProductFromCart(cartId, productId);
        }else {

        cartItem.setProductPrice(product.getSpecialPrice());
        cartItem.setQuantity(cartItem.getQuantity() + quantity);
        cartItem.setDiscount(cartItem.getDiscount());
        cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * quantity));
        cartRepository.save(cart);

        }

        CartItem updatedItem = cartItemRepository.save(cartItem);
        if(updatedItem.getQuantity() <= 0 ){
            cartItemRepository.deleteById(cartItem.getCartItemId());
        }

        CartDTO cartDto = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();
        Stream<ProductDTO> productDTOStream = cartItems.stream().map(item->{
                ProductDTO productDTO = modelMapper.map(item.getProduct(),ProductDTO.class);
                productDTO.setQuantity(item.getQuantity());
                return productDTO;
        });
        cartDto.setProducts(productDTOStream.toList());

        return cartDto;
    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {

        Cart cart  = cartRepository.findById(cartId)
                .orElseThrow(()->new ResourceNotFoundException("cart","cartId", cartId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartid(cartId,productId);

        if(cartItem == null){
            throw new ResourceNotFoundException("product","productId",productId);
        }
        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity()));
        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId,productId);

        return "Product "+ cartItem.getProduct().getProductName()+ " is removed from the cart";
    }

    @Override
    public void updateProductInCart(Long cartId, Long productId) {

        //cart exist or not
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));


        //check product quantity
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartid(cartId,productId);

        if(cartItem == null){
            throw new ApiException("product " + product.getProductName()+ " not available in the cart !");
        }

        double cartPrice = cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity());

        cartItem.setProductPrice(product.getSpecialPrice());
        cart.setTotalPrice(cartPrice +
                (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItem = cartItemRepository.save(cartItem);
    }


    private Cart createCart(){
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if(userCart != null){
            return userCart;
        }

        Cart cart = new Cart();
        cart.setTotalPrice(0.00);
        cart.setUser(authUtil.loggedInUser());

        return cartRepository.save(cart);
    }


}
