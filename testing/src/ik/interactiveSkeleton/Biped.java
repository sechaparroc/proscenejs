package ik.interactiveSkeleton;

import frames.core.Frame;
import frames.core.Graph;
import frames.core.constraint.FixedConstraint;
import frames.core.constraint.Hinge;
import frames.core.constraint.PlanarPolygon;
import frames.ik.Solver;
import frames.primitives.Vector;
import frames.processing.Scene;
import frames.processing.Shape;
import ik.common.Joint;
import processing.core.PApplet;
import processing.core.PShape;
import processing.event.MouseEvent;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sebchaparr on 30/07/18.
 */
public class Biped extends PApplet {
    //TODO : Update
    Scene scene;
    HashMap<String, ArrayList<Joint>> limbs;
    HashMap<String, Shape> targets;
    String[] keys = {"LeftArm", "RightArm", "LeftFoot", "RightFoot"};
    float boneLength = 50;
    float targetRadius = 7;


    public void settings() {
        size(700, 700, P3D);
    }

    public void setup() {
        scene = new Scene(this);
        scene.setType(Graph.Type.ORTHOGRAPHIC);
        scene.setFieldOfView(PI / 3);
        scene.setRadius(boneLength * 5);
        scene.fitBallInterpolation();
        scene.disableBackBuffer();

        targets = new HashMap<>();
        limbs = new HashMap<>();

        PShape redBall = createShape(SPHERE, targetRadius);
        redBall.setStroke(false);
        redBall.setFill(color(255,0,0));

        for(String key : keys){
            targets.put(key, new Shape(scene, redBall));
        }

        Joint chest = new Joint(scene);
        Joint lowerRoot = new Joint(scene);

        //right arm
        limbs.put("LeftArm", limb(chest, new Vector(10,-30,0), new Vector(1,0,0), boneLength, new Vector(0,0,10)));
        limbs.put("RightArm", limb(chest, new Vector(-10,-30,0), new Vector(-1,0,0), boneLength, new Vector(0,0,10)));
        limbs.put("LeftFoot", limb(lowerRoot, new Vector(10,10,0), new Vector(0,1,0), boneLength, new Vector(-10,0,0)));
        limbs.put("RightFoot", limb(lowerRoot, new Vector(-10,10,0), new Vector(0,1,0), boneLength, new Vector(-10,0,0)));

        chest.translate(0,-50,0);
        Joint upperRoot = new Joint(scene);
        chest.setReference(upperRoot);

        //applyConstraint(chest.frame, boneLength);
        applyConstraint(chest.children().get(0), boneLength, chest.translation(), 20);

        lowerRoot.setConstraint(new FixedConstraint());

        Solver solver  = scene.registerTreeSolver(upperRoot);
        solver.maxIter = 100;
        solver  = scene.registerTreeSolver(lowerRoot);
        solver.maxIter = 100;

        for(String key : keys){
            ArrayList<Joint> skeleton = limbs.get(key);
            targets.get(key).setPosition(skeleton.get(skeleton.size()-1).position());
            //TESTING WITH FABRIK
            scene.addIKTarget(skeleton.get(skeleton.size() - 1), targets.get(key));
        }
    }
    public void draw() {
        background(0);
        lights();
        scene.drawAxes();
        scene.traverse();
    }

    public void applyConstraint(Frame child, float boneLength){
        applyConstraint(child, boneLength, child.translation().get(), 40);
    }

    public void applyConstraint(Frame child, float boneLength, Vector twist, float degrees){
        //APPLY CONSTRAINTS
        ArrayList<Vector> vertices = new ArrayList<Vector>();
        int sides = 10;
        float radius = 40;
        float step = 2*PI/sides;
        for(int i = 0; i < sides; i++){
            float angle = i*step;
            vertices.add(new Vector(radius*cos(angle), radius*sin(angle)));
        }

        PlanarPolygon c = new PlanarPolygon(vertices);
        c.setHeight(boneLength / 2.f);
        c.setAngle(radians(degrees));
        c.setRestRotation(child.reference().rotation().get(), twist.orthogonalVector(), twist);
        child.reference().setConstraint(c);
    }


    public ArrayList<Joint> limb(Frame root, Vector origin, Vector direction, float boneLength, Vector hinge) {
        ArrayList<Joint> skeleton = new ArrayList<Joint>();
        Vector bone = direction.normalize(null);
        bone.multiply(boneLength);
        Joint j1 = new Joint(scene);
        j1.setReference(root);
        j1.setPosition(origin);
        Joint j2 = new Joint(scene);
        j2.setReference(j1);
        j2.translate(bone);
        Joint j3 = new Joint(scene);
        j3.setReference(j2);
        j3.translate(bone);


        //applyConstraint(j2.frame, boneLength);

        float constraint_factor_x = 170;
        Hinge c2 = new Hinge(radians(0), radians(constraint_factor_x));
        c2.setRestRotation(j2.rotation().get());
        c2.setAxis(Vector.projectVectorOnPlane(hinge, j2.translation()));
        if(Vector.squaredNorm(c2.axis()) != 0) {
            //j2.frame.setConstraint(c2);
        }
        skeleton.add(j1);
        skeleton.add(j2);
        skeleton.add(j3);

        return skeleton;
    }

    @Override
    public void mouseMoved() {
        scene.cast();
    }

    public void mouseDragged() {
        if (mouseButton == LEFT){
            scene.spin();
        } else if (mouseButton == RIGHT) {
            scene.translate();
        } else {
            scene.zoom(scene.mouseDX());
        }
    }

    public void mouseWheel(MouseEvent event) {
        scene.scale(event.getCount() * 20);
    }

    public void mouseClicked(MouseEvent event) {
        if (event.getCount() == 2)
            if (event.getButton() == LEFT)
                scene.focus();
            else
                scene.align();
    }


    public static void main(String args[]) {
        PApplet.main(new String[]{"ik.interactiveSkeleton.Biped"});
    }
}