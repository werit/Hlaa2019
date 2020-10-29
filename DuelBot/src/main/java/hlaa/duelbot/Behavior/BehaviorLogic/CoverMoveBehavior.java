/*
 * Copyright (C) 2020 AMIS research group, Faculty of Mathematics and Physics, Charles University in Prague, Czech Republic
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
package hlaa.duelbot.Behavior.BehaviorLogic;

import cz.cuni.amis.pogamut.base.utils.math.DistanceUtils;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import hlaa.duelbot.Behavior.BehaviorResource;
import hlaa.duelbot.Behavior.BotCapabilities;
import hlaa.duelbot.Behavior.ConditionDto;
import java.util.Set;

/**
 *
 * @author msi
 */
public class CoverMoveBehavior implements IBehavior {

    private double priority;
    private BehaviorResource behaviorResource;
    private ConditionDto conditionDto;

    public CoverMoveBehavior(double priority, BehaviorResource behaviorResource) {
        this.priority = priority;
        this.behaviorResource = behaviorResource;
        conditionDto = new ConditionDto(true, false, false, true);
    }

    @Override
    public BotCapabilities[] GetBotCapabilities() {
        return new BotCapabilities[]{BotCapabilities.MOVE};
    }

    @Override
    public IBehavior Stop() {
        if (behaviorResource.navigation.isNavigating()) {
            behaviorResource.navigation.stopNavigation();
        }
        // TODO return this if we are close to the item
        return null;
    }

    @Override
    public IBehavior Execute() {
        Player enemyPLayer = behaviorResource.players.getNearestVisibleEnemy();
        if (enemyPLayer == null) {
            enemyPLayer = behaviorResource.focusedEnemy;
        }
        if (enemyPLayer == null) {
            // no enemy just serach for an item
            return new ItemPickBehavior(0, behaviorResource).Execute();
        }

        // HIDE FROM THE PLAYER!
        Set<NavPoint> covers = behaviorResource.visibility.getCoverNavPointsFrom(enemyPLayer);
        if (covers == null) {
            return new ItemPickBehavior(0, behaviorResource).Execute();
        }
        NavPoint target = DistanceUtils.getNearest(covers, behaviorResource.info.getLocation(), new DistanceUtils.IGetDistance<NavPoint>() {

            @Override
            public double getDistance(NavPoint object, ILocated target) {
                return behaviorResource.navMeshModule.getAStarPathPlanner().getDistance(target, object);
            }

        });
        if (target == null) {
            return new ItemPickBehavior(0, behaviorResource).Execute();
        }

        behaviorResource.navigation.setFocus(behaviorResource.players.getNearestVisiblePlayer());
        behaviorResource.navigation.navigate(target);

        return this;
    }

    @Override
    public boolean IsUsable(ConditionDto currentlyMetConditionDto) {
        return conditionDto.AreConditionsMetAtCurrentRun(currentlyMetConditionDto);
    }

    @Override
    public double GetPriority() {
        return this.priority;
    }

    @Override
    public String GetBehaviorName() {
        return this.getClass().toString();
    }

}
