package com.group.book_selling.controllers;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.group.book_selling.dto.AddressForm;
import com.group.book_selling.dto.CheckoutForm;
import com.group.book_selling.models.Cart;
import com.group.book_selling.models.Coupon;
import com.group.book_selling.models.CustomUserDetail;
import com.group.book_selling.models.Order;
import com.group.book_selling.models.PaymentMethod;
import com.group.book_selling.models.User;
import com.group.book_selling.repositories.IOrderRepository;
import com.group.book_selling.repositories.IUserRepository;
import com.group.book_selling.services.CouponService;
import com.group.book_selling.services.OrderCheckoutService;
import com.group.book_selling.utils.CartSessionUtils;
import com.group.book_selling.validators.AddressFormValidator;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class OrderController {

    private final OrderCheckoutService orderCheckoutService;
    private final CouponService couponService;
    private final IOrderRepository orderRepository;
    private final IUserRepository userRepository;
    private final AddressFormValidator addressFormValidator;

    @InitBinder("addAddressForm")
    protected void initAddAddressBinder(WebDataBinder binder) {
        binder.addValidators(addressFormValidator);
    }

    @GetMapping("/checkout")
    public String checkoutPage(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (userDetail == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByEmail(userDetail.getEmail());
        if (user == null) {
            return "redirect:/login";
        }

        Cart cart = CartSessionUtils.getOrCreate(session);
        if (cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng đang trống, không thể thanh toán.");
            return "redirect:/cart/view";
        }

        prepareCheckoutModel(user, cart, session, model, null, null, null);
        return "cart/checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @Valid @ModelAttribute("checkoutForm") CheckoutForm checkoutForm,
            BindingResult result,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (userDetail == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByEmail(userDetail.getEmail());
        if (user == null) {
            return "redirect:/login";
        }

        Cart cart = CartSessionUtils.getOrCreate(session);
        if (cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng đang trống, không thể thanh toán.");
            return "redirect:/cart/view";
        }

        resolveSelectedAddress(user, checkoutForm.getSelectedAddressIndex(), result);

        if (result.hasErrors()) {
            prepareCheckoutModel(user, cart, session, model, checkoutForm, null, null);
            return "cart/checkout";
        }

        try {
            Coupon appliedCoupon = couponService.resolveAppliedCoupon(session);
            Order order = orderCheckoutService.placeOrderWithFakePayment(
                    userDetail.getEmail(),
                    cart,
                    appliedCoupon,
                    checkoutForm);

            cart.clear();
            CartSessionUtils.save(session, cart);
            session.removeAttribute("appliedCouponCode");

            redirectAttributes.addFlashAttribute("success",
                    "Thanh toán giả lập thành công. Mã đơn hàng: " + order.getOrderNumber());
            return "redirect:/orders/" + order.getId();
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            prepareCheckoutModel(user, cart, session, model, checkoutForm, null, null);
            return "cart/checkout";
        }
    }

    @PostMapping("/checkout/add-address")
    public String addAddressFromCheckout(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @Validated @ModelAttribute("addAddressForm") AddressForm form,
            BindingResult result,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (userDetail == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByEmail(userDetail.getEmail());
        if (user == null) {
            return "redirect:/login";
        }

        Cart cart = CartSessionUtils.getOrCreate(session);
        if (cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Giỏ hàng đang trống, không thể thanh toán.");
            return "redirect:/cart/view";
        }

        if (result.hasErrors()) {
            prepareCheckoutModel(user, cart, session, model, null, form, "addAddressModal");
            return "cart/checkout";
        }

        if (form.isDefaultAddress()) {
            clearDefaultAddressFlag(user);
        }

        User.Address newAddress = User.Address.builder()
                .recipientName(form.getRecipientName().trim())
                .phoneNumber(form.getPhoneNumber().trim())
                .provinceOrCity(form.getProvinceOrCity().trim())
                .district(form.getDistrict().trim())
                .ward(form.getWard().trim())
                .streetDetails(form.getStreetDetails().trim())
                .isDefault(form.isDefaultAddress())
                .country("Vietnam")
                .build();

        user.getAddresses().add(newAddress);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Đã thêm địa chỉ giao hàng.");
        return "redirect:/checkout";
    }

    @GetMapping("/orders")
    public String myOrders(@AuthenticationPrincipal CustomUserDetail userDetail, Model model) {
        if (userDetail == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByEmail(userDetail.getEmail());
        if (user == null) {
            return "redirect:/login";
        }

        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        model.addAttribute("orders", orders);
        return "orders/list";
    }

    @GetMapping("/orders/{orderId}")
    public String orderDetail(
            @AuthenticationPrincipal CustomUserDetail userDetail,
            @PathVariable Long orderId,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (userDetail == null) {
            return "redirect:/login";
        }

        User user = userRepository.findByEmail(userDetail.getEmail());
        if (user == null) {
            return "redirect:/login";
        }

        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElse(null);

        if (order == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng.");
            return "redirect:/orders";
        }

        model.addAttribute("order", order);
        return "orders/detail";
    }

    private void prepareCheckoutModel(
            User user,
            Cart cart,
            HttpSession session,
            Model model,
            CheckoutForm form,
            AddressForm addAddressForm,
            String openModal) {
        BigDecimal subtotal = cart.getTotalPrice("VND");
        BigDecimal tax = cart.getTotalTax("VND");
        Coupon appliedCoupon = couponService.resolveAppliedCoupon(session);
        BigDecimal discount = couponService.calculateDiscount(subtotal, appliedCoupon);
        BigDecimal total = subtotal.add(tax).subtract(discount).max(BigDecimal.ZERO);

        model.addAttribute("user", user);
        model.addAttribute("cart", cart);
        model.addAttribute("subtotal", subtotal);
        model.addAttribute("tax", tax);
        model.addAttribute("discount", discount);
        model.addAttribute("total", total);
        model.addAttribute("couponCode", appliedCoupon != null ? appliedCoupon.getCode() : "");
        model.addAttribute("paymentMethods", PaymentMethod.values());

        if (!model.containsAttribute("checkoutForm")) {
            model.addAttribute("checkoutForm", form != null ? form : buildDefaultCheckoutForm(user));
        }

        if (!model.containsAttribute("addAddressForm")) {
            model.addAttribute("addAddressForm", addAddressForm != null ? addAddressForm : new AddressForm());
        }

        if (openModal != null) {
            model.addAttribute("openModal", openModal);
        }
    }

    private CheckoutForm buildDefaultCheckoutForm(User user) {
        CheckoutForm form = new CheckoutForm();

        List<User.Address> addresses = user.getAddresses();
        if (addresses != null && !addresses.isEmpty()) {
            int selectedIndex = 0;
            for (int i = 0; i < addresses.size(); i++) {
                if (addresses.get(i).isDefault()) {
                    selectedIndex = i;
                    break;
                }
            }

            form.setSelectedAddressIndex(selectedIndex);
            return form;
        }
        return form;
    }

    private User.Address resolveSelectedAddress(User user, Integer selectedAddressIndex, BindingResult result) {
        List<User.Address> addresses = user.getAddresses();
        if (addresses == null || addresses.isEmpty()) {
            result.rejectValue("selectedAddressIndex", "selectedAddressIndex.required",
                    "Bạn chưa có địa chỉ giao hàng. Vui lòng thêm địa chỉ.");
            return null;
        }

        if (selectedAddressIndex == null) {
            result.rejectValue("selectedAddressIndex", "selectedAddressIndex.required",
                    "Vui lòng chọn địa chỉ giao hàng.");
            return null;
        }

        if (selectedAddressIndex < 0 || selectedAddressIndex >= addresses.size()) {
            result.rejectValue("selectedAddressIndex", "selectedAddressIndex.invalid",
                    "Địa chỉ giao hàng không hợp lệ.");
            return null;
        }

        return addresses.get(selectedAddressIndex);
    }

    private void clearDefaultAddressFlag(User user) {
        for (User.Address address : user.getAddresses()) {
            address.setDefault(false);
        }
    }

}
