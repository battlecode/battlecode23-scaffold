package examplefuncsplayer;

import battlecode.common.*;

public class CarrierStrategy {

    static MapLocation hqLoc;
    static MapLocation wellLoc;
    static MapLocation islandLoc;

    static boolean anchorMode = false;
    static int numHeadquarters = 0;

    /**
     * Run a single turn for a Carrier.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runCarrier(RobotController rc) throws GameActionException {
        if (RobotPlayer.turnCount == 2) {
            Communication.updateHeadquarterInfo(rc);
        }
        if(hqLoc == null) scanHQ(rc);
        if(wellLoc == null) scanWells(rc);
        scanIslands(rc);

        //Collect from well if close and inventory not full
        if(wellLoc != null && rc.canCollectResource(wellLoc, -1)) rc.collectResource(wellLoc, -1);

        //Transfer resource to headquarters
        depositResource(rc, ResourceType.ADAMANTIUM);
        depositResource(rc, ResourceType.MANA);

        if(rc.canTakeAnchor(hqLoc, Anchor.STANDARD)) {
            rc.takeAnchor(hqLoc, Anchor.STANDARD);
            anchorMode = true;
        }

        //no resources -> look for well
        if(anchorMode) {
            if(islandLoc == null) {
                for (int i = Communication.STARTING_ISLAND_IDX; i < Communication.STARTING_ISLAND_IDX + GameConstants.MAX_NUMBER_ISLANDS; i++) {
                    MapLocation islandNearestLoc = Communication.readIslandLocation(rc, i);
                    if (islandNearestLoc != null) {
                        islandLoc = islandNearestLoc;
                        break;
                    }
                }
            }
            else RobotPlayer.moveTowards(rc, islandLoc);

            if(rc.canPlaceAnchor() && rc.senseTeamOccupyingIsland(rc.senseIsland(rc.getLocation())) == Team.NEUTRAL) {
                rc.placeAnchor();
                anchorMode = false;
            }
        }
        else {
            int total = getTotalResources(rc);
            if(total == 0) {
                //move towards well or search for well
                if(wellLoc == null) RobotPlayer.moveRandom(rc);
                else if(!rc.getLocation().isAdjacentTo(wellLoc)) RobotPlayer.moveTowards(rc, wellLoc);
            }
            if(total == GameConstants.CARRIER_CAPACITY) {
                //move towards HQ
                RobotPlayer.moveTowards(rc, hqLoc);
            }
        }
        Communication.tryWriteMessages(rc);
    }

    static void scanHQ(RobotController rc) throws GameActionException {
        RobotInfo[] robots = rc.senseNearbyRobots();
        for(RobotInfo robot : robots) {
            if(robot.getTeam() == rc.getTeam() && robot.getType() == RobotType.HEADQUARTERS) {
                hqLoc = robot.getLocation();
                break;
            }
        }
    }

    static void scanWells(RobotController rc) throws GameActionException {
        WellInfo[] wells = rc.senseNearbyWells();
        if(wells.length > 0) wellLoc = wells[0].getMapLocation();
    }

    static void depositResource(RobotController rc, ResourceType type) throws GameActionException {
        int amount = rc.getResourceAmount(type);
        if(amount > 0) {
            if(rc.canTransferResource(hqLoc, type, amount)) rc.transferResource(hqLoc, type, amount);
        }
    }

    static int getTotalResources(RobotController rc) {
        return rc.getResourceAmount(ResourceType.ADAMANTIUM)
                + rc.getResourceAmount(ResourceType.MANA)
                + rc.getResourceAmount(ResourceType.ELIXIR);
    }

    static void scanIslands(RobotController rc) throws GameActionException {
        int[] ids = rc.senseNearbyIslands();
        for(int id : ids) {
            if(rc.senseTeamOccupyingIsland(id) == Team.NEUTRAL) {
                MapLocation[] locs = rc.senseNearbyIslandLocations(id);
                if(locs.length > 0) {
                    islandLoc = locs[0];
                    break;
                }
            }
            Communication.updateIslandInfo(rc, id);
        }
    }
}