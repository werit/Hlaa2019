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
package hlaa.duelbot.Behavior.BehaviorLogic;

import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import hlaa.duelbot.Behavior.BehaviorResource;
import hlaa.duelbot.Behavior.BotCapabilities;
import hlaa.duelbot.Behavior.ConditionDto;

/**
 *
 * @author msi
 */
public class CoverBehavior implements IBehavior {

    private double priority;
    private BehaviorResource behaviorResource;
    private ConditionDto conditionDto;

    public CoverBehavior(double priority, BehaviorResource behaviorResource) {
        this.priority = priority;
        this.behaviorResource = behaviorResource;
        conditionDto = new ConditionDto(false, false, false, true);
    }

    @Override
    public BotCapabilities[] GetBotCapabilities() {
        return new BotCapabilities[]{BotCapabilities.FIRE, BotCapabilities.MOVE};
    }

    @Override
    public IBehavior Stop() {
        behaviorResource.shoot.stopShooting();
        return null;
    }

    @Override
    public IBehavior Execute() {

        if (behaviorResource.players.getNearestVisibleEnemy() != null && behaviorResource.senses.isBeingDamaged()) {
            behaviorResource.weaponry.changeWeapon(UT2004ItemType.SHIELD_GUN);
            behaviorResource.shoot.shootSecondary(behaviorResource.players.getNearestVisibleEnemy());
        }
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
