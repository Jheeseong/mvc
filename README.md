# mvc

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
@PutMapping 은 데이터 수정할 때 사용(데이터 전체를 생신하는 HTTP 메서드)  
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

