
# hello-jpa

인프런 김영한 님의 "자바 ORM 표준 JPA 프로그래밍 - 기본편" 강의를 듣고 학습 내용을 간략하게 정리하기 위한 Repository

---

## 프로젝트 설정

- java : 11
- 빌드 : Maven
- 의존 라이브러리(`pom.xml`)
  - `com.h2database:1.4.200` : H2 데이터베이스
  - `org.hibernate:hibernate-entitymanager` : 하이버네이트 엔티티 매니저
  - `javax.xml.bind:javaxb-api` : java 11 이상 의존성 추가 해야함.

---

## JPA의 구동방식

1. 설정정보 조회
   - `META.INF/persistence.xml`에서 Persistence 조회

2. 설정정보 기반 EntityManagerFactory 생성
   - 설정에 등록된 Persistence name 기반으로 EMF를 생성

3. 요청이 들어오고 나갈 때마다, EntityManager를 생성 후 버리기
   - enf.createEntityManger();

---

## EntityManagerFactory, EntityManager
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

---

## JPA의 기본 CRUD

기본적인 CRUD에 관한 메서드를 제공하는데, 데이터 변경은 트랜잭션 안에서 이루어져야한다.

- 등록 : `em.persist(...)`
- 기본키로 단건 조회 : `em.find(클래스, 기본키)
- 삭제 : `em.remove(...)`
- 수정 : `findMember.setName(...)`
  - 트랜잭션 안에서, 데이터 변경이 일어날 경우 commit 직전에 jpa가 변경 쿼리를 날려준다.

---

## JPQL
```java
em.createQuery("SELECT m from Member as m").getResultList();
```
- JPA는 엔티티 객체 중심 개발.
- 검색 시 테이블이 아닌 엔티티 대상으로 검색.
- 검색 시 모든 DB 데이터를 가져와서 맵핑하여 객체를 생성하고, 필터링하기엔 비용이 너무 크다.
- 필요한 데이터만 DB에서 가져오려면 결국 검색조건이 포함된 SQL을 작성해야함.
- JPA는 SQL을 추상화한 JPQL이라는 객체지향 쿼리언어를 제공함. JPQL을 통해 엔티티 중심의 쿼리를 작성하고, JPA가 각 DBMS별 방언에 맞게 쿼리를 작성하여 날려줌

---

## 엔티티의 생명주기

![EntityLifeCycle.png](img/EntityLifeCycle.png)

- 비영속 : 영속성 컨텍스트와 무관하게 새로운 상태
  - 예> new Member();

- 영속 : 영속성 컨텍스트에 관리되는 상태
  - em.persist(member);

- 준영속 : 영속성 컨텍스트의 관리에서 벗어난 상태
  - em.detach(member);

- 삭제 : 엔티티를 영속성 컨텍스트, DB에서 삭제
  - em.remove()


<details>
<summary>예시 코드</summary>
<div markdown="1">

### 실험
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

## 결과

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

## 영속성 컨텍스트 -  1차 캐시, 영속 엔티티의 동일성 보장

- 영속성 컨텍스트는 엔티티를 1차 캐시에 우선적으로 저장한다.
- key로 id, value로 엔티티를 저장함.
- 객체를 찾아올 때 1차 캐시에서 우선적으로 조회하고 존재하면 쿼리를 날려서 찾아오지 않고 바로 1차캐시에서 가져온다.
- 같은 캐시에서 찾아오므로 같은 영속성 컨텍스트의 동일 트랜잭션에서 관리되는 객체는 동일성(주솟값 같음)을 보장함

<details>
<summary>예시 코드</summary>
<div markdown="1">

### 실험
```java
Member member1 = em.find(Member.class, 101L);
Member member2 = em.find(Member.class, 101L);
System.out.println("member1 == member2 ? : " + (member1 ==member2));
tx.commit();
```
- 동일한 id로 EM을 통해 찾아오기 요청

### 결과
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

---

## 영속성 컨텍스트 - 트랜잭션을 지원하는 쓰기 지연
- `persist` : 영속성 컨텍스트의 1차 캐시에 저장 + 쓰기 지연 SQL 저장소에 쿼리를 저장함 
- tx.commit() -> flush(쿼리 날아감), commit(실제 반영)이 일어나며 실제로 DB에 반영됨
- 이를 활용하여, 대량의 쿼리를 날리는 것을 커밋 직전까지 지연시키고 모아서 처리(배치 처리) 가능.
  - 배치사이즈 조절 : `<property name="hibernate.jdbc.batch_size" value="..."/>`

<details>
<summary>예시 코드</summary>
<div markdown="1">

### 실험
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
### 결과
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

---

## 영속성 컨텍스트 - 변경 감지(Dirty Checking)

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

<details>
<summary>예시 코드</summary>
<div markdown="1">

### 실험
```java
Member member = em.find(Member.class, 150L);
member.setName("ZZZZZ");
System.out.println("=======================================");
tx.commit();
```
- DB에서 멤버를 찾아와서 1차 캐시에 가져옴
- setName을 호출하여 값을 변경한다.
- 커밋한다.
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

---

## 영속성 컨텍스트 - flush()

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

<details>
<summary>예시 코드</summary>
<div markdown="1">

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

---