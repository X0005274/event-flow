# MES Event 처리 모듈 — 기존 시스템 연동 가이드

순수 Java 17 + JDBC. Spring / Lombok / ORM 의존성 **없음**. 외부 라이브러리 0개.

## 1. 연동 이음새(seam) — 기존 시스템에 맞춰 손볼 곳은 5군데뿐

| 이음새 | 위치 | 무엇을 하나 |
|---|---|---|
| 패키지명 | 전체 `com.mes.event` | 사내 패키지 규칙으로 일괄 변경 (IDE refactor) |
| 커넥션 | `tx/ConnectionProvider` | 사내 풀/DataSource 를 람다로 연결 |
| 로깅 | `support/EventLogFactory` | 기동 시 1회 setProvider 로 Log4j/SLF4J 브리지 |
| 스키마 | `sql/*Sql.java` | 실제 테이블/컬럼명으로 보정 (`// TODO[MES]`) |
| 거래코드 | `type/TxnCode` | 사내 코드 체계로 보정 (`dbCode()`) |

**핵심 로직(processor/, dao/ 추상클래스, dto/, command/)은 손대지 않는다.**

## 2. 트랜잭션 — 두 가지 모드 중 선택

### 모드 A) 기존 MES 트랜잭션에 합류 (권장 — 한 트랜잭션에 묶을 때)
```java
EventProcessorFactory factory = new EventProcessorFactory();
EventDispatcher dispatcher = new EventDispatcher(factory); // 트랜잭션 매니저 불필요

// 기존 시스템이 이미 연 Connection (commit/rollback 은 기존 시스템 책임)
dispatcher.dispatchWithin(existingConn, lotEvent);
dispatcher.dispatchWithin(existingConn, waferEvent); // 같은 트랜잭션
```

### 모드 B) 이 모듈이 트랜잭션 관리 (독립 실행)
```java
ConnectionProvider provider = () -> myDataSource.getConnection(); // 사내 풀 연결
TransactionManager txManager = new TransactionManager(provider);
EventDispatcher dispatcher =
        new EventDispatcher(txManager, new EventProcessorFactory());

dispatcher.dispatch(lotEvent); // 내부에서 begin → process → commit/rollback
```

## 3. 로깅 브리지 (선택)
```java
// 애플리케이션 기동 시 1회. 안 하면 java.util.logging 으로 동작.
EventLogFactory.setProvider(owner -> new MyLog4jEventLog(owner));
```
로그 포인트: dispatch 소요시간(debug), MAS insert/update 구분(debug),
처리 완료 시 영향행수(info), rollback(warn), rollback 실패(error).

## 4. 새 이벤트 추가 절차 (예: Reticle)
기존 코드 수정은 `EventType` enum + `EventProcessorFactory` 등록 **2곳뿐**.
1. `type/EventType` 에 `RETICLE` 추가
2. `dto/ReticleEvent extends MesEvent` 작성
3. `sql/ReticleSql` 작성
4. `dao/reticle/ReticleMasDao`, `ReticleHisDao` 작성
5. `processor/ReticleEventProcessor` 작성 (masterKey, specificColumns 구현)
6. `EventProcessorFactory` 에 `register(RETICLE, new ReticleEventProcessor(...))` 1줄

## 5. 컴파일 주의
한글 주석이 UTF-8 이다. 사내 빌드(Maven/Gradle)에서 소스 인코딩을 UTF-8 로 지정할 것.
- javac: `-encoding UTF-8`
- Maven: `<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>`
- Gradle: `compileJava { options.encoding = 'UTF-8' }`

## 6. 예제 DDL (H2/테스트용 — 실제 스키마에 맞게 조정)
```sql
CREATE TABLE MES_LOT_MAS (
  LOT_ID VARCHAR(40) PRIMARY KEY, PRODUCT_ID VARCHAR(40), STEP_ID VARCHAR(40),
  WAFER_QTY INT, LAST_TXN_CODE VARCHAR(20), FACTORY_ID VARCHAR(20),
  EQUIPMENT_ID VARCHAR(20), LAST_EVENT_ID VARCHAR(40), LAST_EVENT_TIME TIMESTAMP,
  UPD_USER VARCHAR(20), REMARK VARCHAR(200), UPD_DT TIMESTAMP);
CREATE TABLE MES_LOT_HIS (
  EVENT_ID VARCHAR(40) PRIMARY KEY, LOT_ID VARCHAR(40), PRODUCT_ID VARCHAR(40),
  STEP_ID VARCHAR(40), WAFER_QTY INT, TXN_CODE VARCHAR(20), FACTORY_ID VARCHAR(20),
  EQUIPMENT_ID VARCHAR(20), EVENT_TIME TIMESTAMP, USER_ID VARCHAR(20),
  REMARK VARCHAR(200), CRT_DT TIMESTAMP);
```

## 7. 테스트 전략
- `EventProcessor` 는 `Connection` 만 받으므로, H2 인메모리 DB + 실제 DAO 로 통합 테스트.
- 또는 `MasDao`/`HisDao` 인터페이스를 Mockito 로 모킹해 Processor 단위 테스트.
- `EventProcessorFactory.register()` 로 테스트용 Processor 주입 가능.
- 회귀 방지 핵심: SQL 상수와 DAO `bindXxx` 의 **바인딩 순서 일치**를 통합 테스트로 검증.
```

