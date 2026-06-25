package com.mes.event.processor;

import com.mes.event.dao.HisDao;
import com.mes.event.dao.MasDao;
import com.mes.event.dto.WaferEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Wafer 이벤트 처리기. 차별화 포인트: 마스터 키 = waferId, 특화 컬럼 = lotId/slotNo/gradeCode.
 */
public final class WaferEventProcessor extends AbstractEventProcessor<WaferEvent> {

    private final MasDao masDao;
    private final HisDao hisDao;

    public WaferEventProcessor(MasDao masDao, HisDao hisDao) {
        this.masDao = masDao;
        this.hisDao = hisDao;
    }

    @Override
    protected MasDao masDao() {
        return masDao;
    }

    @Override
    protected HisDao hisDao() {
        return hisDao;
    }

    @Override
    protected String masterKey(WaferEvent event) {
        return event.getWaferId();
    }

    @Override
    protected Map<String, Object> specificColumns(WaferEvent event) {
        Map<String, Object> columns = new LinkedHashMap<>();
        columns.put("LOT_ID", event.getLotId());
        columns.put("SLOT_NO", event.getSlotNo());
        columns.put("GRADE_CODE", event.getGradeCode());
        return columns;
    }
}
