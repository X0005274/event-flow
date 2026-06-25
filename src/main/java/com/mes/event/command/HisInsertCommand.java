package com.mes.event.command;

import java.util.Map;

/**
 * 히스토리(HIS) 테이블 insert 파라미터.
 *
 * <p>HIS 는 append-only(항상 insert) 성격이므로 update 변형이 없다.
 *
 * @param key      관련 마스터 키 (lotId / waferId / durableId)
 * @param common   공통 감사 컬럼
 * @param specific 이벤트별 특화 컬럼 (컬럼명 → 값)
 */
public record HisInsertCommand(String key, CommonColumns common, Map<String, Object> specific) {
}
