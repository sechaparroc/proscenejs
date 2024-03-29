package ik.common;

/**
 * Created by sebchaparr on 21/07/18.
 */

import frames.core.Frame;
import frames.primitives.Vector;
import frames.processing.Scene;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

public class Joint extends Frame{
    protected String _name;
    protected int _color;
    protected float _radius = 5;
    //set to true only when the joint is the root (for rendering purposes)
    protected boolean _isRoot = false;

    public Joint(Scene scn, int color){
        super(scn);
        _color = color;
    }

    public Joint(Scene scn){
        this(scn, scn.pApplet().color(scn.pApplet().random(0,255),scn.pApplet().random(0,255), scn.pApplet().random(0,255)));
    }

    @Override
    public void visit(){
        Scene scene = (Scene) this._graph;
        PGraphics pg = scene.frontBuffer();
        pg.hint(PConstants.DISABLE_DEPTH_TEST);
        pg.pushStyle();
        pg.fill(_color);
        pg.noStroke();
        if (scene.is2D()) pg.ellipse(0, 0, 3, 3);
        else pg.sphere(_radius);
        if (!_isRoot) {
            pg.strokeWeight(_radius/2);
            pg.stroke(_color);
            Vector v = location(new Vector(), reference());
            if (scene.is2D()) {
                pg.line(0, 0, v.x(), v.y());
            } else {
                pg.line(0, 0, 0, v.x(), v.y(), v.z());
            }
            pg.popStyle();
        }

        if (constraint() != null) {
            scene.drawConstraint(this);
        }
        pg.hint(PConstants.ENABLE_DEPTH_TEST);
    }

    public void setRadius(float radius){
        _radius = radius;
    }
    public void setName(String name){
        _name = name;
    }
    public void setRoot(boolean isRoot){
        _isRoot = isRoot;
    }
}
