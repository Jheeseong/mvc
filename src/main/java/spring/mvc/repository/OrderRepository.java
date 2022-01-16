package spring.mvc.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import spring.mvc.domain.Order;
import spring.mvc.domain.OrderSearch;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderRepository {
    private final EntityManager em;


    public void save(Order order) {
        em.persist(order);
    }

    public Order findOne(Long id) {
        return em.find(Order.class, id);
    }

    public List<Order> findAll(OrderSearch orderSearch) {
        //language=JPAQL
        String jpql = "select o From Order o join o.user m";
        boolean isFirstCondition = true;
        //주문 상태 검색
        if (orderSearch.getOrderStatus() != null) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " o.status = :status";
        }
        //회원 이름 검색
        if (StringUtils.hasText(orderSearch.getUserName())) {
            if (isFirstCondition) {
                jpql += " where";
                isFirstCondition = false;
            } else {
                jpql += " and";
            }
            jpql += " m.name like :name";
        } TypedQuery<Order> query = em.createQuery(jpql, Order.class)
                .setMaxResults(1000); //최대 1000건
        if (orderSearch.getOrderStatus() != null) {
            query = query.setParameter("status", orderSearch.getOrderStatus());
        }
        if (StringUtils.hasText(orderSearch.getUserName())) {
            query = query.setParameter("name", orderSearch.getUserName());
        }
        return query.getResultList();
        }

    public List<Order> findUserDelivery() {
        return em.createQuery("select o from Order o" +
                " join fetch o.user u" +
                " join fetch o.delivery d", Order.class)
                .getResultList();
    }

    public List<Order> findAllOrderItem() {
        return em.createQuery(
                "select o from Order o" +
                        " join fetch o.user u" +
                        " join fetch o.delivery d" +
                        " join fetch o.orderItemList oi" +
                        " join fetch oi.item i", Order.class)
                .getResultList();
    }
}
