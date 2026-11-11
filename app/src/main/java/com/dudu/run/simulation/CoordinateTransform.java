package com.dudu.run.simulation;

/**
 * Offline WGS-84 -> GCJ-02 conversion, ported from wandergis/coordtransform (MIT).
 * https://github.com/wandergis/coordtransform
 * Copyright (c) 2015 记忆的残骸. Full license: third-party-notices/coordtransform-LICENSE.txt.
 */
public final class CoordinateTransform {
    private static final double A=6378245.0,EE=0.00669342162296594323,PI=Math.PI;
    private CoordinateTransform() {}
    public static SimulationEngine.Point fromWgs84(SimulationEngine.Point point) {
        double lon=point.longitude,lat=point.latitude;
        if(!(lon>73.66&&lon<135.05&&lat>3.86&&lat<53.55)) return point;
        double x=lon-105,y=lat-35;
        double dlat=-100+2*x+3*y+.2*y*y+.1*x*y+.2*Math.sqrt(Math.abs(x));
        dlat+=(20*Math.sin(6*x*PI)+20*Math.sin(2*x*PI))*2/3;
        dlat+=(20*Math.sin(y*PI)+40*Math.sin(y/3*PI))*2/3;
        dlat+=(160*Math.sin(y/12*PI)+320*Math.sin(y*PI/30))*2/3;
        double dlon=300+x+2*y+.1*x*x+.1*x*y+.1*Math.sqrt(Math.abs(x));
        dlon+=(20*Math.sin(6*x*PI)+20*Math.sin(2*x*PI))*2/3;
        dlon+=(20*Math.sin(x*PI)+40*Math.sin(x/3*PI))*2/3;
        dlon+=(150*Math.sin(x/12*PI)+300*Math.sin(x/30*PI))*2/3;
        double rad=lat/180*PI,sin=Math.sin(rad),magic=1-EE*sin*sin,root=Math.sqrt(magic);
        dlat=dlat*180/(A*(1-EE)/(magic*root)*PI);
        dlon=dlon*180/(A/root*Math.cos(rad)*PI);
        return new SimulationEngine.Point(lat+dlat,lon+dlon,point.altitude);
    }
}
