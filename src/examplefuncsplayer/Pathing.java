package examplefuncsplayer;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Pathing {
    // Basic bug nav - Bug 0

    static Direction currentDirection = null;
    static Direction previousDir = null;
    static void moveRandomNoBacktrack(RobotController rc) throws GameActionException {
        if(previousDir ==null) { /**SEANS EDIT: If you havent moved yet, this will choose randomly*/
            Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
            if (rc.canMove(dir)) {
                rc.move(dir);
                previousDir = dir;
            }
        }
        else { /**If you have moved, prevents you from going backwards*/
            int directionIndex;
            for(directionIndex = 0; directionIndex<RobotPlayer.directions.length; directionIndex++) {
                if (RobotPlayer.directions[directionIndex] ==previousDir){
                    break;
                }
            }
            System.out.println(directionIndex);
            //int directionIndex = RobotPlayer.directions.indexOf(previousDir);//i dont think you can use the indexOf method since its not an arrayList
            Direction forbidden = RobotPlayer.directions[(directionIndex+4)%8];
            //Direction forbidden = previousDir;
            while (true) {
                Direction dir = RobotPlayer.directions[RobotPlayer.rng.nextInt(RobotPlayer.directions.length)];
                if(dir!=forbidden && rc.canMove(dir)){
                    rc.move(dir);
                    previousDir = dir;
                    break;
                }
            }
        }
    }
    static void moveTowards(RobotController rc, MapLocation target) throws GameActionException {
        if (rc.getLocation().equals(target)) {
            return;
        }
        if (!rc.isMovementReady()) {
            return;
        }
        Direction d = rc.getLocation().directionTo(target);
        if (rc.canMove(d)) {
            rc.move(d);
            currentDirection = null; // there is no obstacle we're going around
        } else {
            // Going around some obstacle: can't move towards d because there's an obstacle there
            // Idea: keep the obstacle on our right hand

            if (currentDirection == null) {
                currentDirection = d;
            }
            // Try to move in a way that keeps the obstacle on our right
            for (int i = 0; i < 8; i++) {
                if (rc.canMove(currentDirection)) {
                    rc.move(currentDirection);
                    currentDirection = currentDirection.rotateRight();
                    break;
                } else {
                    currentDirection = currentDirection.rotateLeft();
                } /**Does this break with moving obsticles?? Also, lets try to impliment bug 1 or bug 2*/
            }
        }
    }
}