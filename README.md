
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

# Section 9. 값 타입

## 9.1 기본 값 타입

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

### 9.1.1 엔티티 타입 vs 기본 값 타입
1. 엔티티 타입
   - `@Entity`로 정의하는 객체
   - 데이터가 변해도(내부값이 변경되어도) 식별자(id)로 지속해서 추적 가능
   - 예) 회원 엔티티의 키나 나이 값을 변경해도 식별자로 인식 가능

2. 값 타입 : 값으로 쓰이는 것. 값 그 자체.
   - int, Integer, String처럼 단순히 값으로 사용하는 자바 기본 타입
   - 식별자가 없고 값만 있으므로 변경 시 추적 불가.

### 9.1.2 값 타입의 분류
- 기본값 타입 : 기본적인 값을 사용하는 타입
  - 자바 기본 타입(int, double, boolean)
  - 래퍼 클래스(Integer, Long)
  - String
- 임베디드 타입(embedded type, 복합 값 타입) : 별도로 JPA에서 정의해서 사용해야함
- 컬렉션 값 타입(collection value type)

### 9.1.3 기본 값 타입 상세
- 기본타입, 래퍼 클래스, String
- 생명주기를 엔티티에 의존함
  - 예) Member - `name`, `age` // 회원 삭제 시 이름, 나이 필드도 같이 삭제 됨
- 값 타입은 공유하면 안 됨.
  - 예) 회원 이름 변경 시 다른 회원의 이름도 함께 변경되면 안 됨.
- 참고 : java와 기본 값 타입
  - int, double과 같은 기본 타입(Primitive)은 다른 변수와 공유되지 않는다.
    - 기본 타입은 값 그 자체를 복사하여 전달함.
  - Integer와 같은 래퍼클래스 또는 String과 같은 특수 클래스는 참조를 복사하지만 ,Immutable하므로 값이 변경되지 않음

</div>
</details>

## 9.2 임베디드 타입(복합 값 타입)

<details>
<summary>접기/펼치기</summary>
<div markdown="1">


### 9.2.1 임베디드 타입
- 새로운 값 타입을 직접 정의
- JPA는 이를 임베디드 타입(embedded Type)이라 함.
- 주로 기본 값 타입을 모아서 만들어서 복합 값 타입이라고 함
- int, String과 같은 값 타입.

### 9.2.2 임베디드 타입 사용법
```java
@Embedded
private Period workPeriod;

@Embedded
private Address homeAddress;
```
- `@Embeddable` : 값 타입을 정의하는 곳에 표시
- `@Embedded` : 값 타입을 사용하는 곳에 표시
- 기본 생성자 필수

### 9.2.3 임베디드 타입의 장점
- 재사용, 높은 응집도
- 내부적으로 자율적인 행동을 부여할 수 있음. (비즈니스 로직)
- 임베디드 타입을 포함한 모든 값 타입은, 값 타입을 소유한 엔티티에 생명주기를 의존함

### 9.2.4 임베디드 타입과 테이블 매핑
- 임베디드 타입은 엔티티의 값일 뿐이다.
- 임베디드 타입을 사용하기 전과 후에 매핑하는 테이블은 같다.
- 객체와 테이블을 아주 세밀하게 매핑하는 것이 가능.(의미있는 단위로 객체를 분리)
- 잘 설계한 ORM 애플리케이션은 매핑한 테이블의 수보다 클래스의 수가 더 많음

### 9.2.5 임베디드 타입과 연관관계
- 하나의 엔티티는 여러개의 임베디드 타입을 포함할 수 있다.
- 하나의 임베디드 타입은 다른 임베디드 타입을 포함할 수 있다.
- 하나의 임베디드 타입은 다른 엔티티 타입을 포함할 수 있다.

### 9.2.6 `@AttributeOverride` : 속성 재정의
```java
@Embedded
@AttributeOverrides({
        @AttributeOverride(name = "city", column = @Column(name = "company_city")),
        @AttributeOverride(name = "street", column = @Column(name = "company_street")),
        @AttributeOverride(name = "zipcode", column = @Column(name = "company_zipcode"))
})
private Address companyAddress;
```
- 한 엔티티에서 같은 값 타입을 사용하면 -> 칼럼 명이 중복되는 문제 발생
- `@AttributeOverrides`, `@AttributeOverride`을 사용해서 칼럼명 속성을 재정의

### 9.2.7 임베디드 타입과 null
- 임베디드 타입의 값이 null이면 매핑한 칼럼 값은 모두 null

</div>
</details>

## 9.3 값 타입과 불변 객체

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

> 값 타입은 복잡한 객체 세상을 조금이라도 단순화하려고 만든 개념이다. 따라서 값 타입은 안전하게 다룰 수 있어야한다.

### 9.3.1 값 타입 공유 참조
- 임베디드 타입 같은 값 타입을 여러 엔티티에서 공유하면 위험함
- 부작용(sideEffect) 발생
  - 예) 값타입의 특정 프로퍼티 값을 변경하면 여러 군데에서 변경의 여지가 있음

### 9.3.2 값 타입 복사
- 값 타입의 실제 인스턴스 참조를 공유하는 것은 위험
- 대신, 인스턴스를 별도로 생성하여 값을 복사하여 사용

### 9.3.3 객체 타입의 한계
```java
int a = 10;
int b = a; // 기본형 타입은 값을 복사
a = 4;
```
```java
Address a = new Address("old");
Address b = a; // 참조형 타입은 참조(주소)를 보 ㄱ사
b.setCity("New");
```
- 위와 같이 값을 복사해서 사용하면 공유 참조로 인해 발생하는 부작용을 피할 수는 있음.
- 하지만... 임베디드 타입처럼 직접 정의한 값 타입은 java의 기본 타입이 아니라 객체 타입.
  - java의 기본 타입에 값을 대입하면 값을 복사함
  - 객체타입은 참조값(주소)을 복사하여 대입하는데 이를 완전히 막을 방법이 없다.
- 결국 객체의 공유참조는 피할 수 없다.

### 9.3.4 불변 객체
```
불변이라는 작은 제약으로 부작용이라는 큰 재앙을 막을 수 있다.
```
- 객체 타입을 수정할 수 없게 만들면 부작용을 원천 차단
- 값 타입은 불변 객체(immutable object)로 설계해야함.
- 불변 객체 : 생성 시점 이후 절대 값을 변경할 수 없는 객체
- 생성자로만 값을 설정하고, 수정자(Setter)를 만들지 않으면 됨
  - 참고 : Integer, String은 java에서 제공하는 대표적인 불변 객체

</div>
</details>

## 9.4 값 타입의 비교

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

### 9.4.1 동일성
`a == b`
- 기본형 : 값이 같다
- 참조형 : 참조(주솟값)이 같다.

### 9.4.2 동등성
`a.equals(b)`
- 기본형 : 값이 같다
- 참조형
  - 디폴트 : 참조(주솟값)이 같다
  - 잘 알려진 클래스들 : 주로 내부값들이 모두 같을 때 동등

### 9.4.3 값 타입의 비교
- 인스턴스의 참조가 달라도 그 안의 값이 같으면 같은 것으로 봐야한다.
- 동등성 비교 : 인스턴스의 내부 속성값들이 동등하면 동등한 것으로 봐야한다.
- 값 타입의 동등성은 equals, hashCode를 적절히 오버라이드하여 정의
  - 주로 모든 속성값이 동등하면 동등하도록 함.
  - 주의점 : IDE의 도움을 받을 때 getter로 호출하여 비교하도록 하는 것이 좋음
    - getter를 호출하지 않고 값을 호출하여 비교할 경우는 위험성이 있음(프록시일때)

</div>
</details>

## 9.5 값 타입 컬렉션

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

### 9.5.1 값 타입 컬렉션
```java
@ElementCollection
@CollectionTable(
        name = "favorite_food",
        joinColumns = @JoinColumn(name = "member_id"))
@Column(name = "food_name")
private Set<String> favoriteFoods = new HashSet<>();
```
- 값 타입을 하나 이상 저장할 때 사용
- 데이터베이스는 컬렉션을 같은 테이블에 저장할 수 없다.
- 컬렉션을 저장하기 위한 별도의 테이블이 필요
- `@ElementCollection`, `@CollectionTable`사용
  - `@ElementCollection` : 값타입 컬렉션 선언
  - `@CollectionTable` : 값 타입 컬렉션을 저장할 테이블 정의
    - name : 테이블명
    - joinColumn : 컬렉션 테이블에서 FK로 사용할 속성
  - `@Column(name = "...")` : 컬렉션 테이블에서 사용할 속성명

### 9.5.2 값 타입 컬렉션 CRUD
- 저장 : 값타입 컬렉션에 값타입 인스턴스를 추가하면 DB에 반영됨
  - 예) `member.getFavoriteFoods().add("치킨")`;
- 조회 : 기본적으로 지연로딩 전략을 사용함.
- 삭제 : 값타입 컬렉션에서 인스턴스를 삭제
  - 주의사항 : 값 타입에 대한 equals, hashcode를 적절히 정의해야 remove 메서드에서 동등성을 기준으로 인스턴스를 제거할 수 있음
  - 예) `member.getAddressHistory().remove(new Address("city", "street", "zipcode"))`
- 수정 : 값타입은 식별자 개념이 없기 때문에 추적이 어려움. 삭제 후 새로 삽입하는 식으로 처리
- 참고사항
  - 값 타입 컬렉션은 영속성 전이(`Cascade = ALL`), 고아 객체 제거 기능(`OrphanRemoval = true`)을 필수로 가진다고 볼 수 있음.

### 9.5.3 값 타입 컬렉션의 제약사항
- 값 타입은 엔티티와 다르게 식별자 개념이 없다.
- 값은 변경하면 추적이 어렵다.
- 값 타입 컬렉션에 변경 사항이 발생하면, 주인 엔티티와 연관된 모든 데이터를 삭제하고, 값 타입 컬렉션에 있는 현재 값을 모두 다시 저장한다.
  - 부분이 변경되도 싹 지우고 다시 Insert하는 점에서 성능상의 문제를 야기시킬 수 있다.
- 값 타입 컬렉션을 매핑하는 테이블은 모든 칼럼을 묶어서 기본키를 구성해야한다.
  - 모든 칼럼 - PK
  - 외래키 - PK, FK
  - null 입력을 허용하지 않아야함.
  - 중복저장 허용하지 않도록 하기
- `@OrderColumn` 어노테이션을 사용하면 컬렉션 내에서의 순서값도 저장할 수 있긴 한데 실무에서 사용하기는 추천하지 않는다.

### 9.5.4 값 타입 컬렉션 대안
```java
@Entity
@Table(name = "address")
public class AddressEntity {

    @Id @GeneratedValue
    @Column(name = "address_id")
    private Long id;

    @Embedded
    private Address address;
```
```java
    @OneToMany(cascade = ALL, orphanRemoval = true)
    @JoinColumn(name = "member_id")
    private List<AddressEntity> addressHistory = new ArrayList<>();
```

- 실무에서는 상황에 따라 값 타입 컬렉션 대신에 **일대다 관계**를 고려
- 값타입 컬렉션은 정말 단순한 상황에서만 사용
- 일대다 관계를 위한 엔티티를 만들고, 여기에서 값 타입을 사용
- 영속성 전이(cascade) + 고아객체 제거를 사용해서 값 타입 컬렉션처럼 사용
- 예시
  - Address를 값타입으로 포함한 AddressEntity 클래스

</div>
</details>

## 9.6 값 타입 정리

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

### 엔티티 타입의 특징
- 식별자 있다
- JPA를 통해 생명 주기 관리
  - `객체 - 영속성 컨텍스트 - DB`
- 공유 가능

### 값 타입의 특징
- 식별자 없다.
- 생명주기를 엔티티에 의존
  - 엔티티가 제거되면 제거됨
- 공유하지 않는 것이 안전(새로운 인스턴스로 복사해서 사용하자)
- 불변 객체로 만드는 것이 안전 (Setter 두지 말기)

### 값 타입 주의점
- 값 타입은 정말 값 타입이라 판단될 때만 사용
- 엔티티와 값 타입을 혼동해서 엔티티를 값 타입으로 만들면 안 됨.
- 식별자가 필요하고, 지속해서 값을 추적/변경 해야한다면, 그것은 값 타입이 아닌 엔티티
  - 로직이 복잡해지면 값 타입으로 쓰지 말고 엔티티 타입으로 사용하자.

</div>
</details>

---

# Section 10. 객체지향 쿼리언어

## 10.1 JPQL 소개

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

### 10.1.1 JPA가 지원하는 쿼리 방법

- JPQL
- JPA Criteria
- QueryDSL
- 네이티브 SQL
- JDBC API 직접 사용, MyBatis, SpringJdbcTemplate 함께 사용

### 10.1.2 SQL의 한계
- JPA를 사용하면 엔티티 객체를 중심으로 개발
- 문제는 검색쿼리. 검색을 할 때도 테이블이 아닌 엔티티 객체를 대상으로 검색
- 모든 DB 데이터를 조회하여 java단에서 필터링하기에는 비용이 너무 비쌈.
- 애플리케이션이 필요한 데이터만 DB에서 불러오려면 결국 검색 조건이 포함된 SQL 필요

### 10.1.3 JPQL
```java
String jpql = "SELECT m FROM Member as m WHERE m.name = 'ttasjwi'";
List<Member> resultList = em.createQuery(jpql, Member.class).getResultList();
```
- JPA는 SQL을 추상화한 JPQL이라는 객체 지향 쿼리 언어 제공
  - SQL과 문법 유사, SELECT, FROM, WHERE, GROUP BY, HAVING, JOIN 지원
  - SQL을 추상화해서 특정 데이터베이스 SQL에 의존하지 않음
- JPQL은 엔티티 객체를 대상으로 쿼리(객체지향 쿼리언어)
- SQLQ은 데이터베이스 테이블을 대상으로 쿼리

### 10.1.4 Criteria
- 문자가 아닌 자바코드로 JPQL을 작성할 수 있음
- JPQL 빌더 역할
- JPA 공식 기능
- 단점 : 너무 복잡하고 실용성이 없다. 유지보수 힘들다.
- Criteria 대신에 QueryDSL 사용 권장

### 10.1.5 QueryDSL
- 문자가 아닌 java 코드로 JPQL을 작성할 수 있음
- JPQL 빌더 역할
- 컴파일 시점에 문법 오류를 작성할 수 있음
- 동적쿼리 작성이 편리함
- 단순하고 쉽다
- **실무 사용 권장!**

### 10.1.6 네이티브 SQL
```java
String sql = "SELECT id, age, team_id, name from member where name = 'kim'";
List<Member> resultList = em.createNativeQuery(sql, Member.class).getResultList();
```
- JPA가 제공하는 SQL을 직접 사용하는 기능
- JPQL로 해결할 수 없는 특정 데이터베이스에 의존적인 기능
  - 예) 오라클의 CONNECT BY, 특정 DB만 사용하는 SQL 힌트, ...

### 10.1.7 JDBC 직접사용, SpringJdbcTemplate 등
- JPA를 직접 사용하면서 JDBC 커넥션을 직접 사용하거나, 스프링 JdbcTemplate, MyBatis 등을 함께 사용 가능
- 단, JPA와 이들을 직접 사용한다면 영속성 컨텍스트에서 관리하는 것들을 수동으로 강제 플러시할 필요가 있음.
  - 예) 회원을 JPA로 등록하고, jdbc의 커넥션을 얻어와서 직접 쿼리를 할 경우
    - jpa를 거치지 않고 쿼리한 것은 영속성 컨텍스트에서 관리되지 않음
    - jpa의 flush는 커밋 직전, 쿼리 직전에만 자동 호출된다.
- 예) JPA를 우회해서 SQL을 실행하기 직전에 영속성 컨텍스트를 수동 플러시
- JPA, JPQL, QueryDSL을 주로 사용하고, 정말 이들로 안 되는 극소수의 상황에서만 Spring에서 공식적으로 지원하는 JdbcTemplate를 활용

</div>
</details>

## 10.2 JPQL 기본 문법과 기능

<details>
<summary>접기/펼치기</summary>
<div markdown="1">


### 10.2.1 JPQL 소개
- JPQL은 객체지향 쿼리 언어이다. 테이블을 대상으로 쿼리하는 것이 아니라 엔티티 객체를 대상으로 쿼리
- JPQL은 SQL을 추상화함. 특정 데이터베이스 SQL에 의존하지 않는다.
- JPQL은 결국 SQL로 번역된다. 번역된 SQL이 DB에 날아감

### 10.2.2 JPQL 문법 - 구조
- SELECT문
  ```sql
  SELECT ...
  FROM ...
  [WHERE] ... -- 조건
  [GROUP BY] ...  --그릅화
  [HAVING] ...  -- 그룹 조건
  [ORDER BY] ... -- 정렬
  ```
  - 예시 : `SELECT m FROM Member as m WHERE m.age > 18;`
- UPDATE문
  ```sql
  UPDATE ...
  [WHERE] ...
  ```
- DELETE문
  ```sql
  DELETE ...
  WHERE ...
  ```

### 10.2.3 JPQL 문법 - 구분
- 엔티티와 속성은 대소문자 구분함. (Member, age, ...)
- JPQL 키워드는 대소문자를 구분하지 않음 (SELECT, FROM, WHERE)
- 엔티티 이름 사용, 테이블 이름이 아님. (Member)
  - `@Entity(name = "...")`에서 엔티티 이름을 지정할 경우 이를 사용함. 그런데 실무에선 그렇게 잘 사용 안 함
- 별칭은 필수(예: Member as m)(as 키워드 생략 가능)

### 10.2.4 TypeQuery, Query
- TypeQuery : `em.createQuery(jpql, Member.class)`
  - 반환타입이 명확할 때 사용
- Query : `em.createQuery(jpql)`
  - 반환타입이 명확하지 않을 때 사용

### 10.2.5 결과조회 API
- `query.getResultList()` : 결과가 하나 이상일 때 사용. 리스트 반환.
  - 결과가 없으면 빈 리스트 반환
  - 예외로부터 안전하다.
- query.getSingleResult() : 결과가 정확히 하나일 때 사용. 단일 객체 반환
  - 결과가 없으면 예외 발생 : `javax.persistence.NoResultException`
  - 결과가 둘 이상이면 예외 발생 : `javax.persistence.NonUniqueResultException`

### 10.2.6 파라미터 바인딩 - 이름 기준, 위치 기준
- 파라미터 이름 기준
  ```java
    String jpql1 = "SELECT m FROM Member as m WHERE m.name = :memberName";
    Member findMember = em.createQuery(jpql1, Member.class)
                        .setParameter("memberName", member.getName())
                        .getSingleResult();
  ```
- 파라미터 위치 기준 (장애 발생 가능성이 많음. 사용하지 않는 것을 권장)
  ```java
    String jpql2 = "SELECT m FROM Member as m Where m.name = ?1";
    Member findMember = em.createQuery(jpql2, Member.class)
                        .setParameter(1, member.getName())
                        .getSingleResult();
  ```

</div>
</details>

## 10.3 프로젝션

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

### 10.3.1 프로젝션이란?
- SELECT 절에서 조회할 대상을 지정하는 것
- 프로젝션 대상 : 엔티티, 임베디드 타입, 스칼라 타입(숫자, 문자 등 기본 데이터 타입)
- 종류
  - 엔티티 프로젝션 : 영속성 컨텍스트의 관리 대상이 된다.
    - `SELECT m FROM Member as m`
    - `SELECT m.team FROM MEMBER as m` : 조인쿼리가 나가는데 실제로는 이렇게 쓰지 말고 명시적으로 조인 SQL을 작성하는게 좋다.
  - 임베디드 타입 프로젝션
    - `SELECT m.address FROM MEMBER as m`
  - 스칼라 타입 프로젝션
    - `SELECT m.name, m.age FROM MEMBER as m`
- DISTINCT 키워드로 중복을 제거할 수 있다.
  - 예> `SELECT DISCINCT m.team FROM MEMBER as m`

### 10.3.2 프로젝션 - 여러값 조회
> SELECT m.username, m.age FROM Member as m
```java
String jpql = "SELECT new jpql.MemberDTO(m.name, m.age) FROM Member as m";
List<MemberDTO> memberDTOs = em.createQuery(jpql, MemberDTO.class)
        .getResultList();
for (MemberDTO memberDTO : memberDTOs) {
    System.out.println("memberDTO.name = " + memberDTO.getName());
    System.out.println("memberDTO.age = " + memberDTO.getAge());
}
```
보통 다음 두 가지 방식으로 데이터를 조회한다.
- List에 지네릭을 `Object[]`타입으로 걸어 가져온 뒤 각각 가져오기
  - 프로젝션에 지정한 순서대로 값이 담겨옴.
  - 별도로 형변환을 해야함. 
    - 예) m.name은 0번 인덱스, m.age는 1번 인덱스에 Object 타입으로 받아와짐 
- new 명령어를 사용하여 적당한 DTO를 생성하여 값을 담아오기
  - 패키지 명을 포함한 FQCN 입력
  - 순서와 타입이 일치하는 생성자 필요
  - 이 방식에서 잘못 입력할 때, 컴파일러의 도움을 받을 수 없는 문제가 있는데 QueryDSL에서 이를 극복할 수 있다.

</div>
</details>

## 10.4 페이징

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

```java
List<Member> members = em.createQuery("SELECT m FROM Member as m ORDER BY m.age desc")
    .setFirstResult(10)
    .setMaxResults(10)
    .getResultList();
```
1. JPA는  페이징을 다음 두가지 API로 추상화한다.
   - setFirstResult(int startPosition) : 조회 시작 위치
     - 0부터 시작한다.
   - setMaxResults(int maxResult) : 조회할 데이터 수

2. 페이징을 목적으로 한다면 순서가 필요할텐데, JPQL에서 ORDER BY로 정렬 기준을 부여해야함

</div>
</details>

## 10.5 조인

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

### 10.5.1 조인의 종류
1. 내부 조인(Inner Join)
> SELECT m FROM Member as m [inner] JOIN m.team as t
- m의 team_id와 같은 pk를 가진 Team 엔티티를 가져와라.
- team이 없는 경우 가져와지지 않음

2. 외부 조인(Outer Join)
> SELECT m FROM Member m LEFT [OUTER] JOIN m.team as t
- m의 team_id와 같은 pk를 가진 Team 엔티티뿐만 아니라 team이 없는 엔티티를 가져와라.

3. 세타 조인(Theta Join)
> SELECT count(m) FROM Member as m, Team as t WHERE m.name = t.name
- 전혀 연관관계 없는 테이블을 곱집합 연산으로 싹 가져올 때

### 10.5.2 ON절을 활용한 조인(JPA 2.1부터)
1. 조인 대상 필터링
   - 예) 회원과 팀을 조인하면서 팀 이름이 A인 팀만 조인
     - SELECT m FROM Member as m LEFT JOIN m.team as t ON t.name = 'A';
2. 연관관계가 전혀 없는 엔티리를 외부조인(하이버네이트 5.1부터)
   - 예) 회원의 이름과, 팀의 이름이 같은 대상 외부 조인
     - SELECT m FROM Member as m LEFT JOIN Team as t ON m.name = t.name

</div>
</details>

## 10.6 서브쿼리

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

### 10.6.1 JPQL 서브 쿼리
- 나이가 평균보다 많은 회원
  ```sql
  SELECT m
  FROM Member as m
  WHERE
     m.age > (SELECT avg(m2.age)
              FROM Member as m2
              );
  ```
- 한 건이라도 주문한 고객
  ```sql
  SELECT m
  FROM Member as m
  WHERE
  (SELECT count(o) FROM Order as o WHERE m = o.member)
  as numberOfMemberOrder > 0
  ```

### 10.6.2 서브쿼리 지원 함수
- `[NOT] EXISTS (서브쿼리...)` : 서브쿼리에서 반환되는 행이 존재하면 true
- `ALL (서브쿼리)` : 모두 만족하면 참
- `ANY (서브쿼리)`, `SOME (서브쿼리)` : 하나라도 조건을 만족하면 참
- `[NOT] IN (서브쿼리...)` : 서브쿼리의 결과 중 하나라도 같은 것이 있으면 참

### 10.6.3 서브쿼리 - 예제
- 팀 A 소속인 회원들
  ```sql
  SELECT m FROM Member as m
  WHERE EXISTS (SELECT t FROM m.team as t WHERE t.name = '팀A');
  ```
- 전체 상품 각각의 재고보다 주문량이 많은 주문들
  ```sql
  SELECT o FROM Order as o
  WHERE o.orderAmount > ALL (SELECT p.stockAmount FROM Product as p);
  ```
- 어떤 팀이든 팀에 소속된 회원
  ```sql
  SELECT m FROM Member as m
  WHERE m.team = ANY(SELECT t FROM Team as t);
  ```

### 10.6.4 JPA 서브쿼리의 한계

- JPA는 WHERE, HAVING 절에서만 서브 쿼리를 사용할 수 있다.
- 하이버네이트에서는 SELECT절에서도 서브 쿼리를 사용할 수 있다.
- FROM절의 서브쿼리는 JPQL에서 지원되지 않는다.
  - 조인으로 풀 수 있으면 풀어서 해결
  - 영 안 되면 native SQL 사용

</div>
</details>

## 10.7 JPQL 타입 표현식, 기타

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

### 10.7.1 JPQL 타입 표현식
- 문자
  - 작은 따옴표로 표현 : 'Hello'
  - 작은 따옴표 쓰고 싶으면 두번 입력 : 'She''s'
- 숫자
  - Long : 10L
  - Double : 10D
  - Float : 10F
- Boolean : true, false
- ENUM : jpabook.MemberType.Admin(FQCN으로 작성)
  - 하드코딩을 하고 싶지 않다면 파라미터 지정을 하는 방법, QueryDSL을 사용하는 방법을 사용
- 엔티티 타입 : Type(m) = Member(상속 관계에서 사용)

### 10.7.2 JPQL 기타 표현식
- SQL과 문법이 같은 식들.
- EXISTS, IN
- AND, OR, NOT
- `=`, `>`, `>=`, `>`, `>=`, `<>`
- BETWEEN, LIKE, IS NULL

</div>
</details>

## 10.8 조건식

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

### 10.8.1 기본 CASE식
```sql
SELECT
    CASE
        when m.age <= 10 then '학생요금'
        when m.age >= 60 then '경로요금'
        else '일반요금'
    end
FROM Member as m;
```

### 10.8.2 단순 CASE식
```sql
SELECT
    CASE t.name
        when '팀A' then '인센티브 110%'
        when '팀B' then '인센티브 120%'
        else '인센티브 105%'
    end
FROM Team as t;
```

### 10.8.3 그 외 조건식
- Coalesce(스칼라식1, 스칼라식2, ...)
  - 지정 스칼라식들을 순서대로 확인하여 null이 아닌 첫번째 값을 반환

- NULLIF(스칼라식1, 스칼라식2)
  - 두 값이 일치하는 지 확인하여 다르면 첫번째 값
  - 같으면 null 반환

- 예시 1 : 사용자 이름이 없으면 '이름 없는 회원'을 반환. 나머지는 본인의 이름
  ```sql
  SELECT coalesce(m.name, '이름 없는 회원')
  FROM Member as m;
  ```
- 예시 2 : 사용자 이름이 '관리자'이면 null 반환. 나머지는 본인의 이름
  ```sql
  SELECT nullif(m.name, '관리자')
  FROM Member as m;
  ```

</div>
</details>

## 10.9 JPQL 함수

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

### 10.9.1 JPQL 기본 함수
JPQL이 제공하는 표준 함수들. DB에 관계 없이 사용가능하다.
- CONCAT
- SUBSTRING
- TRIM
- LOWER, UPPER
- LENGTH
- LOCATE
- ABS, SQRT, MOD
- SIZE
- INDEX(JPA의 `@OrderColumn` 대응)

### 10.9.2 사용자 정의 함수
```java
public class MyH2Dialect extends H2Dialect {

    public MyH2Dialect() {
       registerFunction("group_concat", new StandardSQLFunction("group_concat", StandardBasicTypes.STRING));
    }
}
```
```xml
<property name="hibernate.dialect" value="dialect.MyH2Dialect"/>
```
- 사용 전 방언에 추가해야함
  - 기본 방언 : DB에서 기본적으로 제공하는 방언 클래스 `persistence.xml`에 등록
  - 사용자 정의함수
    - DB방언 클래스 상속
    - 생성자로 추가적으로 정의한 함수를 `registerFunction`메서드로 추가.
    - 상속체를 persistence.xml에 방언 등록

</div>
</details>

---

# Section 11. JPQL - 중급문법

## 11.1 경로 표현식 및 조인

<details>
<summary>접기/펼치기</summary>
<div markdown="1">

### 11.1.1 경로 표현식
> .(점)을 찍어서 객체 그래프를 탐색하는 것
- 상태필드 : 단순히 값을 저장하기 위한 필드
    - 예) m.name
- 연관 필드 : 연관관계를 위한 필드
  - 단일 값 연관 필드 : `@ManyToOne`, `@OneToOne`
    - 예) m.team
  - 컬렉션 값 연관 필드 : `@OneToMany`, `@ManyToMany`
    - 예) t.members

### 11.1.2 경로 표현식의 특징
- 상태필드 : 경로 탐색의 끝. 더 이상 탐색할 수 없다.
- 단일 값 연관 경로 : 묵시적 내부조인(inner join) 발생. 더 이상 탐색할 수 있다.
  - join 쿼리를 작성하지 않았음에도 join 쿼리가 날아감. 쿼리를 예측하기 힘듬
- 컬렉션 값 연관 경로 : 묵시적 내부 조인 발생. Collection으로 얻어와지며, 더 이상 탐색할 수 없다.
  - From 절에서, 명시적 조인을 통해 별칭을 얻으면 별칭을 통해서 내부탐색이 가능하다.

### 11.1.3 경로 탐색
- 연관 경로 탐색 시 묵시적 join이 발생하는 문제가 발생함.
- 상대필드 경로 탐색
  ```sql
  -- jpql
  SELECT m.name, m.age FROM Member as m;
  
  -- sql
  SELECT m.name, m.age FROM Member as m;
  ```
- 단일 값 연관 경로 탐색
  ```sql
  -- jpql
  SELECT o.member from Order as o;
  
  -- sql
  SELECT m.*
  FROM Orders as o
    INNER JOIN Member as m ON o.member_id = m.member_id;
  ```

### 11.1.4 명시적 조인, 묵시적 조인
- 명시적 조인 : join 키워드를 명시적으로 직접 사용
  - 예) SELECT m From Member m INNER JOIN m.team as t;
- 묵시적 조인 : 경로 표현식에 의해 묵시적으로 SQL 조인이 발생(내부 조인만 가능)
  - 예) SELECT m.team FROM Member as m;
- 예시
  ```sql
  SELECT o.member.team from Order as o; -- 성공
  
  SELECT t.members from Team as t; -- 성공
  
  SELECT t.members.name from Team as t; -- 실패
  
  SELECT m.name from Team as t JOIN t.members as m; -- 성공
  ```

### 11.1.5 경로 탐색을 사용한 묵시적 조인 시 주의사항
- 항상 내부조인이 발생함
- 컬렉션은 경로 탐색의 끝. 명시적 조인을 통해 별칭을 얻어야 내부 탐색이 가능
- 경로 탐색은 주로 SELECT, WHERE 절에서 사용하지만, 묵시적 조인으로 인해 SQL의 FROM(JOIN)절에 영향을 줌.
  - 예상치 못 한 조인 쿼리가 발생함. 성능 튜닝에서 곤란해진다.

### 11.1.6 실무 조언
- 가급적 묵시적 조인 대신에 명시적 조인을 사용하라.
- 조인은 SQL 튜닝에 중요 포인트.
  - ORM이 아무리 객체지향적이더라도, JOIN은 성능 튜닝에 있어 핵심적인 요소이므로 명시적으로 조인을 하자.
- 묵시적 조인은 조인이 일어나는 상황을 한 눈에 파악하기 힘듬.

</div>
</details>

## 11.2 페치 조인

### 11.2.1 페치 조인이란?
- SQL에서 지원하는 조인이 아님
- JPQL에서 성능 최적화를 위해 제공하는 기능
- 연관된 엔티티나, 컬렉션을 SQL 한번에 조회하는 기능
- join fetch 명령어를 사용하여 한 방에 가져오기
- 페치 조인 : `[left [outer] | inner] join fetch 조인경로

### 11.2.2 엔티티 페치 조인
```sql
-- jpql
SELECT m FROM Member as m JOIN FETCH m.team;

-- sql
SELECT m.*, t.*
FROM Member as m
INNER JOIN Team as t ON m.team_id = t.team_id;
```
- 회원을 조회하면서 연관된 팀도 함께 조회(SQL 한 방에 가져오기)
- 실제 SQL을 보면 회원 뿐만 아니라 팀도 함께 Select 해온다.

### 11.2.3 컬렉션 페치 조인
```sql
-- jpql
SELECT t FROM Team as t JOIN FETCH t.members WHERE t.name = 'teamA';

-- sql
SELECT t.*, m.*
FROM Team as t
INNER JOIN Member as m ON t.team_id = m.team_id
WHERE t.name = 'teamA';
```
- 일대다 관계를 페치 조인
- 팀과 회원들 각각의 엔티티를 모두 영속성 컨텍스트로 땡겨옴
- 주의점
  - 일대다 조인이기 때문에 한 team에 대해 여러 member가 조인되므로 row가 늘어남
  - 실제 받아온 list에는 같은 team 변수가 2개 들어있음
    - 물론 이들은 같은 식별자를 가진 team이고, 영속성 컨텍스트에서 관리됨
    - 하나의 Team 인스턴스를 가리킨다.

### 11.2.4 컬렉션 페치 조인 - 중복 제거
- SQL의 distinct는 중복된 결과를 제거하는 명령
  - row의 모든 값이 같으면 중복으로 보고 제거함
- JPQL의 distinct는 다음 두가지 기능을 제공한다.
  - SQL에 distinct를 추가하여 날림
    - 이것만으로는 행마다 내용이 다르므로, 완전히 중복이 제거되지 않음
  - 애플리케이션 단에서, 같은 식별자를 가진 엔티티의 중복을 제거한다.


### 11.2.5 일반 조인과 페치 조인의 차이
- 일반조인 : 연관된 엔티티를 함께 조회하지 않음 (즉시로딩 X)
  - JPQL은 단순히 SELECT 절에서 지정한 엔티티만 조회함
  - 연관관계를 고려하지 않음. TEAM만 지정하면 Team 엔티티만 조회하고, 연관 엔티티는 조회 x
- 페치조인 : 연관된 엔티티를 함께 조회 (즉시 로딩)
  - 객체 그래프를 SQL 한번에 조회하는 개념

---
