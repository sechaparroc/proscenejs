/****************************************************************************************
 * frames
 * Copyright (c) 2018 National University of Colombia, https://visualcomputing.github.io/
 * @author Sebastian Chaparro, https://github.com/sechaparroc
 * @author Jean Pierre Charalambos, https://github.com/VisualComputing
 *
 * All rights reserved. A 2D or 3D scene graph library providing eye, input and timing
 * handling to a third party (real or non-real time) renderer. Released under the terms
 * of the GPL v3.0 which is available at http://www.gnu.org/licenses/gpl.html
 ****************************************************************************************/

package frames.core.constraint;

//TODO: CHECK FORWARD STEP WITH HINGE 3D
//TODO : Update

import frames.core.Frame;
import frames.primitives.Quaternion;
import frames.primitives.Vector;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.min;

/**
 * A Frame is constrained to disable translation and
 * allow 1-DOF rotation limiting Rotation by defining an
 * Axis (according to Local Frame Coordinates) a Rest Rotation
 * and upper and lower bounds with values between 0 and PI
 */
public class Hinge extends Constraint {
  /*
  With this Kind of Constraint no Translation is allowed
  * and the rotation depends on 2 angles this kind of constraint always
  * look for the reference frame (local constraint), if no initial position is
  * set Identity is assumed as rest position
  * */
  protected float _max;
  protected float _min;
  protected Quaternion _restRotation = new Quaternion();
  protected Vector _axis;


  public Vector axis() {
    return _axis;
  }

  /**
   * Set the twist axis.
   * The axis must be defined with respect to {@link #restRotation()} Quaternion
   *
   * @param axis
   */
  public void setAxis(Vector axis) {
    this._axis = axis;
  }

  /**
   * Set the twist axis.
   * The axis must be defined with respect to reference Quaternion.
   * Call this method only after setting {@link #restRotation()} Quaternion
   *
   * @param reference
   * @param axis
   */
  public void setAxis(Quaternion reference, Vector axis) {
    this._axis = _restRotation.inverse().rotate(reference.rotate(axis));
  }

  /**
   * Get the restRotation Quaternion.
   *
   * @return
   */
  public Quaternion restRotation() {
    return _restRotation;
  }

  public void setRestRotation(Quaternion restRotation) {
    this._restRotation = restRotation.get();
  }


  public Hinge() {
    _max = (float) (PI);
    _min = (float) (PI);
    _axis = new Vector(0, 1, 0);
  }

  public Hinge(boolean is2D) {
    this();
    if (is2D) _axis = new Vector(0, 0, 1);
  }

  /*Create a Hinge constraint in a with rest rotation
   * Given by the axis (0,0,1) and angle restAngle*/
  public Hinge(float min, float max, float restAngle) {
    this(true);
    this._restRotation = new Quaternion(new Vector(0, 0, 1), restAngle);
  }

  public Hinge(float min, float max, Quaternion rotation) {
    this(min, max);
    this._restRotation = rotation.get();
  }

  public Hinge(float min, float max, Quaternion rotation, Vector axis) {
    this(min, max, rotation);
    this._axis = axis;
  }

  public Hinge(boolean is2D, float min, float max) {
    this(is2D);
    this._max = max;
    this._min = min;
  }

  public Hinge(float min, float max) {
    this(false);
    this._max = max;
    this._min = min;
  }

  public float maxAngle() {
    return _max;
  }

  public void setMaxAngle(float max) {
    this._max = max;
  }

  public float minAngle() {
    return _min;
  }

  public void setMinAngle(float min) {
    this._min = min;
  }

  @Override
  public Quaternion constrainRotation(Quaternion rotation, Frame frame) {
    Vector axis = _restRotation.rotate(this._axis);
    Vector rotationAxis = new Vector(rotation._quaternion[0], rotation._quaternion[1], rotation._quaternion[2]);
    rotationAxis = Vector.projectVectorOnAxis(rotationAxis, axis);
    //Get rotation component on Axis direction
    Quaternion rotationTwist= new Quaternion(rotationAxis.x(), rotationAxis.y(), rotationAxis.z(), rotation.w());
    float deltaAngle = rotationTwist.angle();
    if (rotationAxis.dot(axis) < 0) deltaAngle *= -1;
    /*First rotation of Frame with respect to Axis*/
    Quaternion current = Quaternion.compose(_restRotation.inverse(), frame.rotation());
    /*It is possible that the current rotation axis is not parallel to Axis*/
    Vector currentAxis = new Vector(current._quaternion[0], current._quaternion[1], current._quaternion[2]);
    currentAxis = Vector.projectVectorOnAxis(currentAxis, axis);
    //Get rotation component on Axis direction
    Quaternion currentTwist = new Quaternion(currentAxis.x(), currentAxis.y(), currentAxis.z(), current.w());
    float frameAngle = currentTwist.angle();
    if (current.axis().dot(axis) < 0) frameAngle *= -1;
    float desired = frameAngle + deltaAngle;
    desired = (float) (desired - PI * 2 * Math.floor((desired + PI) / (2*PI)));
    float inverted;
    if(desired > 0){
      inverted = (float)(-2*PI + desired);
    } else{
      inverted = (float)(2*PI + desired);
    }
    if((desired >= 0 && desired <= _max)){
      return new Quaternion(axis, deltaAngle);
    }
    if((desired <= 0 && desired >= -_min)){
      return new Quaternion(axis, deltaAngle);
    }

    //get nearest bound
    float dist_to_min = min(abs(desired + _min), abs(inverted + _min));
    float dist_to_max = min(abs(desired - _max), abs(inverted - _max));
    if(dist_to_min < dist_to_max){
      float r = -_min - frameAngle;
      return new Quaternion(axis, r);
    }else{
      float r = _max - frameAngle;
      return new Quaternion(axis, r);
    }
  }
  @Override
  public Vector constrainTranslation(Vector translation, Frame frame) {
    return new Vector();
  }
}
