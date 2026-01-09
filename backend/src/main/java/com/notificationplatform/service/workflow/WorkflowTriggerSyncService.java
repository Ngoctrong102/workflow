package com.notificationplatform.service.workflow;

import com.notificationplatform.entity.Trigger;
import com.notificationplatform.entity.Workflow;
import com.notificationplatform.entity.enums.TriggerStatus;
import com.notificationplatform.entity.enums.TriggerType;
import com.notificationplatform.entity.enums.WorkflowStatus;
import com.notificationplatform.repository.TriggerRepository;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
/**
 * Service to sync all trigger statuses based on workflow status
 * When a workflow is paused, inactive, or archived, all triggers should be deactivated
 * When a workflow is active, triggers are managed by their respective sync services
 */
@Slf4j
@Service
public class WorkflowTriggerSyncService {

    private final TriggerRepository triggerRepository;

    public WorkflowTriggerSyncService(TriggerRepository triggerRepository) {
        this.triggerRepository = triggerRepository;
    }

    /**
     * Sync all trigger statuses based on workflow status
     * - If workflow is active: triggers are managed by their respective sync services (event, schedule)
     *   API triggers that were manually created will remain active
     * - If workflow is paused, inactive, or archived: deactivate all triggers
     * Runs in a separate transaction to avoid rollback of main workflow update
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void syncAllTriggers(Workflow workflow) {
        WorkflowStatus workflowStatus = workflow.getStatus();
        
        List<Trigger> allTriggers = triggerRepository.findByWorkflowId(workflow.getId());
        
        if (workflowStatus == WorkflowStatus.ACTIVE) {
            // Active workflows: activate API triggers that were manually created
            // Event and schedule triggers are handled by their respective sync services
            // But we should not interfere with them here - they will be activated by their sync services
            int activatedCount = 0;
            for (Trigger trigger : allTriggers) {
                // Only activate API triggers (event and schedule are handled by their sync services)
                if (trigger.getTriggerType() == TriggerType.API_CALL && trigger.getStatus() != TriggerStatus.ACTIVE) {
                    log.info("Activating API trigger: triggerId={}, workflowId={}", 
                               trigger.getId(), workflow.getId());
                    trigger.setStatus(TriggerStatus.ACTIVE);
                    triggerRepository.save(trigger);
                    activatedCount++;
                }
            }
            if (activatedCount > 0) {
                log.info("Activated {} API trigger(s) for active workflow: workflowId={}", 
                           activatedCount, workflow.getId());
            }
            // Don't return here - let event and schedule sync services handle their triggers
            // This service only ensures API triggers are active, and deactivates all when workflow is not active
            return;
        }

        // For non-active workflows (paused, inactive, archived), deactivate all triggers
        log.info("Workflow status is '{}', deactivating all triggers: workflowId={}", 
                   workflowStatus, workflow.getId());
        
        int deactivatedCount = 0;
        for (Trigger trigger : allTriggers) {
            if (trigger.getStatus() == TriggerStatus.ACTIVE) {
                log.info("Deactivating trigger: triggerId={}, type={}, workflowId={}, workflowStatus={}", 
                           trigger.getId(), trigger.getTriggerType(), workflow.getId(), workflowStatus);
                trigger.setStatus(TriggerStatus.INACTIVE);
                triggerRepository.save(trigger);
                deactivatedCount++;
            }
        }
        
        log.info("Deactivated {} trigger(s) for workflow: workflowId={}, status={}", 
                   deactivatedCount, workflow.getId(), workflowStatus);
    }
}

