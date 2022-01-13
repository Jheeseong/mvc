package spring.mvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import spring.mvc.domain.Order;
import spring.mvc.domain.OrderSearch;
import spring.mvc.domain.User;
import spring.mvc.domain.item.Item;
import spring.mvc.service.ItemService;
import spring.mvc.service.OrderService;
import spring.mvc.service.UserService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final UserService userService;
    private final ItemService itemService;

    @GetMapping(value = "/order")
    public String createForm(Model model) {
        List<User> users = userService.findUsers();
        List<Item> items = itemService.findItems();

        model.addAttribute("users",users);
        model.addAttribute("items",items);

        return "order/orderForm";
    }

    @PostMapping(value = "/orders")
    public String order(@RequestParam("userId") Long userId,
                        @RequestParam("itemId") Long itemId, @RequestParam("count")int count) {

        orderService.order(userId,itemId,count);
        return "redirect:/orders";
    }

    @GetMapping(value = "/orders")
    public String orderList(@ModelAttribute("orderSearch")OrderSearch orderSearch, Model model) {

        List<Order> orders = orderService.findOrders(orderSearch);
        model.addAttribute("orders",orders);

        return "order/orderList";
    }

    @PostMapping(value = "/orders/{orderId}/cancel")
    public String cancelOrder(@PathVariable("orderId") Long orderId) {
        orderService.orderCancel(orderId);

        return "redirect:/orders";
    }
}
