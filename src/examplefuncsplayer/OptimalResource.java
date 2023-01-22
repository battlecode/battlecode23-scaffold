package examplefuncsplayer;
import battlecode.common.GameConstants;

public class OptimalResource {
    public static int getOptimalResourceCount(int distance, boolean isUpgradedWell){
        int resourceGatheringRate = isUpgradedWell ? GameConstants.WELL_ACCELERATED_RATE : GameConstants.WELL_STANDARD_RATE;
        double bestRate = 0;
        int bestAmount = 1;
        for (int m = 1; m < GameConstants.CARRIER_CAPACITY; m++){
            int numTurns = 0;
            //number of turns to get from HQ to well
            numTurns += numTurns(distance, getCarrierMovementCooldown(0), 1);
            //number of turns to gather m amount of resources
            numTurns += numTurns(m, 8, resourceGatheringRate);
            //number of turns to get from well to HQ
            numTurns += numTurns(distance, getCarrierMovementCooldown(m), 1);
            //rate = total number of resources/distance
            double curRate = ((double) m)/numTurns;
            if (curRate > bestRate){
                bestRate = curRate;
                bestAmount = m;
            }
        }
        return bestAmount; /**Binary Search Instead?*/
    }
    public static int getCarrierMovementCooldown(int amount){
        return (int) (GameConstants.CARRIER_MOVEMENT_INTERCEPT+ GameConstants.CARRIER_MOVEMENT_SLOPE*amount);
    }
    public static int numTurns(int distance, int cooldown, int increment){
        int numTurns = 1;
        int curDistance = 0;
        int curCD = 0;
        while (curDistance != distance){
            if (curCD < 10){
                curDistance += increment;
                curCD += cooldown;
            }
            else{
                numTurns++;
                curCD -= 10;
            }
        }
        return numTurns;
    }
}