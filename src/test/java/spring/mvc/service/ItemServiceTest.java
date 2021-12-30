package spring.mvc.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import spring.mvc.domain.item.Hat;
import spring.mvc.domain.item.Item;
import spring.mvc.domain.item.Top;
import spring.mvc.repository.ItemRepository;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
class ItemServiceTest {

    @Autowired
    ItemRepository itemRepository;
    @Autowired
    ItemService itemService;
    @Autowired
    EntityManager em;
    @Test
    public void itemTest() throws Exception {
        //given
        Item top1 = createTop("padding","pink",20000);
        Item top2 = createTop("T-shirt","black",10000);
        //when
        itemService.saveItem(top1);
        itemService.saveItem(top2);
        //then
        assertEquals(top2,itemRepository.findOne(top2.getId()));

    }
    public Top createTop(String name, String color, int price) {
        Top top = new Top();
        top.setName(name);
        top.setColor(color);
        top.setPrice(price);
        return top;
    }
}