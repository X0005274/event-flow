package com.mes.event.processor;

import com.mes.event.dao.HisDao;
import com.mes.event.dao.MasDao;
import com.mes.event.dto.DurableEvent;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Durable 이벤트 처리기. 차별화 포인트: 마스터 키 = durableId, 특화 컬럼 = durableType/usageCount.
 */
public final class DurableEventProcessor extends AbstractEventProcessor<DurableEvent> {

    private final MasDao masDao;
    private final HisDao hisDao;

    public DurableEventProcessor(MasDao masDao, HisDao hisDao) {
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
    protected String masterKey(DurableEvent event) {
        return event.getDurableId();
    }

    @Override
    protected Map<String, Object> specificColumns(DurableEvent event) {
        Map<String, Object> columns = new LinkedHashMap<>();
        columns.put("DURABLE_TYPE", event.getDurableType());
        columns.put("USAGE_COUNT", event.getUsageCount());
        return columns;
    }
}
