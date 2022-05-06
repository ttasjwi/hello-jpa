
# hello-jpa

인프런 김영한 님의 "자바 ORM 표준 JPA 프로그래밍 - 기본편" 강의를 듣고 학습 내용을 간략하게 정리하기 위한 Repository

---

## 프로젝트 설정

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

- java : 11
- 빌드 : Maven
- 의존 라이브러리(`pom.xml`)
  - `com.h2database:1.4.200` : H2 데이터베이스
  - `org.hibernate:hibernate-entitymanager` : 하이버네이트 엔티티 매니저
  - `javax.xml.bind:javaxb-api` : java 11 이상 의존성 추가 해야함.

</div>
</details>

---

# Section 2 - JPA 시작하기

## 2.1 JPA의 구동방식

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

1. 설정정보 조회
   - `META.INF/persistence.xml`에서 Persistence 조회

2. 설정정보 기반 EntityManagerFactory 생성
   - 설정에 등록된 Persistence name 기반으로 EMF를 생성

3. 요청이 들어오고 나갈 때마다, EntityManager를 생성 후 버리기
   - enf.createEntityManger();

</div>
</details>

## 2.2 EntityManagerFactory, EntityManager

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

```java
EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

EntityManager em = emf.createEntityManager(); // 엔티티 매니저 생성
EntityTransaction tx = em.getTransaction(); // 트랜잭션 생성
tx.begin(); // 트랜잭션 시작 선언

try {
    //... 작업
    tx.commit(); // 커밋
} catch(Exception e) {
    tx.rollback();
} finally {
    em.close(); // 트랜잭션이 종료되면 EntityManager를 반환해야한다.
}

emf.close(); // 어플리케이션이 종료되기 전에 EntityManagerFactory를 반환
```
- EntityManagerFactory : 어플리케이션에서 DB당 한 개 생성
  - `persistence.xml`에서 지정해준 persistence-unit의 name을 인자로 생성하면 됨
  - 어플리케이션 구동 후 하나만 생성해서, 애플리케이션 전체에서 공유한다.

- EntityManager : 요청이 들어올 때마다 생성, 요청이 종료되면 버리면 됨
  - 여러 스레드가 공유해선 안 된다.

- EntityTransaction : 트랜잭션
  - JPA의 모든 데이터 변경은 트랜잭션 안에서 이루어져야한다.

</div>
</details>

## 2.3 JPA의 기본 CRUD

<details>
<summary>접기/펼치기</summary>
<div markdown="1">


기본적인 CRUD에 관한 메서드를 제공하는데, 데이터 변경은 트랜잭션 안에서 이루어져야한다.

- 등록 : `em.persist(...)`
- 기본키로 단건 조회 : `em.find(클래스, 기본키)
- 삭제 : `em.remove(...)`
- 수정 : `findMember.setName(...)`
  - 트랜잭션 안에서, 데이터 변경이 일어날 경우 commit 직전에 jpa가 변경 쿼리를 날려준다.

</div>
</details>

## 2.4 JPQL

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

```java
em.createQuery("SELECT m from Member as m").getResultList();
```
- JPA는 엔티티 객체 중심 개발.
- 검색 시 테이블이 아닌 엔티티 대상으로 검색.
- 검색 시 모든 DB 데이터를 가져와서 맵핑하여 객체를 생성하고, 필터링하기엔 비용이 너무 크다.
- 필요한 데이터만 DB에서 가져오려면 결국 검색조건이 포함된 SQL을 작성해야함.
- JPA는 SQL을 추상화한 JPQL이라는 객체지향 쿼리언어를 제공함. JPQL을 통해 엔티티 중심의 쿼리를 작성하고, JPA가 각 DBMS별 방언에 맞게 쿼리를 작성하여 날려줌

</div>
</details>

## 2.5 엔티티의 생명주기

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

![EntityLifeCycle.png](img/EntityLifeCycle.png)

- 비영속 : 영속성 컨텍스트와 무관하게 새로운 상태
  - 예> new Member();

- 영속 : 영속성 컨텍스트에 관리되는 상태
  - em.persist(member);

- 준영속 : 영속성 컨텍스트의 관리에서 벗어난 상태
  - em.detach(member);

- 삭제 : 엔티티를 영속성 컨텍스트, DB에서 삭제
  - em.remove()
  
### 엔티티의 생명주기 - 실험
```java
// 비영속
Member member = new Member(); // new (비영속)
member.setId(102L);
member.setName("helloJPA");

// 영속
System.out.println("=== BEFORE ===");
em.persist(member); // 영속(managed)
System.out.println("=== AFTER ===");

// 제거
em.remove(member);
tx.commit();
```
- 객체 생성
- 객체를 persist
  - persist 전, 후에 sout문을 두어, 언제 쿼리가 실행되는지 확인하기
- 객체를 remove

### 엔티티의 생명주기 - 결과

```
=== BEFORE ===
=== AFTER ===
Hibernate: 
    /* insert hellojpa.Member
        */ insert 
        into
            Member
            (name, id) 
        values
            (?, ?)
Hibernate: 
    /* delete hellojpa.Member */ delete 
        from
            Member 
        where
            id=?
```
- before, after 이후 쿼리가 연이어 나감.
- persist는 실제로 저장하는 것이 아니며, 영속성 컨텍스트가 중간에서 어떤 역할을 수행함을 알 수 있다.

</div>
</details>

---

# Section 3. 영속성 관리 - 내부 동작 방식

## 3.1 영속성 컨텍스트 1 : 1차 캐시 / 영속 엔티티의 동일성 보장

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

### 1차 캐시, 영속 엔티티의 동일성 보장

- 영속성 컨텍스트는 엔티티를 1차 캐시에 우선적으로 저장한다.
- key로 id, value로 엔티티를 저장함.
- 객체를 찾아올 때 1차 캐시에서 우선적으로 조회하고 존재하면 쿼리를 날려서 찾아오지 않고 바로 1차캐시에서 가져온다.
- 같은 캐시에서 찾아오므로 같은 영속성 컨텍스트의 동일 트랜잭션에서 관리되는 객체는 동일성(주솟값 같음)을 보장함

### 영속성 컨텍스트 1 - 실험
```java
Member member1 = em.find(Member.class, 101L);
Member member2 = em.find(Member.class, 101L);
System.out.println("member1 == member2 ? : " + (member1 ==member2));
tx.commit();
```
- 동일한 id로 EM을 통해 찾아오기 요청

### 영속성 컨텍스트 1 - 결과
```
Hibernate: 
    select
        member0_.id as id1_0_0_,
        member0_.name as name2_0_0_ 
    from
        Member member0_ 
    where
        member0_.id=?
member1 == member2 ? : true
```
- 실제로 select 쿼리가 날려지는 것은 단 한번
- 1차 캐시에 저장된 동일 객체를 찾아옴.
- 같은 캐싱된 객체를 찾아오므로 동일성이 보장된다.

</div>
</details>

## 3.2 영속성 컨텍스트 2 : 트랜잭션을 지원하는 쓰기 지연

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

### 트랜잭션을 지원하는 쓰기 지연
- `persist` : 영속성 컨텍스트의 1차 캐시에 저장 + 쓰기 지연 SQL 저장소에 쿼리를 저장함 
- tx.commit() -> flush(쿼리 날아감), commit(실제 반영)이 일어나며 실제로 DB에 반영됨
- 이를 활용하여, 대량의 쿼리를 날리는 것을 커밋 직전까지 지연시키고 모아서 처리(배치 처리) 가능.
  - 배치사이즈 조절 : `<property name="hibernate.jdbc.batch_size" value="..."/>`



### 트랜잭션을 지원하는 쓰기 지연 : 실험
```java
            Member member1 = new Member(150L, "A");
            Member member2 = new Member(160L, "B");

            em.persist(member1);
            em.persist(member2);
            System.out.println("=======================================");
            
            tx.commit();
```
- 객체 생성 후 persist
- sout문으로 구분선을 그어줌.
- commit
### 트랜잭션을 지원하는 쓰기 지연 : 실험결과
```
=======================================
Hibernate: 
    /* insert hellojpa.Member
        */ insert 
        into
            Member
            (name, id) 
        values
            (?, ?)
Hibernate: 
    /* insert hellojpa.Member
        */ insert 
        into
            Member
            (name, id) 
        values
            (?, ?)
```
- 실제 실행 시 구분선이 먼저 뜨고 쿼리가 날아감
- commit 이후 실제 쿼리가 날아감을 알 수 있음

</div>
</details>

## 3.3 영속성 컨텍스트 3 : 변경 감지(Dirty Checking)

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

### 변경 감지(Dirty Checking)
0. 스냅샷
   - JPA는 영속성 컨텍스트에 보관할 때, 최초 상태를 1차 캐시에 복사해서 저장함.

1. 트랜잭션 커밋 직전 `flush()` 호출
   - 트랜잭션을 커밋하면 엔티티 매니저 내부에서 먼저 `flush()`가 호출됨
      - 엔티티와 1차 캐시의 스냅샷을 비교하여 변경된 엔티티를 찾는다. 
      - 변경된 엔티티가 있으면 수정/삭제 쿼리를 생성 -> 쓰기지연 SQL 저장소에 보냄
        - 변경 : 스냅샷과 비교하여 변경점을 확인하고, update 쿼리를 생성
        - 삭제 : `em.remove(...)` -> delete 쿼리 생성
   - DB에 쿼리가 날아감

2. commit : 데이터베이스 트랜잭션을 실제 커밋(실제 반영)

### 변경 감지(Dirty Checking) - 실험
```java
Member member = em.find(Member.class, 150L);
member.setName("ZZZZZ");
System.out.println("=======================================");
tx.commit();
```
- DB에서 멤버를 찾아와서 1차 캐시에 가져옴
- setName을 호출하여 값을 변경한다.
- 커밋한다.
### 변경 감지(Dirty Checking) - 결과
```
Hibernate: 
    select
        member0_.id as id1_0_0_,
        member0_.name as name2_0_0_ 
    from
        Member member0_ 
    where
        member0_.id=?
=======================================
Hibernate: 
    /* update
        hellojpa.Member */ update
            Member 
        set
            name=? 
        where
            id=?
```
- 트랜잭션을 커밋하면 스냅샷과 비교하여 엔티티 변경을 감지하고 update 쿼리를 작성하여 날림

</div>
</details>

## 3.4 영속성 컨텍스트 - flush()

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

1. 플러시
   - 영속성 컨텍스트의 변경 내역이 실제 DB에 반영(동기화)

2. 플러시 방법
   - em.flush() : 직접 호출하여 강제로 동기화
   - 트랜잭션 커밋 : 트랜잭션 커밋 직전에 자동으로 호출됨
   - JPQL 쿼리 실행 : 쿼리 실행 직전 플러시 자동 호출

3. 플러시 모드 옵션
   - `em.setFulshMode(...)` : 거의 잘 안 씀. 웬만해선 디폴트값 쓰자.
     - FlushModeType.AUTO : 디폴트(커밋, 쿼리 실행 시 플러시)
     - FlushModeType.COMMIT : 커밋할 때만 플러시 (JPQL 실행시 flush 안 함)

```java
Member member = new Member(202L, "member202");
em.persist(member);
em.flush();
System.out.println("=======================================");
tx.commit();
```
```
Hibernate: 
    /* insert hellojpa.Member
        */ insert 
        into
            Member
            (name, id) 
        values
            (?, ?)
=======================================
```
- 커밋 직전에 쿼리가 날아가야하는데 flush를 강제 호출한 시점에 쿼리가 날아가서 반영됨

</div>
</details>

## 3.5 영속성 컨텍스트 - 준영속 상태(detached)

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

- 영속 상태의 엔티티가, 영속성 컨텍스트에서 분리된 상태
- 영속성 컨텍스트가 제공하는 기능을 사용하지 못 함. (DirtyChecking, ...)
- 준영속 상태로 만드는 방법
  - `em.detach(...)` : 특정 엔티티를 준영속 상태로 전환 
  - `em.clear()` : 영속성 컨텍스트를 완전히 초기화
  - `em.close()` : 영속성 컨텍스트 종료

```java
Member member = em.find(Member.class, 150L);
member.setName("AAAAAA");

em.detach(member); // 영속성 컨텍스트에서 떼어냄.
em.flush();
System.out.println("=======================================");
tx.commit();
```
- find(...) 호출 -> 영속성 컨텍스트에 존재하지 않음 -> DB에서 찾아옴
- member.setName(...) : 찾아온 엔티티의 상태를 변경
- `em.detach(member)` : member을 영속성 컨텍스트의 관리대상에서 제외함
```
Hibernate: 
    select
        member0_.id as id1_0_0_,
        member0_.name as name2_0_0_ 
    from
        Member member0_ 
    where
        member0_.id=?
=======================================
```
- DB에서 엔티티를 찾아오고, 내부 프로퍼티를 변경했음.
- 하지만 detach로 인해 영속성 컨텍스트의 관리대상에서 제외되어 update가 되지 않음

</div>
</details>

---

# Section 4. 엔티티 매핑

## 4.1 객체와 테이블 매핑

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

### @Entity
- 데이터베이스의 테이블과 매핑할 클래스
- name 값을 통해 JPA 내부적으로 사용할 이름을 지정할 수도 있긴한데 사용하지 않는걸 권장
- 기본생성자가 필수적(public, protected)
- 저장 필드에 final 기입 불가
- final, enum, interface, inner 클래스 사용 불가

### @Table

- 주로 엔티티와 매핑할 테이블 이름 지정(생략 시 엔티티 이름을 테이블 이름으로 사용)
- name, catalog, schema, uniqueConstraints
    - name : 매핑 테이블 이름
    - uniqueConstraints : DDL 생성 시 제약 조건

</div>
</details>

## 4.2 필드, 컬럼 매핑

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

1. `@Column` : 객체의 필드를 Column에 맵핑

2. `@Enumerated` : enum을 매핑할 때 사용
   - 주의 : 지정하지 않을 경우 value가 EnumType.ORDINAL로 지정되어버리는데 나중에 enum 변경으로 ordinal이 변경될 경우 매우 위험해진다. **반드시 EnumType.STRING을 쓰자.**

3. `@Temporal` : 날짜, 시간 맵핑에 사용하는데, java 1.8 이후 추가된 LocalDate, LocalTime, LocalDateTime을 사용하면 사용할 필요가 없다.

4. `@Lob` : Large Object
   - CLOB : `VARCHAR`로도 다루기 힘든 긴 문자열을 처리할 때
     - String, char[], java.sql.CLOB
   - BLOB : CLOB이 아닌 모든 LOB
     - byte[], java.sql.BLOB

5. `@Transient` : DB에 매핑하지 않음(주로 임시적으로 사용하고 싶은 필드)
   
6. `@Access` : 엔티티의 프로퍼티에 JPA가 접근하는 방식 결정
   - `AccessType.FIELD` : 필드에 직접 접근. 접근제어자가 private여도 접근할 수 있음.
   - `AccessType.PROPERTY` : getter를 통해 접근
   - 지정하지 않을 경우 `@Id`의 위치를 기준으로 접근방식이 설정됨

</div>
</details>

## 4.3 기본 키 매핑

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

### 4.3.1) 기본키 매핑 어노테이션
- `@Id` : 기본키 지정
- `@GeneratedValue` : 달아주면 DB가 자동으로 생성. 안 달면 수동 Id 지정해야함.

### 4.3.2) 기본키 매핑 방법
- 직접할당 : `@Id`만 사용
- 자동생성 : `@GeneratedValue`
  - 전략 : strategy
    - `GenerateType.AUTO`
    - `GenerateType.IDENTITY`
    - `GenerateType.SEQUENCE` 
    - `GenerateType.TABLE`

### 4.3.3) AUTO 전략
- 기본값. 데이터베이스 방언에 따라 DB 방언에 따라, 자동으로 지정된다.
  - `oracle` : sequence
  - ...

### 4.3.4) IDENTITY 전략 : DB에 위임
```java
Member member = new Member();
member.setUsername("C");

System.out.println("===================================");
em.persist(member);
System.out.println("==================================");

tx.commit();
```
```
===================================
Hibernate: 
    /* insert hellojpa.Member
        */ insert 
        into
            Member
            (id, name) 
        values
            (null, ?)
==================================
```
- 기본키 생성을 DB에 위임.
- MySQL, PostgreSQL, SQL Server, DB2 등에서 사용.
  - 예) MySQL의 AUTO_INCREMENT
- 보통 JPA는 트랜잭션 커밋 시점에, INSERT SQL을 실행한다. 하지만 IDENTITY 전략은 em.persist() 시점에 즉시 INSERT SQL을 실행하고 DB에서 식별자를 조회한다.
  - 엔티티가 영속상태가 되려면 식별자가 반드시 필요한데, IDENTITY 전략은 엔티티를 DB에 저장해야 식별자를 구할 수 있기 때문
- 벌크 INSERT에는 불리 (매 INSERT마다 DB와 통신해야함)

### 4.3.5) SEQUENCE 전략 
- 유일한 값을 순서대로 생성하는 특별한 오브젝트를 사용
- Oracle, PostgreSQL, DB2, H2에서 사용 가능(MySQL은 Sequence기능이 별도로 존재하지 않음)
- `@SequenceGenerator` : id 식별자값을 할당하는 시퀀스 생성기
  - `name` : sequenceGenerator의 이름이 값을 `@GeneratedValue`의 generator 속성에 넣어줘야함.
  - `sequenceName` : 매핑할 데이터베이스 Sequence 이름(실제 DB의 Sequence명을 매핑해야함)
  - `initialValue` : 시퀀스 DDL 생성시에만 사용하는 옵션. 처음 DDL 생성시 처음 시작하는 수를 지정
  - `allocationSize` : 시퀀스 한번 호출에 증가하는 숫자. (기본값 50)
    - 설정한 값만큼 한번에 시퀀스 값을 증가
    - 이 값만큼 메모리에서 식별자를 할당
    - 50을 넘어 51이 되면 시퀀스 값을 100으로 증가시키고 51~100까지 메모리에서 식별자 할당
  - `catalog`, `schema` : 데이터베이스 catalog, schema 이름 

### 4.3.6) TABLE 전략
- 키 생성 전용 테이블을 하나 마 들어서 데이터베이스 시퀀스를 흉내내는 전략
  - 장점 : 모든 DB에 적용 가능
  - 단점 : 성능
    - 값을 조회하면서 SELECT 쿼리를 사용하고, 다음 값으로 증가시키기 위해 update 쿼리를 또 날려야함.
    - SEQUENCE 전략에 비했을 때 DB와 한번 더 통신함.
- `@TableGenerator`
  - `name` : 식별자 생성기 이름
  - `table` : 키 생성 테이블명
  - `pkColumnName` : 시퀀스 칼럼명
  - `valueColumnNa` : 시퀀스 값 칼럼명
  - `pkColumnValue` : 키로 사용할 이름
  - `initialValue` : 초기값, 마지막으로 생성된 값이 기준 (기본값 0)
  - `allocationSize` : 시퀀스 한번 호출에 증가하는 수. (기본값 50)
  - `catalog` : 데이터베이스 catalog, schema 이름
  - `uniqueConstraints` : 유니크 제약 조건을 지정

### 4.3.7) 권장 식별자 전략
- 기본 키 제약조건 : not null, 유일, **변하면 안 된다.**
- 보통 위의 제약조건을 만족하는 자연키는 찾기 매우 힘듬. 대리키(대체키)를 사용하는 것이 좋다.
  - 자연키(Natural Key) : 비즈니스 모델에서 자연스레 나오는 속성으로 기본키를 정함 (회원로그인 아이디 등)
  - 대리키(Surrogate key, 인조키) : 인공적이거나 합성적인 키. MySQL의 AutoIncrement 등으로 자동적으로 생성된 키가 이에 해당.
- 비즈니스 로직에 깊게 얽혀있는 키는 미래에 변경 가능성이 있을 수 있다. 기본키로 사용하지 적절하지 않음.
  - 예) 주민등록번호, 회원 로그인 아이디, ...
- 권장
  - AutoIncrement / Sequence Object
  - UUID
  - 키 생성전략
     
</div>
</details>

---

# Section 5. 연관관계 매핑 - 기초

## 5.1) 단방향 연관관계

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

### 5.1.1) 테이블 중심 객체설계의 문제점
객체를 테이블에 맞추어 데이터 중심으로 모델링하면 협력 관계를 만들 수 없다.
- 테이블 : 외래키를 조인을 사용해서 연관된 테이블을 찾는다.
- 객체 : 참조를 사용해서 연관된 객체를 찾는다.

### 5.1.2) 단방향 연관관계
```java
    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;
```
- `@ManyToOne` : 다대일 관계라는 매핑정보.
  - (참고) : 일대다(OneToMany), 일대일(OneToOne) 관계도 존재
- `@JoinColumn`: 외래키 맵핑할 때 사용.
  - name : 매핑할 외래키

</div>
</details>

## 5.2 양방향 연관관계

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

### 5.2.1) 어노테이션
```java
    // Team 엔티티
    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();
```
- `@OneToMany` : 일대다 관계라는 매핑정보
  - `mappedBy` : 반대쪽 매핑의 필드명 지정.
    - 예) (Member 엔티티의 team 필드)
- 컬렉션은 필드에서 초기화

### 5.2.2) 연관관계의 주인
- 두 연관관계 중 하나를 연관관계의 주인으로 설정해야함.
  - 테이블 기준, 외래키는 한 곳에서 관리하고 양방향 참조가 가능.
  - 객체는 양쪽에서 두개의 참조가 존재. 어느 쪽의 변경이 실제 DB에 반영되는지를 정함
- 외래키가 있는 곳을 연관관계의 주인으로 한다.
  - 반대편은 읽기만 가능하고, 외래키를 변경하지 못 함.

### 5.2.3) 양방향 연관관계 - 주의
```java
      Team team = new Team();
            team.setName("TeamA");
            em.persist(team);

            Member member = new Member();
            member.setName("member1");
            member.setTeam(team);
            em.persist(member);

//            em.flush();
//            em.clear();


            Member findMember = em.find(Member.class, member.getId());
            List<Member> members = findMember.getTeam().getMembers(); //  // 1차 캐시에 보관된 team이 찾아짐
            System.out.println("=============================");
            for (Member m : members) {
                System.out.println("m = "+m.getName()); // 하지만 team 입장에서 멤버를 찾아보면 멤버가 없음.
            }
            System.out.println("=============================");
            tx.commit();
```
- 연관관계의 주인에 값을 입력해야 실제 쿼리가 날려질 때 반영됨.
  - 가짜 매핑(주인의 반대편)에서만 연관관계를 설정하면 반영되지 않는다.
```java
    public void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
        }
```
- 객체지향적 설계를 고려했을 때는(코드 상의 완결성 관점에서) 자바 코드에서 양쪽 다 값을 입력하는게 맞다.
  - 연관관계 편의 메서드를 활용하자. (1 또는 다 어느 한 쪽에 두기)
- 양방향 매핑 시 무한 루프 주의
  - `toString` -> 무한 루프
  - 엔티티를 그대로 JSON으로 넘겨버릴 때 : Controller 단에서 반환할 때는 DTO를 거쳐서 반환하도록 하자.

### 5.2.4) 양방향 연관관계 - 정리
- 단방향 매핑만으로 이미 연관관계 매핑은 완료됨.
  - DB에 반영되는 것은 외래키를 통한 단방향 매핑관계 뿐이다.
  - 양방향 매핑은 실무에서, 실제로 무한루프와 같은 위험요소를 발생시킬 가능성이 존재.
  - 정말 필요한게 아닌 이상 가급적 단방향 설계를 하는 것이 맞다.
- 양방향 매핑은 반대 방향으로 조회 기능이 추가된 것 뿐
- 하지만 JPQL에서 역방향 탐색을 할 일이 많음...
- 단방향 매핑을 잘 하고, 양방향은 필요할 때 추가해도 됨. (테이블에 영향을 주지는 않는다.)

</div>
</details>

---

# Section 6. 다양한 연관관계 매핑

## 6.1 연관관계 매핑 시 고려사항

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

### 6.1.1) 다중성
- 다대일 : `@ManyToOne`
- 일대다 : `@OneToMany`
- 일대일 : `@OneToOne`
- 다대다 : `@ManyToMany`

### 6.1.2) 단방향, 양방향
- 테이블
  - 한쪽에 외래키를 걸어두고 외래 키 하나로 양쪽 조인 가능
  - 방향이라는 개념이 없음.
- 객체
  - 참조용 필드가 있는 쪽으로만 참조 가능
  - 한쪽만 참조하면 단방향
  - 양쪽이 서로 참조하면 양방향

### 6.1.3) 연관관계의 주인
- 테이블은 외래 키 하나로 두 테이블이 연관관계를 맺음
- 객체 양방향 관계는 A->B, B->A처럼 참조가 2곳
- 객체 양방향 관계는 참조가 2곳에 있음. 둘 중 외래키 관리할 곳을 지정해야함
- 연관관계의 주인 : 외래키를 관리하는 참조
- 주인의 반대편 : 외래키에 영향을 주지 않음. 단순 조회만 가능

</div>
</details>

## 6.2 다대일(N:1)

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

### 6.2.1) 다대일 - 단방향 연관관계
- `@ManyToOne`으로 매핑
- 가장 많이 사용하는 연관관계
- 다대일의 반대는 일대다

### 6.2.2) 다대일 - 양방향 연관관계
- 외래키가 있는 곳이 연관관계의 주인
  - 반대쪽에서는 `@OneToMany(mappedBy= "...")`으로 매핑. 이는 읽기 전용
- 양쪽을 서로 참조하도록 개발

</div>
</details>

## 6.3 일대다(1:N)

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

### 6.3.1) 일대다 - 단방향 매핑
```java
@OneToMany
@JoinColumn(name = "team_id")
private List<Member> members = new ArrayList<>();
```
- DB : 외래키를 다쪽에서 관리하는건 같음.
- '일'쪽을 연관관계의 주인으로 할 때는 `@JoinColumn`을 사용
- 컬렉션의 요소가 변경될 경우, UPDATE 쿼리가 날아감
- JoinColumn을 두지 않을 경우 조인테이블(중간에 테이블을 하나 추가함) 방식 사용

### 6.3.2) 일대다 - 양방향 연관관계
```java
@ManyToOne
@JoinColumn(name = "team_id", insertable = false, updatable = false)
private Team team;
```
- '다'쪽에 `@JoinColumn`을 둠
- 읽기전용으로 설정 (`insertable=false`,`updateable = false`)

### 6.3.3) 일대다 - 단점
- 엔티티가 관리하는 외래키가 다른 테이블에 있음.
- 연관관계를 위해 추가로 UPDATE 쿼리 실행
- 양방향을 쓰려거든 차라리 다대일 양방향을 사용하는 것 낫다.

</div>
</details>

## 6.4 일대일(1:1)

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

### 6.4.1) 일대일 개요
- 일대일 관계는 반대방향도 일대일
- 주 테이블 또는 대상 테이블 중에서 외래 키를 선택 가능
  - 주 테이블에 외래키 (김영한님 추천)
  - 대상 테이블에 외래키
- 외래키쪽에 데이터베이스 유니크 제약조건 주가 (FK, UNI)

### 6.4.2) 일대일 - 주 테이블에 외래키 단방향
- 다대일(`@ManyToOne`) 단방향 매핑과 유사함

### 6.4.3) 일대일 - 주 테이블에 외래키 양방향
- 반대쪽애 `@OneToOne(mappedBy = "반대족 필드명")`

### 6.4.4) 일대일 - 대상 테이블에 외래키를 두고 단방향
- 대상 테이블에서 외래키, 유니크 제약조건을 붙여도 JPA에서는 이를 매핑할 수단이 없다.

### 6.4.5) 일대일 - 대상 테이블에 외래키를 두고 양방향
- 대상 엔티티를 연관관계의 주인으로 둠
- 주 엔티티쪽에서는 대상 엔티티를 mappedBy로 맵핑(읽기전용)
- 사실상 일대일 주 테이블에 외래키 양방향과 매핑 방법은 같다.

### 6.4.6) 일대일 정리
1. 주 테이블에 외래키
   - 주 객체가 대상 객체의 참조를 가지는 것처럼, 주 테이블에 외애키를 두고 대상 테이블 조회
   - 객체 지향 개발자 선호
   - JPA 매핑 편리
   - 장점 : 주 테이블만 조회해도 대상 테이블에 데이터가 있는 지 확인 가능
   - 단점 : 값이 없으면 외래키에 null 허용

2. 대상 테이블에 외래키
   - 대상 테이블에 외래키가 존재함.
   - 전통적인 데이터베이스 개발자(DBA)가 선호
   - 장점 : 주 테이블과 대상 테이블을 일대일에서 일대다로 변경할 때 테이블 구조 유지
     - 대상 테이블을 변경하고, 주 테이블에서는 mappedBy로 가져오기만 하면 됨.
   - 단점 : 프록시 기능의 한계로 지연로딩(LazyLoading)으로 설정해도 항상 즉시 로딩됨.
     - 엔티티 생성 시 대상 테이블에 대상이 있는지 여부를 확인하기 때문에 항상 즉시 로딩이 강제됨... 

</div>
</details>

## 6.5 다대다(M:N)

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

### 6.5.1) 다대다 - 개요
1. 데이터베이스 관점
   - 관계형 데이터베이스는 정규화된 테이블 2개로 다대다 관계를 표현할 수 없음
   - 연결 테이블을 추가, 일대다-다대일 관계로 풀어써야함.

2. 객체 관점
   - 컬렉션을 사용해서 객체 2개로 다대다 관계를 표현 가능
   - `@ManyToMany`, `@JoinTable`로 연결 테이블을 지정
   - 다대다 매핑은 단방향, 양방향 모두 가능

### 6.5.2) 다대다 - 한계
- 편리해보이지만 실무에서 사용하지 않음
- 연결 테이블이 단순히 연결만 하고 끝나지 않는다...
  - 세부적으로 attribute가 더 추가될 수 있음 (주문 시간, 수량 등 부가적인 데이터)

### 6.5.3) 다대다 - 한계의 극복
- 연결 테이블용 엔티티 추가(연결 테이블을 엔티티로 승격)
- `@ManyToMany` -> 단방향 - `@ManyToOne` / 양방향 - `@OneToMany`로 풀어냄.
- 중간 엔티티에는 별도의 PK(비즈니스 로직상으로 아무런 의미 없는 데이터베이스 생성값)을 사용

</div>
</details>

---

# Section 7. 고급 매핑

## 7.1 상속관계 매핑

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

### 7.1.1) 상속관계를 매핑하는 방법들
- 상속관계 매핑 : 객체의 상속 구조 - DB의 서브타입 관계를 매핑
  - 관계형 데이터 베이스 : 상속관계 없다. 슈퍼타입 - 서브타입 관계라는 모델링 기법이 객체 상속과 유사
  - 객체 : 상속관계 있다.
- 슈퍼타입 - 서브타입의 논리 모델을 실제 물리 모델로 구현하는 방법
  - 조인 전략 : 추상클래스, 구현 클래스 각각 테이블로 분리, 변환
  - 단일테이블 전략 : 한 테이블에 모두 때려박기
  - 구현클래스마다 테이블 전략 : 서브타입 테이블로 변환

### 7.1.2) 어노테이션
- `@Inheritance(strategy=InheritanceType.XXX)`
  - JOINED : 조인전략
  - SINGLE_TABLE : 단일테이블 전략(JPA에서 디폴트로 사용하는 전략)
  - TABLE_PER_CLASS : 구현 클래스마다 테이블 전략
- `@DiscriminatorColumn(name="DTYPE")`
  - 상위클래스에 사용
  - 구체클래스들의 타입을 구분하는데 사용. 디폴트 네임은 "DTYPE"이다.
- `@DiscriminatorValue(value = "...")`
  - DTYPE의 구분에 사용될 이름 결정
  - 디폴트 : 클래스명

### 7.1.3) 조인전략
- 특징
  - 상위 클래스, 하위 클래스 테이블을 모두 생성
  - INSERT 시 두 테이블에 쿼리를 날림
  - 하위 클래스 테이블에서는 상위클래스 테이블의 PK를 그대로 가져다가 PK, FK로 사용한다.
- 장점
  - 테이블 정규화 : 불필요한 필드가 줄어듬
  - 외래키 참조 무결성 제약조건 활용 가능
  - 저장공간 효율화
- 단점
  - 조회 시 조인을 많이 사용. 성능이 저하됨. (단일 테이블과 비교시 테이블이 복잡해지는 단점)
  - 조회 쿼리가 복잡해짐
  - 데이터 저장시 INSERT 쿼리를 두번 호출

### 7.1.4) 단일테이블 전략
- 특징
  - 추상클래스, 모든 구체 클래스의 프로퍼티를 한 테이블에 싹 때려박음
  - 구체클래스를 구분하기 위해 구분 필드인 `DTYPE`를 필수로 둠
- 장점
  - 조인이 필요 없으므로 일반적으로 조회 성능이 빠름
  - 조회 쿼리가 단순함(한 테이블에서 찾기만 하면 됨)
- 단점
  - 자식 엔티티가 매핑한 칼럼은 모두 null 허용(데이터 무결성 관점에서 위험하다.)
  - 단일 테이블에 모든 것을 저장하므로, 테이블이 커질 수 있음.
    - 상황에 따라서, 조회 성능이 오히려 조인 전략보다 느려질 수 있음.

### 7.1.5) 구현 클래스마다 테이블 전략
- 특징
  - 상위 클래스에 대응하는 테이블을 생성하지 않음.
  - 구체 클래스마다 테이블을 다 따로 만듬.
  - DTYPE(`@DiscriminatorColumn(name="DTYPE")`으로 지정)을 쓰지 않음
- 장점
  - 서브 타입을 명확하게 구분해서 처리할 때 효과적
  - not null 제약조건 사용 가능 -> 무결성 유지 차원에서 유리
- 단점
  - 여러 자식 테이블을 함께 조회할 대 성능이 느림(UNION SQL 필요)
    - 모든 구현 테이블을 싹다 조회함...
  - 자식 테이블을 통합해서 쿼리하기 어려움...
  - 테이블이 추가될 때마다 비즈니스 로직을 다시 짜야하는 경우가 생김.
    - 예) 정산 코드를 짤 때 추가된 테이블을 고려해서 다시 짜야함...
- **DBA, ORM 전문가 양쪽 다 추천하지 않는 전략. 쓰지마!!!**

### 7.1.6) 결론
- 조인 전략, 단일테이블 전략 양쪽 간의 이점, 단점 양쪽을 고려해서 상황에 맞게 DBA와 협의 후 결정
- 구현클래스마다 테이블 전략은 웬만해선 쓰지말자...

</div>
</details>

## 7.2 `@MappedSuperclass` - 매핑 정보 상속
<details>
<summary>접기/펼치기</summary>
<div markdown="1">

- 엔티티가 아님. 테이블과 매핑하는 용도의 클래스가 아님.
- 상속관계를 매핑하는 것이 아님.
- 여러 entity들에게 공통의 속성을 가지게 하고 싶을 때 사용
  - 자손 클래스들에는 매핑 정보만 제공함.
  - 상위 클래스의 테이블은 생성되지 않음
- 조회 불가. em.find(BaseEntity) 불가
- 직접 생성해서 사용할 일이 없으므로 추상 클래스 권장

### `@MappedSuperclass`
```java
@MappedSuperclass
public class BaseEntity {

    private String createdBy;
    private LocalDateTime createdDate;
    private String lastModifiedBy;
    private LocalDateTime lastModifiedDate;
```
- 테이블과 관계 없고, 단순히 엔티티가 공통적으로 사용하는 맵핑정보를 모으는 역할
- 주로 등록일, 수정일, 등록자, 수정자 같은 전체 엔티티에서 공통으로 적용하는 정보를 모을 때 사용
- 참고 : `@Entity` 클래스는 엔티티나 `@MappedSuperclass`로 지정한 클래스만 상속 가능

</div>
</details>

---

# Section 8. 프록시와 연관관계 관리

## 8.1 프록시

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

### 8.1.1 프록시 기초
- em.find()
  - DB에서 실제 entity 객체 조회
- em.getReference()
  - 데이터베이스 조회를 미루는 프록시(가짜) 객체 생성
  - 초기화 시점에 영속성 컨텍스트를 통해 DB에서 진짜 엔티티 객체를 가져오고 그 참조를 가짐

### 8.1.2 프록시의 특징
```
refMember.class = class hellojpa.domain.Member$HibernateProxy$8zLDrlOX
```
- 실제 클래스를 상속받아서 만들어짐 
  - 하이버네이트가, 여러 프록시라이브러리를 사용하여 만들어내는 객체
- 실제 클래스와 겉 모양이 같다.
- 사용하는 입장에서는 진짜 객체인지, 프록시 객체인지 구분하지 않고 사용하면 된다. (이론상으로는. 실제로는 몇 가지 주의할 것이 있음)
- 프록시 객체는 실제 객체의 참조(target)를 보관
- 프록시 객체의 메서드를 호출하면, 프록시는 내부의 참조(target)의 실제 메서드를 호출
  - Proxy(가짜) - 위임(delegate) - target(Entity)

### 8.1.3 프록시의 초기화
1. `em.getReference`
   - 영속성 컨텍스트에 이미 객체 있을 경우 : 해당 객체의 참조를 반환
   - 영속성 컨텍스트에 객체가 존재하지 않을 경우 : 빈 프록시 객체를 생성
2. 메서드 호출 / 강제 초기화
   - target이 초기화 되어있을 경우 target의 메서드를 호출한다.
   - target이 존재하지 않을 경우 3으로 넘어간다.
3. 영속성 컨텍스트에 target 초기화 요청
4. 영속성 컨텍스트는 DB를 조회하여 엔티티를 가져온 뒤, 프록시의 target에 참조를 연결한다.
5. 실제 target을 통하여 메서드를 호출한다.

### 8.1.4 프록시의 특징
- 프록시 객체는 처음 사용할 때 한 번만 초기화한다. 이후 객체 참조는 변하지 않음
- 프록시 객체를 초기화할 때, 프록시 객체가 실제 엔티티로 바뀌는 것은 아님. 초기화되면 프록시 객체를 통해서 실제 엔티티에 접근 가능하다.
- 프록시 객체는 원본 엔티티를 상속받는다. 따라서 **타입 체크시 주의해야함.**
  - 원본과 프록시 객체의 타입이 같음이 보장되지 않는다.
  - `인스턴스1.getClass() == 인스턴스2.getClass()`로 타입을 비교하지 않고, `instance of` 연산자를 통해 타입을 비교할 것.
- 영속성 컨텍스트에 찾는 엔티티가 이미 있으면 `em.getReference`를 호출해도 실제 엔티티가 반환된다.
  - 역으로 영속성 컨텍스트에 프록시 객체가 있을 경우, `em.find`를 호출할 때 프록시 객체가 반환된다.
  - 왜?(다음 두가지 이유)
    1. 이미 1차 캐시에 있는데 굳이 DB에서 새로 가져올 필요가 없음
    2. 객체가 가짜 객체인지 진짜 객체인지 여부에 관계 없이 JPA는 같은 영속성 컨텍스트 안의 동일 PK를 가진 객체에 대하여 동일성을 보장해야함.
- 영속성 컨텍스트의 도움을 받을 수 없는 준영속 상태(detach, clear) 상태일 때 프록시를 초기화하면 문제가 발생한다.
  - 하이버네이트는 `org.hibernate.LazyInitializationException` 예외를 발생시킴
    - 영속성 컨텍스트를 통해 프록시를 초기화하지 못 함

### 8.1.5 프록시 확인
- 프록시 인스턴스의 초기화 여부 확인
  - `emf.getPersistenceUnitUtil().isLoaded(Object entity)` : 이 프록시 인스턴스가 초기화 됐니?
- 프록시 클래스인지 확인 여부
  - `entity.getClass().getName()` 출력해보기
- 프록시 강제 초기화
  - 하이버네이트 : `org.hibernate.Hibernate.initialize(entity)`
  - JPA 표준 : 강제 초기화 메서드가 존재하지 않음. 메서드를 초기화해야 초기화됨
    - 예) `member.getName()`

</div>
</details>

## 8.2 즉시 로딩과 지연 로딩

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

### 8.2.1 지연 로딩
```java
@ManyToOne(fetch = FetchType.LAZY)
```
- 영속성 컨텍스트에서 엔티티를 찾아올 때, 엔티티가 참조하는 다른 엔티티를 프록시로 가져옴
  - 메서드 호출 또는 강제 초기화 시, 초기화(쿼리 날아감)

### 8.2.2 즉시 로딩
```java
@ManyToOne(fetch = FetchType.EAGER)
```
- 영속성 컨텍스트에서 엔티티를 찾아올 때, 엔티티가 참조하는 다른 엔티티를 한번에 조인해서 가져옴
- JPA 구현체는 가능하면 조인을 사용해서 SQL 한번에 함께 조회
- 즉시로딩은 JPQL 이용시, 쿼리 여러방이 날아감.

### 8.2.3 프록시와 즉시로딩 주의
- 가급적 지연 로딩만 사용(특히 실무에선 지연 로딩만 사용해야함)
- 즉시 로딩을 적용하면 예상치 못 한 SQL이 발생한다.
- 즉시 로딩은 JPQL에서 N+1 문제를 발생시킨다.
  - 최초쿼리, 추가쿼리 N개
- `@ManyToOne`, `@OneToOne`은 기본이 즉시 로딩이다.
  - LAZY로 설정할 것!!!
- `@OneToMany`, `@ManyToMany`는 기본이 지연 로딩이다.

### 8.2.4 지연로딩 - 이론적
- 함께 자주 사용하는 연관관계는 즉시로딩
- 가끔 사용하는 연관관계는 지연로딩

### 8.2.5 지연로딩 활용 - 실무
```
Hibernate: 
    /* select
        m 
    from
        Member as m */ select
            member0_.member_id as member_i1_3_,
            member0_.createdBy as createdB2_3_,
            member0_.createdDate as createdD3_3_,
            member0_.lastModifiedBy as lastModi4_3_,
            member0_.lastModifiedDate as lastModi5_3_,
            member0_.name as name6_3_,
            member0_.team_id as team_id7_3_ 
        from
            member member0_
Hibernate: 
    select
        team0_.team_id as team_id1_7_0_,
        team0_.name as name2_7_0_ 
    from
        team team0_ 
    where
        team0_.team_id=?
Hibernate: 
    select
        team0_.team_id as team_id1_7_0_,
        team0_.name as name2_7_0_ 
    from
        team team0_ 
    where
        team0_.team_id=?
```
- 모든 연관관계에 지연 로딩을 사용해라!
- 실무에서 즉시 로딩을 사용하지 마라!
- JPQL fetch 조인이나 엔티티 그래프 기능을 사용해라!
- 즉시 로딩은 상상하지 못 한 쿼리가 나간다! (위의 경우 한 번에 3번의 쿼리가 날아감...)

</div>
</details>

## 8.3 영속성 전이(CASCADE)와 고아 객체

<details>
<summary>접기/펼치기</summary>
<div markdown="1">


### 8.3.1 영속성 전이(CASCADE)
- 특정 엔티티를 영속 상태로 만들 때, 연관된 엔티티도 함께 영속상태로 만들고 싶은 경우.
  - 예) 부모1, 자손2을 등록할 때 부모 등록, 자손 등록 2번을 해야함. 이를 한 번에 할 수 없을까?

### 8.3.2 영속성 전이 - 주의점
- 영속성 전이는 연관관계를 매핑하는 것과 아무런 관련이 없다.
- 엔티티를 영속화할 때 연관된 엔티티도 함께 영속화하는 편리함을 제공하는 용도

### 8.3.3 CASCADE의 종류
- **ALL**
- **PERSIST**
  - 저장 : 부모와 연관된 자식들도 모두 영속화
- **REMOVE**
  - 삭제 : 부모 엔티티를 삭제하면 자식 엔티티들도 함께 삭제
- MERGE : 병합
- REFRESH
- DETACH

### 8.3.4 고아객체
```java
Parent findParent = em.find(Parent.class, parent.getId());
findParent.getChilds().remove(0);
```
- 고아객체 제거 : 부모 엔티티와 연관관계가 끊어진 자식 엔티티를 자동으로 삭제하는 기능
  - `orphanRemoval = true, false` : true일 때 고아객체 자동 삭제
- 부모 객체의 컬렉션에서 자손을 제거할 때, delete 쿼리 날아감

### 8.3.5 고아객체 - 주의
- 참조가 제거된 엔티티는 다른 곳에서 참조하지 않는 고아 객체로 보고, 삭제하는 기능
- 참조하는 곳이 하나일 때 또는 특정 엔티티가 개인 소유할 때 사용 (라이프 사이클이 거의 비슷한 상황)
  - 여러 parent들이 child를 소유하는 경우에는 고아객체 삭제 옵션을 두거나, cascade 걸어두면 위험하다.
- `@OneToOne`, `@OneToMany`만 사용 가능.
- 참고 : 개념적으로 부모를 제거(`em.remove(parent)`)하면 자식은 고아가 된다. 고아객체 삭제기능을 사용하면, 부모가 제거될 때 자식도 제거된다. 이는 CascadeType.REMOVE 처럼 동작한다.

### 8.3.6 영속성 전이 + 고아 객체, 생명주기
- `CASCADEType.ALL + orphanRemoval = true`
- 스스로 생명주기를 관리하는 엔티티는 `em.persist()`로 영속화, `em.remove()`로 제거
- 두 옵션을 모두 활성화하면 부모 엔티티를 통해서 자식의 생명 주기를 모두 관리할 수 있다.
  - 부모 Repository만 만들고 자식 엔티티의 생명주기는 부모를 통해서 관리
- 도메인 주도 설계(Domain-driven design, DDD)의 Aggregate Root 개념을 구현할 때 유용

</div>
</details>

---

