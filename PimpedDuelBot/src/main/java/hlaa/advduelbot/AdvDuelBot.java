package hlaa.advduelbot;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import cz.cuni.amis.pathfinding.alg.astar.AStarResult;
import cz.cuni.amis.pathfinding.map.IPFMapView;
import cz.cuni.amis.pogamut.base.agent.navigation.IPathFuture;
import cz.cuni.amis.pogamut.base.agent.navigation.impl.PrecomputedPathFuture;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.EventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.listener.annotation.ObjectClassEventListener;
import cz.cuni.amis.pogamut.base.communication.worldview.object.event.WorldObjectUpdatedEvent;
import cz.cuni.amis.pogamut.base.utils.guice.AgentScoped;
import cz.cuni.amis.pogamut.base.utils.math.DistanceUtils;
import cz.cuni.amis.pogamut.base.utils.math.DistanceUtils.IGetDistance;
import cz.cuni.amis.pogamut.base3d.worldview.object.ILocated;
import cz.cuni.amis.pogamut.base3d.worldview.object.Location;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.ManualControl;
import cz.cuni.amis.pogamut.ut2004.agent.module.sensor.visibility.model.VisibilityLocation;
import cz.cuni.amis.pogamut.ut2004.agent.module.utils.UT2004Skins;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.NavigationState;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.levelGeometry.RayCastResult;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.NavMeshClearanceComputer.ClearanceLimit;
import cz.cuni.amis.pogamut.ut2004.agent.navigation.navmesh.node.NavMeshPolygon;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004Bot;
import cz.cuni.amis.pogamut.ut2004.bot.impl.UT2004BotModuleController;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbcommands.Initialize;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotDamaged;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.BotKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ConfigChange;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.GameInfo;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.IncomingProjectile;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.InitedMessage;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Item;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.ItemPickedUp;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.NavPoint;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerDamaged;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.PlayerKilled;
import cz.cuni.amis.pogamut.ut2004.communication.messages.gbinfomessages.Self;
import cz.cuni.amis.pogamut.ut2004.utils.UT2004BotRunner;
import cz.cuni.amis.utils.exception.PogamutException;
import cz.cuni.amis.utils.flag.FlagListener;
import math.geom2d.Vector2D;

@AgentScoped
public class AdvDuelBot extends UT2004BotModuleController {

	public static final boolean MANUAL_CONTROL = true;
	
	public static final boolean LOAD_LEVEL_GEOMETRY = false;
	
	private long   lastLogicTime        = -1;
    private long   logicIterationNumber = 0; 
    
    private boolean lastManualActive = false;
    
    private ManualControl manualControl;
    
    @Override
    protected void initializeModules(UT2004Bot bot) {
    	super.initializeModules(bot);
    	levelGeometryModule.setAutoLoad(LOAD_LEVEL_GEOMETRY);
    }
    
    /**
     * Here we can modify initializing command for our bot, e.g., sets its name or skin.
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
    	if (MANUAL_CONTROL) {
    		log.warning("INITIALIZING MANUAL CONTROL WINDOW");
    		manualControl = new ManualControl(bot, info, body, levelGeometryModule, draw, navPointVisibility, navMeshModule);
    	}    	
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
     * @param changedValue
     */
    private void navigationStateChanged(NavigationState changedValue) {
    	switch(changedValue) {
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
    
    // ===============
    // BOT SELF UPDATE
    // ===============
        
    private long lastSelfTime;
    
    @ObjectClassEventListener(eventClass=WorldObjectUpdatedEvent.class, objectClass=Self.class)
    public void selfUpdate(WorldObjectUpdatedEvent<Self> selfUpdate) {
    	// WHEN THE BOT IS MOVING, SELF COMES IN 40ms INTERVAL, good for "dodge move" implementation
    	if (lastSelfTime < 0) {
    		lastSelfTime = System.currentTimeMillis();
    		return;
    	}	
    	log.info("---SELF: " + (++logicIterationNumber) + " / D=" + (System.currentTimeMillis() - lastSelfTime) + "ms ---");
    	lastSelfTime = System.currentTimeMillis();
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
    	
    	// MANUAL CONTROL
    	if (manualControl != null && manualControl.isActive()) {
    		if (!lastManualActive) {
    			setDebugInfo("MANUAL CONTROL");
        		lastManualActive = true;
    		}
    		lastLogicTime = System.currentTimeMillis();
    		return;
    	} else {
    		if (lastManualActive) {
    			lastManualActive = false;
    			setDebugInfo(null);
    		}
    	}

    	log.info("---LOGIC: " + (++logicIterationNumber) + " / D=" + (System.currentTimeMillis() - lastLogicTime) + "ms ---");
    	lastLogicTime = System.currentTimeMillis();

    	// FOLLOWS THE BOT'S LOGIC
    	
    	if (players.canSeePlayers()) {
    		// HIDE FROM THE PLAYER!
    		Set<NavPoint> covers = visibility.getCoverNavPointsFrom(players.getNearestVisiblePlayer());
    		NavPoint target = DistanceUtils.getNearest(covers, info.getLocation(), new IGetDistance<NavPoint>() {

				@Override
				public double getDistance(NavPoint object, ILocated target) {
					return navMeshModule.getAStarPathPlanner().getDistance(target, object);
				}
    			
    		});
    		
    		navigation.setFocus(players.getNearestVisiblePlayer());
    		if (navigation.isNavigating()) return;
    		navigation.navigate(target);
    	} else {
    		navigation.setFocus(null);
    	}    	
    	
    	//navigation.getPathExecutor().getPath();
    	//navigation.getPathExecutor().getPathElementIndex();
    	
    	// use Bot Name to visualize high-level state of your bot to ease debugging
    	//setDebugInfo("BRAIN-DEAD");    	
    }
    
    // ===============
    // SENSORY METHODS
    // ===============
    
    public Location getNavMeshLocation(ILocated location) {
    	if (location == null || location.getLocation() == null) return null;
    	NavMeshPolygon nmPoly = navMeshModule.getDropGrounder().tryGround(location);
    	if (nmPoly == null) return null;
    	Location nmLoc = new Location(nmPoly.getShape().project(location.getLocation().asPoint3D()));
    	return nmLoc;    	
    }
    
    /**
     * Returns NULL on "no-hit".
     * @param location
     * @param direction
     * @return
     */
    public ClearanceLimit raycastNavMesh(ILocated location, Location direction) {
    	return raycastNavMesh(location, direction, 1000);
    }
    
    /**
     * Returns NULL on "no-hit".
     * @param location
     * @param direction
     * @return
     */
    public ClearanceLimit raycastNavMesh(ILocated location, Location direction, double maxDistance) {
    	return navMeshModule.getClearanceComputer().findEdge(location.getLocation(), new Vector2D(direction.x, direction.y), maxDistance, 1000);
    }
    
    public RayCastResult raycastGeom(ILocated from, ILocated to) {
    	if (from == null || from.getLocation() == null || to == null || to.getLocation() == null) return null;
    	return levelGeometryModule.getLevelGeometry().rayCast(from.getLocation(), to.getLocation());
    }
    
    public boolean isVisible(ILocated from, ILocated to) { 
    	if (from == null || from.getLocation() == null || to == null || to.getLocation() == null) return false;
    	return visibility.isVisible(from, to);
    }
    
    public double getVisibleEpsilon(ILocated from, ILocated to) {
    	if (from == null || from.getLocation() == null || to == null || to.getLocation() == null) return Double.POSITIVE_INFINITY;
    	VisibilityLocation fromVL = visibility.getNearestVisibilityLocationTo(from);
    	VisibilityLocation toVL = visibility.getNearestVisibilityLocationTo(to);
    	return fromVL.getLocation().sub(from.getLocation()).getLength() + toVL.getLocation().sub(to.getLocation()).getLength();
    }
    
    // ===========================
    // CUSTOM PATH FINDING EXAMPLE
    // ===========================
    
    /**
     * BEWARE, cannot be called every frame
     * @param customPath
     */
    public void navigateAlongPath(List<NavPoint> customPath) {
    	IPathFuture pathFuture = new PrecomputedPathFuture<ILocated>(customPath.get(0), customPath.get(customPath.size()-1), (List)customPath);

    	// VISUALIZE THE PATH
    	draw.clearAll();
    	draw.drawPath(pathFuture);
    	
    	// smooth-in-path-points
    	navigation.navigate(pathFuture);
    }
    
    public List<NavPoint> customPathFinding(ILocated from, ILocated to) {
    	NavPoint fromNP = navPoints.getNearestNavPoint(from);
    	NavPoint toNP = navPoints.getNearestNavPoint(to);
    	
    	AStarResult<NavPoint> result = 
	    	aStar.findPath(fromNP, toNP, new IPFMapView<NavPoint>() {
	
				@Override
				public Collection<NavPoint> getExtraNeighbors(NavPoint node, Collection<NavPoint> mapNeighbors) {
					return null;
				}
	
				@Override
				public int getNodeExtraCost(NavPoint node, int mapCost) {
					return 0; // return EXTRA cost ONLY
				}
	
				@Override
				public int getArcExtraCost(NavPoint nodeFrom, NavPoint nodeTo, int mapCost) {
					return 0; // return EXTRA cost ONLY
				}
	
				@Override
				public boolean isNodeOpened(NavPoint node) {
					return true;
				}
	
				@Override
				public boolean isArcOpened(NavPoint nodeFrom, NavPoint nodeTo) {
					return true;
				}
			});
    	
    	if (result.success) {
    		return result.getPath();
    	}
    	return null;
    }
    
    
    // ==============
    // EVENT HANDLERS
    // ==============
    
    /**
     * You have just picked up some item.
     * @param event
     */
    @EventListener(eventClass=ItemPickedUp.class)
    public void itemPickedUp(ItemPickedUp event) {
    	if (info.getSelf() == null) return; // ignore the first equipment...
    	Item pickedUp = items.getItem(event.getId());
    	if (pickedUp == null) return; // ignore unknown items
    }
    
    /**
     * YOUR bot has just been damaged.
     * @param event
     */
    @EventListener(eventClass=BotDamaged.class)
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
     * @param event
     */
    @EventListener(eventClass=PlayerDamaged.class)
    public void playerDamaged(PlayerDamaged event) {
    }
    
    /**
     * Some other BOT has just been killed by someone (may be even by you).
     * @param event
     */
    @EventListener(eventClass=PlayerKilled.class)
    public void playerKilled(PlayerKilled event) {    	
    }
    
    @ObjectClassEventListener(eventClass=WorldObjectUpdatedEvent.class, objectClass=IncomingProjectile.class)
    public void incomingProjectile(WorldObjectUpdatedEvent<IncomingProjectile> event) {
    	  //event.getObject().getDirection();
    	  //event.getObject().getSpeed();
    }

    // =========
    // UTILITIES
    // =========
    
    private void setDebugInfo(String info) {
    	bot.getBotName().setInfo(info);
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
        new UT2004BotRunner(     // class that wrapps logic for bots executions, suitable to run single bot in single JVM
                AdvDuelBot.class,   // which UT2004BotController it should instantiate
                "DuelBot"        // what name the runner should be using
        ).setMain(true)          // tells runner that is is executed inside MAIN method, thus it may block the thread and watch whether agent/s are correctly executed
         .startAgents(1);        // tells the runner to start 2 agent
    }
}
