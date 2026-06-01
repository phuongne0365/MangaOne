package com.mangaone.controller;

import com.mangaone.entity.CartItem;
import com.mangaone.entity.User;
import com.mangaone.service.CartService;
import com.mangaone.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;

    public CheckoutController(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    // GET /checkout: Hiển thị trang checkout (tất cả sản phẩm)
    @GetMapping
    public String showCheckout(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/?openLogin=true";
        }

        List<CartItem> cartItems = cartService.getCartItems(user);
        if (cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Giỏ hàng trống! Vui lòng thêm sản phẩm trước khi thanh toán.");
            return "redirect:/cart";
        }

        Double totalAmount = cartService.calculateTotal(cartItems);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("user", user);
        model.addAttribute("isPartialCheckout", false);

        return "checkout";
    }

    // POST /checkout: Xử lý đặt hàng (tất cả)
    @PostMapping
    public String processCheckout(@RequestParam String receiverName,
                                  @RequestParam String receiverPhone,
                                  @RequestParam String shippingAddress,
                                  @RequestParam String paymentMethod, // 🔥 BỔ SUNG: Nhận phương thức thanh toán (COD hoặc BANK_TRANSFER)
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/?openLogin=true";
        }

        try {
            
            orderService.checkout(user, receiverName, receiverPhone, shippingAddress, paymentMethod);

            redirectAttributes.addFlashAttribute("successMsg", "Đặt hàng thành công! Cảm ơn bạn đã mua hàng.");
            return "redirect:/checkout/success";

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/cart";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Có lỗi xảy ra trong quá trình đặt hàng. Vui lòng thử lại.");
            return "redirect:/cart";
        }
    }

    // GET /checkout/selected: Hiển thị trang checkout cho sản phẩm được chọn
    @GetMapping("/selected")
    public String showCheckoutSelected(@RequestParam(value = "cartIds", required = false) List<Integer> cartIds,
                                       HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/?openLogin=true";
        }

        if (cartIds == null || cartIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Vui lòng chọn ít nhất 1 sản phẩm để thanh toán.");
            return "redirect:/cart";
        }

        // Lấy cart items được chọn
        List<CartItem> selectedItems = cartService.getCartItemsByIds(cartIds);
        if (selectedItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Không tìm thấy sản phẩm được chọn.");
            return "redirect:/cart";
        }

        Double totalAmount = cartService.calculateTotal(selectedItems);

        model.addAttribute("cartItems", selectedItems);
        model.addAttribute("cartIds", cartIds);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("user", user);
        model.addAttribute("isPartialCheckout", true);

        return "checkout";
    }

    
    @PostMapping("/selected")
    public String processCheckoutSelected(@RequestParam String receiverName,
                                          @RequestParam String receiverPhone,
                                          @RequestParam String shippingAddress,
                                          @RequestParam String paymentMethod, 
                                          @RequestParam(value = "cartIds", required = false) List<Integer> cartIds,
                                          HttpSession session,
                                          RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/?openLogin=true";
        }

        try {
            
            orderService.checkoutSelected(user, receiverName, receiverPhone, shippingAddress, cartIds, paymentMethod);

            redirectAttributes.addFlashAttribute("successMsg", "Đặt hàng thành công! Cảm ơn bạn đã mua hàng.");
            return "redirect:/checkout/success";

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/cart";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Có lỗi xảy ra trong quá trình đặt hàng. Vui lòng thử lại.");
            return "redirect:/cart";
        }
    }

    @GetMapping("/success")
    public String showCheckoutSuccess() {
        return "checkout-success";
    }
}