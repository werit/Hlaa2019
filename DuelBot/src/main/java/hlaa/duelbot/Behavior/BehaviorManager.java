/*
 * Copyright (C) 2019 AMIS research group, Faculty of Mathematics and Physics, Charles University in Prague, Czech Republic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package hlaa.duelbot.Behavior;

import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import hlaa.duelbot.Behavior.BehaviorLogic.CoverMoveBehavior;
import hlaa.duelbot.Behavior.BehaviorLogic.CoverShootingBehavior;
import hlaa.duelbot.Behavior.BehaviorLogic.FireAtEnemy;
import hlaa.duelbot.Behavior.BehaviorLogic.FocusEnemy;
import hlaa.duelbot.Behavior.BehaviorResource;
import hlaa.duelbot.Behavior.BehaviorLogic.PursueBehavior;
import hlaa.duelbot.Behavior.BehaviorLogic.IBehavior;
import hlaa.duelbot.Behavior.BehaviorLogic.ItemPickBehavior;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import net.sf.saxon.instruct.ForEach;

/**
 *
 * @author msi
 */
public class BehaviorManager {

    private List<IBehavior> behaviors;
    private List<IBehavior> previousBehaviors;
    private List<IBehavior> currentBehaviors;
    private BehaviorResource behaviorResource;

    public BehaviorManager(BehaviorResource behaviorResource) {
        this.behaviorResource = behaviorResource;
        AddBehaviors();
        previousBehaviors = new ArrayList<>();
        currentBehaviors = new ArrayList<>();
    }

    private void AddBehaviors() {
        behaviors = new LinkedList<>();
        behaviors.add(new PursueBehavior(5, behaviorResource));
        behaviors.add(new FireAtEnemy(4, behaviorResource));
        behaviors.add(new FocusEnemy(3, behaviorResource));
        behaviors.add(new ItemPickBehavior(6, behaviorResource));
        behaviors.add(new CoverMoveBehavior(4.5, behaviorResource));
    }

    public BehaviorResource GetResources() {
        return behaviorResource;
    }

    public void DoLogic() {
        /*
         if (combatBeh()) {
         return;
         }
         fireAtEnemy();
         if (pursueEnemy()) {
         return;
         }
         focusEnemy();
         collectItems();
         */

        ConditionDto conditionEvaluation = EvaluateConditions();
        List<BotCapabilities> botCapabilities = GetAllBotcapabilities();
        //TODODODODO
//todo get all currently executed behaviors and stop the ones that should not execute
        previousBehaviors = currentBehaviors;
        currentBehaviors = new ArrayList<>();

        while (botCapabilities.size() > 0) {
            IBehavior nextBeahvior = null;
            for (IBehavior behavior : GetBehaviorsRequiringCapabilities(behaviors, botCapabilities)) {
                if (behavior.IsUsable(conditionEvaluation)) {
                    if (nextBeahvior == null || behavior.GetPriority() < nextBeahvior.GetPriority()) {
                        nextBeahvior = behavior;
                    }
                }
            }
            if (nextBeahvior != null) {
                for (BotCapabilities GetBotCapability : nextBeahvior.GetBotCapabilities()) {
                    botCapabilities.remove(GetBotCapability);
                }
                currentBehaviors.add(nextBeahvior);
            } else {
                break;
            }
        }
        StopNotUsedBehaviors(previousBehaviors, currentBehaviors);

        for (IBehavior executionBehav : currentBehaviors) {
            behaviorResource.log.log(Level.INFO, "Starting execution of {0}", executionBehav.GetBehaviorName());
            executionBehav.Execute();
        }
        // reset to verify whether fight is still ongoing
        behaviorResource.otherEnemy = null;
    }

    private void StopNotUsedBehaviors(List<IBehavior> previousBehaviors, List<IBehavior> currentBehaviors) {
        for (IBehavior previousBehav : previousBehaviors) {
            boolean isContained = false;
            for (IBehavior currentBehav : currentBehaviors) {
                if (previousBehav == currentBehav) {
                    isContained = true;
                    break;
                }
            }
            if (!isContained) {
                behaviorResource.log.log(Level.INFO, "Stopping execution of {0}", previousBehav.GetBehaviorName());
                previousBehav.Stop();
            }
        }
    }

    private List<BotCapabilities> GetAllBotcapabilities() {
        List<BotCapabilities> botCapabilities = new ArrayList<>();
        botCapabilities.add(BotCapabilities.FIRE);
        botCapabilities.add(BotCapabilities.MOVE);
        botCapabilities.add(BotCapabilities.FOCUS);
        return botCapabilities;
    }

    private ConditionDto EvaluateConditions() {
        ChooseFocusedEnemy();

        boolean isEnemyInFocus = behaviorResource.focusedEnemy != null;
        boolean isEnemyInFocusAtLocation = false;
        boolean isNotNavigating = false;
        if (isEnemyInFocus) {
            isEnemyInFocusAtLocation = behaviorResource.info.atLocation(behaviorResource.focusedEnemy.getLocation());
            //behaviorResource.focusedEnemy = null;
        }

        /*if (behaviorResource.info.atLocation(behaviorResource.focusedEnemy.getLocation())) {
         behaviorResource.focusedEnemy = null;
         return false;
         }*/
        isNotNavigating = !behaviorResource.navigation.isNavigating();
        boolean isHealthBelow70 = behaviorResource.info.getHealth() < 70;
        //if(isNotNavigating)behaviorResource.focusedEnemy = null;
        /*if (!behaviorResource.navigation.isNavigating()) {
         behaviorResource.focusedEnemy = null;
         return false;
         }*/
        return new ConditionDto(isEnemyInFocus, isEnemyInFocusAtLocation, isNotNavigating, isHealthBelow70);
    }

    private void ChooseFocusedEnemy() {

        behaviorResource.focusedEnemy = behaviorResource.players.getNearestVisibleEnemy();

        // navigate to other enemy only f you do not have enemy
        if (behaviorResource.focusedEnemy == null) {
            // if other enemy is null never mind, nothing will change otherwise we navigate to other enemy
            behaviorResource.focusedEnemy = behaviorResource.otherEnemy;
            return;
        }

        // enemy of other bot is visible
        if (behaviorResource.otherEnemy != null
                && behaviorResource.players.getVisibleEnemies().values().contains(behaviorResource.otherEnemy)) {
            //nearest enemy is not enemy targeted by my ally
            if (!behaviorResource.focusedEnemy.equals(behaviorResource.otherEnemy)) {
                //have to choose one
                // not sure how to get health bot who si not me
                if (behaviorResource.focusedEnemy.getId().getLongId() < behaviorResource.otherEnemy.getId().getLongId()) {
                    behaviorResource.focusedEnemy = behaviorResource.otherEnemy;
                }
            }
        }

    }

    private List<IBehavior> GetBehaviorsRequiringCapabilities(List<IBehavior> behaviors, List<BotCapabilities> freeBotCapabilities) {
        List<IBehavior> admissibleBehaviors = new ArrayList<>();

        for (IBehavior behavior : behaviors) {
            BotCapabilities[] getBotBehaviorActions = behavior.GetBotCapabilities();

            if (IsBehaviorUsable(getBotBehaviorActions, freeBotCapabilities)) {
                admissibleBehaviors.add(behavior);
            }
        }
        return admissibleBehaviors;
    }

    private boolean IsBehaviorUsable(BotCapabilities[] getBotBehaviorActions, List<BotCapabilities> freeBotCapabilities) {
        boolean isBehaviorUsable = false;
        for (BotCapabilities botBehaviorAction : getBotBehaviorActions) {
            boolean isCapabilityPresent = false;
            for (int i = 0; i < freeBotCapabilities.size(); i++) {
                if (botBehaviorAction == freeBotCapabilities.get(i)) {
                    isCapabilityPresent = true;
                    break;
                }
            }
            if (isCapabilityPresent) {
                isBehaviorUsable = true;
            } else {
                isBehaviorUsable = false;
                break;
            }
        }
        return isBehaviorUsable;
    }
}
