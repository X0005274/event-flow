package com.mes.event.processor;

import com.mes.event.dao.HisDao;
import com.mes.event.dao.MasDao;
import com.mes.event.dto.LotEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Lot 이벤트 처리기. 차별화 포인트: 마스터 키 = lotId, 특화 컬럼 = product/step/waferQty.
 */
public final class LotEventProcessor extends AbstractEventProcessor<LotEvent> {

    private final MasDao masDao;
    private final HisDao hisDao;

    public LotEventProcessor(MasDao masDao, HisDao hisDao) {
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
    protected String masterKey(LotEvent event) {
        return event.getLotId();
    }

    @Override
    protected Map<String, Object> specificColumns(LotEvent event) {
        Map<String, Object> columns = new LinkedHashMap<>();
        columns.put("PRODUCT_ID", event.getProductId());
        columns.put("STEP_ID", event.getStepId());
        columns.put("WAFER_QTY", event.getWaferQty()); // nullable 허용
        return columns;
    }
}
