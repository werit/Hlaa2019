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

import hlaa.duelbot.Behavior.BehaviorLogic.FireAtEnemy;
import hlaa.duelbot.Behavior.BehaviorLogic.FocusEnemy;
import hlaa.duelbot.Behavior.BehaviorResource;
import hlaa.duelbot.Behavior.BehaviorLogic.PursueBehavior;
import hlaa.duelbot.Behavior.BehaviorLogic.IBehavior;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author msi
 */
public class BehaviorManager {
    private List<IBehavior> behaviors;
    private BehaviorResource behaviorResource;
    private IBehavior currentBehavior = null;
    
    public BehaviorManager(BehaviorResource behaviorResource){
        this.behaviorResource = behaviorResource;
        AddBehaviors();
    }
    
    private void AddBehaviors() {
        behaviors = new LinkedList<>();
        behaviors.add(new PursueBehavior(5, behaviorResource));
        behaviors.add(new FireAtEnemy(4, behaviorResource));
        behaviors.add(new FocusEnemy(3, behaviorResource));
    }
    public void DoLogic(){
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
        
        IBehavior nextBeahvior = null;
        ConditionDto conditionEvaluation = EvaluateConditions();
        for (IBehavior behavior : behaviors) {
            if (behavior.IsUsable(conditionEvaluation)) {
                if (nextBeahvior == null || behavior.GetPriority() > nextBeahvior.GetPriority()) {
                    nextBeahvior = behavior;
                }
            }
        }
    }
    private ConditionDto EvaluateConditions() {
        /*if (behaviorResource.focusedEnemy == null) {
            return false;
        }*/
        behaviorResource.focusedEnemy = behaviorResource.players.getNearestVisibleEnemy();
        
        boolean isEnemyInFocus = behaviorResource.focusedEnemy != null;
        boolean isEnemyInFocusAtLocation = false;
        boolean isNotNavigating = false;
        if (isEnemyInFocus) {
            isEnemyInFocusAtLocation = behaviorResource.info.atLocation(behaviorResource.focusedEnemy.getLocation());
            behaviorResource.focusedEnemy = null;
        }
        
        /*if (behaviorResource.info.atLocation(behaviorResource.focusedEnemy.getLocation())) {
            behaviorResource.focusedEnemy = null;
            return false;
        }*/
        isNotNavigating = !behaviorResource.navigation.isNavigating();
        //if(isNotNavigating)behaviorResource.focusedEnemy = null;
        /*if (!behaviorResource.navigation.isNavigating()) {
            behaviorResource.focusedEnemy = null;
            return false;
        }*/
        return new ConditionDto(isEnemyInFocus,isEnemyInFocusAtLocation,isNotNavigating);
    }
}
