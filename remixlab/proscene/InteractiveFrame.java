/**************************************************************************************
 * ProScene (version 3.0.0)
 * Copyright (c) 2014-2016 National University of Colombia, https://github.com/remixlab
 * @author Jean Pierre Charalambos, http://otrolado.info/
 * 
 * All rights reserved. Library that eases the creation of interactive scenes
 * in Processing, released under the terms of the GNU Public License v3.0
 * which is available at http://www.gnu.org/licenses/gpl.html
 **************************************************************************************/

// Thanks to Sebastian Chaparro, url-PENDING and William Rodriguez, url-PENDING
// for providing an initial picking example and searching the documentation for it:
// http://n.clavaud.free.fr/processing/picking/pickcode.htm
// http://content.gpwiki.org/index.php/OpenGL_Selection_Using_Unique_Color_IDs

package remixlab.proscene;

import java.lang.reflect.Method;

import processing.core.*;
import remixlab.bias.core.*;
import remixlab.bias.event.*;
import remixlab.bias.ext.*;
import remixlab.dandelion.core.*;
import remixlab.dandelion.core.AbstractScene.Platform;
import remixlab.dandelion.geom.*;
import remixlab.util.*;

/**
 * A Processing {@link remixlab.dandelion.core.GenericFrame} with a {@link #profile()}
 * instance which allows {@link remixlab.bias.core.Shortcut} to
 * {@link java.lang.reflect.Method} bindings high-level customization (see all the
 * <b>*Binding*()</b> methods). Refer to
 * {@link remixlab.bias.ext.Profile#setBinding(Shortcut, String)} and
 * {@link remixlab.bias.ext.Profile#setBinding(Object, Shortcut, String)} for the type of
 * actions and method signatures that may be bound.
 * <p>
 * Visual representations (PShapes or arbitrary graphics procedures) may be related to an
 * interactive-frame in two different ways:
 * <ol>
 * <li>Applying the frame transformation just before the graphics code happens in
 * <b>papplet.draw()</b> (refer to the {@link remixlab.dandelion.core.GenericFrame} API
 * class documentation).
 * <li>Setting a visual representation directly to the frame, either by calling
 * {@link #setShape(PShape)} (retained mode) or {@link #setShape(Object, String)}
 * (immediate mode) in <b>papplet.setup()</b>, and then calling
 * {@link remixlab.proscene.Scene#drawFrames()} in <b>papplet.draw()</b>.
 * </ol>
 * When a visual representation is attached to a frame, picking can be performed in an
 * exact manner (using the pixels of the projected visual representation themselves)
 * provided that the {@link #pickingPrecision()} is set to {@link PickingPrecision#EXACT}
 * and the scene {@link remixlab.proscene.Scene#pickingBuffer()} is enabled (see
 * {@link remixlab.proscene.Scene#enablePickingBuffer()}). Using a picking buffer requires
 * the geometry to be drawn twice, one at the front-buffer and one at the picking-buffer.
 * If performance is a concern, use another {@link #pickingPrecision()} strategy, or
 * differentiate the front and the picking shapes (using a simpler representation in the
 * later case) by calling {@link #setFrontShape(PShape)} and
 * {@link #setPickingShape(PShape)}, respectively. Note that {@link #setShape(PShape)}
 * just calls {@link #setFrontShape(PShape)} and {@link #setPickingShape(PShape)} on the
 * same shape.
 * <p>
 * If the above conditions are met, the visual representation may be highlighted when
 * picking takes place (see {@link #setHighlightingMode(HighlightingMode)}).
 * 
 * @see remixlab.dandelion.core.GenericFrame
 */
public class InteractiveFrame extends GenericFrame {
  // TODO decide whether or not to include this and if so whether or not to include the
  // profile
  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(fShape).append(pShape).toHashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (obj == this)
      return true;
    if (obj.getClass() != getClass())
      return false;

    InteractiveFrame other = (InteractiveFrame) obj;
    return new EqualsBuilder().appendSuper(super.equals(obj)).append(fShape, other.fShape).append(pShape, other.pShape)
        .isEquals();
  }

  /**
   * Enumerates the highlighting modes.
   */
  public enum HighlightingMode {
    NONE, FRONT_SHAPE, FRONT_PICKING_SHAPES, PICKING_SHAPE
  };

  // profile
  protected Profile profile;
  // id
  protected int id;
  // shape
  protected Shape fShape, pShape;

  HighlightingMode highlight;

  /**
   * Calls {@code super(eye)}, add the {@link #drawEye(PGraphics)} graphics handler,
   * creates the frame {@link #profile()} and calls {@link #setDefaultMouseBindings()} and
   * {@link #setDefaultKeyBindings()}.
   * 
   * @see #drawEye(PGraphics)
   * @see remixlab.dandelion.core.GenericFrame#GenericFrame(Eye)
   */
  public InteractiveFrame(Eye eye) {
    super(eye);
    init();
    setShape("drawEye");
  }

  /**
   * Same as {@code scene().drawEye(pg, eye())}.
   * <p>
   * This method is only meaningful when frame {@link #isEyeFrame()}.
   * 
   * @see remixlab.proscene.Scene#drawEye(PGraphics, Eye)
   * @see #isEyeFrame()
   */
  public void drawEye(PGraphics pg) {
    if (isEyeFrame())
      scene().drawEye(pg, eye(), is3D());
    else
      AbstractScene.showOnlyEyeWarning("drawEye", true);
  }

  /**
   * Same as {@code scene().drawEyeNearPlane(pg, eye())}.
   * <p>
   * This method is only meaningful when frame {@link #isEyeFrame()}.
   * 
   * @see remixlab.proscene.Scene#drawEyeNearPlane(PGraphics, Eye)
   * @see #isEyeFrame()
   */
  public void drawEyeNearPlane(PGraphics pg) {
    if (isEyeFrame())
      scene().drawEyeNearPlane(pg, eye(), is3D());
    else
      AbstractScene.showOnlyEyeWarning("drawEyeNearPlane", true);
  }

  /**
   * Calls {@code super(scn}. Sets the {@link #pickingPrecision()} to
   * {@link PickingPrecision#EXACT}.
   * 
   * @see remixlab.dandelion.core.GenericFrame#GenericFrame(AbstractScene)
   */
  public InteractiveFrame(Scene scn) {
    super(scn);
    init();
    setPickingPrecision(PickingPrecision.EXACT);
  }

  /**
   * Constructs an interactive-frame as a child of reference frame. Calls
   * {@code super(scn, referenceFrame}. Sets the {@link #pickingPrecision()} to
   * {@link PickingPrecision#EXACT}.
   * 
   * @see remixlab.dandelion.core.GenericFrame#GenericFrame(AbstractScene, GenericFrame)
   */
  public InteractiveFrame(Scene scn, GenericFrame referenceFrame) {
    super(scn, referenceFrame);
    init(referenceFrame);
    setPickingPrecision(PickingPrecision.EXACT);
  }

  /**
   * Calls {@code super(scn}. Calls {@link #setShape(PShape)} on the {@code ps}. Sets the
   * {@link #pickingPrecision()} to {@link PickingPrecision#EXACT}.
   * 
   * @see remixlab.dandelion.core.GenericFrame#GenericFrame(AbstractScene)
   * @see #setShape(PShape)
   */
  public InteractiveFrame(Scene scn, PShape ps) {
    super(scn);
    init();
    setShape(ps);
    setPickingPrecision(PickingPrecision.EXACT);
  }

  /**
   * Calls {@code super(scn, referenceFrame)}. Calls {@link #setShape(PShape)} on the
   * {@code ps}. Sets the {@link #pickingPrecision()} to {@link PickingPrecision#EXACT}.
   * 
   * @see remixlab.dandelion.core.GenericFrame#GenericFrame(AbstractScene, GenericFrame)
   * @see #setShape(String)
   */
  public InteractiveFrame(Scene scn, GenericFrame referenceFrame, PShape ps) {
    super(scn, referenceFrame);
    init(referenceFrame);
    setShape(ps);
    setPickingPrecision(PickingPrecision.EXACT);
  }

  /**
   * Calls {@code super(scn)}. Calls {@link #setShape(String)} on the {@code methodName}.
   * Sets the {@link #pickingPrecision()} to {@link PickingPrecision#FIXED} if
   * {@code methodName=="drawAxes" || methodName=="drawGrid" || methodName=="drawDottedGrid"}.
   * Otherwise sets it to {@link PickingPrecision#EXACT}.
   * 
   * @see remixlab.dandelion.core.GenericFrame#GenericFrame(AbstractScene)
   */
  public InteractiveFrame(Scene scn, String methodName) {
    super(scn);
    init();
    setShape(methodName);
    if (methodName != "drawAxes" && methodName != "drawGrid" && methodName != "drawDottedGrid")
      setPickingPrecision(PickingPrecision.EXACT);
  }

  /**
   * Calls {@code super(scn}. Calls {@link #setShape(Object, String)}. Sets the
   * {@link #pickingPrecision()} to {@link PickingPrecision#EXACT}.
   * 
   * @see remixlab.dandelion.core.GenericFrame#GenericFrame(AbstractScene)
   * @see #setShape(Object, String)
   */
  public InteractiveFrame(Scene scn, Object obj, String methodName) {
    super(scn);
    init();
    setShape(obj, methodName);
    setPickingPrecision(PickingPrecision.EXACT);
  }

  /**
   * Calls {@code super(scn, referenceFrame)}. Calls {@link #setShape(String)} on the
   * {@code methodName}. Sets the {@link #pickingPrecision()} to
   * {@link PickingPrecision#EXACT}.
   * 
   * @see remixlab.dandelion.core.GenericFrame#GenericFrame(AbstractScene, GenericFrame)
   * @see #setShape(String)
   */
  public InteractiveFrame(Scene scn, GenericFrame referenceFrame, String methodName) {
    super(scn, referenceFrame);
    init(referenceFrame);
    setShape(methodName);
    setPickingPrecision(PickingPrecision.EXACT);
  }

  /**
   * Calls {@code super(scn, referenceFrame}. Calls {@link #setShape(Object, String)}.
   * Sets the {@link #pickingPrecision()} to {@link PickingPrecision#EXACT}.
   * 
   * @see remixlab.dandelion.core.GenericFrame#GenericFrame(AbstractScene, GenericFrame)
   * @see #setShape(Object, String)
   */
  public InteractiveFrame(Scene scn, GenericFrame referenceFrame, Object obj, String methodName) {
    super(scn, referenceFrame);
    init(referenceFrame);
    setShape(obj, methodName);
    setPickingPrecision(PickingPrecision.EXACT);
  }

  protected void init() {
    id = ++scene().iFrameCount;
    // unlikely but theoretically possible
    if (id == 16777216)
      throw new RuntimeException("Maximum iFrame instances reached. Exiting now!");
    fShape = new Shape(this);
    pShape = new Shape(this);
    highlight = HighlightingMode.FRONT_SHAPE;
    setProfile(new Profile(this));
    if (Scene.platform() == Platform.PROCESSING_DESKTOP)
      setDefaultMouseBindings();
    else
      setDefaultTouchBindings();
    setDefaultKeyBindings();
  }

  protected void init(GenericFrame referenceFrame) {
    id = ++scene().iFrameCount;
    // unlikely but theoretically possible
    if (id == 16777216)
      throw new RuntimeException("Maximum iFrame instances reached. Exiting now!");
    fShape = new Shape(this);
    pShape = new Shape(this);
    highlight = HighlightingMode.FRONT_SHAPE;
    setProfile(new Profile(this));
    if (referenceFrame instanceof InteractiveFrame)
      this.profile.set(((InteractiveFrame) referenceFrame).profile);
    else {
      if (Scene.platform() == Platform.PROCESSING_DESKTOP)
        setDefaultMouseBindings();
      else
        setDefaultTouchBindings();
      setDefaultKeyBindings();
    }
  }

  protected InteractiveFrame(InteractiveFrame otherFrame) {
    super(otherFrame);
    setProfile(new Profile(this));
    this.profile.set(otherFrame.profile);
    this.highlight = otherFrame.highlight;
    this.id = otherFrame.id;
    this.fShape = new Shape(this);
    this.pShape = new Shape(this);
    this.pShape.set(otherFrame.pShape);
    this.fShape.set(otherFrame.fShape);
  }

  @Override
  public InteractiveFrame get() {
    return new InteractiveFrame(this);
  }

  @Override
  protected InteractiveFrame detach() {
    InteractiveFrame frame = new InteractiveFrame(scene());
    scene().pruneBranch(frame);
    frame.setWorldMatrix(this);
    return frame;
  }

  // common api
  @Override
  public Scene scene() {
    return (Scene) gScene;
  }

  /**
   * Same as {@code profile.handle(event)}.
   * 
   * @see remixlab.bias.ext.Profile#handle(BogusEvent)
   */
  @Override
  public void performInteraction(BogusEvent event) {
    profile.handle(event);
  }

  /**
   * Same as {@code profile.removeBindings()}.
   * 
   * @see remixlab.bias.ext.Profile#removeBindings()
   */
  public void removeBindings() {
    profile.removeBindings();
  }

  /**
   * Same as {@code return profile.action(key)}.
   * 
   * @see remixlab.bias.ext.Profile#action(Shortcut)
   */
  public String action(Shortcut key) {
    return profile.action(key);
  }

  /**
   * Same as {@code profile.isActionBound(action)}.
   * 
   * @see remixlab.bias.ext.Profile#isActionBound(String)
   */
  public boolean isActionBound(String action) {
    return profile.isActionBound(action);
  }

  /**
   * Same as {@code scene().mouseAgent().setDefaultBindings(this)}. The default frame muse
   * bindings which may be queried with {@link #info()}.
   * 
   * @see remixlab.proscene.MouseAgent#setDefaultBindings(InteractiveFrame)
   */
  public void setDefaultMouseBindings() {
    scene().mouseAgent().setDefaultBindings(this);
  }

  public void setDefaultTouchBindings() {
    scene().droidTouchAgent().setDefaultBindings(this);
  }

  /**
   * Calls {@link #removeKeyBindings()} and sets the default frame key bindings which may
   * be queried with {@link #info()}.
   */
  public void setDefaultKeyBindings() {
    removeKeyBindings();
    setKeyBinding('n', "align");
    setKeyBinding('c', "center");
    // 2D and 3D
    setKeyBinding(KeyAgent.RIGHT_KEY, "translateXPos");
    setKeyBinding(KeyAgent.LEFT_KEY, "translateXNeg");
    setKeyBinding(KeyAgent.UP_KEY, "translateYPos");
    setKeyBinding(KeyAgent.DOWN_KEY, "translateYNeg");
    setKeyBinding(BogusEvent.ALT, KeyAgent.UP_KEY, "rotateZPos");
    setKeyBinding(BogusEvent.ALT, KeyAgent.DOWN_KEY, "rotateZNeg");
    if (is3D()) {
      setKeyBinding((BogusEvent.SHIFT | BogusEvent.CTRL), KeyAgent.UP_KEY, "translateZPos");
      setKeyBinding((BogusEvent.SHIFT | BogusEvent.CTRL), KeyAgent.DOWN_KEY, "translateZNeg");
      setKeyBinding(BogusEvent.CTRL, KeyAgent.RIGHT_KEY, "rotateXPos");
      setKeyBinding(BogusEvent.CTRL, KeyAgent.LEFT_KEY, "rotateXNeg");
      setKeyBinding(BogusEvent.CTRL, KeyAgent.UP_KEY, "rotateYPos");
      setKeyBinding(BogusEvent.CTRL, KeyAgent.DOWN_KEY, "rotateYNeg");
    }
  }

  // good for all dofs :P

  /**
   * Same as {@code profile.setBinding(new MotionShortcut(id), action)}.
   * 
   * @see remixlab.bias.ext.Profile#setBinding(Shortcut, String)
   */
  public void setMotionBinding(int id, String action) {
    profile.setBinding(new MotionShortcut(id), action);
  }

  /**
   * Same as {@code profile.setBinding(object, new MotionShortcut(id), action)}.
   * 
   * @see remixlab.bias.ext.Profile#setBinding(Object, Shortcut, String)
   */
  public void setMotionBinding(Object object, int id, String action) {
    profile.setBinding(object, new MotionShortcut(id), action);
  }

  /**
   * Remove all motion bindings. Same as
   * {@code profile.removeBindings(MotionShortcut.class)}.
   * 
   * @see remixlab.bias.ext.Profile#removeBindings(Class)
   */
  public void removeMotionBindings() {
    profile.removeBindings(MotionShortcut.class);
  }

  /**
   * Same as {@code profile.hasBinding(new MotionShortcut(id))}.
   * 
   * @see remixlab.bias.ext.Profile#hasBinding(Shortcut)
   */
  public boolean hasMotionBinding(int id) {
    return profile.hasBinding(new MotionShortcut(id));
  }

  /**
   * Same as {@code profile.removeBinding(new MotionShortcut(id))}.
   * 
   * @see remixlab.bias.ext.Profile#removeBindings(Class)
   */
  public void removeMotionBinding(int id) {
    profile.removeBinding(new MotionShortcut(id));
  }

  // Key

  /**
   * Same as {@code profile.setBinding(new KeyboardShortcut(vkey), action)}.
   * 
   * @see remixlab.bias.ext.Profile#setBinding(Shortcut, String)
   */
  public void setKeyBinding(int vkey, String action) {
    profile.setBinding(new KeyboardShortcut(vkey), action);
  }

  /**
   * Same as {@code profile.setBinding(new KeyboardShortcut(key), action)}.
   * 
   * @see remixlab.bias.ext.Profile#setBinding(Object, Shortcut, String)
   */
  public void setKeyBinding(char key, String action) {
    profile.setBinding(new KeyboardShortcut(key), action);
  }

  /**
   * Same as {@code profile.setBinding(object, new KeyboardShortcut(vkey), action)}.
   * 
   * @see remixlab.bias.ext.Profile#setBinding(Object, Shortcut, String)
   */
  public void setKeyBinding(Object object, int vkey, String action) {
    profile.setBinding(object, new KeyboardShortcut(vkey), action);
  }

  /**
   * Same as {@code profile.setBinding(object, new KeyboardShortcut(key), action)}.
   * 
   * @see remixlab.bias.ext.Profile#setBinding(Object, Shortcut, String)
   */
  public void setKeyBinding(Object object, char key, String action) {
    profile.setBinding(object, new KeyboardShortcut(key), action);
  }

  /**
   * Same as {@code return profile.hasBinding(new KeyboardShortcut(vkey))}.
   * 
   * @see remixlab.bias.ext.Profile#hasBinding(Shortcut)
   */
  public boolean hasKeyBinding(int vkey) {
    return profile.hasBinding(new KeyboardShortcut(vkey));
  }

  /**
   * Same as {@code return profile.hasBinding(new KeyboardShortcut(key))}.
   * 
   * @see remixlab.bias.ext.Profile#hasBinding(Shortcut)
   */
  public boolean hasKeyBinding(char key) {
    return profile.hasBinding(new KeyboardShortcut(key));
  }

  /**
   * Same as {@code profile.removeBinding(new KeyboardShortcut(vkey))}.
   * 
   * @see remixlab.bias.ext.Profile#removeBinding(Shortcut)
   */
  public void removeKeyBinding(int vkey) {
    profile.removeBinding(new KeyboardShortcut(vkey));
  }

  /**
   * Same as {@code profile.removeBinding(new KeyboardShortcut(key))}.
   * 
   * @see remixlab.bias.ext.Profile#removeBinding(Shortcut)
   */
  public void removeKeyBinding(char key) {
    profile.removeBinding(new KeyboardShortcut(key));
  }

  /**
   * Same as {@code profile.setBinding(new KeyboardShortcut(mask, vkey), action)}.
   * 
   * @see remixlab.bias.ext.Profile#setBinding(Shortcut, String)
   */
  public void setKeyBinding(int mask, int vkey, String action) {
    profile.setBinding(new KeyboardShortcut(mask, vkey), action);
  }

  /**
   * Same as {@code profile.setBinding(object, new KeyboardShortcut(mask, vkey), action)}
   * .
   * 
   * @see remixlab.bias.ext.Profile#setBinding(Object, Shortcut, String)
   */
  public void setKeyBinding(Object object, int mask, int vkey, String action) {
    profile.setBinding(object, new KeyboardShortcut(mask, vkey), action);
  }

  /**
   * Same as {@code return profile.hasBinding(new KeyboardShortcut(mask, vkey))} .
   * 
   * @see remixlab.bias.ext.Profile#hasBinding(Shortcut)
   */
  public boolean hasKeyBinding(int mask, int vkey) {
    return profile.hasBinding(new KeyboardShortcut(mask, vkey));
  }

  /**
   * Same as {@code profile.removeBinding(new KeyboardShortcut(mask, vkey))}.
   * 
   * @see remixlab.bias.ext.Profile#removeBinding(Shortcut)
   */
  public void removeKeyBinding(int mask, int vkey) {
    profile.removeBinding(new KeyboardShortcut(mask, vkey));
  }

  /**
   * Same as {@code setKeyBinding(mask, Scene.keyCode(key), action)}.
   * 
   * @see #setKeyBinding(int, int, String)
   */
  public void setKeyBinding(int mask, char key, String action) {
    setKeyBinding(mask, Scene.keyCode(key), action);
  }

  /**
   * Same as {@code setKeyBinding(object, mask, Scene.keyCode(key), action)}.
   * 
   * @see #setKeyBinding(Object, int, int, String)
   */
  public void setKeyBinding(Object object, int mask, char key, String action) {
    setKeyBinding(object, mask, Scene.keyCode(key), action);
  }

  /**
   * Same as {@code hasKeyBinding(mask, Scene.keyCode(key))}.
   * 
   * @see #hasKeyBinding(int, int)
   */
  public boolean hasKeyBinding(int mask, char key) {
    return hasKeyBinding(mask, Scene.keyCode(key));
  }

  /**
   * Same as {@code removeKeyBinding(mask, Scene.keyCode(key))}.
   * 
   * @see #removeKeyBinding(int, int)
   */
  public void removeKeyBinding(int mask, char key) {
    removeKeyBinding(mask, Scene.keyCode(key));
  }

  /**
   * Remove all key bindings. Same as
   * {@code profile.removeBindings(KeyboardShortcut.class)}.
   * 
   * @see remixlab.bias.ext.Profile#removeBindings(Class)
   */
  public void removeKeyBindings() {
    profile.removeBindings(KeyboardShortcut.class);
  }

  // click

  /**
   * Same as {@code profile.setBinding(new ClickShortcut(id, count), action)}.
   * 
   * @see remixlab.bias.ext.Profile
   */
  public void setClickBinding(int id, int count, String action) {
    if (count > 0 && count < 4)
      profile.setBinding(new ClickShortcut(id, count), action);
    else
      System.out.println("Warning no click binding set! Count should be between 1 and 3");
  }

  /**
   * Same as {@code profile.setBinding(object, new ClickShortcut(id, count), action)}.
   * 
   * @see remixlab.bias.ext.Profile#setBinding(Shortcut, String)
   */
  public void setClickBinding(Object object, int id, int count, String action) {
    if (count > 0 && count < 4)
      profile.setBinding(object, new ClickShortcut(id, count), action);
    else
      System.out.println("Warning no click binding set! Count should be between 1 and 3");
  }

  /**
   * Same as {@code return profile.hasBinding(new ClickShortcut(id, count))}.
   * 
   * @see remixlab.bias.ext.Profile#hasBinding(Shortcut)
   */
  public boolean hasClickBinding(int id, int count) {
    return profile.hasBinding(new ClickShortcut(id, count));
  }

  /**
   * Same as {@code profile.removeBinding(new ClickShortcut(id, count))}.
   * 
   * @see remixlab.bias.ext.Profile#removeBinding(Shortcut)
   */
  public void removeClickBinding(int id, int count) {
    profile.removeBinding(new ClickShortcut(id, count));
  }

  /**
   * Same as
   * {@code for (int i = 1; i < 4; i++) profile.removeBinding(new ClickShortcut(id, i))}.
   * 
   * @param id
   */
  public void removeClickBinding(int id) {
    for (int i = 1; i < 4; i++)
      profile.removeBinding(new ClickShortcut(id, i));
  }

  /**
   * Remove all click bindings. Same as
   * {@code profile.removeBindings(ClickShortcut.class)}.
   * 
   * @see remixlab.bias.ext.Profile#removeBindings(Class)
   */
  public void removeClickBindings() {
    profile.removeBindings(ClickShortcut.class);
  }

  /**
   * Returns the frame {@link remixlab.bias.ext.Profile} instance.
   */
  public Profile profile() {
    return profile;
  }

  /**
   * Sets the frame {@link remixlab.bias.ext.Profile} instance. Note that the
   * {@link remixlab.bias.ext.Profile#grabber()} object should equals this scene.
   * 
   * @see #setBindings(InteractiveFrame)
   */
  public void setProfile(Profile p) {
    if (p.grabber() == this)
      profile = p;
    else
      System.out.println("Nothing done, profile grabber is different than this grabber");
  }

  /**
   * Same as {@code profile.from(otherFrame.profile())}.
   * 
   * @see remixlab.bias.ext.Profile#set(Profile)
   * @see #setProfile(Profile)
   */
  public void setBindings(InteractiveFrame otherFrame) {
    profile.set(otherFrame.profile());
  }

  /**
   * Returns a description of all the bindings this frame holds.
   */
  public String info() {
    return profile.info();
  }

  /**
   * Calls {@link #setWorldMatrix(Frame)}, {@link #setBindings(InteractiveFrame)}, and
   * {@link #setShape(InteractiveFrame)} on the other frame instance.
   * <p>
   * After calling {@code set} a call to {@code this.equals(otherFrame)} should return
   * {@code true}.
   */
  public void set(InteractiveFrame otherFrame) {
    setWorldMatrix(otherFrame);
    setBindings(otherFrame);
    setShape(otherFrame);
  }

  /**
   * @deprecated use {@link #set(Frame)}.
   */
  @Deprecated
  public void fromFrame(InteractiveFrame otherFrame) {
    super.fromFrame(otherFrame);
    setShape(otherFrame);
  }

  /**
   * Same as {@code ((Scene) scene).applyTransformation(pg, this)}.
   * 
   * @see remixlab.proscene.Scene#applyTransformation(PGraphics, Frame)
   */
  public void applyTransformation(PGraphics pg) {
    Scene.applyTransformation(pg, this);
  }

  /**
   * Same as {@code ((Scene) scene).applyWorldTransformation(pg, this)}.
   * 
   * @see remixlab.proscene.Scene#applyWorldTransformation(PGraphics, Frame)
   */
  public void applyWorldTransformation(PGraphics pg) {
    Scene.applyWorldTransformation(pg, this);
  }

  /**
   * Enables highlighting of the frame visual representation when picking takes place
   * ({@link #grabsInput()} returns {@code true}) according to {@code mode} as follows:
   * 
   * <ol>
   * <li>{@link remixlab.proscene.InteractiveFrame.HighlightingMode#NONE}: no highlighting
   * takes place.</li>
   * <li>{@link remixlab.proscene.InteractiveFrame.HighlightingMode#FRONT_SHAPE}: the
   * front-shape (see {@link #setFrontShape(PShape)}) is scaled by a {@code 1.15}
   * factor.</li>
   * <li>{@link remixlab.proscene.InteractiveFrame.HighlightingMode#PICKING_SHAPE}: the
   * picking-shape (see {@link #setPickingShape(PShape)} is displayed instead of the
   * front-shape.</li>
   * <li>{@link remixlab.proscene.InteractiveFrame.HighlightingMode#FRONT_PICKING_SHAPES}:
   * both, the front and the picking shapes are displayed.</li>
   * </ol>
   * 
   * Default is {@link remixlab.proscene.InteractiveFrame.HighlightingMode#FRONT_SHAPE}.
   * 
   * @see #highlightingMode()
   */
  public void setHighlightingMode(HighlightingMode mode) {
    if (isEyeFrame())
      AbstractScene.showOnlyEyeWarning("setHighlightingMode", false);
    if (mode == HighlightingMode.FRONT_PICKING_SHAPES
        && (fShape.equals(pShape) || fShape.isReset() || pShape.isReset()))
      System.out.println(
          "Warning: FRONT_PICKING_SHAPES highlighting mode requires the frame to have different non-null front and picking shapes");
    if (mode == HighlightingMode.PICKING_SHAPE && pShape.isReset())
      System.out
          .println("Warning: PICKING_SHAPE highlighting mode requires the frame to have a non-null picking shape");
    highlight = mode;
  }

  /**
   * Returns the highlighting mode.
   * 
   * @see #setHighlightingMode(HighlightingMode)
   */
  public HighlightingMode highlightingMode() {
    return highlight;
  }

  /**
   * Internal use. Frame graphics color to use in the
   * {@link remixlab.proscene.Scene#pickingBuffer()}.
   */
  protected int id() {
    // see here:
    // http://stackoverflow.com/questions/2262100/rgb-int-to-rgb-python
    return (255 << 24) | ((id & 255) << 16) | (((id >> 8) & 255) << 8) | (id >> 16) & 255;
  }

  /**
   * Same as {@code shiftFrontShape(shift); shiftPickingShape(shift)}.
   * <p>
   * This method is only meaningful when frame is not eyeFrame.
   * 
   * @see #shiftFrontShape(Vec)
   * @see #shiftPickingShape(Vec)
   */
  public void shiftShape(Vec shift) {
    shiftFrontShape(shift);
    shiftPickingShape(shift);
  }

  /**
   * Shifts the front-frame shape respect to the frame {@link #position()}. Default value
   * is zero.
   * <p>
   * This method is only meaningful when frame is not eyeFrame.
   * 
   * @see #shiftShape(Vec)
   * @see #shiftPickingShape(Vec)
   */
  public void shiftFrontShape(Vec shift) {
    fShape.shift(shift);
  }

  /**
   * Shifts the picking-frame shape respect to the frame {@link #position()}. Default
   * value is zero.
   * <p>
   * This method is only meaningful when frame is not eyeFrame.
   * 
   * @see #shiftShape(Vec)
   * @see #shiftFrontShape(Vec)
   */
  public void shiftPickingShape(Vec shift) {
    pShape.shift(shift);
  }

  @Override
  public void setPickingPrecision(PickingPrecision precision) {
    if (precision == PickingPrecision.EXACT)
      if (!scene().isPickingBufferEnabled())
        System.out.println(
            "Warning: EXACT picking precision will behave like FIXED until the scene.pickingBuffer() is enabled.");
    pkgnPrecision = precision;
    if (isEyeFrame()) {
      AbstractScene.showOnlyEyeWarning("setPickingPrecision", false);
      return;
    }
    updatePickingBufferCache();
  }

  /**
   * Checks for the existence of the
   * {@link remixlab.bias.core.Grabber#checkIfGrabsInput(BogusEvent)} condition at the
   * {@link #scene()} {@link remixlab.proscene.Scene#pApplet()} and it doesn't find it
   * there, looks for it at this instance.
   * <p>
   * Allows to register a {@link remixlab.bias.core.Grabber#checkIfGrabsInput(BogusEvent)}
   * on custom {@code BogusEvent} types without the need to derive from this class.
   */
  @Override
  public boolean checkIfGrabsInput(BogusEvent event) {
    // TODO: Performance boost, but will not allow to be reflective on events derived from
    // default ones
    // if(event instanceof KeyboardEvent || event instanceof ClickEvent || event
    // instanceof MotionEvent)
    // return super.checkIfGrabsInput(event);
    Method mth = null;
    Object obj = scene().pApplet();
    boolean frameParam = false;
    // 1. Retrieving
    try {
      mth = obj.getClass().getMethod("checkIfGrabsInput", new Class<?>[] { event.getClass() });
    } catch (Exception e1) {
      try {
        mth = obj.getClass().getMethod("checkIfGrabsInput",
            new Class<?>[] { InteractiveFrame.class, event.getClass() });
        frameParam = true;
      } catch (Exception e2) {
        obj = this;
        try {
          mth = obj.getClass().getMethod("checkIfGrabsInput", new Class<?>[] { event.getClass() });
        } catch (Exception e3) {
          PApplet.println("Error: no picking condition for " + event.getClass().getName());
          e1.printStackTrace();
          e2.printStackTrace();
          e3.printStackTrace();
        }
      }
    }
    // 2. Invocation
    try {
      if (frameParam)
        return (boolean) mth.invoke(obj, new Object[] { this, event });
      else
        return (boolean) mth.invoke(obj, new Object[] { event });
    } catch (Exception e) {
      PApplet.println("Error: no picking condition found");
      e.printStackTrace();
    }
    return false;
  }

  /**
   * An interactive-frame may be picked using
   * <a href="http://schabby.de/picking-opengl-ray-tracing/">'ray-picking'</a> with a
   * color buffer (see {@link remixlab.proscene.Scene#pickingBuffer()}). This method
   * compares the color of the {@link remixlab.proscene.Scene#pickingBuffer()} at
   * {@code (x,y)} with {@link #id()}. Returns true if both colors are the same, and false
   * otherwise.
   * <p>
   * This method is only meaningful when {@link #isEyeFrame()} returns false.
   * 
   * @see #setPickingPrecision(PickingPrecision)
   * @see #isEyeFrame()
   */
  @Override
  public final boolean checkIfGrabsInput(float x, float y) {
    if (isEyeFrame()) {
      AbstractScene.showOnlyEyeWarning("checkIfGrabsInput", false);
      return false;
    }
    if (pickingPrecision() != PickingPrecision.EXACT || pShape.isReset() || !scene().isPickingBufferEnabled())
      return super.checkIfGrabsInput(x, y);
    int index = (int) y * gScene.width() + (int) x;
    if ((0 <= index) && (index < scene().pickingBuffer().pixels.length))
      return scene().pickingBuffer().pixels[index] == id();
    return false;
  }

  /**
   * Same as {@code return profile.hasBinding(event.shortcut())}.
   * 
   * @see remixlab.proscene.KeyAgent#keyEvent(processing.event.KeyEvent)
   */
  @Override
  public boolean checkIfGrabsInput(KeyboardEvent event) {
    return profile.hasBinding(event.shortcut());
  }

  /**
   * Same as {@code draw(scene.pg())}.
   * 
   * @see remixlab.proscene.Scene#drawFrames(PGraphics)
   */
  public void draw() {
    if (!fShape.isReset())
      draw(scene().pg());
  }

  /**
   * Draw the visual representation of the frame into the given PGraphics using the
   * current point of view (see
   * {@link remixlab.proscene.Scene#applyWorldTransformation(PGraphics, Frame)} ).
   * <p>
   * This method is internally called by the scene to
   * {@link remixlab.proscene.Scene#drawFrames(PGraphics)} into the
   * {@link remixlab.proscene.Scene#disablePickingBuffer()} and by {@link #draw()} to draw
   * the frame into the scene main {@link remixlab.proscene.Scene#pg()}.
   */
  public boolean draw(PGraphics pg) {
    if (fShape.isReset())
      return false;
    pg.pushMatrix();
    Scene.applyWorldTransformation(pg, this);
    visit(pg);
    pg.popMatrix();
    return true;
  }

  @Override
  public void visit() {
    visit(scene().targetPGraphics);
  }

  protected void visit(PGraphics pg) {
    pg.pushStyle();
    if (pg == scene().pickingBuffer()) {
      float r = (float) (id & 255) / 255.f;
      float g = (float) ((id >> 8) & 255) / 255.f;
      float b = (float) ((id >> 16) & 255) / 255.f;
      // funny, only safe way. Otherwise break things horribly when setting shapes
      // and there are more than one iFrame
      scene().applyPickingBufferShaders();
      scene().pickingBufferShaderTriangle.set("id", new PVector(r, g, b));
      scene().pickingBufferShaderLine.set("id", new PVector(r, g, b));
      scene().pickingBufferShaderPoint.set("id", new PVector(r, g, b));
    }
    if (!isEyeFrame()) {
      pg.pushMatrix();
      if (pg != scene().pickingBuffer()) {
        switch (highlightingMode()) {
        case FRONT_PICKING_SHAPES:
          fShape.draw(pg);
          if (!fShape.equals(pShape) && grabsInput())
            pShape.draw(pg);
          break;
        case NONE:
          fShape.draw(pg);
          break;
        case PICKING_SHAPE:
          if (grabsInput())
            pShape.draw(pg);
          else
            fShape.draw(pg);
          break;
        case FRONT_SHAPE:
          if (grabsInput())
            pg.scale(1.15f);
          fShape.draw(pg);
          break;
        }
      } else
        pShape.draw(pg);
      pg.popMatrix();
    }
    pg.popStyle();
  }

  // shape

  /**
   * Internal cache optimization method.
   */
  protected void updatePickingBufferCache() {
    if (!isEyeFrame() && pickingPrecision() == PickingPrecision.EXACT && !pShape.isReset()) {
      scene().unchachedBuffer = true;
      return;
    }
    for (InteractiveFrame frame : scene().frames())
      if (!frame.isEyeFrame() && frame.pickingPrecision() == PickingPrecision.EXACT && !frame.pShape.isReset()) {
        scene().unchachedBuffer = true;
        return;
      }
    scene().unchachedBuffer = false;
  }

  /**
   * Same as {@code setFrontShape(ps); setPickingShape(ps);}.
   * 
   * @see #setShape(Object, String)
   * @see #setShape(String)
   * @see #setShape(InteractiveFrame)
   * @see #resetShape()
   * @see #setFrontShape(PShape)
   * @see #setPickingShape(PShape)
   * @see #resetShape()
   */
  public void setShape(PShape ps) {
    setFrontShape(ps);
    setPickingShape(ps);
  }

  /**
   * Retained mode rendering of the front shape using a PShape.
   * <p>
   * Replaces previous frame front-shape with {@code ps}.
   * 
   * @see #setShape(PShape)
   * @see #setFrontShape(Object, String)
   * @see #setFrontShape(String)
   * @see #setFrontShape(InteractiveFrame)
   * @see #resetFrontShape()
   * @see #setPickingShape(PShape)
   * @see #resetShape()
   */
  public void setFrontShape(PShape ps) {
    fShape.set(ps);
  }

  /**
   * Retained mode rendering of the picking shape using a PShape.
   * <p>
   * Replaces previous frame picking-shape with {@code ps}.
   * 
   * @see #setShape(PShape)
   * @see #setFrontShape(PShape)
   * @see #setPickingShape(Object, String)
   * @see #setPickingShape(String)
   * @see #setPickingShape(InteractiveFrame)
   * @see #resetPickingShape()
   * @see #resetShape()
   */
  public void setPickingShape(PShape ps) {
    pShape.set(ps);
    updatePickingBufferCache();
  }

  /**
   * Same as {@code setFrontShape(otherFrame); setPickingShape(otherFrame);}.
   * 
   * @see #setShape(PShape)
   * @see #setShape(Object, String)
   * @see #setShape(String)
   * @see #resetShape()
   * @see #setFrontShape(InteractiveFrame)
   * @see #setPickingShape(InteractiveFrame)
   * @see #resetShape()
   */
  public void setShape(InteractiveFrame otherFrame) {
    setFrontShape(otherFrame);
    setPickingShape(otherFrame);
  }

  /**
   * Sets the frame front-shape from that of other frame. Useful when sharing the same
   * front-shape drawing method among different frame instances is desirable.
   * 
   * @see #setShape(InteractiveFrame)
   * @see #setFrontShape(PShape)
   * @see #setFrontShape(Object, String)
   * @see #setFrontShape(String)
   * @see #resetFrontShape()
   * @see #setPickingShape(InteractiveFrame)
   * @see #resetShape()
   */
  public void setFrontShape(InteractiveFrame otherFrame) {
    fShape.set(otherFrame.fShape);
  }

  /**
   * Sets the frame picking-shape from that of other frame. Useful when sharing the same
   * picking-shape drawing method among different frame instances is desirable.
   * 
   * @see #setShape(InteractiveFrame)
   * @see #setFrontShape(InteractiveFrame)
   * @see #setPickingShape(PShape)
   * @see #setPickingShape(Object, String)
   * @see #setPickingShape(String)
   * @see #setPickingShape(InteractiveFrame)
   * @see #resetPickingShape()
   * @see #resetShape()
   */
  public void setPickingShape(InteractiveFrame otherFrame) {
    pShape.set(otherFrame.pShape);
    updatePickingBufferCache();
  }

  /**
   * Same as {@code resetFrontShape(); resetPickingShape();}.
   * 
   * @see #setShape(PShape)
   * @see #setShape(Object, String)
   * @see #setShape(String)
   * @see #setShape(InteractiveFrame)
   * @see #resetFrontShape()
   * @see #resetPickingShape()
   * @see #resetShape()
   */
  public void resetShape() {
    resetFrontShape();
    resetPickingShape();
  }

  /**
   * Resets the front-shape which is wrapped by this interactive-frame.
   * 
   * @see #resetShape()
   * @see #setFrontShape(PShape)
   * @see #setFrontShape(Object, String)
   * @see #setFrontShape(String)
   * @see #setFrontShape(InteractiveFrame)
   * @see #resetPickingShape()
   * @see #resetShape()
   */
  public void resetFrontShape() {
    fShape.reset();
  }

  /**
   * Resets the picking-shape which is wrapped by this interactive-frame.
   * 
   * @see #resetShape()
   * @see #resetFrontShape()
   * @see #setPickingShape(PShape)
   * @see #setPickingShape(Object, String)
   * @see #setPickingShape(String)
   * @see #setPickingShape(InteractiveFrame)
   * @see #resetShape()
   */
  public void resetPickingShape() {
    pShape.reset();
    updatePickingBufferCache();
  }

  /**
   * Same as {@code setFrontShape(methodName); setPickingShape(methodName);}.
   * 
   * @see #setShape(PShape)
   * @see #setShape(Object, String)
   * @see #setShape(InteractiveFrame)
   * @see #resetShape()
   * @see #setFrontShape(String)
   * @see #setPickingShape(String)
   * @see #resetShape()
   */
  public void setShape(String methodName) {
    setFrontShape(methodName);
    setPickingShape(methodName);
  }

  /**
   * Same as {@code setFrontShape(obj, methodName); setPickingShape(obj, methodName);}.
   * 
   * @see #setShape(PShape)
   * @see #setShape(String)
   * @see #setShape(InteractiveFrame)
   * @see #resetShape()
   * @see #setFrontShape(Object, String)
   * @see #setPickingShape(Object, String)
   * @see #resetShape()
   */
  public void setShape(Object obj, String methodName) {
    setFrontShape(obj, methodName);
    setPickingShape(obj, methodName);
  }

  /**
   * Immediate mode rendering of the front shape using a graphics procedure
   * ({@code methodName}) implemented by the {@code object}.
   * <p>
   * Attempt to set an immediate mode visual representation to the frame from the graphics
   * procedure {@code methodName} implemented by the {@code object}. The graphics
   * procedure may have two different prototypes:
   * <ol>
   * <li><b>public void methodName(PGraphics)</b></li>
   * <li><b>public void methodName(InteractiveFrame, PGraphics)</b></li>
   * </ol>
   * Note that the latter prototype isn't available if this frame {@link #isEyeFrame()}.
   * 
   * @param object
   *          the object defining the shape graphics-procedure
   * @param methodName
   *          the front-shape graphics-procedure
   * 
   * @see #setShape(Object, String)
   * @see #setFrontShape(PShape)
   * @see #setFrontShape(String)
   * @see #setFrontShape(InteractiveFrame)
   * @see #resetFrontShape()
   * @see #setPickingShape(Object, String)
   * @see #resetShape()
   */
  public void setFrontShape(Object object, String methodName) {
    if (pShape.mth != null)
      if (pShape.obj == object && pShape.mth.getName().equals(methodName)) {
        fShape.set(pShape);
        // version without copying the shift reference:
        // fShape.set(pShape.obj, pShape.mth);
        return;
      }
    fShape.set(object, methodName);
  }

  /**
   * Immediate mode rendering of the picking shape using a graphics procedure
   * ({@code methodName}) implemented by the {@code object}.
   * <p>
   * Attempt to set an immediate mode visual representation to the frame from the graphics
   * procedure {@code methodName} implemented by the {@code object}. The graphics
   * procedure may have two different prototypes:
   * <ol>
   * <li><b>public void methodName(PGraphics)</b></li>
   * <li><b>public void methodName(InteractiveFrame, PGraphics)</b></li>
   * </ol>
   * Note that the latter prototype isn't available if this frame {@link #isEyeFrame()}.
   * 
   * @param object
   *          the object defining the shape graphics-procedure
   * @param methodName
   *          the front-shape graphics-procedure
   * 
   * @see #setShape(Object, String)
   * @see #setFrontShape(Object, String)
   * @see #setPickingShape(PShape)
   * @see #setPickingShape(String)
   * @see #setPickingShape(InteractiveFrame)
   * @see #resetPickingShape()
   * @see #resetShape()
   */
  public void setPickingShape(Object object, String methodName) {
    if (fShape.mth != null)
      if (fShape.obj == object && fShape.mth.getName().equals(methodName)) {
        pShape.set(fShape);
        updatePickingBufferCache();
        // version without copying the shift reference:
        // pShape.set(fShape.obj, fShape.mth);
        return;
      }
    if (pShape.set(object, methodName))
      updatePickingBufferCache();
  }

  /**
   * Immediate mode rendering of the front shape using a graphics procedure
   * ({@code methodName}).
   * <p>
   * Attempt to set an immediate mode visual representation to the frame from the graphics
   * procedure defined by {@code methodName} which may have two different prototypes:
   * <ol>
   * <li><b>public void methodName(PGraphics)</b></li>
   * <li><b>public void methodName(InteractiveFrame, PGraphics)</b></li>
   * </ol>
   * These prototypes may (or may not) be used depending on the object declaring the
   * procedure, as follows:
   * <ol>
   * <li>The {@link remixlab.proscene.Scene#pApplet()}, which may use both
   * prototypes;</li>
   * <li>The {@link remixlab.proscene.InteractiveFrame} instance this shape is attached
   * to, which may use only prototype 1, or;</li>
   * <li>The {@link remixlab.proscene.InteractiveFrame#scene()} handling that frame
   * instance, which may use both prototypes.
   * </ol>
   * The algorithm looks for the prototypes within the objects in the above order.
   * 
   * @param methodName
   *          the front-shape graphics-procedure
   * 
   * @see #setShape(String)
   * @see #setFrontShape(PShape)
   * @see #setFrontShape(Object, String)
   * @see #setFrontShape(InteractiveFrame)
   * @see #resetFrontShape()
   * @see #setPickingShape(String)
   * @see #resetShape()
   */
  public void setFrontShape(String methodName) {
    if (pShape.mth != null)
      if (pShape.mth.getName().equals(methodName)) {
        if (pShape.obj == this || pShape.obj == scene()) {
          // this copies also the shift reference
          // use set(Object object, Method method) if need to keep the shift reference
          fShape.set(pShape);
          return;
        }
      }
    fShape.set(methodName);
  }

  /**
   * Immediate mode rendering of the picking shape using a graphics procedure
   * ({@code methodName}).
   * <p>
   * Attempt to set an immediate mode visual representation to the frame from the graphics
   * procedure defined by {@code methodName} which may have two different prototypes:
   * <ol>
   * <li><b>public void methodName(PGraphics)</b></li>
   * <li><b>public void methodName(InteractiveFrame, PGraphics)</b></li>
   * </ol>
   * These prototypes may (or may not) be used depending on the object declaring the
   * procedure, as follows:
   * <ol>
   * <li>The {@link remixlab.proscene.Scene#pApplet()}, which may use both
   * prototypes;</li>
   * <li>The {@link remixlab.proscene.InteractiveFrame} instance this shape is attached
   * to, which may use only prototype 1, or;</li>
   * <li>The {@link remixlab.proscene.InteractiveFrame#scene()} handling that frame
   * instance, which may use both prototypes.
   * </ol>
   * The algorithm looks for the prototypes within the objects in the above order.
   * 
   * @param methodName
   *          the shape graphics-procedure
   * 
   * @see #setShape(String)
   * @see #setFrontShape(String)
   * @see #setPickingShape(PShape)
   * @see #setPickingShape(Object, String)
   * @see #setPickingShape(InteractiveFrame)
   * @see #resetPickingShape()
   * @see #resetShape()
   */
  public void setPickingShape(String methodName) {
    if (fShape.mth != null)
      if (fShape.mth.getName().equals(methodName)) {
        if (fShape.obj == this || fShape.obj == scene()) {
          // this copies also the shift reference
          // use set(Object object, Method method) if need to keep the shift reference
          pShape.set(fShape);
          updatePickingBufferCache();
          return;
        }
      }
    if (pShape.set(methodName))
      updatePickingBufferCache();
  }

  /**
   * @deprecated use {@link #setShape(Object, String)}.
   */
  @Deprecated
  public void addGraphicsHandler(Object obj, String methodName) {
    setShape(obj, methodName);
  }

  /**
   * @deprecated use {@link #resetShape()}.
   */
  @Deprecated
  public void removeGraphicsHandler() {
    resetShape();
  }

  /**
   * @deprecated keep pshape and method references at application space.
   */
  @Deprecated
  public PShape shape() {
    return fShape.shp;
  }
}