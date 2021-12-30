# mvc
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

