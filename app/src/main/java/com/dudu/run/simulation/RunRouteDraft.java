package com.dudu.run.simulation;

import java.util.*;

/** Temporary route for this run only. Never serialized to preferences or files. */
final class RunRouteDraft {
    private static long runId;
    private static SimulationEngine.Point anchor;
    private static List<SimulationEngine.Point> points;
    private static boolean snapped,accepted;
    static void clear(){runId=0;anchor=null;points=null;snapped=false;accepted=false;}
    static void save(SimulationController.RouteEdit edit,List<SimulationEngine.Point> route,boolean isSnapped,boolean isAccepted){
        runId=edit.runId;anchor=edit.anchor;points=new ArrayList<>(route);snapped=isSnapped;accepted=isAccepted;
    }
    static boolean matches(SimulationController.RouteEdit edit){return edit!=null&&points!=null&&edit.runId==runId&&anchor!=null&&SimulationEngine.distance(edit.anchor,anchor)<=.02;}
    static boolean accepted(SimulationController.RouteEdit edit){return matches(edit)&&accepted;}
    static List<SimulationEngine.Point> load(SimulationController.RouteEdit edit){
        if(edit==null||points==null||edit.runId!=runId||SimulationEngine.distance(edit.anchor,anchor)>.02)return Collections.emptyList();
        return new ArrayList<>(points);
    }
    static boolean snapped(SimulationController.RouteEdit edit){return edit!=null&&edit.runId==runId&&anchor!=null&&SimulationEngine.distance(edit.anchor,anchor)<=.02&&snapped;}
}
