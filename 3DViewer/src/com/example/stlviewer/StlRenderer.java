package com.example.stlviewer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLU;
import android.opengl.GLSurfaceView.Renderer;

public class StlRenderer implements Renderer{
	public static final int FRAME_BUFFER_COUNT = 5;
	public float angleX;
	public float angleY;
	public float positionX = 0f;
	public float positionY = 0f;
	public float translation_z;

	private static int bufferCounter = FRAME_BUFFER_COUNT;
	private STLModel stlModel;
	
	/**
	 * stlRenderer class constructor.
	 * @param stlModel
	 */
	public StlRenderer(STLModel stlModel){
		this.stlModel = stlModel;
		setTransLation_Z();
	}
	
	/**
	 * relocate stl model in the middle of the view.
	 */
	private void setTransLation_Z() {

		float distance_x = stlModel.maxX - stlModel.minX;
		float distance_y = stlModel.maxY - stlModel.minY;
		float distance_z = stlModel.maxZ - stlModel.minZ;
		translation_z = distance_x;
		if (translation_z < distance_y) {
			translation_z = distance_y;
		}
		if (translation_z < distance_z) {
			translation_z = distance_z;
		}
		translation_z *= -2;
		
	}
	
	/**
	 * delete stl model.
	 * it's used in View class.
	 */
	public void delete(){
		stlModel.delete();
		stlModel=null;
	}
	
	/**
	 * real redraw
	 */
	public void requestRedraw() {
		bufferCounter = FRAME_BUFFER_COUNT;
	}
	
	/**
	 * whenever there is a new model import, call this method.
	 * @param stlModel
	 */
	public void requestRedraw(STLModel stlModel) {
		
		this.stlModel = stlModel;
		setTransLation_Z();
		bufferCounter = FRAME_BUFFER_COUNT;
	}
	
	/**
	 * Routine set up.
	 */
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// TODO Auto-generated method stub
		gl.glEnable(GL10.GL_BLEND);
		gl.glClearDepthf(1.0f);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glDepthFunc(GL10.GL_LEQUAL);
		gl.glHint(3152, 4354);
		gl.glEnable(GL10.GL_NORMALIZE);
		gl.glShadeModel(GL10.GL_SMOOTH);

		gl.glMatrixMode(GL10.GL_PROJECTION);

		gl.glEnable(GL10.GL_LIGHTING);
		gl.glLightModelfv(GL10.GL_LIGHT_MODEL_AMBIENT, createTriangleBuffer(new  float[]{0.5f,0.5f,0.5f,1.0f}));
	}

	/**
	 * Routine set up.
	 */
	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		float aspectRatio = (float) width / height;

		gl.glViewport(0, 0, width, height);
		
		gl.glLoadIdentity();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		GLU.gluPerspective(gl, 45f, aspectRatio, 1f, 5000f);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		GLU.gluLookAt(gl, 0, 0, 100f, 0, 0, 0, 0, 1f, 0);
	}
	
	/**
	 * routine set up.
	 * source:
	 * https://gist.github.com/ybakos/4151696#file-graphicgldemoactivity-java-L62
	 * https://www.opengl.org/discussion_boards/showthread.php/127643-glRotatef
	 * this is about how rotation works
	 * https://www.codota.com/doSearchScenarios?searchQuery=GL10.glEnableClientState&source=snippet
	 * this source is about glEnableClientState.
	 * for rotation,
	 * source:
	 * http://stackoverflow.com/questions/7631890/opengl-rotation-at-a-point
	 * 
	 * Wednesday, March, 1, 2017
	 */
	@Override
	public void onDrawFrame(GL10 gl) {

		if (bufferCounter < 1) {
			return;
		}
		bufferCounter--;
		gl.glLoadIdentity();
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glTranslatef(positionX, -positionY, 0);
		gl.glTranslatef(0, 0, translation_z);
		gl.glRotatef(angleX, 0, 1, 0);
		gl.glRotatef(angleY, 1, 0, 0);
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		if (stlModel != null) {
			gl.glMaterialfv(GL10.GL_FRONT, GL10.GL_AMBIENT, new float[] { 0.75f,0.75f,0.75f,1.0f }, 0);
			gl.glMaterialfv(GL10.GL_FRONT, GL10.GL_DIFFUSE, new float[] { 0.75f,0.75f,0.75f,1.0f }, 0);
			gl.glEnable(GL10.GL_COLOR_MATERIAL);
			gl.glPushMatrix();
			stlModel.draw(gl);
			gl.glPopMatrix();
			gl.glDisable(GL10.GL_COLOR_MATERIAL);
		}
	}
	
	private FloatBuffer createTriangleBuffer(float[] vertexArray) {
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertexArray.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		FloatBuffer triangleBuffer = vbb.asFloatBuffer();
		triangleBuffer.put(vertexArray);
		triangleBuffer.position(0);
		return triangleBuffer;
	}
	
}
