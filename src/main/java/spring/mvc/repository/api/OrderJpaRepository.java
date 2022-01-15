package spring.mvc.repository.api;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class OrderJpaRepository {

    private final EntityManager em;

    public List<OrderQueryDto> findQueryDto() {
        return em.createQuery(
                "select new spring.mvc.repository.api.OrderQueryDto(o.id, m.username, o.orderDate, o.status, d.address)" +
                        " from Order o" +
                        " join o.user m" +
                        " join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }
}