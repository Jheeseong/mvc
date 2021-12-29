# mvc
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

