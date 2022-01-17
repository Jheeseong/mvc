# mvc 

# v1.12 1/16 1/17
## API 개발 고급
### API 개발 고급 - 컬렉션 조회
#### 엔티티 직접 노출

    @RestController
    @RequiredArgsConstructor
    public class OrderCollectionController {

       private final OrderRepository orderRepository;

       //1. 엔티티 노출//
       @GetMapping("/api/v1/orders")
       public List<Order> OrderV1() {
           List<Order> all = orderRepository.findAll(new OrderSearch());
             for (Order order : all) {
                 order.getUser().getUsername();
                 order.getDelivery().getAddress();
                 List<OrderItem> orderItemList = order.getOrderItemList();
                 orderItemList.stream().forEach(o -> o.getItem().getName());
             }
             return all;
       }
   }

- 문제점
  - 엔티티  노출 시 AIP 스펙 변화
  - 트랜젝션 안에서 지연 로딩 필요
  - 양방향 연관관계 문제(JsonIgnore)

#### 엔티티를 DTO로 변환

    //2. DTO 변환//
    //(API 로직)
    @GetMapping("/api/v2/orders")
    public List<OrderDto> OrdersV2() {
        List<Order> orders = orderRepository.findAll(new OrderSearch());
        List<OrderDto> result = orders.stream()
                .map(order -> new OrderDto(order))
                .collect(toList());

        return result;
    }

    //(DTO)
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
            name = order.getUser().getUsername();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();
            orderItems = order.getOrderItemList().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(toList());
        }
    }

    //(DTO)
    @Data
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
    
- 문제점
  -  1+N+M 문제 발생 : order(조회 1번) -> user,addres(지연 로딩 조회 N번) -> orderItem(지연 로딩 조회 N번) -> item(지연 로딩 조회 N번) 순으로 **쿼리가 총 1 + N + M + P 번 실행** , 이미 조회된 경우 영속성 컨텍스트에서 조회하여 쿼리를 생략 

- 해결 방법
  - **join fetch 를 사용**하여 1 + N + M 문제를 해결!!

#### 엔티티를 DTO 변환 - 페이징과 한계 돌파
- 컬렉션을 조인 패치 시 페이징이 불가능
  - 컬렉션을 조인 패치 하면 일대다 조인이 발생하여 데이터가 증가
  - 일대다에서 1을 기준으로 페이징 하는 것이 목적 but 데이터는 다(N)를 기준으로 row 생성
  - order 기준으로 페이징하려고 하였으나, 다(N)인 orderitem을 조인하면 orderitem이 기준이 됨

- 한계 돌파
  - ToOne관계를 모두 조인 패치 -> row수를 증가시키지 않음(페이징 영향X)
  - 컬렉션은 지연 로딩
  - hibernate.default_batch_fetch_size 글로벌 설정  

        //3.1 DTO 변환 - join fetch//
        @GetMapping("/api/v3.1/orders")
        public List<OrderDto> OrderV3_page(
              @RequestParam(value = "offset", defaultValue = "0") int offset,
              @RequestParam(value = "limit", defaultValue = "100") int limit) {
           List<Order> orders = orderRepository.findUserDelivery();
           List<OrderDto> result = orders.stream()
                  .map(order -> new OrderDto(order))
                  .collect(toList());
           return result;
        }
      
- 장점
  - 쿼리 호출 수가 1 + N 에서 1 + 1 로 최적화
  - 조인보다 DB 데이터 전솔량이 최적화
  - 조인 패치 방식과 비교해서 쿼리 호출 수는 약간 증가하지만 DB 데이터 전송량 감소
  - 컬렉션 조인 패치 는 페이징 불가하지만 이 방법은 페이징 가능      
      
- 결론
  - ToOne 관계는 조인 패치하여도 페이징에 영향 X
  - ToOne 관계는 조인 패치로 쿼리 수를 줄이고 나머지는 fetch_size 설정으로 최적화

#### JPA에서 DTO 직접 조회

    //4. JPA에서 DTO 조회
    (API 로직)
    @GetMapping("/api/v4/orders")
    public List<OrderListQueryDto> OrderV4() {
        List<OrderListQueryDto> result = orderCollectionRepository.findOrderQueryDtos();
        return result;
    }
    
**OrderCollectionRepository에 쿼리 추가**

    public List<OrderListQueryDto> findOrderQueryDtos() {
        List<OrderListQueryDto> result = findOrders();
        result.forEach(o ->{
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
            o.setOrderItems(orderItems);
        });
        return result;
    }
    
**findOrders - ToOne 관계 조회**

    private List<OrderListQueryDto> findOrders() {
        return em.createQuery("select new spring.mvc.repository.api.OrderListQueryDto(o.id, m.username, o.orderDate, o.status, d.address)" +
                " from Order o" +
                " join o.user m" +
                " join o.delivery d",OrderListQueryDto.class)
                .getResultList();
    }
    
**findOrderItems - ToMany 관계 조회**

    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em. createQuery("select new spring.mvc.repository.api.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)" +
                " from OrderItem oi" +
                " join oi.item i" +
                " where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }
    
**Order DTO 추가**

    @Data
    public class OrderListQueryDto {

    private Long orderId;
    private String name;
    private LocalDateTime orderDate;
    private OrderStatus orderStatus;
    private Address address;
    private List<OrderItemQueryDto> orderItems;

    public OrderListQueryDto(Long orderId, String name, LocalDateTime orderDate, OrderStatus orderStatus, Address address) {
        this.orderId = orderId;
        this.name = name;
        this.orderDate = orderDate;
        this.orderStatus = orderStatus;
        this.address = address;
	}
    }
    
**OrderItem Dto 추가**

    @Data
    public class OrderItemQueryDto {

    @JsonIgnore
    private Long orderId;
    private String itemName;
    private int OrderPrice;
    private int count;

    public OrderItemQueryDto(Long orderId, String itemName, int orderPrice, int count) {
        this.orderId = orderId;
        this.itemName = itemName;
        this.OrderPrice = orderPrice;
        this.count = count;
        }
    }

# v1.11 1/14,1/15
## API 개발 고급
### 지연로딩과 조회 성능 최적화
- 지연로딩으로 인한 성능 문제를 단계적으로 해결해볼 계획
#### V1 : 엔티티 직접 노출(xToOne 관계)

    @RestController
    @RequiredArgsConstructor
    public class OrderApiController {

    private final OrderRepository orderRepository;

    //v1 : 엔티티 직접 노출
    @GetMapping("/api/v1/simple-orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAll(new OrderSearch());
        for (Order order : all) {
            order.getUser().getUsername();
            order.getDelivery().getAddress();
            }
        return all;
        }
    }

- 문제점
  - 엔티티 직접 노출 
  - Jackson의 프록시 객체 인식 문제 : 모든 것이 **LAZY를 통한 지연로딩 설정**이 되어 있어 order에 있는 user과 address는 **프록시 객체** (프록시 객체 초기화가 이루어지지 않기 떄문)  
  이러한 상황에서 JSON이 되는 과정에서 프록시 객체를 인식하지 못해 **예외 발생& 무한루프 발생**
  - 지연 로딩(LAZY)를 피하기위해 즉시 로딩(EARGR)으로 설정 X, 항상 지연 로딩을 기본으로 하고, 성능 최적화 필요 시 **조인 패치(join fetch)** 를 사용!!

- 해결 방법
  - Hibernate5Module + @JsonIgnore 사용 : **초기화된 프록시 객체만 노출**되어 무한루프 해결
        
        @SpringBootApplication
        public class MvcApplication {

             public static void main(String[] args) {
             SpringApplication.run(MvcApplication.class, args);
	         }

	         @Bean
             Hibernate5Module hibernate5Module() {
	         return new Hibernate5Module();
             }
        }
        
  - **DTO** 사용 -> 가장 좋은 방법

#### V2 : DTO 사용

    //DTO 사용//
    @GetMapping("/api/v2/simple-orders")
    public List<SimpleOrderDto> ordersV2() {
        List<Order> orders = orderRepository.findAll(new OrderSearch());
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }

    @Data
    static class SimpleOrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate; 
        private OrderStatus orderStatus;
        private Address address;

        public SimpleOrderDto(Order order) {
            orderId = order.getId();
            name = order.getUser().getUsername(); //LAZY 초기화
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress(); //LAZY 초기화
        }
    }
    
- 문제점
  - 1+N+M 문제 발생 : order(조회 1번) -> user(지연 로딩 조회 N번) -> delivery(지연 로딩 조회 N번) 순으로 **쿼리가 총 1 + N + M 번 실행** , 이미 조회된 경우 영속성 컨텍스트에서 조회하여 쿼리를 생략 

- 해결 방법
  - **join fetch 를 사용**하여 1 + N + M 문제를 해결!!

#### V3 : DTO 변환 후 join fetch 사용

    //3. join fetch 사용//
    @GetMapping("/api/v3/simple-orders")
    public List<SimpleOrderDto> orderV3() {

        List<Order> orders = orderRepository.findUserDelivery();
        List<SimpleOrderDto> result = orders.stream()
                .map(o -> new SimpleOrderDto(o))
                .collect(Collectors.toList());

        return result;
    }
    
    public class OrderRepository {
    ....
        public List<Order> findUserDelivery() { //쿼리 작성 후 join fetch
            return em.createQuery("select o from Order o" +
                    " join fetch o.user u" +
                    " join fetch o.delivery d", Order.class)
                    .getResultList();
        }
    }
    
- 문제점 : 해당 엔티티의 모든 column을 가져와 성는 저하의 문제 발생(성능 차이는 미비)  
![image](https://user-images.githubusercontent.com/96407257/149612418-a19a4874-e4b8-4d6f-b886-09d0544d95dd.png)

- 해경방법 : JPQL을 직접 받는 DTO 설정(효과는 미비), **DTO + fetch join은 대부분 최적화 문제 해결**, JPQL에 직접 DTO 설정하는 것은 **선택적으로 설정**
    
#### V4 : JPA로 DTO 설정

    //4. JPA를 사용한 DTO
    //(API 로직)
    @GetMapping("/api/v4/simple-orders")
    public List<OrderQueryDto> ordersV4() {
        return orderJpaRepository.findQueryDto();
    }

- JPQL에 DTO를 직접 설정해서 원하는 필드만 받게 설정

      //(JPQL - Repository)
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
   
- JPQL에 new 패키지 명.dto 를 통해서 JPQL의 결과를 DTO로 즉시 변환, 리포지토리 재사용성 떨어짐
    
      //(DTO)
      @Data
      public class OrderQueryDto {
         private Long orderId;
         private String name;
         private LocalDateTime orderDate;
         private OrderStatus orderStatus;
         private Address address;

         public OrderQueryDto(Long orderId, String name, LocalDateTime orderDate, OrderStatus orderStatus, Address address) {
             this.orderId = orderId;
             this.name = name;
             this.orderDate = orderDate;
             this.orderStatus = orderStatus;
             this.address = address;
          }
      }
    
- 생성자에 객체가 아닌 필드를 직접 받아서 매핑

- 결론 
  - 엔티티를 DTO로 변환시 Repository 재사용성도 좋고 개발도 단순  
  - DTO로 바로 조회 시 성능이 좋아짐(효과는 미비)

- 쿼리 방식 선택 권장 순서
  - 우선 엔티티를 DTO로 변환 -> 필요 시 join fetch로 성능 최적화(대부분 분제 해결) -> 추가 필요 시 DTO로 직접 조회 방법 선택 -> 최후의 방법으로 JPA가 제공하는 네이티브SQL 혹은 스프링 JDBC Template을 사용하여 SQL 직접 사용

# v1.10 1/13
## API 개발 기본
### 회원 등록 API

#### 엔티티를 RequestBody에 직접 매핑

    @RestController // JSON으로 변환
    @RequiredArgsConstructor
    public class ApiMemberController {

        private final UserService userService;

        @PostMapping("/api/v1/members")
        public UserResponse saveUserV1(@RequestBody @Valid User user) {
        ...
        return new UserResponse(id,name);
        
    @Data
    static class UserResponse {
        private Long id;
        private String name;

        public UserResponse(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

- @RestController : JSON으로 변환시켜주는 어노테이션
- @RequsetBody : API통신을 통해 JSON으로 온 바디를 매핑
- 만약 엔티티에 로직이 추가될 경우(API 검증 로직, @NotEmpty 등) 엔티티가 변하게 되고 API 스펙도 변함  
-> API 요청 스펙에 맞춰 별도의 DTO를 파라미터로 받으며 해결!!

####  DTO를 RequestBody에 매핑
    
    @PostMapping("/api/v2/members")
    public UserResponse saveUserV2(@RequestBody @Valid UserDto dto) {

        User user = new User();
        user.setUsername(dto.getName());

        Long id = userService.join(user);
        String name = dto.getName();
        return new UserResponse(id,name);
    }

    @Data
    static class UserDto{
        private Long id;
        private String name;
    }
    
- UserDto를 User엔티티 대신 RequestBody에 매핑
- 엔티티와 프레젠테이션 계층을 위한 로직을 구분
- 엔티티와 API 스펙을 명확하게 구분
- 엔티티가 변해도 API 스펙은 변하지 않음
- 실무에서 엔티티를 API 스펙에 노출X

### 회원 수정 API
- 변경 감지를 사용해서 데이터 수정

      //회원 수정
      @PutMapping("/api/v2/members/{id}")
      public UpdateUser updateUserV2(@PathVariable("id") Long id,
                                   @RequestBody @Valid UserDto dto){
          userService.update(id, dto.getName());
          User userFind = userService.findOne(id);
          return new UpdateUser(userFind.getId(),userFind.getUsername());
      }

      @Data
      @AllArgsConstructor
      static class UpdateUser {
          private Long id;
          private String name;
      }
      
      public class UserService {
      ....
      @Transactional
      public void update(Long id, String name) {
          User user = userRepository.findOne(id);
          user.setUsername(name);
      ....
      }
      
### 회원 조회 API
#### 응답 값으로 엔티티를 직접 외부에 노출

    //회원 조회 - 엔티티 직접 조회
    @GetMapping("/api/v1/members")
    public List<User> UsersV1() {
        return userService.findUsers();
    }
    
- 엔티티에 프레젠테이션 계층을 위한 로칙이 추가
- 엔티티의 모든 값이 노출
- 응답 스펙을 맞추기 위해 로직이 추가(@JsonIgnore 등의 로직)
- 엔티티가 변경되면 API 스펙이 변함
- 추가로 컬렉션 직접 반환 시 API 스펙을 변경하기 힘듦
-> 별도의 DTO를 반환

#### 응답 값으로 엔티티가 아닌 별도의 DTO 사용
    
    //회원 조회 - DTO 조회
    @GetMapping("/api/v2/members")
    public Result UsersV2() {

        List<User> findUsers = userService.findUsers();

        List<UserDto> collect = findUsers.stream()
                .map(u -> new UserDto(u.getUsername()))
                .collect(Collectors.toList());

        return new Result(collect);
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {  //바로 return 할 경우 JSON 배열 타입으로 반환되어 유연성 
        private T data;
    }
    
- 엔티티를 DTO로 변환하여 반환
- 엔티티가 변해도 API 스펙 변경 X
- result 클래스로 컬렉션을 감싸서 향후 필요한 필드 추가 가능

### 결론 : 엔티티를 매핑하여 노출시키지말고 DTO로 변환 후 사용하자!!!!
# v1.09 1/5

### MVC

MVC : Model,View,Controller로 구성된 디자인 패턴

Controller   

    @Controller
    public class BasicController {
    
    @GetMapping("basic-mvc")
    public String basicMvc(Model model) {
    ...
    }
    return "basic-template";
    
View

    <html xmlns:th="http://www.thymeleaf.org">
    <body>
    <p th:text="'hello ' + ${name}">hello! empty</p>
    </body></html>
    
### API

@ResponseBody 문자 반환

@ResponseBody를 사용하면 viewResoler을 사용X  
대신 HTTP의 BODY에 문자 내용을 직접 반환

    @Controller
    public class BasicController {
     @GetMapping("basic-string")
     @ResponseBody
     public String basicString(@RequestParam("name") String name) {
     return "hello " + name;
       }
     }
     
@ResponseBody 객체 반환

@ResponseBody를 사용하고 객체가 반환되면 객체가 JSON으로 변환

    @Controller
    public class BasicController {
     @GetMapping("basic-api")
     @ResponseBody
     public Basic basicApi(@RequestParam("name") String name) {
     Basic basic = new Basic();
     basic.setName(name);
     return basic;
     }
     static class Basic {
     private String name;
     public String getName() {
     return name;
     }
     public void setName(String name) {
     this.name = name;
       }
     }
    }
    
### 일반적 웹 애플리케이션 계층 구조

![app계층](https://user-images.githubusercontent.com/96407257/148177104-10b9b9eb-534c-4b78-a755-43dfc98d0231.JPG)  

컨트롤러 : 웹 MVC의 컨트롤러 역할  
서비스 : 핵심 비지니스 로직 구현  
리포지토리 :  데이터베이스에 접근, 도메인 객체를 DB에 저장하고 관리  
도메인 : 비지니스 도메인 객체

# v1.08 1/4

http 관련 코드

- Lombok의 Slf4j  
Slf4j는 log를 사용할 수 있게 해주는 애노테이션  
log는 system.out.println() 보다 IO리소스 사용이 적어 메모리 관리에 효율적  
log.info를 통해 출력이 가능  
    
      @Slf4j
      ...
      log.info("home Controller");
    
- @valid  
validation을 한 객체 내 @NotEmpty, NotNull 등을 체크하여 validation 한다.  

      public String create(@Valid MemberForm form)

- BindingResult
@Valid 와 BindingResult가 있을 시 오류가 담겨 다음 코드를 진행해준다.

      public String create(@Valid MemberForm form, BindingResult result) {
          if (result.hasErrors()){
              return "members/createMemberForm";
          }
          ...

- http Mapping

http Method | 동작 | URL 형태
---- | ---- | ----
GET | 조회(select * read) | /user/{id}
POST | 생성(create) | /user
PUT / PATCH | 수정(update) * create | /user
DELETE | 삭제(delete) | /user/{1}

@GetMapping 은 데이터를 가져올 떄 사용  
@PostMapping 은 데이터를 보내줄 떄(게시,생성) 사용  
@PutMapping 은 데이터 수정할 때 사용(데이터 전체를 신하는 HTTP 메서드)  
@PatchMapping 은 데이터 수정할 떄 사용(수정하는 영역만 갱신하는 HTTP 메서드)  




# v1.07 1/3

변경 감지와 병합(merge)

- 준영속 엔티티  
영속성 컨텍스트가 관리하지 않는 엔티티  
여기서 수정을 시도하는 Book 객체는 이미 DB에 한번 저장되어 식별자가 존재  
기존 식별자를 가지고 있다면 준영속 엔티티

- 준영속 엔티티 수정 방법  
변경 감지 기능 사용  
병합(merge) 사용

변경 감지 기능 사용

    @Transactional
    void update(Item itemParam) { //itemParam: 파라미터로 넘어온 준영속 엔티티
        Item findItem = em.find(Item.class, itemParam.getId()); //갘은 엔티티 조회
        
        findItem.setPrice(ItemParam.getPrice()); //데이터 수정
    }
    
트렌잭션 안에서 엔티티를 다시 조회,변경할 값 선택 -> 트랜잭션 커밋 시점에 변경 감지(Dirty checking) -> 데이터베이스에 UPDATE SQL 실행

병합 사용

    @Transactional
    void update(Item itemParam) { //itemParam: 파라미터로 넘어온 준영속 엔티티
        item mergeItem = em.merge(item);
    }

준영속 엔티티의 식별자 값으로 영속 엔티티 조회 -> 영속 엔티티 값을 준영속 엔티티의 값으로 모두 교체(member 엔티티의 모든 값을 mergeMember에 집어 넣음) -> 트랜젝션 커밋 시점에 변경 감지 -> DB에 UPDATE SQL 실행  

변경 감지 기능은 원하는 속성만 선택하여 변경 가능하지만, 병합 사용 시 모든 속성이 변경  
-> 병합(merge) 사용 시 값이 없으면 null로 업데이트하는 위험이 존재

엔티티를 변결 할 떄 변경 감지 사용을 권장  
컨트롤러에서 엔티티 생성X  
트랜잭션이 있는 서비스 계층에 id와 데이터를 명확하게 전달  
트랜잭션이 있는 서비스 계층에서 영속 상태의 엔티티 조회 후 엔티티 데이터를 직접 변경  

# v1.06 1/2

TopForm, ItemController 클래스 추가

수정 시 /items/{itemId}/edit URL을 GET 방식으로 요청  

# v1.05 1/1

UserForm, UserController 클래스 추가

폼 객체를 사용해서 화면 계층과 서비스 계층 분리

조회한 상품을 뷰에 전달하기 위해 스프링MVC의 모델객체에 보관

폼 객체 사용 이유

- 엔티티를 직접적으로 사용 시 엔티티가 화면에 종속적으로 변하고 그렇게 되면 화면 기능에 의해 지저분해져 유지보수가 어려움
- 실무에서 엔티티는 핵심 비지니스 로직만 가지고 화면을 위한 로직은 없어야 한다
- 폼 객체 혹은 DTO 사용을 권장

# v1.04 12/31

Order Search 기능 및 homeController 클래스 추가

OrderRepository에 JPQL을 통해 검색 조건을 통한 쿼리 검색 코드 추가(추후 Querydsl 추가 예정)
# v1.03 12/30

Order의 Repository, service 클래스 추가

비지니스 로직 cancel, TotalPrice를 Order과 OrderItem에 추가

OrderService의 order과 ordercancel을 보면 비지니스 로직 대부분이 엔티티에 존재

서비스계층은 엔티티에 필요한 요청을 위임하는 역할

이처럼 엔티티가 비지니스 로직을 가지고 객체 지향의 특성을 적극 활용하는 것을 도메인 모델 페턴이라 함

반대로 엔티티에 비지니스 로직이 없고 서비스 계층에서 대부분의 비지니스 로직을 처리하는 것을 트랜잭션 스크립트 패턴이라 함


정적 팩토리 메서드 적용

- 생성자 대신 정적 팩토리 메서드 사용이 유용
- 정적 팩토리 메서드는 이름이 존재(객체의 생성 목적을 담을수 있음)
- 호출 마다 새로운 객체 생성 필요 X
- 하위 자료형 객테 반환
- 객테 생성을 캡슐화(데이터 은닉) -> 생성자를 메서드 안으로 숨기며 외부에 드러내지 않고 개게 인터페이스를 단순화

Test 클래스 작성 후 Test 완료(상품 주문)


# v1.02 12/30

Item의 repository, service 클래스 추가

비지니스 로직 addStock와 removeStock을 Item 도메인에 추가

Stock 갯수 파악 후 부족 시 예외 기능 추가

# v1.01 12/29

User의 repository, service 클래스 추가 

스프링 필드 주입 대신 생성자 주입을 사용

-> 변경 불가능한 안전한 객체 생성 가능, 생성자 하나 시 @Autowired 생략 가능

-> lombok을 통해 생성 가능

@RunWith(SpringRunner.class) : 스프링과 테스트 통합

@Transactional : 반복 가능한 테스트 지원, 각각의 테스트 실행마다 트랜젝션을 시작하고 테스트 종료 시 트랜젝션 강제 롤백
# v1.00 12/29

각 기능 별 엔티티 클래스 개발 및 엔티티 연관관계 매핑

Order와 Item은 다대다 관계지만 거의 사용하지 않음.

-> ManyToMany는 중간 테이블에 칼럼 추가가 불가능하고 세밀한 쿼리 실행이 어려움

-> Order - OrderItem - Item 으로 다대다 관계를 다대일 관계로 풀어냄

Hat, Top, Pants은 상품으로 공통 속성을 사용하므로 상곡 구조로 표현

외래 키가 있는 곳을 연관관계의 주인으로 정함(대체적으로 OneToMany 경우 many쪽이 연관관계 주인!)

※ 엔티티 설계 시 주의점
- 엔티티에는 가급적 Setter 사용 X

Setter가 열려있다면 변경 포인트가 많아 유지보수가 힘듦

- 모든 연관관계는 지연로딩 설정

즉시로딩(EAGER)은 예측이 어렵고 어떤 SQL이 실행될지 추적이 어려움

- 컬렉션은 필드에서 초기화

