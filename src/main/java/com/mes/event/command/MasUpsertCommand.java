package com.mes.event.command;

import java.util.Map;

/**
 * 마스터(MAS) 테이블 upsert 파라미터.
 *
 * @param key      마스터 PK 값 (lotId / waferId / durableId)
 * @param common   공통 감사 컬럼
 * @param specific 이벤트별 특화 컬럼 (컬럼명 → 값). 바인딩 순서 안정성을 위해
 *                 {@link java.util.LinkedHashMap} 사용을 권장한다. 값에는 null 이 허용된다.
 */
public record MasUpsertCommand(String key, CommonColumns common, Map<String, Object> specific) {
}
