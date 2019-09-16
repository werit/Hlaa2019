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

import cz.cuni.amis.pogamut.base.utils.math.DistanceUtils;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.utils.IFilter;
import cz.cuni.amis.utils.collections.MyCollections;
import hlaa.duelbot.Behavior.BehaviorResource;
import hlaa.duelbot.Behavior.BotCapabilities;
import hlaa.duelbot.Behavior.ConditionDto;

/**
 *
 * @author msi
 */
public class ItemPickBehavior implements IBehavior {

    private double priority;
    private BehaviorResource behaviorResource;
    private ConditionDto conditionDto;

    public ItemPickBehavior(double priority, BehaviorResource behaviorResource) {
        this.priority = priority;
        this.behaviorResource = behaviorResource;
        conditionDto = new ConditionDto(false, false, false);
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
        
        Item item = DistanceUtils.getNearest(
                MyCollections.getFiltered(
                        behaviorResource.items.getSpawnedItems().values(),
                        new IFilter<Item>() {
                            @Override
                            public boolean isAccepted(Item object) {
                                // You might try to use this to time pickupus...
                                //items.isPickupSpawned(item)
                                //items.willPickupBeSpawnedIn(item, seconds)
                                //UnrealUtils.CHARACTER_RUN_SPEED // UT_units/sec
                                return behaviorResource.items.isPickable(object);
                            }
                        }),
                behaviorResource.info.getLocation(),
                new DistanceUtils.IGetDistance<Item>() {

                    @Override
                    public double getDistance(Item object, ILocated target) {
                        double multi = 1;
                        if (object.getType() == UT2004ItemType.LIGHTNING_GUN) {
                            multi = 0.5;
                        }
                        if (object.getType() == UT2004ItemType.FLAK_CANNON) {
                            multi = 0.6;
                        }
                        if (object.getType() == UT2004ItemType.SUPER_SHIELD_PACK) {
                            multi = 0.6;
                        }
                        if (object.getType() == UT2004ItemType.U_DAMAGE_PACK) {
                            multi = 0.3;
                        }
                        if (object.getType() == UT2004ItemType.SHIELD_PACK) {
                            multi = 0.7;
                        }
                        if (object.getType() == UT2004ItemType.SUPER_HEALTH_PACK) {
                            multi = 0.4;
                        }
                        if (object.getType().getCategory() == ItemType.Category.AMMO) {
                            multi = Double.POSITIVE_INFINITY;
                        }
                        return multi * behaviorResource.navMeshModule.getAStarPathPlanner().getDistance(target, object);
                    }

                });
        if (item == null) {
            if (!behaviorResource.navigation.isNavigating()) {
                behaviorResource.navigation.navigate(behaviorResource.navPoints.getRandomNavPoint());
            }
        } else {
            behaviorResource.navigation.navigate(item);
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
