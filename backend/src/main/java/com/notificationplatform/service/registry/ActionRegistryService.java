package com.notificationplatform.service.registry;

import com.notificationplatform.entity.Action;
import com.notificationplatform.entity.enums.ActionType;

import java.util.List;

/**
 * Service interface for Action Registry management.
 * Actions must be defined and registered before they can be used in workflows.
 */
public interface ActionRegistryService {

    /**
     * Get all active actions from registry.
     * @return List of all active actions
     */
    List<Action> getAllActions();

    /**
     * Get action by ID.
     * @param id Action ID
     * @return Action if found
     * @throws com.notificationplatform.exception.ResourceNotFoundException if action not found
     */
    Action getActionById(String id);

    /**
     * Get actions by type.
     * @param type Action type
     * @return List of actions matching the type
     */
    List<Action> getActionsByType(ActionType type);

    /**
     * Get all custom actions.
     * @return List of custom actions
     */
    List<Action> getCustomActions();

    /**
     * Register a new action.
     * @param action Action to register
     * @return Registered action
     * @throws com.notificationplatform.exception.ConflictException if action ID already exists
     */
    Action registerAction(Action action);

    /**
     * Update an existing action.
     * @param id Action ID
     * @param action Updated action data
     * @return Updated action
     * @throws com.notificationplatform.exception.ResourceNotFoundException if action not found
     */
    Action updateAction(String id, Action action);

    /**
     * Enable an action.
     * @param id Action ID
     * @return Enabled action
     * @throws com.notificationplatform.exception.ResourceNotFoundException if action not found
     */
    Action enableAction(String id);

    /**
     * Disable an action.
     * @param id Action ID
     * @return Disabled action
     * @throws com.notificationplatform.exception.ResourceNotFoundException if action not found
     */
    Action disableAction(String id);
}

