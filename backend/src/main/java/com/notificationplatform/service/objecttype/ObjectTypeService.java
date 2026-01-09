package com.notificationplatform.service.objecttype;

import com.notificationplatform.dto.request.CreateObjectTypeRequest;
import com.notificationplatform.dto.request.UpdateObjectTypeRequest;
import com.notificationplatform.dto.response.ObjectTypeResponse;
import com.notificationplatform.dto.response.PagedResponse;
import com.notificationplatform.entity.FieldDefinition;

import java.util.List;

public interface ObjectTypeService {

    ObjectTypeResponse createObjectType(CreateObjectTypeRequest request);

    ObjectTypeResponse getObjectTypeById(String id);

    ObjectTypeResponse getObjectTypeByName(String name);

    PagedResponse<ObjectTypeResponse> listObjectTypes(int limit, int offset, String search, String tag);

    ObjectTypeResponse updateObjectType(String id, UpdateObjectTypeRequest request);

    void deleteObjectType(String id);

    List<FieldDefinition> getObjectTypeFields(String id);

    FieldDefinition getFieldDefinition(String objectTypeId, String fieldName);

    void validateFieldReference(String objectTypeId, String fieldPath);

    List<com.notificationplatform.dto.response.FieldSearchResult> searchFields(String query, String type, int limit);
}

