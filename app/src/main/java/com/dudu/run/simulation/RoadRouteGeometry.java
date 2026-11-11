package com.dudu.run.simulation;

import java.util.*;

/** Shape-preserving routing guides and conservative validation; no straight-line fallback. */
public final class RoadRouteGeometry {
    private RoadRouteGeometry(){}
    public static List<Integer> guides(List<SimulationEngine.Point> points){
        if(points.size()<2)throw new IllegalArgumentException("请先画线或选择至少两个点");
        boolean[] keep=new boolean[points.size()];keep[0]=keep[points.size()-1]=true;
        ArrayDeque<int[]> stack=new ArrayDeque<>();stack.push(new int[]{0,points.size()-1});
        while(!stack.isEmpty()){
            if(Thread.currentThread().isInterrupted())throw new java.util.concurrent.CancellationException();
            int[] range=stack.pop();int a=range[0],b=range[1];if(b-a<2)continue;
            double max=0,length=0;int split=-1,half=a+1;
            for(int i=a+1;i<b;i++){
                double d=segmentDistance(points.get(i),points.get(a),points.get(b));
                if(d>max){max=d;split=i;}
            }
            for(int i=a+1;i<=b;i++)length+=SimulationEngine.distance(points.get(i-1),points.get(i));
            if(max<=8&&length<=150)continue;
            if(max<=8){double walked=0;for(int i=a+1;i<b;i++){walked+=SimulationEngine.distance(points.get(i-1),points.get(i));half=i;if(walked>=length/2)break;}split=half;}
            if(split<=a||split>=b)continue;
            keep[split]=true;stack.push(new int[]{a,split});stack.push(new int[]{split,b});
        }
        ArrayList<Integer> result=new ArrayList<>();for(int i=0;i<keep.length;i++)if(keep[i])result.add(i);
        if(result.size()>25)throw new IllegalArgumentException("路线转弯较多，请分几笔画；本次保留原线，不减少转弯点");
        return result;
    }
    public static double length(List<SimulationEngine.Point> points){
        double value=0;for(int i=1;i<points.size();i++)value+=SimulationEngine.distance(points.get(i-1),points.get(i));return value;
    }
    public static double segmentDistance(SimulationEngine.Point p,SimulationEngine.Point a,SimulationEngine.Point b){
        double cos=Math.cos(p.latitude*Math.PI/180),scale=111195;
        double ax=(a.longitude-p.longitude)*cos*scale,ay=(a.latitude-p.latitude)*scale;
        double bx=(b.longitude-p.longitude)*cos*scale,by=(b.latitude-p.latitude)*scale;
        double dx=bx-ax,dy=by-ay,den=dx*dx+dy*dy;
        double t=den==0?0:Math.max(0,Math.min(1,-(ax*dx+ay*dy)/den));return Math.hypot(ax+t*dx,ay+t*dy);
    }
    private static double distanceToLine(SimulationEngine.Point point,List<SimulationEngine.Point> line){
        double best=Double.MAX_VALUE;for(int i=1;i<line.size();i++)best=Math.min(best,segmentDistance(point,line.get(i-1),line.get(i)));return best;
    }
    private static void checkCorridor(List<SimulationEngine.Point> from,List<SimulationEngine.Point> to){
        for(int i=1;i<from.size();i++){
            if(Thread.currentThread().isInterrupted())throw new java.util.concurrent.CancellationException();
            SimulationEngine.Point a=from.get(i-1),b=from.get(i);
            int steps=Math.max(1,(int)Math.ceil(SimulationEngine.distance(a,b)/10));
            if(steps>2000)throw new IllegalArgumentException("道路结果距离异常，已保留原线");
            for(int j=0;j<=steps;j++){
                double t=(double)j/steps;
                SimulationEngine.Point p=new SimulationEngine.Point(a.latitude+(b.latitude-a.latitude)*t,a.longitude+(b.longitude-a.longitude)*t,a.altitude);
                if(distanceToLine(p,to)>35)throw new IllegalArgumentException("附近道路偏离原线超过 35 米，请沿目标道路分段选点；已保留原线");
            }
        }
    }
    public static void validateLeg(List<SimulationEngine.Point> raw,List<SimulationEngine.Point> road){
        if(raw.size()<2||road.size()<2)throw new IllegalArgumentException("没有可用的步行道路，已保留原线");
        if(SimulationEngine.distance(raw.get(0),road.get(0))>25||SimulationEngine.distance(raw.get(raw.size()-1),road.get(road.size()-1))>25)
            throw new IllegalArgumentException("端点离可用道路超过 25 米，请调整端点；已保留原线");
        if(length(road)>Math.max(length(raw)*3,length(raw)+80))throw new IllegalArgumentException("沿路结果绕行过大，请补充转弯点；已保留原线");
        checkCorridor(raw,road);checkCorridor(road,raw);
    }
    public static void appendLeg(List<SimulationEngine.Point> result,List<SimulationEngine.Point> leg){
        int start=0;
        if(!result.isEmpty()){
            if(SimulationEngine.distance(result.get(result.size()-1),leg.get(0))>2)
                throw new IllegalArgumentException("两段道路不连通，请调整转弯点；已保留原线");
            start=1;
        }
        if(result.size()+leg.size()-start>5000)throw new IllegalArgumentException("道路点超过 5000 个，请分段画线；已保留原线");
        result.addAll(leg.subList(start,leg.size()));
    }
}
