
package com.example.stlviewer;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.FrameLayout;
/**
 * This is the only activity for stl model presents..
 * @author Alan
 *
 */
public class MainActivity extends Activity {

	private StlView stlView = null;
	private FrameLayout relativeLayout;
	private STLModel stlModel;
	private Context context;
	private byte[] stlBytes;
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		System.out.println("==============================================");
		System.out.println("====          test in onCreate            ====");
		System.out.println("==============================================");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = this;

		/*
		 * initialize this activity.
		 */
		init();
		/*
		 * I try to implement user menu to load stl file but failed to do so.
		 * For now, this app only  works when hard code file dir and name.
		 */
		isHaveFile("mnt/sdcard/pikachu.stl");
	}



	@Override
	protected void onResume() {
		super.onResume();
		if (stlView != null) {
			System.out.println("onResume");
			stlView.requestRedraw();
		}
	}
	/**
	 *
	 */
	@Override
	protected void onPause() {
		super.onPause();
		if (stlView != null) {
			System.out.println("==============================================");
			System.out.println("====    test in onPause: stlView not null ====");
			System.out.println("==============================================");
		}
	}

	/**
	 * routine override
	 */
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		System.out.println("==============================================");
		System.out.println("====   test in onRestoreInstanceState     ====");
		System.out.println("==============================================");
	}

	/**
	 * Routine override
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	/**
	 * double check if stl_path is valid.
	 * if the path is valid, load the stl file.
	 * This method was used to check if there is a stlmodel file dir on load.
	 * For now this method does not have much use.
	 * @param stl_path
	 */
	public void isHaveFile(String stl_path){
		if(stl_path != null && !stl_path.equals("")){
			File file = new File(stl_path);
			openFile(file);
		}else{
		}
	}

	/**
	 * open a reliable stl file path.
	 * @param file
	 */
	public void openFile(File file){
		/*
		 * sharedPreference
		 */
		SharedPreferences config = getSharedPreferences("PathSetting",
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor configEditor = config.edit();
		configEditor.putString("lastPath", file.getParent());
		configEditor.commit();

		/*
		 * check if stl View is null
		 */
		if(stlView != null){
			relativeLayout.removeAllViews();
			stlView.delete();
			stlView = null;
			stlBytes = null;
			/*
			 * recycle memory.
			 */
			System.gc();
		}
		try{
			stlBytes = getStlBytes(this, Uri.fromFile(file));
		}catch (Exception e){
			System.out.println("==============================================");
			System.out.println("====          error loading stlbytes      ====");
			System.out.println("==============================================");
		}
		stlModel = new STLModel(stlBytes, this, new ActivityModelViewConnector());
	}

	/**
	 * Read file uri and return the stl file as a byte array.
	 * @param context
	 * @param uri
	 * @return stl byte array
	 */
	private byte[] getStlBytes(Context context, Uri uri) {
		// TODO Auto-generated method stub
		byte[] stlBytes = null;
		InputStream inputStream = null;
		try {
			inputStream = context.getContentResolver().openInputStream(uri);
			stlBytes = toByteArray(inputStream);
		} catch (IOException e) {
		} finally {
			closeFileStream(inputStream);
		}
		return stlBytes;
	}

	/**
	 * Convert input stream into byte array.
	 * use copy method to write output stream into output.
	 * Source:
	 * http://www.baeldung.com/convert-input-stream-to-array-of-bytes#highlighter_279923
	 * @param input
	 * @return output: stream byte array
	 * @throws IOException: ...
	 */
	public static byte[] toByteArray(InputStream input) throws IOException {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		copy(input, output);
		return output.toByteArray();
	}

	/**
	 * this method is used for writing output. And it is used in toByteArray method.
	 * Source:
	 * http://www.baeldung.com/convert-input-stream-to-array-of-bytes#highlighter_279923
	 * @param input
	 * @param output
	 * @return long copied length
	 * @throws IOException
	 */
	private static long copy(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		long count = 0;
		int n = 0;
		while ((n = input.read(buffer)) != -1) {
			output.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	/**
	 * close the file stream
	 *
	 * @param closeable
	 */
	public static void closeFileStream(Closeable closeable) {
		try {
			closeable.close();
		} catch (Throwable e) {
			System.out.println("fail to close file");
		}
	}

	/**
	 * initialize layout.
	 */
	public void init(){
		relativeLayout = (FrameLayout) findViewById(R.id.stlFrameLayout);

	}

	/**
	 * override modeltoview method in interface ModelToViewInterface
	 * @author Alan
	 */
	class ActivityModelViewConnector implements STLModel.ModelToViewInterface{

		@Override
		public void modelToView() {

			if (stlView == null) {
				stlView = new StlView(context, stlModel);
				relativeLayout.addView(stlView);
			} else {
				stlView.setNewModel(stlModel);
			}
		}
	}
}
