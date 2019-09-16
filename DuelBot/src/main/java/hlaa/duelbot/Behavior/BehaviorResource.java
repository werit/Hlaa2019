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

import cz.cuni.amis.pogamut.base.utils.logging.LogCategory;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.AgentInfo;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Items;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.NavPoints;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.Players;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.WeaponPrefs;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.IUT2004Navigation;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.NavMeshModule;
import cz.cuni.amis.pogamut.ut2004.bot.command.ImprovedShooting;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;

/**
 *
 * @author msi
 */
public class BehaviorResource {

    public Player focusedEnemy;
    
    public final LogCategory log;
    public final AgentInfo info;
    public final IUT2004Navigation navigation;    
    public final NavPoints navPoints;
    public final NavMeshModule navMeshModule;
    public final ImprovedShooting shoot;
    public final WeaponPrefs weaponPrefs;
    public final Players players;
    public final Items items;

    public BehaviorResource(LogCategory log,
            AgentInfo info, IUT2004Navigation navigation, 
            NavPoints navPoints, NavMeshModule navMeshModule, 
            ImprovedShooting shoot, WeaponPrefs weaponPrefs, 
            Players players, Items items) {
        this.log = log;
        this.info = info;
        this.navigation = navigation;
        this.navPoints = navPoints;
        this.navMeshModule = navMeshModule;
        this.shoot = shoot;
        this.weaponPrefs = weaponPrefs;
        this.players = players;
        this.items = items;
        
    }
}
