package spring.mvc.service;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import spring.mvc.domain.Address;
import spring.mvc.domain.Order;
import spring.mvc.domain.OrderStatus;
import spring.mvc.domain.User;
import spring.mvc.domain.item.Item;
import spring.mvc.domain.item.Top;
import spring.mvc.repository.OrderRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import java.lang.reflect.Member;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
class OrderServiceTest {

    @PersistenceContext
    EntityManager em;

    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;

    @Test
    public void 주문() throws Exception {
        //given
        User user = createUser();
        Top item = createTop("T-shirt",10000,100,"pink");
        int orderCount = 2;
        //when
        Long orderId = orderService.order(user.getId(), item.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals(OrderStatus.ORDER,getOrder.getStatus(),"상품 주문 상태는 ORDER");
        assertEquals(1,getOrder.getOrderItemList().size(),"주문한 상품 종류 수는 정확");
        assertEquals(10000*2,getOrder.getTotalPrice(),"주문 가격");
        assertEquals(98,item.getStockQuantity(),"주문 수량");
        assertEquals("pink",item.getColor(),"상의 색상");
    }

    private User createUser() {
        User user = new User();
        user.setUsername("UserA");
        user.setAddress(new Address("대구","용산로","111-111"));
        em.persist(user);
        return user;
    }

    private Top createTop(String name, int price, int stockQuantity, String color) {
        Top top = new Top();
        top.setName(name);
        top.setPrice(price);
        top.setStockQuantity(stockQuantity);
        top.setColor(color);
        return top;
    }
}