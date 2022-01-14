package spring.mvc;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import spring.mvc.domain.*;
import spring.mvc.domain.item.Top;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;

@Component
@RequiredArgsConstructor
public class InitDb {

    private final InitService initService;
    @PostConstruct
    public void init() {
        initService.dbInit1();
        initService.dbInit2();
    }
    @Component
    @Transactional
    @RequiredArgsConstructor
    static class InitService {
        private final EntityManager em;
        public void dbInit1() {
            User user = createUser("userA", "서울", "강남", "1111");
            em.persist(user);
            Top tShirt1 = createTop("White shirt", 10000, 100);
            em.persist(tShirt1);
            Top tShirt2 = createTop("Black shirt", 20000, 100);
            em.persist(tShirt2);
            OrderItem orderItem1 = OrderItem.createOrderItem(tShirt1, 10000, 1);
            OrderItem orderItem2 = OrderItem.createOrderItem(tShirt2, 20000, 2);
            Order order = Order.createOrder(user, createDelivery(user),
                    orderItem1, orderItem2);
            em.persist(order);
        }
        public void dbInit2() {
            User user = createUser("userB", "대구", "동성로", "2222");
            em.persist(user);
            Top tShirt1 = createTop("Pink shirt", 20000, 200);
            em.persist(tShirt1);
            Top tShirt2 = createTop("Green Shirt", 40000, 300);
            em.persist(tShirt2);
            Delivery delivery = createDelivery(user);
            OrderItem orderItem1 = OrderItem.createOrderItem(tShirt1, 20000, 3);
            OrderItem orderItem2 = OrderItem.createOrderItem(tShirt2, 40000, 4);
            Order order = Order.createOrder(user, delivery, orderItem1,
                    orderItem2);
            em.persist(order); }
        private User createUser(String name, String city, String street,
                                  String zipcode) {
            User user = new User();
            user.setUsername(name);
            user.setAddress(new Address(city, street, zipcode));
            return user;
        }
        private Top createTop(String name, int price, int stockQuantity) {
            Top top = new Top();
            top.setName(name);
            top.setPrice(price);
            top.setStockQuantity(stockQuantity);
            return top;
        }
        private Delivery createDelivery(User user) {
            Delivery delivery = new Delivery();
            delivery.setAddress(user.getAddress());
            return delivery;
        }
    }
}
