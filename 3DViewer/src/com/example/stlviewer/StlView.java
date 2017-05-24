package com.example.stlviewer;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;


/**
 * StlView class.
 * @author Alan
 *
 */
public class StlView extends GLSurfaceView{

	private StlRenderer stlRenderer;
	/*
	 * rotation algorithm
	 * source:
	 * https://www.raywenderlich.com/12667/how-to-rotate-a-3d-object-using-touches-with-opengl
	 */
	private final float TOUCH_SCALE_FACTOR = 180.0f / 320/2;
	private float previousX;
	private float previousY;
	private boolean isRotate = true;
	private static final int TOUCH_NONE = 0;
	private int touchMode = TOUCH_NONE;

	/**
	 * constructor for StlView class.
	 * @param context current context
	 * @param stlModel stl model
	 */
	public StlView(Context context, STLModel stlModel) {
		super(context);

		stlRenderer = new StlRenderer(stlModel);
		setRenderer(stlRenderer);
		stlRenderer.requestRedraw();
	}

	/**
	 * check if model can rotate or not.
	 * @return if model is rotate
	 */
	public boolean isRotate() {
		return isRotate;
	}

	/**
	 * set isRotate value.
	 * @param isRotate take the boolean that the model is rotating or not.
	 */
	public void setRotate(boolean isRotate) {
		this.isRotate = isRotate;
	}

	/**
	 * doing all touching and rotation calculation.
	 * @param simple event
	 * @return always true for touch event.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (touchMode == TOUCH_NONE) {
			float x = event.getX();
			float y = event.getY();

			float dx = x - previousX;
			float dy = y - previousY;
			previousX = x;
			previousY = y;
			/*
			 * calculating after rotation x, y position.
			 */
			if (isRotate) {
				stlRenderer.angleX += dx * TOUCH_SCALE_FACTOR;
				stlRenderer.angleY += dy * TOUCH_SCALE_FACTOR;
			} else {
				// change view point
				//for not rotating case
				stlRenderer.positionX += dx * TOUCH_SCALE_FACTOR / 5;
				stlRenderer.positionY += dy * TOUCH_SCALE_FACTOR / 5;
			}
			stlRenderer.requestRedraw();
			requestRender();
		}

		return true;
	}


	/**
	 * when there is a new stl model, ask for redraw.
	 * @param stlModel stl model
	 */
	public void setNewModel(STLModel stlModel){
		stlRenderer.requestRedraw(stlModel);
	}

	/**
	 * simple redraw the view.
	 */
	public void requestRedraw(){
		stlRenderer.requestRedraw();
	}

	/**
	 * delete the renderer in mainActivity.
	 */
	public void delete (){
		stlRenderer.delete();
	}
}
