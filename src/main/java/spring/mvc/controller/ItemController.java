package spring.mvc.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import spring.mvc.domain.item.Item;
import spring.mvc.domain.item.Top;
import spring.mvc.repository.ItemRepository;
import spring.mvc.service.ItemService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @GetMapping(value = "/items/new")
    public String createForm(Model model) {
        model.addAttribute("form", new TopForm());
        return "items/createItemForm";
    }

    @PostMapping(value = "/items/new")
    public String create(TopForm form) {

        Top top = Top.createTop(form.getName(), form.getPrice(),
                form.getStockQuantity(), form.getSize(), form.getColor());

        itemService.saveItem(top);
        return "redirect:/items";
    }

    //상품 목록//
    @GetMapping(value = "/items")
    public String list(Model model) {

        List<Item> items = itemService.findItems();
        model.addAttribute("items", items);
        return "items/itemList";
    }

    //상품 수정//
    @GetMapping(value = "/items/{itemId}/edit")
    public String updateItemForm(@PathVariable("itemId") Long itemId, Model model) {

        Top item = (Top) itemService.findOne(itemId);

        TopForm form = new TopForm();
        form.setId(item.getId());
        form.setName(item.getName());
        form.setPrice(item.getPrice());
        form.setStockQuantity(item.getStockQuantity());
        form.setColor(item.getColor());
        form.setSize(item.getSize());

        model.addAttribute("form", form);
        return "items/updateItemForm";
    }

    @PostMapping(value = "/items/{itemId}/edit")
    public String updateItem(@ModelAttribute("form") TopForm form) {

        itemService.updateItem(form.getId(), form.getName(), form.getPrice(), form.getPrice());
        return "redirect:/items";
//        Top top = new Top();
//        top.setId(form.getId());
//        top.setName(form.getName());
//        top.setPrice(form.getPrice());
//        top.setStockQuantity(form.getStockQuantity());
//        top.setColor(form.getColor());
//        top.setSize(form.getSize());
//
//        itemService.saveItem(top);

    }
}
