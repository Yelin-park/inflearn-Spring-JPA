package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import jpabook.jpashop.service.query.OrderDto;
import jpabook.jpashop.service.query.OrderQueryService;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

/**
 * V1. 엔티티 직접 노출
 * - 엔티티가 변하면 API 스펙이 변한다.
 * - 트랜잭션 안에서 지연 로딩 필요
 * - 양방향 연관관계 문제
 * <p>
 * V2. 엔티티를 조회해서 DTO로 변환(fetch join 사용X)
 * - 트랜잭션 안에서 지연 로딩 필요
 * V3. 엔티티를 조회해서 DTO로 변환(fetch join 사용O)
 * - 페이징 시에는 N 부분을 포기해야함(대신에 batch fetch size? 옵션 주면 N -> 1 쿼리로 변경 가능)
 * <p>
 * V4. JPA에서 DTO로 바로 조회, 컬렉션 N 조회 (1 + N Query)
 * - 페이징 가능
 * V5. JPA에서 DTO로 바로 조회, 컬렉션 1 조회 최적화 버전 (1 + 1 Query)
 * - 페이징 가능
 * V6. JPA에서 DTO로 바로 조회, 플랫 데이터(1Query) (1 Query)
 * - 페이징 불가능
 */
@RestController
@RequiredArgsConstructor
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    /**
     * 주문 조회 V1: 엔티티 직접 노출
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getUsername(); // lazy 강제 초기화
            order.getDelivery().getAddress(); // lazy 강제 초기화
            List<OrderItem> orderItems = order.getOrderItems();
            orderItems.stream().forEach(o -> o.getItem().getName()); // lazy 강제 초기화
        }
        return all;
    }

    /**
     * 주문 조회 V2 : 엔티티를 DTO로 변환
     * 문제점은 DTO 안에 엔티티가 있음. 이렇게 하는 것도 안된다. 의존성을 완전히 끊어내야함
     */
    @GetMapping("/api/v2/orders")
    public List<OrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());

        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(toList());

        return collect;
    }

    @Data
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItems;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getUsername();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            // 아래와 같이 엔티티를 가지고 있으면 안된다.
            /*order.getOrderItems().stream().forEach(o -> o.getItem().getName()); // 프록시 초기화
            orderItems = order.getOrderItems();*/
            orderItems = order.getOrderItems().stream().map(orderItem -> new OrderItemDto(orderItem)).collect(toList());
        }
    }

    @Getter
    static class OrderItemDto {

        private String itemName;
        private int orderPrice;
        private int count;

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }

    /**
     * 주문 조회 V3 : 엔티티를 DTO로 변환, 페치 조인 최적화
     */

    private final OrderQueryService orderQueryService;

    @GetMapping("/api/v3/orders")
    public List<jpabook.jpashop.service.query.OrderDto> ordersV3() {
        return orderQueryService.ordersV3();
    }

    /**
     * 주문 조회 V3.1 : 엔티티를 DTO로 변환, 페이징과 한계 돌파
     */
    @GetMapping("api/v3.1/orders")
    public List<OrderDto> ordersV3_page(
            @RequestParam(value = "offset", defaultValue = "0") int offset,
            @RequestParam(value = "limit", defaultValue = "100") int limit
    ) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);
        return orders.stream().map(o -> new OrderDto(o)).collect(toList());
    }

    /**
     * 주문 조회 V4 : JPA에서 DTO 직접 조회
     */
    @GetMapping("api/v4/orders")
    public List<OrderQueryDto> ordersV4() {
        return orderQueryRepository.findOrderQueryDtos();
    }

    /**
     * 주문 조회 V5 : JPA에서 DTO 직접 조회, 컬렉션 조회 최적화
     */
    @GetMapping("api/v5/orders")
    public List<OrderQueryDto> ordersV5() {
        return orderQueryRepository.findAllByDto_optimization();
    }

    /**
     * 주문 조회 V6 : JPA에서 DTO로 직접 조회, 플랫 데이터 최적화
     */
    @GetMapping("api/v6/orders")
    public List<OrderFlatDto> ordersV6() {
        return orderQueryRepository.findAllByDto_flat();
    }

    @GetMapping("api/v6.1/orders")
    public List<OrderQueryDto> ordersDistinctV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

        return flats.stream()
                .collect(groupingBy(o -> new OrderQueryDto(o.getOrderId(),
                                o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        mapping(o -> new OrderItemQueryDto(o.getOrderId(),
                                o.getItemName(), o.getOrderPrice(), o.getCount()), toList())
                )).entrySet().stream()
                .map(e -> new OrderQueryDto(
                        e.getKey().getOrderId(),
                        e.getKey().getName(),
                        e.getKey().getOrderDate(),
                        e.getKey().getOrderStatus(),
                        e.getKey().getAddress(),
                        e.getValue()
                ))
                .collect(toList());
    }
}
