package examplefuncsplayer;

import battlecode.common.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * RobotPlayer is the class that describes your main robot strategy.
 * The run() method inside this class is like your main function: this is what we'll call once your robot
 * is created!
 */
public strictfp class RobotPlayer {

    // Sean is a cutie.
    /**
     * We will use this variable to count the number of turns this robot has been alive.
     * You can use static variables like this to save any information you want. Keep in mind that even though
     * these variables are static, in Battlecode they aren't actually shared between your robots.
     */
    static int turnCount = 0;

    /**
     * A random number generator.
     * We will use this RNG to make some random moves. The Random class is provided by the java.util.Random
     * import at the top of this file. Here, we *seed* the RNG with a constant number (6147); this makes sure
     * we get the same sequence of numbers every time this code is run. This is very useful for debugging!
     */
    static final Random rng = new Random(6147);
    static final int ad_standard = 100; //adamantium, mana, and elixir requirements to build anchors
    static final int man_standard = 100;
    static final int elix_accel = 300;

    /** Array containing all the possible movement directions. */
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };
    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * It is like the main function for your robot. If this method returns, the robot dies!
     *
     * @param rc  The RobotController object. You use it to perform actions from this robot, and to get
     *            information on its current status. Essentially your portal to interacting with the world.
     **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // Hello world! Standard output is very useful for debugging.
        // Everything you say here will be directly viewable in your terminal when you run a match!
        System.out.println("I'm a " + rc.getType() + " and I just got created! I have health " + rc.getHealth());

        // You can also use indicators to save debug notes in replays.
        rc.setIndicatorString("Hello world!");

        while (true) {
            // This code runs during the entire lifespan of the robot, which is why it is in an infinite
            // loop. If we ever leave this loop and return from run(), the robot dies! At the end of the
            // loop, we call Clock.yield(), signifying that we've done everything we want to do.

            turnCount += 1;  // We have now been alive for one more turn!

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode.
            try {
                // The same run() function is called for every robot on your team, even if they are
                // different types. Here, we separate the control depending on the RobotType, so we can
                // use different strategies on different robots. If you wish, you are free to rewrite
                // this into a different control structure!
                switch (rc.getType()) {
                    case HEADQUARTERS:     runHeadquarters(rc);  break;
                    case CARRIER: CarrierStrategy.runCarrier(rc);   break;
                    case LAUNCHER: LauncherStrategy.runLauncher(rc); break;
                    case BOOSTER: // Examplefuncsplayer doesn't use any of these robot types below.
                    case DESTABILIZER: // You might want to give them a try!
                    case AMPLIFIER:       break;
                }

            } catch (GameActionException e) {
                // Oh no! It looks like we did something illegal in the Battlecode world. You should
                // handle GameActionExceptions judiciously, in case unexpected events occur in the game
                // world. Remember, uncaught exceptions cause your robot to explode!
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } catch (Exception e) {
                // Oh no! It looks like our code tried to do something bad. This isn't a
                // GameActionException, so it's more likely to be a bug in our code.
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();

            } finally {
                // Signify we've done everything we want to do, thereby ending our turn.
                // This will make our code wait until the next turn, and then perform this loop again.
                Clock.yield();
            }
            // End of loop: go back to the top. Clock.yield() has ended, so it's time for another turn!
        }

        // Your code should never reach here (unless it's intentional)! Self-destruction imminent...
    }

    /**
     * Run a single turn for a Headquarters.
     * This code is wrapped inside the infinite loop in run(), so it is called once per turn.
     */
    static void runHeadquarters(RobotController rc) throws GameActionException {
        // Pick a direction to build in.
        Direction dir = directions[rng.nextInt(directions.length)];
        MapLocation newLoc = rc.getLocation().add(dir);
        if (turnCount == 1) {
            Communication.addHeadquarter(rc);
        } else if (turnCount == 2) {
            Communication.updateHeadquarterInfo(rc);
        }

        /*
        standard anchors - max health of 250, need 100kg of adamantium + 100kg of mana
        accelerating anchors - need 300kg of elixir, can't stack

        other things we can make with elixir - temporal destabilizers (attack square within 13 units) (200 kg of elixir)
        anchor health changes every  turn: (%island occupied by placing team)-(%island occupied
        by opponent)
        standard anchors heal by 1, accelerating anchors heal by 2. allied robots don't get healed
        when they lose control of an island

        if don't control island and can place anchor, do it.
        teams can place anchor on otp of island they already control, will override last anchor.
        It'll be at full health, regardless of health of past anchor.
        */
        int[] near_islands = rc.senseNearbyIslands();
        if (rc.canBuildAnchor(Anchor.ACCELERATING) && rc.getResourceAmount(ResourceType.ELIXIR) > elix_accel) {
            // build accelerating anchor if team is controlling nearby island, still working
            int r_index = rng.nextInt(near_islands.length);
            if (near_islands.length >= 1 && rc.senseTeamOccupyingIsland(near_islands[r_index]).equals(rc.getTeam().opponent())) {
                rc.setIndicatorString("Building Accelerating Anchor! " + rc.getNumAnchors(Anchor.ACCELERATING));
                rc.buildAnchor(Anchor.ACCELERATING); //would only want accelerating anchor if we're losing
            }
        }
        if (rc.canBuildAnchor(Anchor.STANDARD)) {
            int num_anchors = 1;
            while (rc.getResourceAmount(ResourceType.ADAMANTIUM) > ad_standard && rc.getResourceAmount(ResourceType.MANA) > man_standard && num_anchors <= 5) { //build up to 5 standard anchors
                rc.setIndicatorString("Building Standard Anchor! " + rc.getNumAnchors(Anchor.STANDARD));
                rc.buildAnchor(Anchor.STANDARD);
                num_anchors++;
            }
        }
        if (rng.nextInt(3)==0) { /**1/3 chance of building a carrier, else it will build 5 launchers*/
            // Let's try to build a carrier.
            rc.setIndicatorString("Trying to build a carrier");
            if (rc.canBuildRobot(RobotType.CARRIER, newLoc)) {
                rc.buildRobot(RobotType.CARRIER, newLoc);
            }
        } else {
            // Let's try to build a launcher.
            if (rc.getActionCooldownTurns() != 0 || rc.getResourceAmount(ResourceType.MANA) < 5 * RobotType.LAUNCHER.buildCostMana) /**SEAN'S NOTE: This second condition is do we not have much mana compared to the amount it takes to build a launcher, can wiggle these parameters*/
                return;
            int attempts = 0;
            int numPlaced = 0;
            while (numPlaced != 5 && attempts != 30){
                attempts++;
                if (rc.canBuildRobot(RobotType.LAUNCHER, newLoc)) {
                    rc.buildRobot(RobotType.LAUNCHER, newLoc);
                    numPlaced++;
                }
                else{
                    dir = directions[rng.nextInt(directions.length)];
                    newLoc = rc.getLocation().add(dir);
                }
            }
            rc.setIndicatorString("Trying to build a launcher");
        }
        Communication.tryWriteMessages(rc);

    }

    static void moveRandom(RobotController rc) throws GameActionException {
        Direction dir = directions[rng.nextInt(directions.length)];
        if(rc.canMove(dir)) rc.move(dir);
    }

    static void moveTowards(RobotController rc, MapLocation loc) throws GameActionException{
        Direction dir = rc.getLocation().directionTo(loc);
        if(rc.canMove(dir)) rc.move(dir);
        //else moveRandom(rc);
        /**SEAN: WHAT HAPPENS IF WE COMMENT THIS OUT?? WE WANT IT TO THROW A GAME ACTION EXCEPTION IN ORDER FOR PATHING TO WORK?**/
    }

}