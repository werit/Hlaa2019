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
import java.util.ArrayList;
import java.util.List;
import tdm.tc.msgs.TCStartToNavigateToItem;
import tdm.tc.msgs.TcStopNavigatingToItem;

/**
 *
 * @author msi
 */
public class ItemPickBehavior implements IBehavior {

    private double priority;
    private BehaviorResource behaviorResource;
    private ConditionDto conditionDto;
    private Item lastItemNavigatedTo = null;
    private List<ItemType.Category> importantCategories;

    public ItemPickBehavior(double priority, BehaviorResource behaviorResource) {
        this.priority = priority;
        this.behaviorResource = behaviorResource;
        conditionDto = new ConditionDto(false, false, false, false);
        InitializeImportantCategories();
    }

    @Override
    public BotCapabilities[] GetBotCapabilities() {
        return new BotCapabilities[]{BotCapabilities.MOVE};
    }

    @Override
    public IBehavior Stop() {
        SendMessageOfStoppingToNavigateToCertainItem();
        lastItemNavigatedTo = null;
        if (behaviorResource.navigation.isNavigating()) {
            behaviorResource.navigation.stopNavigation();
        }
        // TODO return this if we are close to the item
        return null;
    }

    @Override
    public IBehavior Execute() {

        // Get item where I would like to navigate now
        Item item = GetNearestAllowedItem();

        if (item == null) {
            if (!behaviorResource.navigation.isNavigating()) {
                behaviorResource.navigation.navigate(behaviorResource.navPoints.getRandomNavPoint());
            }
        } else {
            if (item.equals(lastItemNavigatedTo)) {
                // This is a fix for item on rocks when the navigation mesh is not the "best"
                // If navigation mesh is functioning as it should this will hinder the bot movement on stairs and similar spaces.
                // Therefore it is commented out..

                /*if (!behaviorResource.navigation.isNavigating()) {
                 if (item.isVisible() || behaviorResource.info.getDistance(item) < 150 && item.getLocation().sub(behaviorResource.info.getLocation()).z < 50) {
                 behaviorResource.move.moveTo(
                 item.getLocation()
                 .add(item.getLocation().sub(behaviorResource.info.getLocation()).getNormalized().scale(150)));
                 return this;
                 }
                 }*/
            } else {
                // if not navigating to important item and new item belongs to important category than change target
                if (!(lastItemNavigatedTo != null
                        && behaviorResource.navigation.isNavigating()
                        && ( // lastItem is important or new item is not important
                        importantCategories.contains(lastItemNavigatedTo.getType().getCategory())
                        || !importantCategories.contains(item.getType().getCategory())))) {
                    SendMessageOfStoppingToNavigateToCertainItem();
                    behaviorResource.tcClient.sendToTeamOthers(
                            new TCStartToNavigateToItem(behaviorResource.info.getId(), item.getId()));
                    // forbid me from navigating to this item
                    behaviorResource.tabooItems.add(item, 20);
                    behaviorResource.navigation.navigate(item);
                    lastItemNavigatedTo = item;
                }
            }

        }
        return this;
    }

    private Item GetNearestAllowedItem() {
        return DistanceUtils.getNearest(
                MyCollections.getFiltered(
                        behaviorResource.items.getAllItems().values(),
                        new IFilter<Item>() {
                            @Override
                            public boolean isAccepted(Item object) {
                                // You might try to use this to time pickupus...
                                //items.isPickupSpawned(item)
                                //items.willPickupBeSpawnedIn(item, seconds)
                                //UnrealUtils.CHARACTER_RUN_SPEED // UT_units/sec
                                return behaviorResource.items.isPickable(object)
                                && !behaviorResource.tabooItems.isTaboo(object)
                                && (behaviorResource.items.isPickupSpawned(object) // did not know what speed can I expect...than I could use distance from distance utils 
                                /*|| behaviorResource.items.willPickupBeSpawnedIn(object, 3.0)*/);
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
                            multi = 0.6 * GetLoweringHealthMultiplier();
                        }
                        if (object.getType() == UT2004ItemType.U_DAMAGE_PACK) {
                            multi = 0.3;
                        }
                        if (object.getType() == UT2004ItemType.SHIELD_PACK) {
                            multi = 0.7 * GetLoweringHealthMultiplier();
                        }
                        if (object.getType() == UT2004ItemType.SUPER_HEALTH_PACK) {
                            multi = 0.4 * GetLoweringHealthMultiplier();
                        }
                        if (object.getType() == UT2004ItemType.HEALTH_PACK) {
                            multi = 0.45 * GetLoweringHealthMultiplier();

                        }
                        if (object.getType().getCategory() == ItemType.Category.AMMO) {
                            multi = Double.POSITIVE_INFINITY;
                        }
                        return multi * behaviorResource.navMeshModule.getAStarPathPlanner().getDistance(target, object);
                    }

                });
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

    private double GetLoweringHealthMultiplier() {
        return (behaviorResource.info.getHealth() / 100.0);
    }

    private void SendMessageOfStoppingToNavigateToCertainItem() {
        if (lastItemNavigatedTo != null) {
            // we are navigating to different item from lastItem
            behaviorResource.tabooItems.remove(lastItemNavigatedTo);
            behaviorResource.tcClient.sendToTeamOthers(
                    new TcStopNavigatingToItem(behaviorResource.info.getId(), lastItemNavigatedTo.getId()));
        }
    }

    private void InitializeImportantCategories() {
        importantCategories = new ArrayList<>();
        importantCategories.add(ItemType.Category.HEALTH);
        importantCategories.add(ItemType.Category.ARMOR);
    }

}
