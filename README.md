# event-flow

JDBC 기반 MES 이벤트 처리 모듈. 들어온 이벤트를 **마스터(MAS) 테이블에는 최신 상태로 반영(upsert)**하고, **이력(HIS) 테이블에는 한 줄씩 누적(insert)**한다.

특정 프레임워크(Spring, 특정 커넥션 풀, 특정 로깅)에 의존하지 않으며, 기존 MES 시스템에 람다 몇 줄로 붙일 수 있다.

## 지원 이벤트

| 이벤트 | 마스터 테이블 | 이력 테이블 | 마스터 키 |
|--------|--------------|-------------|-----------|
| `LotEvent` (랏) | `MES_LOT_MAS` | `MES_LOT_HIS` | `LOT_ID` |
| `WaferEvent` (웨이퍼) | `MES_WF_MAS` | `MES_WF_HIS` | `WAFER_ID` |
| `DurableEvent` (내구재) | `MES_DURABLE_MAS` | `MES_DURABLE_HIS` | `DURABLE_ID` |

---

## 핵심 개념

- **단일 호출 / 복합 호출**: `dispatch` 는 이벤트를 가변인자로 받는다. 1건이면 단일 처리, 여러 건이면 **한 트랜잭션 안에서** 입력 순서대로 처리되며 하나라도 실패하면 전체 롤백된다.
- **두 가지 트랜잭션 모드**:
  - `dispatch(...)` — 이 모듈이 트랜잭션을 직접 연다(commit/rollback 포함). 독립 실행/단순 연동에 적합.
  - `dispatchWithin(conn, ...)` — 기존 MES 가 이미 연 커넥션/트랜잭션에 합류한다. commit/rollback 은 기존 시스템 책임.

---

## 사용법

### 1. 모듈이 트랜잭션까지 관리하는 경우

```java
import com.mes.event.EventDispatcher;
import com.mes.event.dto.LotEvent;
import com.mes.event.processor.EventProcessorFactory;
import com.mes.event.tx.ConnectionProvider;
import com.mes.event.tx.TransactionManager;
import com.mes.event.type.TxnCode;
import java.time.LocalDateTime;

// (1) 커넥션을 어디서 얻을지 람다 한 줄로 알려준다 (기존 DataSource/풀에 연결)
ConnectionProvider connectionProvider = () -> myDataSource.getConnection();

// (2) 조립
TransactionManager txManager = new TransactionManager(connectionProvider);
EventProcessorFactory factory = new EventProcessorFactory(); // LOT/WAFER/DURABLE 기본 등록
EventDispatcher dispatcher = new EventDispatcher(txManager, factory);

// (3) 이벤트 생성
LotEvent lot = new LotEvent(
        "EVT-0001",                 // eventId   (이벤트 고유 ID)
        LocalDateTime.now(),        // eventTime (발생 시각)
        TxnCode.MOVE,               // txnCode   (거래 코드)
        "user01",                   // userId    (처리자)
        "공정 이동",                 // comment   (비고, nullable)
        "FAB1",                     // factoryId (공장)
        "EQP-12",                   // equipmentId (설비, nullable)
        "LOT12345",                 // lotId     (마스터 키)
        "PROD-A",                   // productId
        "STEP-100",                 // stepId
        25);                        // waferQty  (nullable)

// (4) 단일 호출 — 새 트랜잭션을 열어 처리하고 commit
dispatcher.dispatch(lot);
```

### 2. 복합 호출 (여러 이벤트를 한 트랜잭션으로)

서로 다른 종류를 섞어 묶어도 된다. 입력 순서대로 처리되고, **하나라도 실패하면 전체 롤백**된다.

```java
import com.mes.event.dto.WaferEvent;
import com.mes.event.dto.DurableEvent;

WaferEvent wafer = new WaferEvent(
        "EVT-0002", LocalDateTime.now(), TxnCode.MOVE, "user01", null, "FAB1", "EQP-12",
        "WF-0001",  // waferId (마스터 키)
        "LOT12345", // lotId   (소속 Lot)
        1,          // slotNo  (nullable)
        "A");       // gradeCode (nullable)

DurableEvent durable = new DurableEvent(
        "EVT-0003", LocalDateTime.now(), TxnCode.HOLD, "user01", null, "FAB1", "EQP-12",
        "JIG-77",   // durableId (마스터 키)
        "CARRIER",  // durableType
        7);         // usageCount (nullable)

// 세 이벤트가 하나의 트랜잭션으로 처리된다
dispatcher.dispatch(lot, wafer, durable);
```

### 3. 기존 MES 트랜잭션에 합류하는 경우

이미 다른 코드가 커넥션을 열고 트랜잭션을 관리하고 있다면, 그 커넥션을 넘겨주면 된다. **commit/rollback 은 호출하는 쪽 책임**이다.

```java
import com.mes.event.EventDispatcher;
import com.mes.event.processor.EventProcessorFactory;
import java.sql.Connection;

// TransactionManager 없이 팩토리만으로 구성
EventDispatcher dispatcher = new EventDispatcher(new EventProcessorFactory());

Connection conn = ...; // 기존 시스템이 연 커넥션 (autoCommit=false 상태로 관리 중)
try {
    // ... 기존 MES 업무 처리 ...

    // 같은 트랜잭션에 이벤트 처리를 합류시킨다 (단일/복합 모두 가능)
    dispatcher.dispatchWithin(conn, lot, wafer);

    conn.commit();   // 커밋은 호출자가
} catch (Exception e) {
    conn.rollback(); // 롤백도 호출자가
    throw e;
}
```

---

## 거래 코드 (`TxnCode`)

DB 에는 enum 의 이름(`name()`) 문자열로 저장된다(예: `"MOVE"`).

```java
TxnCode.CREATE   // 신규 생성
TxnCode.MOVE     // 공정 이동
TxnCode.HOLD     // 보류
TxnCode.RELEASE  // 보류 해제
TxnCode.SCRAP    // 폐기

// 외부 문자열을 안전하게 변환 (null/미정의 값은 예외)
TxnCode code = TxnCode.fromCode("move"); // → TxnCode.MOVE
```

---

## 예외

- `DbAccessException` — JDBC(DB 접근) 단계 오류. 실패한 SQL/키를 메시지에 담아 던진다.
- `EventProcessingException` — 검증/디스패치/트랜잭션 단계 오류.

둘 다 `RuntimeException` 이므로 `throws` 선언이 강제되지 않는다.

---

## 새 이벤트 종류 추가하기 (예: Reticle)

이벤트 1종당 **DTO + Processor 2개 파일**만 추가하면 된다.

1. `type/EventType.java` 에 값 추가 — `RETICLE`
2. `dto/ReticleEvent.java` 작성 — `MesEvent` 를 상속하고 도메인 필드를 추가
3. `processor/ReticleEventProcessor.java` 작성 — `AbstractEventProcessor<ReticleEvent>` 를 상속하고 `masterKey` / `upsertMaster` / `insertHistory` 구현 (`LotEventProcessor` 를 그대로 본떠 SQL 만 교체)
4. `processor/EventProcessorFactory.java` 생성자에 등록 한 줄 추가:
   ```java
   register(EventType.RETICLE, new ReticleEventProcessor());
   ```

---

## 로깅 교체 (선택)

기본 로깅은 `java.util.logging` 이다. 사내 표준(Log4j2/SLF4J 등)으로 바꾸려면 애플리케이션 기동 시 한 번만 설정한다.

```java
import com.mes.event.support.EventLogFactory;

EventLogFactory.setProvider(owner -> new MyLog4jEventLog(owner));
```

---

## 빌드 / 요구사항

- Java 17 이상 (텍스트 블록 `"""..."""` 사용)
- 외부 의존성 없음 (순수 JDBC)
