package com.mes.event.type;

/**
 * 트랜잭션(이벤트 동작) 코드.
 *
 * <p>// TODO[MES] 실제 MES 의 거래 코드 체계에 맞춰 값과 코드 매핑을 보정할 것.
 * DB 에는 {@link #name()} 문자열로 저장한다(예: "MOVE").
 * 컬럼이 숫자/사내 코드라면 {@link #dbCode()} 를 두고 그 값을 바인딩하도록 변경한다.
 */
public enum TxnCode {

    CREATE,   // 신규 생성
    MOVE,     // 공정 이동
    HOLD,     // 보류
    RELEASE,  // 보류 해제
    SCRAP;    // 폐기

    /**
     * 외부 문자열 코드를 enum 으로 변환한다. null / 미정의 값은 명시적으로 예외 처리한다.
     */
    public static TxnCode fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("txnCode must not be null/blank");
        }
        try {
            return TxnCode.valueOf(code.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown txnCode: " + code, e);
        }
    }

    /** DB 저장 값. 사내 코드 체계가 있으면 여기만 바꾸면 된다. */
    public String dbCode() {
        return name();
    }
}
