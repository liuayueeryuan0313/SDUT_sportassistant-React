package com.dudu.run.simulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Route playback; SI units except explicitly named km/h values. */
public final class SimulationEngine {
    public static final class Point {
        public final double latitude, longitude, altitude;
        public Point(double lat, double lon, double alt) {
            if (!finite(lat) || !finite(lon) || !finite(alt) || Math.abs(lat)>85
                    || Math.abs(lon)>180 || alt < -500 || alt>9000)
                throw new IllegalArgumentException("坐标范围：纬度 ±85，经度 ±180，高度 -500～9000 米");
            latitude=lat; longitude=lon; altitude=alt;
        }
    }
    public static final class Config {
        public final List<Point> points;
        public final double speedKmh, speedVariationKmh, cadence, cadenceVariation, altitudeVariation, routeOffsetMeters;
        public static final double DEFAULT_ROUTE_OFFSET_METERS=1.2;
        public final boolean loop, fixed;
        public Config(List<Point> points,double speed,double variation,double cadence,
                      double cadenceVariation,double altitudeVariation,boolean loop,boolean fixed) {
            this(points,speed,variation,cadence,cadenceVariation,altitudeVariation,0,loop,fixed);
        }
        public Config(List<Point> points,double speed,double variation,double cadence,
                      double cadenceVariation,double altitudeVariation,double routeOffsetMeters,boolean loop,boolean fixed) {
            if(!finite(routeOffsetMeters)||routeOffsetMeters<0||routeOffsetMeters>5)
                throw new IllegalArgumentException("路线横向偏移为 0～5 米，0 表示关闭");
            if(points==null || points.isEmpty() || points.size()>5000 || (!fixed && points.size()<2))
                throw new IllegalArgumentException("定点至少 1 个坐标；路线至少 2 个坐标，最多 5000 个");
            if(!finite(speed) || speed<1 || speed>30 || !finite(variation)
                    || variation<0 || variation>Math.min(5,speed*.5)
                    || !finite(cadence) || cadence<80 || cadence>220
                    || !finite(cadenceVariation) || cadenceVariation<0 || cadenceVariation>20
                    || !finite(altitudeVariation) || altitudeVariation<0 || altitudeVariation>10)
                throw new IllegalArgumentException("速度 1～30；速度波动≤5且≤均速一半；步频 80～220；步频波动 0～20；高度波动 0～10");
            List<Point> clean=new ArrayList<>();
            for(Point p:points) {
                if(p==null) throw new IllegalArgumentException("坐标不可为空");
                if(clean.isEmpty() || distance(clean.get(clean.size()-1),p)>=.0001) clean.add(p);
            }
            if(!fixed && clean.size()<2) throw new IllegalArgumentException("路线点距离太近");
            this.points=Collections.unmodifiableList(clean); speedKmh=speed; speedVariationKmh=variation;
            this.cadence=cadence; this.cadenceVariation=cadenceVariation;
            this.altitudeVariation=altitudeVariation; this.routeOffsetMeters=routeOffsetMeters; this.loop=loop; this.fixed=fixed;
        }
    }
    public static final class Sample {
        public final Point point;
        public final double speedKmh,cadence,distanceMeters,elapsedSeconds,bearing;
        public final long steps;
        public final boolean finished;
        Sample(Point point,double speed,double cadence,double distance,double elapsed,double bearing,long steps,boolean finished) {
            this.point=point; speedKmh=speed; this.cadence=cadence; distanceMeters=distance;
            elapsedSeconds=elapsed; this.bearing=bearing; this.steps=steps; this.finished=finished;
        }
    }
    private Config config;
    private final List<Point> path;
    private final double[] lengths,cumulative;
    private final double phase;
    private int segment;
    private double offset,speed,cadence,steps,elapsed,distance,travelled;
    private double offsetHeading=Double.NaN;
    private boolean finished;
    private Sample sample;
    public SimulationEngine(Config config,long seed) {
        this.config=config; path=new ArrayList<>(config.points);
        if(!config.fixed && config.loop && distance(path.get(0),path.get(path.size()-1))>=.2)
            path.add(path.get(0)); // Traverse the closing leg instead of teleporting.
        lengths=new double[Math.max(0,path.size()-1)];
        cumulative=new double[path.size()];
        for(int i=0;i<lengths.length;i++) {
            lengths[i]=distance(path.get(i),path.get(i+1));
            cumulative[i+1]=cumulative[i]+lengths[i];
        }
        phase=new Random(seed).nextDouble()*Math.PI*2;
        sample=new Sample(path.get(0),0,0,0,0,0,0,false);
    }
    public Sample current() { return sample; }
    /** Continue the same recording on a new open route, without rewinding position or totals. */
    public SimulationEngine continueOn(Config next,long seed,double recordedSeconds,double recordedMeters){
        if(next.fixed||next.loop)throw new IllegalArgumentException("本次跑步需要单程运动路线");
        if(!finite(recordedSeconds)||!finite(recordedMeters)||recordedSeconds<0||recordedMeters<0)
            throw new IllegalArgumentException("跑步累计数值无效");
        if(distance(sample.point,next.points.get(0))>.02)
            throw new IllegalArgumentException("新路线必须连接当前保持点");
        ArrayList<Point> exact=new ArrayList<>(next.points);exact.set(0,sample.point);
        Config anchored=new Config(exact,next.speedKmh,next.speedVariationKmh,next.cadence,
                next.cadenceVariation,next.altitudeVariation,next.routeOffsetMeters,false,false);
        SimulationEngine result=new SimulationEngine(anchored,seed);
        result.elapsed=Math.max(elapsed,recordedSeconds);result.distance=Math.max(distance,recordedMeters);result.steps=steps;
        result.sample=new Sample(sample.point,0,0,result.distance,result.elapsed,sample.bearing,(long)steps,false);
        return result;
    }
    public void pause() {
        speed=0; cadence=0;
        sample=new Sample(sample.point,0,0,distance,elapsed,sample.bearing,(long)steps,finished);
    }
    public Config configuration(){return config;}
    /** Adjust only speed targets; retain phase, sample, timers, steps and path progress. */
    public void updateSpeed(double speedKmh,double variation){
        config=new Config(config.points,speedKmh,variation,config.cadence,config.cadenceVariation,
            config.altitudeVariation,config.routeOffsetMeters,config.loop,config.fixed);
    }
    /** Current held point followed by all unfinished vertices, in the original order. */
    public List<Point> remainingRoute(){
        ArrayList<Point> remaining=new ArrayList<>();remaining.add(sample.point);
        if(!config.fixed&&!finished)for(int i=segment+1;i<path.size();i++){
            if(distance(remaining.get(remaining.size()-1),path.get(i))>.0001)remaining.add(path.get(i));
        }
        return Collections.unmodifiableList(remaining);
    }
    public Sample advance(double seconds) {
        if(!finite(seconds) || seconds<0 || seconds>5) throw new IllegalArgumentException("单次推进必须在 0～5 秒之间");
        double left=seconds;
        while(left>.000001 && !finished) { double dt=Math.min(.1,left); tick(dt); left-=dt; }
        return sample;
    }
    private void tick(double dt) {
        elapsed+=dt;
        double wave=.65*Math.sin(elapsed/13+phase)+.35*Math.sin(elapsed/5.7+phase*.37);
        double target=config.fixed?0:(config.speedKmh+config.speedVariationKmh*wave)/3.6;
        if(!config.fixed && lengths.length>1 && segment<lengths.length-1) {
            double turn=Math.abs(angle(bearing(path.get(segment),path.get(segment+1)),bearing(path.get(segment+1),path.get(segment+2))));
            double near=Math.max(0,1-(lengths[segment]-offset)/12);
            target*=1-Math.min(.3,turn/180*.3)*near;
        }
        if(!config.fixed && !config.loop)
            target=Math.min(target,Math.sqrt(Math.max(0,2*.3*(cumulative[cumulative.length-1]-cumulative[segment]-offset))));
        double oldSpeed=speed; speed=approach(speed,target,.3*dt);
        double travel=(oldSpeed+speed)*.5*dt,moved=0;
        if(!config.fixed) while(travel>0 && !finished) {
            double amount=Math.min(travel,lengths[segment]-offset);
            offset+=amount; travel-=amount; moved+=amount;
            if(offset>=lengths[segment]-1e-8) {
                if(segment==lengths.length-1) {
                    if(config.loop) { segment=0; offset=0; }
                    else { finished=true; speed=0; }
                } else { segment++; offset=0; }
            }
        }
        travelled+=moved;
        double warmup=Math.min(1,speed*3.6/Math.max(1,config.speedKmh-config.speedVariationKmh));
        double desired=config.fixed||finished?0:Math.max(0,warmup*(config.cadence
                +config.cadenceVariation*(.6*wave+.4*Math.sin(elapsed/8.3+phase))));
        double oldCadence=cadence;
        cadence=config.fixed||finished?0:approach(cadence,desired,24*dt);
        steps+=(oldCadence+cadence)*.5*dt/60;
        Point position; double heading=0;
        if(config.fixed) position=path.get(0);
        else {
            Point a=path.get(segment),b=path.get(segment+1);
            position=interpolate(a,b,finished?1:offset/lengths[segment]); heading=bearing(a,b);
        }
        // Offset follows distance, so a pause cannot create a sideways position jump.
        if(!config.fixed&&config.routeOffsetMeters>0&&!finished){
            double along=cumulative[segment]+offset,total=cumulative[cumulative.length-1];
            double envelope=smooth(Math.min(travelled,8)/8);
            if(!config.loop)envelope*=smooth(Math.min(Math.max(0,total-along),8)/8);
            double lateral=config.routeOffsetMeters*envelope*(.65*Math.sin(travelled/12+phase)
                    +.35*Math.sin(travelled/5.5+phase*.61));
            // Average the local tangent across nearby vertices; dense freehand points stay continuous.
            Point behind=pointAt(along-3),ahead=pointAt(along+3);
            double tangent=distance(behind,ahead)>.05?bearing(behind,ahead):heading;
            if(Double.isNaN(offsetHeading))offsetHeading=tangent;
            else offsetHeading+=Math.max(-20*dt,Math.min(20*dt,angle(offsetHeading,tangent)));
            position=displace(position,offsetHeading+90,lateral);
        }
        double horizontal=distance(sample.point,position);
        distance+=config.routeOffsetMeters>0?horizontal:moved;
        double reportedSpeed=config.routeOffsetMeters>0?(finished||config.fixed?0:horizontal/dt*3.6):speed*3.6;
        if(horizontal>.00001)heading=bearing(sample.point,position);
        double altitude=position.altitude+config.altitudeVariation*(.7*Math.sin(elapsed/31)+.3*Math.sin(elapsed/11));
        altitude=approach(sample.point.altitude,altitude,.5*dt);
        position=new Point(position.latitude,position.longitude,Math.max(-500,Math.min(9000,altitude)));
        sample=new Sample(position,reportedSpeed,cadence,distance,elapsed,heading,(long)steps,finished);
    }
    private Point pointAt(double along){
        double total=cumulative[cumulative.length-1];
        if(config.loop)along=((along%total)+total)%total;
        else along=Math.max(0,Math.min(total,along));
        int low=0,high=lengths.length-1;
        while(low<high){int mid=(low+high+1)/2;if(cumulative[mid]<=along)low=mid;else high=mid-1;}
        return interpolate(path.get(low),path.get(low+1),Math.max(0,Math.min(1,(along-cumulative[low])/lengths[low])));
    }
    private static double smooth(double v){return v*v*(3-2*v);}
    private static Point displace(Point p,double heading,double meters){
        double delta=meters/6371008.8,theta=Math.toRadians(heading),lat=Math.toRadians(p.latitude),lon=Math.toRadians(p.longitude);
        double q=Math.asin(Math.sin(lat)*Math.cos(delta)+Math.cos(lat)*Math.sin(delta)*Math.cos(theta));
        double r=lon+Math.atan2(Math.sin(theta)*Math.sin(delta)*Math.cos(lat),Math.cos(delta)-Math.sin(lat)*Math.sin(q));
        return new Point(Math.max(-85,Math.min(85,Math.toDegrees(q))),((Math.toDegrees(r)+540)%360)-180,p.altitude);
    }
    public static double distance(Point a,Point b) {
        double p=Math.toRadians(b.latitude-a.latitude),l=Math.toRadians(angle(a.longitude,b.longitude));
        double v=Math.sin(p/2)*Math.sin(p/2)+Math.cos(Math.toRadians(a.latitude))*Math.cos(Math.toRadians(b.latitude))*Math.sin(l/2)*Math.sin(l/2);
        return 6371008.8*2*Math.atan2(Math.sqrt(Math.max(0,v)),Math.sqrt(Math.max(0,1-v)));
    }
    private static Point interpolate(Point a,Point b,double f) {
        double lon=((a.longitude+angle(a.longitude,b.longitude)*f+540)%360)-180;
        return new Point(a.latitude+(b.latitude-a.latitude)*f,lon,a.altitude+(b.altitude-a.altitude)*f);
    }
    private static double bearing(Point a,Point b) {
        double l=Math.toRadians(angle(a.longitude,b.longitude)),p=Math.toRadians(a.latitude),q=Math.toRadians(b.latitude);
        return (Math.toDegrees(Math.atan2(Math.sin(l)*Math.cos(q),Math.cos(p)*Math.sin(q)-Math.sin(p)*Math.cos(q)*Math.cos(l)))+360)%360;
    }
    private static double angle(double a,double b) { return ((b-a+540)%360)-180; }
    private static double approach(double v,double target,double limit) { return v+Math.max(-limit,Math.min(limit,target-v)); }
    private static boolean finite(double v) { return !Double.isNaN(v)&&!Double.isInfinite(v); }
}
