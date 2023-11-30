package com.xion.payload;

import com.xion.app.dto.AbstractResponse;
import com.xion.models.gst.Action;
import com.xion.resultObjectModel.resultSummeries.ResultSummery;

import java.util.List;
import java.util.Map;

public class AuditDTO extends AbstractResponse {

    private Map<ResultSummery, List<Action>> dataMap;

    public Map<ResultSummery, List<Action>> getDataMap() {
        return dataMap;
    }

    public void setDataMap(Map<ResultSummery, List<Action>> dataMap) {
        this.dataMap = dataMap;
    }
}
