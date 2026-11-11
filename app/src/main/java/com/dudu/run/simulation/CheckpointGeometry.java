package com.dudu.run.simulation;

import java.util.List;

/** Shared client radius for actual samples and planned-route coverage. */
public final class CheckpointGeometry {
    public static final double RADIUS_METERS=35;
    private CheckpointGeometry(){}
    public static boolean near(SimulationEngine.Point checkpoint,SimulationEngine.Point from,SimulationEngine.Point to){
        return RoadRouteGeometry.segmentDistance(checkpoint,from==null?to:from,to)<=RADIUS_METERS;
    }
    public static boolean covered(SimulationEngine.Point checkpoint,List<SimulationEngine.Point> route){
        if(route.isEmpty())return false;
        if(route.size()==1)return near(checkpoint,null,route.get(0));
        for(int i=1;i<route.size();i++)if(near(checkpoint,route.get(i-1),route.get(i)))return true;
        return false;
    }
}
