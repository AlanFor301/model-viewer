package com.example.stlviewer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import javax.microedition.khronos.opengles.GL10;
import android.content.Context;
import android.os.AsyncTask;

public class STLModel {
	private byte[] stlBytes = null;
	List<Float> normalList;
	FloatBuffer triangleBuffer;
	ModelToViewInterface interChange;
	
	public float maxX;
	public float maxY;
	public float maxZ;
	public float minX;
	public float minY;
	public float minZ;
	
	
	

	private  float[] normal_array=null;
	private  float[] vertex_array=null;
	private  int vertext_size=0;
	
	/**
	 * constructor STLModel
	 * @param stlBytes input stl array
	 * @param context current context
	 * @param interChange model to view interface
	 */
	public STLModel(byte[] stlBytes, Context context ,ModelToViewInterface interChange) {
		this.stlBytes = stlBytes;
		this.interChange=interChange;
		processSTL(stlBytes, context);
	}

	/**
	 * convert bytes in arrays to integer for later matrix calculation.  
	 * Source:
	 * https://github.com/apache/hive/blob/master/serde/src/java/org/apache/hadoop/hive/serde2/io/TimestampWritable.java#L622
	 * https://github.com/kshoji/STLViewer/blob/master/src/jp/kshoji/stlviewer/object/STLObject.java#L72
	 * Saturday, March, 25, 2017
	 * 
	 * the input bits array should be converted to integer value.
	 * I chose to use 4-byte integer conversion.
	 * The second answer in stackoverflow explained the method. 
	 * The first answer gave the detailed mechanism.
	 * 4 bytes to integer source:
	 * http://stackoverflow.com/questions/2840190/java-convert-4-bytes-to-int
	 * extended source:
	 * http://docs.oracle.com/javase/7/docs/api/java/io/DataInput.html#readInt()
	 * @param bytes
	 * @param offset
	 * @return
	 */
	private int byteToInt(byte[] bytes, int offset) {
		return (0xff & stlBytes[offset]) | ((0xff & stlBytes[offset + 1]) << 8) | ((0xff & stlBytes[offset + 2]) << 16) | ((0xff & stlBytes[offset + 3]) << 24);
	}

	/**
	 * process the most jobs of the window by using asyncTask.
	 * @param stlBytes
	 * @param context
	 */
	private boolean processSTL(byte[] stlBytes, final Context context) {
		/*
		 * initialize the stlbytes array with min/max x,y 
		 * extreme large or small value for now.
		 */
		maxX = Float.MIN_VALUE;
		maxY = Float.MIN_VALUE;
		maxZ = Float.MIN_VALUE;
		minX = Float.MAX_VALUE;
		minY = Float.MAX_VALUE;
		minZ = Float.MAX_VALUE;

		normalList = new ArrayList<Float>();
		/**
		 * a AsyncTask class to handle the window updating job.
		 * 3 points defines one face.
		 * source:http://www.songho.ca/opengl/gl_vertexarray.html
		 * for buffer 3d object from a byte array.
		 * source:
		 * https://www.khronos.org/opengl/wiki/Buffer_Object
		 * detailed loading 3d bytes array method source:
		 * https://vulkan-tutorial.com/Loading_models
		 * 
		 */
		final AsyncTask<byte[], Integer, float[]> task = new AsyncTask<byte[], Integer, float[]>() {

			float[] processBinary(byte[] stlBytes) throws Exception {
				
				vertext_size=byteToInt(stlBytes, 80);;
				vertex_array=new float[vertext_size*9];
				normal_array=new float[vertext_size*9];
				for (int i = 0; i < vertext_size; i++) {
					for(int n=0;n<3;n++){
						normal_array[i*9+n*3]=Float.intBitsToFloat(byteToInt(stlBytes, 84 + i * 50));
						normal_array[i*9+n*3+1]=Float.intBitsToFloat(byteToInt(stlBytes, 84 + i * 50 + 4));
						normal_array[i*9+n*3+2]=Float.intBitsToFloat(byteToInt(stlBytes, 84 + i * 50 + 8));
					}
					float x = Float.intBitsToFloat(byteToInt(stlBytes, 84 + i * 50 + 12));
					float y = Float.intBitsToFloat(byteToInt(stlBytes, 84 + i * 50 + 16));
					float z = Float.intBitsToFloat(byteToInt(stlBytes, 84 + i * 50 + 20));
					adjustCoor(x, y, z);
					vertex_array[i*9]=x;
					vertex_array[i*9+1]=y;
					vertex_array[i*9+2]=z;
					
					x = Float.intBitsToFloat(byteToInt(stlBytes, 84 + i * 50 + 24));
					y = Float.intBitsToFloat(byteToInt(stlBytes, 84 + i * 50 + 28));
					z = Float.intBitsToFloat(byteToInt(stlBytes, 84 + i * 50 + 32));
					adjustCoor(x, y, z);
					vertex_array[i*9+3]=x;
					vertex_array[i*9+4]=y;
					vertex_array[i*9+5]=z;
					
					x = Float.intBitsToFloat(byteToInt(stlBytes, 84 + i * 50 + 36));
					y = Float.intBitsToFloat(byteToInt(stlBytes, 84 + i * 50 + 40));
					z = Float.intBitsToFloat(byteToInt(stlBytes, 84 + i * 50 + 44));
					adjustCoor(x, y, z);
					vertex_array[i*9+6]=x;
					vertex_array[i*9+7]=y;
					vertex_array[i*9+8]=z;
					/*
					 * check if vertices byte to integer progress is done.
					 * 50 vertices is used per loop.
					 * so, there is not enough vertices, the process is done for then.
					 */
					if (i % (vertext_size / 50) == 0) {
						publishProgress(i);
					}
				}
				
				return vertex_array;
			}
			
			@Override
			protected float[] doInBackground(byte[]... stlBytes) {
				
				float[]processResult = null;
				
				try {
						processResult = processBinary(stlBytes[0]);
				} catch (Exception e) {
				}
				
				if (processResult != null && processResult.length > 0 && normal_array != null && normal_array.length > 0) {
					return processResult;
				}
				
				return processResult;
			}
			

			
			@Override
			protected void onPostExecute(float[] vertexList) {

				/*
				 * adjust center.
				 */
				for(int i=0;i<vertext_size*3;i++){ adjustCenter(vertex_array,i*3, i*3+1, i*3+2, 0);}
				
				
				ByteBuffer vbb = ByteBuffer.allocateDirect(vertex_array.length * 4);
				vbb.order(ByteOrder.nativeOrder());
				triangleBuffer = vbb.asFloatBuffer();
				triangleBuffer.put(vertex_array);
				triangleBuffer.position(0);
				/*
				 * model to view.
				 */
				interChange.modelToView();

			}
		};

		try {
			task.execute(stlBytes);
		} catch (Exception e) {
			return false;
		}

		return true;
	}
	
	/**
	 * when the model moves all coordinates are calculated 
	 * as the result of new coordinate - center coordinate.
	 * @param vertex_array
	 * @param postion
	 */
	private void adjustCenter(float[] vertex_array , int x, int y, int z,float adjust){
		vertex_array[x]-=adjust;
		vertex_array[y]-=adjust;
		vertex_array[y]-=adjust;
	}
	
	/**
	 * set current model min/max model coordinates.
	 * @param x
	 * @param y
	 * @param z
	 */
	private void adjustCoor(float x, float y, float z) {
		if (x > maxX) { maxX = x;}
		if (y > maxY) { maxY = y;}
		if (z > maxZ) { maxZ = z;}
		if (x < minX) { minX = x;}
		if (y < minY) { minY = y;}
		if (z < minZ) { minZ = z;}
	}
	
	/**
	 * drawing process
	 */
	public void draw(GL10 gl) {
		if (normalList == null || triangleBuffer == null) {
			return;
		}
		
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, triangleBuffer);
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, vertext_size*3);
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_NORMAL_ARRAY);
		

	}
	/**
	 * delete stlbytes array.
	 * it's used in Renderer class.
	 */
	public void delete (){
		stlBytes=null;
	}
	public interface ModelToViewInterface{
		public void modelToView();
	}
}
