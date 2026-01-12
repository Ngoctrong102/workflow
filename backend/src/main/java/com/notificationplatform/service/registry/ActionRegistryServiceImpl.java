package com.notificationplatform.service.registry;

import com.notificationplatform.entity.Action;
import com.notificationplatform.entity.enums.ActionType;
import com.notificationplatform.exception.ConflictException;
import com.notificationplatform.exception.ResourceNotFoundException;
import com.notificationplatform.repository.ActionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of Action Registry Service.
 * Manages action definitions in the registry.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ActionRegistryServiceImpl implements ActionRegistryService {

    private final ActionRepository actionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Action> getAllActions() {
        log.debug("Getting all actions from registry");
        return actionRepository.findAllEnabled();
    }

    @Override
    @Transactional(readOnly = true)
    public Action getActionById(String id) {
        log.debug("Getting action by ID: {}", id);
        return actionRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Action not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Action> getActionsByType(ActionType type) {
        log.debug("Getting actions by type: {}", type);
        return actionRepository.findByTypeAndEnabled(type);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Action> getCustomActions() {
        log.debug("Getting all custom actions");
        // Custom actions have type = 'custom-action'
        return actionRepository.findByTypeAndEnabled(ActionType.CUSTOM_ACTION);
    }

    @Override
    @Transactional
    public Action registerAction(Action action) {
        log.info("Registering new action: {}", action.getId());
        
        // Check if action already exists
        if (actionRepository.existsByIdAndNotDeleted(action.getId())) {
            throw new ConflictException("Action already exists with id: " + action.getId());
        }

        // Set timestamps
        action.setCreatedAt(LocalDateTime.now());
        action.setUpdatedAt(LocalDateTime.now());
        
        Action saved = actionRepository.save(action);
        log.info("Successfully registered action: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Action updateAction(String id, Action updatedAction) {
        log.info("Updating action: {}", id);
        
        Action existing = actionRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Action not found with id: " + id));

        // Update only non-null fields (partial update)
        if (updatedAction.getName() != null) {
            existing.setName(updatedAction.getName());
        }
        if (updatedAction.getType() != null) {
            existing.setType(updatedAction.getType());
        }
        if (updatedAction.getDescription() != null) {
            existing.setDescription(updatedAction.getDescription());
        }
        if (updatedAction.getConfigTemplate() != null) {
            existing.setConfigTemplate(updatedAction.getConfigTemplate());
        }
        if (updatedAction.getMetadata() != null) {
            existing.setMetadata(updatedAction.getMetadata());
        }
        if (updatedAction.getVersion() != null) {
            existing.setVersion(updatedAction.getVersion());
        }
        if (updatedAction.getEnabled() != null) {
            existing.setEnabled(updatedAction.getEnabled());
        }
        existing.setUpdatedAt(LocalDateTime.now());

        Action saved = actionRepository.save(existing);
        log.info("Successfully updated action: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Action enableAction(String id) {
        log.info("Enabling action: {}", id);
        
        Action action = actionRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Action not found with id: " + id));

        action.setEnabled(true);
        action.setUpdatedAt(LocalDateTime.now());

        Action saved = actionRepository.save(action);
        log.info("Successfully enabled action: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Action disableAction(String id) {
        log.info("Disabling action: {}", id);
        
        Action action = actionRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Action not found with id: " + id));

        action.setEnabled(false);
        action.setUpdatedAt(LocalDateTime.now());

        Action saved = actionRepository.save(action);
        log.info("Successfully disabled action: {}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public void deleteAction(String id) {
        log.info("Deleting action: {}", id);
        
        Action action = actionRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new ResourceNotFoundException("Action not found with id: " + id));

        // Soft delete
        action.setDeletedAt(LocalDateTime.now());
        action.setEnabled(false);
        action.setUpdatedAt(LocalDateTime.now());
        
        actionRepository.save(action);
        log.info("Successfully deleted action: {}", id);
    }
}

