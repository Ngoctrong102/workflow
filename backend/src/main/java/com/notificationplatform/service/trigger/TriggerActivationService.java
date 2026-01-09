package com.notificationplatform.service.trigger;

import com.notificationplatform.entity.Trigger;
import com.notificationplatform.entity.Workflow;
import com.notificationplatform.entity.enums.TriggerStatus;
import com.notificationplatform.entity.enums.WorkflowStatus;
import com.notificationplatform.repository.TriggerRepository;
import com.notificationplatform.service.trigger.api.TriggerInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for activating/deactivating triggers for workflows.
 * Handles trigger lifecycle synchronization with workflow status.
 * 
 * See: @import(features/trigger-registry.md#trigger-instance-lifecycle)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TriggerActivationService {

    private final TriggerRepository triggerRepository;
    private final TriggerInstanceService triggerInstanceService;

    /**
     * Activate all triggers for a workflow.
     * Called when workflow is activated.
     * 
     * @param workflow Workflow to activate triggers for
     */
    @Transactional
    public void activateTriggersForWorkflow(Workflow workflow) {
        log.info("Activating triggers for workflow: workflowId={}", workflow.getId());
        
        List<Trigger> triggers = triggerRepository.findByWorkflowId(workflow.getId());
        
        for (Trigger trigger : triggers) {
            try {
                // Only activate if trigger is not already active
                if (trigger.getStatus() != TriggerStatus.ACTIVE) {
                    trigger.setStatus(TriggerStatus.ACTIVE);
                    triggerRepository.save(trigger);
                    
                    // Start trigger instance
                    triggerInstanceService.startTrigger(trigger.getId());
                    
                    log.info("Activated trigger: triggerId={}, type={}", 
                             trigger.getId(), trigger.getTriggerType());
                }
            } catch (Exception e) {
                log.error("Error activating trigger: triggerId={}, workflowId={}", 
                          trigger.getId(), workflow.getId(), e);
                // Continue with other triggers even if one fails
            }
        }
        
        log.info("Activated triggers for workflow: workflowId={}, count={}", 
                 workflow.getId(), triggers.size());
    }

    /**
     * Deactivate all triggers for a workflow.
     * Called when workflow is deactivated, paused, or archived.
     * 
     * @param workflow Workflow to deactivate triggers for
     */
    @Transactional
    public void deactivateTriggersForWorkflow(Workflow workflow) {
        log.info("Deactivating triggers for workflow: workflowId={}", workflow.getId());
        
        List<Trigger> triggers = triggerRepository.findByWorkflowId(workflow.getId());
        
        for (Trigger trigger : triggers) {
            try {
                // Only deactivate if trigger is active
                if (trigger.getStatus() == TriggerStatus.ACTIVE) {
                    // Stop trigger instance
                    triggerInstanceService.stopTrigger(trigger.getId());
                    
                    log.info("Deactivated trigger: triggerId={}, type={}", 
                             trigger.getId(), trigger.getTriggerType());
                }
            } catch (Exception e) {
                log.error("Error deactivating trigger: triggerId={}, workflowId={}", 
                          trigger.getId(), workflow.getId(), e);
                // Continue with other triggers even if one fails
            }
        }
        
        log.info("Deactivated triggers for workflow: workflowId={}, count={}", 
                 workflow.getId(), triggers.size());
    }

    /**
     * Sync trigger instances with workflow definition.
     * Activates/deactivates triggers based on workflow status.
     * 
     * @param workflow Workflow to sync triggers for
     */
    @Transactional
    public void syncTriggersWithWorkflow(Workflow workflow) {
        log.info("Syncing triggers with workflow: workflowId={}, status={}", 
                 workflow.getId(), workflow.getStatus());
        
        if (workflow.getStatus() == WorkflowStatus.ACTIVE) {
            // Activate triggers for active workflow
            activateTriggersForWorkflow(workflow);
        } else {
            // Deactivate triggers for non-active workflow
            deactivateTriggersForWorkflow(workflow);
        }
    }
}

