package examplefuncsplayer;

import battlecode.common.*;

public class LauncherStrategy {

    /**
     * Run a single turn for a Launcher.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    /**All the direction stuff, moved to pathing!*/
//    static Direction previousDir = null; /**SEAN'S EDIT, INSTEAD OF MOVING TOWARD WELL, LAUNCHERS WILL MOVE PSEUDO-RANDOM BUT WILL NOT RETRACE THEIR MOST RECENT STEP*/
    static void runLauncher(RobotController rc) throws GameActionException {
        // Try to attack someone
        int radius = rc.getType().actionRadiusSquared;
        Team opponent = rc.getTeam().opponent();
        RobotInfo[] enemies = rc.senseNearbyRobots(radius, opponent);
        int lowestHealth = 1000;
        int smallestDistance = 100;
        RobotInfo target = null;
        if (RobotPlayer.turnCount == 2) {
            Communication.updateHeadquarterInfo(rc);
        }
        Communication.clearObsoleteEnemies(rc);
        if (enemies.length > 0) {
            for (RobotInfo enemy: enemies){
                Communication.reportEnemy(rc, enemy.location);
                int enemyHealth = enemy.getHealth();
                int enemyDistance = enemy.getLocation().distanceSquaredTo(rc.getLocation());
                if (enemyHealth < lowestHealth){
                    target = enemy;
                    lowestHealth = enemyHealth;
                    smallestDistance = enemyDistance;
                }
                else if (enemyHealth == lowestHealth){
                    if (enemyDistance < smallestDistance){
                        target = enemy;
                        smallestDistance = enemyDistance;
                    }
                }
            }
        }
        Communication.tryWriteMessages(rc);
        if (target != null){
            if (rc.canAttack(target.getLocation()))
                rc.attack(target.getLocation());
            Pathing.moveTowards(rc, target.location);
        }
        else {
            RobotInfo[] allies = rc.senseNearbyRobots(9, rc.getTeam());
            int lowestID = rc.getID();
            MapLocation leaderPos = null;
            for (RobotInfo ally : allies){
                if (ally.getType() != RobotType.LAUNCHER)
                    continue;
                if (ally.getID() < lowestID){
                    lowestID = ally.getID();
                    leaderPos = ally.getLocation();
                }
            }
            if (leaderPos != null){
                Pathing.moveTowards(rc, leaderPos);
                rc.setIndicatorString("Following " + lowestID);
            }
            else{
                Pathing.moveRandomNoBacktrack(rc);
                rc.setIndicatorString("I'm the leader!");
            }
//            if(previousDir ==null) { /**SEANS EDIT: If you havent moved yet, this will choose randomly*/
//                Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
//                if (rc.canMove(dir)) {
//                    rc.move(dir);
//                    previousDir = dir;
//                }
//            }
//            else { /**If you have moved, prevents you from going backwards*/
//                int directionIndex;
//                for(directionIndex = 0; directionIndex<RobotPlayer.directions.length; directionIndex++) {
//                   if (RobotPlayer.directions[directionIndex] ==previousDir){
//                       break;
//                   }
//                }
//                System.out.println(directionIndex);
//                //int directionIndex = RobotPlayer.directions.indexOf(previousDir);//i dont think you can use the indexOf method since its not an arrayList
//                Direction forbidden = RobotPlayer.directions[(directionIndex+4)%8];
//                //Direction forbidden = previousDir;
//                while (true) {
//                    Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
//                    if(dir!=forbidden && rc.canMove(dir)){
//                        rc.move(dir);
//                        previousDir = dir;
//                        break;
//                    }
//                }
//            }
            /**WellInfo[] wells = rc.senseNearbyWells();
            if (wells.length > 0){
                MapLocation wellLoc = wells[0].getMapLocation();
                Direction dir = rc.getLocation().directionTo(wellLoc);
                if (rc.canMove(dir))
                    rc.move(dir);
            }*/
        }

        RobotInfo[] visibleEnemies = rc.senseNearbyRobots(-1, opponent);
        for (RobotInfo enemy : visibleEnemies) {
            if (enemy.getType() != RobotType.HEADQUARTERS) {
                MapLocation enemyLocation = enemy.getLocation();
                MapLocation robotLocation = rc.getLocation();
                Direction moveDir = robotLocation.directionTo(enemyLocation);
                if (rc.canMove(moveDir)) {
                    rc.move(moveDir);
                }
            }
        }

        // Also try to move randomly.
        Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
        if (rc.canMove(dir)) {
            rc.move(dir);
        }
    }
}