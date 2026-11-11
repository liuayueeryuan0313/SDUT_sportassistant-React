package com.dudu.run.simulation;

/** Android MotionEvent action codes without a framework dependency, for deterministic gesture checks. */
public final class RouteGestureState {
    public enum Action{BEGIN_STROKE,MOVE_STROKE,COMMIT_STROKE,BEGIN_ZOOM,MOVE_ZOOM,END_ZOOM,IGNORE,CANCEL}
    private boolean single,multiple,zoom;
    public void reset(){single=false;multiple=false;zoom=false;}
    public Action accept(int action,int pointers){
        if(action==0){reset();single=true;return Action.BEGIN_STROKE;}
        if(action==3){reset();return Action.CANCEL;}
        if(action==5||(action==2&&pointers>1&&!multiple)){
            single=false;multiple=true;if(!zoom){zoom=true;return Action.BEGIN_ZOOM;}return Action.MOVE_ZOOM;
        }
        if(action==6){if(zoom){zoom=false;return Action.END_ZOOM;}return Action.IGNORE;}
        if(action==1){Action result=zoom?Action.END_ZOOM:single&&!multiple?Action.COMMIT_STROKE:Action.IGNORE;reset();return result;}
        if(action==2){if(zoom)return Action.MOVE_ZOOM;if(single&&!multiple&&pointers==1)return Action.MOVE_STROKE;}
        return Action.IGNORE;
    }
}
