package hlaa.duelbot;

import hlaa.duelbot.Behavior.BehaviorManager;
import hlaa.duelbot.Behavior.BehaviorResource;
import java.util.logging.Level;

import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.ObjectClassEventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.object.event.WorldObjectUpdatedEvent;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.base.utils.math.DistanceUtils;
import cz.cuni.amis.pogamut.base.utils.math.DistanceUtils.IGetDistance;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.UT2004Skins;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.NavigationState;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.UT2004ItemType;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotDamaged;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ConfigChange;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.IncomingProjectile;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.InitedMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ItemPickedUp;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Player;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerDamaged;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Self;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.pogamut.ut2004.utils.UnrealUtils;
import cz.cuni.amis.utils.IFilter;
import cz.cuni.amis.utils.collections.MyCollections;
import cz.cuni.amis.utils.exception.PogamutException;
import cz.cuni.amis.utils.flag.FlagListener;
import java.util.LinkedList;
import java.util.List;

@AgentScoped
public class DuelBot extends UT2004BotModuleController {

    private long lastLogicTime = -1;
    private long logicIterationNumber = 0;
    public Player focusedEnemy;
    private int counter = 0;

    
    
    
    private BehaviorManager behaviorManager;

    

    /**
     * Here we can modify initializing command for our bot, e.g., sets its name
     * or skin.
     *
     * @return instance of {@link Initialize}
     */
    @Override
    public Initialize getInitializeCommand() {
        return new Initialize().setName("DuelBot").setSkin(UT2004Skins.getRandomSkin()).setDesiredSkill(6);
    }

    @Override
    public void botInitialized(GameInfo gameInfo, ConfigChange currentConfig, InitedMessage init) {
        bot.getLogger().getCategory("Yylex").setLevel(Level.OFF);

        weaponPrefs.addGeneralPref(UT2004ItemType.LIGHTNING_GUN, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.SHOCK_RIFLE, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.MINIGUN, false);
        weaponPrefs.addGeneralPref(UT2004ItemType.ROCKET_LAUNCHER, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.ASSAULT_RIFLE, true);
        weaponPrefs.addGeneralPref(UT2004ItemType.LINK_GUN, false);
        weaponPrefs.addGeneralPref(UT2004ItemType.SHIELD_GUN, false);

        weaponPrefs.newPrefsRange(400)
                .add(UT2004ItemType.FLAK_CANNON, true)
                .add(UT2004ItemType.LINK_GUN, true)
                .add(UT2004ItemType.ROCKET_LAUNCHER, true);

        weaponPrefs.newPrefsRange(1050)
                .add(UT2004ItemType.LIGHTNING_GUN, true)
                .add(UT2004ItemType.SHOCK_RIFLE, true)
                .add(UT2004ItemType.LINK_GUN, false)
                .add(UT2004ItemType.MINIGUN, false);

       
        behaviorManager = new BehaviorManager(new BehaviorResource(info, navigation));
            }

    @Override
    public void botFirstSpawn(GameInfo gameInfo, ConfigChange config, InitedMessage init, Self self) {
        navigation.addStrongNavigationListener(new FlagListener<NavigationState>() {
            @Override
            public void flagChanged(NavigationState changedValue) {
                navigationStateChanged(changedValue);
            }
        });
    }

    /**
     * The navigation state has changed...
     *
     * @param changedValue
     */
    private void navigationStateChanged(NavigationState changedValue) {
        switch (changedValue) {
            case TARGET_REACHED:
                return;
            case PATH_COMPUTATION_FAILED:
                return;
            case STUCK:
                return;
        }
    }

    @Override
    public void beforeFirstLogic() {
    }

    // ====================
    // BOT MIND MAIN METHOD
    // ====================
    @Override
    public void logic() throws PogamutException {
        if (lastLogicTime < 0) {
            lastLogicTime = System.currentTimeMillis();
            return;
        }

        log.info("---LOGIC: " + (++logicIterationNumber) + " / D=" + (System.currentTimeMillis() - lastLogicTime) + "ms ---");
        lastLogicTime = System.currentTimeMillis();

        // FOLLOWS THE BOT'S LOGIC
        // use Bot Name to visualize high-level state of your bot to ease debugging
        setDebugInfo("BRAIN-DEAD");
        // TODO test if current behavior ended
        behaviorManager.DoLogic();

        /*if (combatBeh()) {
            return;
        }
        fireAtEnemy();
        if (pursueEnemy()) {
            return;
        }
        focusEnemy();
        collectItems();*/
    }

    private boolean combatBeh() {
        if (!players.canSeeEnemies()) {
            return false;
        }
        focusedEnemy = players.getNearestVisibleEnemy();
        focusEnemy();
        fireAtEnemy();
        approachEnemy();
        return true;
    }

    private boolean approachEnemy() {
        if (!players.canSeeEnemies()) {
            return false;
        }
        navigation.navigate(players.getNearestVisibleEnemy());
        return true;
    }

    private boolean pursueEnemy() {
        if (focusedEnemy == null) {
            return false;
        }
        if (info.atLocation(focusedEnemy.getLocation())) {
            focusedEnemy = null;
            return false;
        }
        if (!navigation.isNavigating()) {
            focusedEnemy = null;
            return false;
        }
        navigation.navigate(focusedEnemy);
        return true;
    }

    private boolean focusEnemy() {
        if (focusedEnemy == null) {
            navigation.setFocus(null);
            return false;
        }
        navigation.setFocus(focusedEnemy);
        return true;
    }

    private boolean fireAtEnemy() {
        if (!players.canSeeEnemies()) {
            shoot.stopShooting();
            return false;
        }
        shoot.shoot(weaponPrefs, players.getNearestVisibleEnemy());
        return true;
    }

    private void collectItems() {
        Item item = DistanceUtils.getNearest(
                MyCollections.getFiltered(
                        items.getSpawnedItems().values(),
                        new IFilter<Item>() {
                            @Override
                            public boolean isAccepted(Item object) {
                                // You might try to use this to time pickupus...
                                //items.isPickupSpawned(item)
                                //items.willPickupBeSpawnedIn(item, seconds)
                                //UnrealUtils.CHARACTER_RUN_SPEED // UT_units/sec
                                return items.isPickable(object);
                            }
                        }),
                info.getLocation(),
                new IGetDistance<Item>() {

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
                        return multi * navMeshModule.getAStarPathPlanner().getDistance(target, object);
                    }

                });
        if (item == null) {
            if (!navigation.isNavigating()) {
                navigation.navigate(navPoints.getRandomNavPoint());
            }
        } else {
            navigation.navigate(item);
        }
    }

    // ==============
    // EVENT HANDLERS
    // ==============
    /**
     * You have just picked up some item.
     *
     * @param event
     */
    @EventListener(eventClass = ItemPickedUp.class)
    public void itemPickedUp(ItemPickedUp event) {
        if (info.getSelf() == null) {
            return; // ignore the first equipment...
        }
        Item pickedUp = items.getItem(event.getId());
        if (pickedUp == null) {
            return; // ignore unknown items
        }
    }

    /**
     * YOUR bot has just been damaged.
     *
     * @param event
     */
    @EventListener(eventClass = BotDamaged.class)
    public void botDamaged(BotDamaged event) {
    }

    /**
     * YOUR bot has just been killed.
     */
    @Override
    public void botKilled(BotKilled event) {
        sayGlobal("I was KILLED!");

        navigation.stopNavigation();
        shoot.stopShooting();

        // RESET YOUR MEMORY VARIABLES HERE
    }

    /**
     * Some other BOT has just been damaged by someone (may be even by you).
     *
     * @param event
     */
    @EventListener(eventClass = PlayerDamaged.class)
    public void playerDamaged(PlayerDamaged event) {
    }

    /**
     * Some other BOT has just been killed by someone (may be even by you).
     *
     * @param event
     */
    @EventListener(eventClass = PlayerKilled.class)
    public void playerKilled(PlayerKilled event) {

    }

    @ObjectClassEventListener(eventClass = WorldObjectUpdatedEvent.class, objectClass = IncomingProjectile.class)
    public void incomingProjectile(WorldObjectUpdatedEvent<IncomingProjectile> event) {
        event.getObject().getDirection();
        event.getObject().getSpeed();
    }

    // =========
    // UTILITIES
    // =========
    private void setDebugInfo(String info) {
        bot.getBotName().setInfo(info);
        bot.getBotName().setInfo("#", "" + ++counter);
        log.info(info);
    }

    private void sayGlobal(String msg) {
        // Simple way to send msg into the UT2004 chat
        body.getCommunication().sendGlobalTextMessage(msg);
        // And user log as well
        log.info(msg);
    }

    // ===========
    // MAIN METHOD
    // ===========
    public static void main(String args[]) throws PogamutException {
        new UT2004BotRunner( // class that wrapps logic for bots executions, suitable to run single bot in single JVM
                DuelBot.class, // which UT2004BotController it should instantiate
                "DuelBot" // what name the runner should be using
        ).setMain(true) // tells runner that is is executed inside MAIN method, thus it may block the thread and watch whether agent/s are correctly executed
                .startAgents(2);        // tells the runner to start 2 agent
    }

    

}
