package jpabook.jpashop;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class initDb {

    private final InitService initService;

    /**
     * 애플리케이션 호출 시점에 해당 데이터를 만들기 위함
     * <p>
     * userA
     * - JPA1 BOOK
     * - JPA2 BOOK
     * userB
     * - SPRING1 BOOK
     * - SPRING2 BOOK
     */
    @PostConstruct
    public void init() {
        initService.dbInit1();
        initService.dbInit2();
    }

    @Component
    @RequiredArgsConstructor
    @Transactional
    static class InitService {

        private final EntityManager em;

        public void dbInit1() {
            Member member = createMember("userA", "서울", "길거리", "54321");
            em.persist(member);

            Book book1 = Book.createBook("JPA1", 10000, 100, "김영한", "23456");
            em.persist(book1);
            Book book2 = Book.createBook("JPA2", 20000, 100, "김영한", "34567");
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 10000, 1);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 20000, 2);

            Delivery delivery = createDelivery(member);

            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            em.persist(order);
        }

        public void dbInit2() {
            Member member = createMember("userB", "수원", "길거리", "12345");
            em.persist(member);

            Book book1 = Book.createBook("SPRING1", 20000, 200, "슈퓨람", "45678");
            em.persist(book1);
            Book book2 = Book.createBook("SPRING2", 40000, 300, "슈퓨람", "56789");
            em.persist(book2);

            OrderItem orderItem1 = OrderItem.createOrderItem(book1, 20000, 3);
            OrderItem orderItem2 = OrderItem.createOrderItem(book2, 40000, 4);

            Delivery delivery = createDelivery(member);

            Order order = Order.createOrder(member, delivery, orderItem1, orderItem2);
            em.persist(order);
        }

        private Delivery createDelivery(Member member) {
            Delivery delivery = new Delivery();
            delivery.setAddress(member.getAddress());
            return delivery;
        }

        private Member createMember(String userName, String city, String street, String zipcode) {
            Member member = new Member();
            member.setUsername(userName);
            member.setAddress(new Address(city, street, zipcode));
            return member;
        }
    }
}
