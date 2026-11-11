package com.dudu.run.simulation;

import android.content.Context;
import android.util.Xml;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.xmlpull.v1.XmlPullParser;

public final class RouteIO {
    private RouteIO() {}
    public static List<SimulationEngine.Point> parse(String text,double defaultAltitude) {
        ArrayList<SimulationEngine.Point> points=new ArrayList<>();
        if(text.length()>600000) throw new IllegalArgumentException("路线文本过长");
        int lineNumber=0;
        for(String line:text.split("\\r?\\n")) {
            lineNumber++;
            if(line.trim().isEmpty()) continue;
            String[] parts=line.trim().split("[,，\\s]+");
            if(parts.length<2 || parts.length>3) throw new IllegalArgumentException("第 "+lineNumber+" 行应为：纬度,经度[,高度]");
            try {
                points.add(new SimulationEngine.Point(Double.parseDouble(parts[0]),Double.parseDouble(parts[1]),
                        parts.length==3?Double.parseDouble(parts[2]):defaultAltitude));
            } catch(NumberFormatException error) { throw new IllegalArgumentException("第 "+lineNumber+" 行包含无效数字"); }
            if(points.size()>5000) throw new IllegalArgumentException("最多 5000 个坐标");
        }
        return points;
    }
    public static String format(List<SimulationEngine.Point> points) {
        StringBuilder text=new StringBuilder();
        for(SimulationEngine.Point p:points)
            text.append(String.format(Locale.US,"%.7f,%.7f,%.2f\n",p.latitude,p.longitude,p.altitude));
        return text.toString();
    }
    public static List<SimulationEngine.Point> readGpx(Context context,InputStream input,boolean gps,double altitude) throws Exception {
        XmlPullParser parser=Xml.newPullParser();
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES,true);
        parser.setInput(new LimitedInputStream(input,2*1024*1024),null);
        List<SimulationEngine.Point> result=new ArrayList<>();
        Double latitude=null,longitude=null;
        double elevation=altitude;
        String pointTag=null;
        for(int event=parser.nextToken();event!=XmlPullParser.END_DOCUMENT;event=parser.nextToken()) {
            if(event==XmlPullParser.DOCDECL || event==XmlPullParser.ENTITY_REF)
                throw new IllegalArgumentException("GPX 不允许外部实体或文档类型声明");
            String name=parser.getName();
            if(event==XmlPullParser.START_TAG && ("trkpt".equals(name)||"rtept".equals(name))) {
                if(pointTag!=null) throw new IllegalArgumentException("GPX 坐标嵌套无效");
                pointTag=name; latitude=Double.parseDouble(parser.getAttributeValue(null,"lat"));
                longitude=Double.parseDouble(parser.getAttributeValue(null,"lon")); elevation=altitude;
            } else if(event==XmlPullParser.START_TAG && pointTag!=null && "ele".equals(name)) {
                elevation=Double.parseDouble(parser.nextText());
            } else if(event==XmlPullParser.END_TAG && name!=null && name.equals(pointTag)) {
                SimulationEngine.Point checked=new SimulationEngine.Point(latitude,longitude,elevation);
                if(gps) {
                    checked=CoordinateTransform.fromWgs84(checked);
                }
                result.add(checked); pointTag=null;
                if(result.size()>5000) throw new IllegalArgumentException("GPX 最多 5000 个轨迹点");
            }
        }
        if(result.isEmpty()) throw new IllegalArgumentException("未找到 trkpt 或 rtept 轨迹点");
        return result;
    }
    private static final class LimitedInputStream extends java.io.FilterInputStream {
        private final long limit; private long count;
        LimitedInputStream(InputStream input,long limit) { super(input); this.limit=limit; }
        private void record(long amount) throws java.io.IOException {
            if(amount>0 && (count+=amount)>limit) throw new java.io.IOException("GPX 文件超过 2 MiB");
        }
        @Override public int read() throws java.io.IOException {
            int value=super.read(); if(value>=0) record(1); return value;
        }
        @Override public int read(byte[] data,int off,int length) throws java.io.IOException {
            int count=in.read(data,off,length); record(count); return count;
        }
    }
}
