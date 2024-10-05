package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.OrderSimpleQueryDto;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * xToOne(컬렉션이 아닌 관계)
 * Order
 * Order -> Member (ManyToOne)
 * Order -> Delivery (OneToOne)
 */
@RestController
@RequiredArgsConstructor
public class OrderSimpleApiController {

    private final OrderRepository orderRepository;

    /**
     * 간단한 주문 조회 V1 : 엔티티를 직접 노출
     * Order에서 Member 호출, Member에서 다시 Order 호출을 하면서 무한루프
     * 양방향이 걸리는 곳에 둘 중에 하나는 @JsonIgnore를 해줘야함
     */
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getUsername(); // Lazy 강제 초기화
            order.getDelivery().getAddress(); // Lazy 강제 초기화
        }
        return all;
    }

    /**
     * 간단한 주문 조회 V2 : 엔티티를 DTO로 변환
     * N+1 문제 발생
     */
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        /*return orderRepository.findAllByString(new OrderSearch()).stream()
                .map(SimpleOrderDto::new)
                .collect(Collectors.toList());*/

        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        return orders.stream().map(o -> new SimpleOrderDto(o)).collect(Collectors.toList());
    }

    @Data
    static class SimpleOrderDto {
        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            this.orderId = order.getId();
            this.name = order.getMember().getUsername(); // Lazy 강제 초기화
            this.orderDate = order.getOrderDate();
            this.orderStatus = order.getStatus();
            this.address = order.getDelivery().getAddress(); // Lazy 강제 초기화
        }
    }

    /**
     * 간단한 주문 조회 V3 : 엔티티를 DTO로 변환 - 페치 조인 최적화
     * 엔티티를 페치 조인을 사용해서 쿼리 1번에 조회
     * 페치 조인으로 order -> member, order -> delivery는 이미 조회된 상태이므로 지연로딩X
     */
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> ordersV3() {
        List<Order> orders = orderRepository.findAllWithMemberDelivery();
        return orders.stream().map(o -> new SimpleOrderDto(o)).collect(Collectors.toList());
    }

    /**
     * 간단한 주문 조회 V4 : JPA에서 DTO로 바로 조회
     * 쿼리 1번 호출
     * select 절에서 원하는 데이터만 선택해서 조회
     */
    @GetMapping("/api/v4/simple-orders")
    public List<OrderSimpleQueryDto> ordersV4() {
        return orderRepository.findOrderDtos();
    }
}
