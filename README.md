# mvc

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

