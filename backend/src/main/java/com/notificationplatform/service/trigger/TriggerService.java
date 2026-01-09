package com.notificationplatform.service.trigger;

import com.notificationplatform.dto.request.CreateApiTriggerRequest;
import com.notificationplatform.dto.request.CreateEventTriggerRequest;
import com.notificationplatform.dto.request.CreateFileTriggerRequest;
import com.notificationplatform.dto.request.CreateScheduleTriggerRequest;
import com.notificationplatform.dto.request.UpdateTriggerRequest;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.dto.response.TriggerActivationResponse;
import com.notificationplatform.dto.response.TriggerResponse;

import java.util.List;
import java.util.Map;

public interface TriggerService {

    TriggerResponse createApiTrigger(CreateApiTriggerRequest request);

    TriggerResponse createScheduleTrigger(CreateScheduleTriggerRequest request);

    TriggerResponse createFileTrigger(CreateFileTriggerRequest request);

    TriggerResponse createEventTrigger(CreateEventTriggerRequest request);

    TriggerResponse getTriggerById(String id);

    List<TriggerResponse> listTriggers(String workflowId);

    PagedResponse<TriggerResponse> listTriggersPaged(String workflowId, String type, String status, String search, int limit, int offset);

    TriggerResponse updateTrigger(String id, UpdateTriggerRequest request);

    void deleteTrigger(String id);

    TriggerActivationResponse activateApiTrigger(String path, String method, Map<String, Object> requestData, String apiKey);

    TriggerResponse getTriggerByPath(String path, String method);

    TriggerResponse activateTrigger(String id);

    TriggerResponse deactivateTrigger(String id);
}

