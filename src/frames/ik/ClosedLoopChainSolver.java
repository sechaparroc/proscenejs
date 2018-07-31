package frames.ik;

import frames.core.Frame;
import frames.primitives.Quaternion;
import frames.primitives.Vector;

import java.util.ArrayList;

/**
 * Created by sebchaparr on 30/07/18.
 */

public class ClosedLoopChainSolver extends FABRIKSolver {

    protected ArrayList<? extends Frame> _chain;
    protected ArrayList<Frame> _reversedChain;
    protected ArrayList<? extends Frame> _original;
    protected float _distanceHeadToTail;
    protected ArrayList<Vector> _positionsChildren = new ArrayList<Vector>();
    protected ArrayList<Float> _distancesChildren = new ArrayList<Float>();


    //here we don't consider any kind of constraint
    //chain must not include the head twice
    public ClosedLoopChainSolver(ArrayList<? extends Frame> chain) {
        super();
        this._original = chain;
        //this._chain = _copy(chain);
        this._chain = chain;
        _positions = new ArrayList<Vector>();
        _distances = new ArrayList<Float>();
        _orientations = new ArrayList<Quaternion>();
        Vector prevPosition = chain.get(0).reference() != null
                ? chain.get(0).reference().position().get() : new Vector(0, 0, 0);
        Quaternion prevOrientation = chain.get(0).reference() != null
                ? chain.get(0).reference().orientation().get() : new Quaternion();
        _reversedChain = new ArrayList<>();
        for (Frame joint : chain) {
            _reversedChain.add(_reversedChain.size(), joint);
            Vector position = joint.position().get();
            Quaternion orientation = prevOrientation.get();
            orientation.compose(joint.rotation().get());
            _positions.add(position);
            _distances.add(Vector.subtract(position, prevPosition).magnitude());
            _orientations.add(orientation);

            Vector positionChild = joint.children() != null ?
                    joint.children().get(0).position() : position.get();
            _positionsChildren.add(positionChild);
            _distancesChildren.add(Vector.subtract(position, positionChild).magnitude());
            prevPosition = position;
            prevOrientation = orientation.get();
        }

        Vector head = _chain.get(0).position().get();
        Vector tail = _chain.get(_chain.size()-1).position().get();
        _distanceHeadToTail = Vector.subtract(head, tail).magnitude();
    }

    public void setUnknown(Frame frame, Vector vector){
        for(int i = 0; i < _original.size(); i++){
            if(frame == _original.get(i)){
                _positions.set(i, vector);
            }
        }
    }

    public void fixNoisyJoints(){
        //consider the case when there're 2 unknowns
        Frame root = _chain.get(0);
        Frame end = _chain.get(_chain.size() - 1);
        //Initial root position
        Vector initial = _chain.get(0).position().get();
        //Stage 1: Forward Reaching
        _forwardReaching(_chain);
        //Move root
        move(0, true);
        //Move in the other order
        _forwardReaching(_reversedChain);
        //set root to initial position
        _positions.set(0 , initial);
        //keep distances with children
        for(int i = 1; i < _chain.size(); i++){
            Vector v = move(_positions.get(i), _positionsChildren.get(i), _distancesChildren.get(i));
            _positions.set(i, move(v, _positions.get(0), Vector.distance(_chain.get(i).position(), _chain.get(0).position())));
        }
        //Stage 1: Forward Reaching
        _forwardReaching(_chain);
        //Move root
        move(0, true);
        //Move in the other order
        _forwardReaching(_reversedChain);
    }

    protected void _forwardReaching(ArrayList<? extends Frame> chain) {
        for (int i = chain.size() - 2; i >= 0; i--) {
            Vector pos_i = _positions.get(i);
            Vector pos_i1 = _positions.get(i + 1);
            float dist_i = _distances.get(i + 1);
            if (dist_i == 0) {
                _positions.set(i, pos_i1.get());
                continue;
            }
            float r_i = Vector.distance(pos_i, pos_i1);
            float lambda_i = dist_i / r_i;
            Vector new_pos = Vector.multiply(pos_i1, 1.f - lambda_i);
            new_pos.add(Vector.multiply(pos_i, lambda_i));
            _positions.set(i, new_pos);
        }
    }

    protected void _backwardReaching(ArrayList<? extends Frame> chain) {
        for (int i = 0; i < chain.size() - 1; i++) {
            Vector pos_i = _positions.get(i);
            Vector pos_i1 = _positions.get(i + 1);
            float dist_i = _distances.get(i + 1);
            if (dist_i == 0) {
                _positions.set(i+1, pos_i.get());
                continue;
            }
            float r_i = Vector.distance(pos_i, pos_i1);
            float lambda_i = dist_i / r_i;
            Vector new_pos = Vector.multiply(pos_i, 1.f - lambda_i);
            new_pos.add(Vector.multiply(pos_i1, lambda_i));
            _positions.set(i+1, new_pos);
        }
    }

    public Vector move(Vector i, Vector j, float distance){
        float r_i = Vector.distance(i, j);
        float lambda_i = distance / r_i;
        Vector new_pos = Vector.multiply(i, 1.f - lambda_i);
        new_pos.add(Vector.multiply(j, lambda_i));
        return new_pos;
    }



    public void move(int i, boolean forward){
        int j = forward ? i + 1 : i - 1;
        j = j < 0 ? _chain.size()-1 : j == _chain.size() ? 0 : j;
        Vector pos_i = _positions.get(i);
        Vector pos_i1 = _positions.get(j);
        float dist_i;
        if(i == 0 && j == _chain.size() || i == _chain.size() && j == 0){
            dist_i = _distanceHeadToTail;
        }else{
            dist_i = forward ? _distances.get(j) : _distances.get(i);
        }
        float r_i = Vector.distance(pos_i, pos_i1);
        float lambda_i = dist_i / r_i;
        Vector new_pos = Vector.multiply(pos_i1, 1.f - lambda_i);
        new_pos.add(Vector.multiply(pos_i, lambda_i));
        _positions.set(i, new_pos);
    }

    @Override
    protected boolean _iterate() {
        return false;
    }

    @Override
    protected void _update() {

    }

    @Override
    protected boolean _changed() {
        return false;
    }

    @Override
    protected void _reset() {

    }
}