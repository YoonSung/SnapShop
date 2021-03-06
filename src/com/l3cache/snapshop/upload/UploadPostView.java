package com.l3cache.snapshop.upload;

import java.io.File;
import java.io.IOException;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.l3cache.snapshop.R;
import com.l3cache.snapshop.SnapConstants;
import com.l3cache.snapshop.SnapPreference;
import com.l3cache.snapshop.app.AppController;
import com.l3cache.snapshop.app.AppController.TrackerName;
import com.l3cache.snapshop.photocrop.CropUtil;
import com.l3cache.snapshop.retrofit.DefaultResponse;
import com.l3cache.snapshop.retrofit.SnapShopService;
import com.l3cache.snapshop.volley.ExtendedImageLoader;
import com.l3cache.snapshop.volley.FeedImageView;

public class UploadPostView extends Activity {
	private String TAG = UploadPostView.class.getSimpleName();
	private ExtendedImageLoader imageLoader = AppController.getInstance().getImageLoader();
	private EditText titleEditText;
	private EditText contentsEditText;
	private FeedImageView uploadingImageView;
	private EditText priceEditText;
	private EditText shopUrlEditText;
	private Button uploadingButton;
	private String imageUrl;
	private TypedFile imageTypedFile;
	private int handlerId;
	private RestAdapter restAdapter;
	private SnapShopService service;
	private SnapPreference pref;
	private Context context;
	private ProgressDialog progressDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = this;
		setContentView(R.layout.activity_upload_snap_view);
		trackActivity(UploadPostView.class.getSimpleName());

		pref = new SnapPreference(getApplicationContext());

		titleEditText = (EditText) findViewById(R.id.upload_snap_editText_title);
		contentsEditText = (EditText) findViewById(R.id.upload_snap_editText_contents);
		uploadingImageView = (FeedImageView) findViewById(R.id.upload_snap_image_view);
		priceEditText = (EditText) findViewById(R.id.upload_snap_editText_price);
		shopUrlEditText = (EditText) findViewById(R.id.upload_snap_editText_link);
		uploadingButton = (Button) findViewById(R.id.upload_snap_button_upload);

		handlerId = getIntent().getExtras().getInt("handler");
		switch (handlerId) {
		case SnapConstants.CAMERA_BUTTON: {
			break;
		}

		case SnapConstants.GALLERY_BUTTON: {
			Uri imageUri = (Uri) getIntent().getExtras().get("data");
			File imageFile = CropUtil.getFromMediaUri(getContentResolver(), imageUri);
			ExifInterface exifResult;
			try {
				exifResult = new ExifInterface(imageFile.getAbsolutePath());
				int orientation = exifResult.getAttributeInt(ExifInterface.TAG_ORIENTATION,
						ExifInterface.ORIENTATION_NORMAL);
				pref.put(SnapPreference.PREF_EXIF_ORIENTATION, orientation);
				Log.i(TAG, "Orientation - " + orientation);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			uploadingImageView.setImageUrl(imageUri.toString(), imageLoader);
			imageTypedFile = new TypedFile("image/jpeg", new File(imageUri.getPath()));
			Log.i(TAG, imageTypedFile.toString());
			break;

		}
		case SnapConstants.INTERNET_BUTTON: {
			Log.i(TAG, "INTERNET!");
			Bundle extras = getIntent().getExtras();
			titleEditText.setText(extras.getString("title"));
			imageUrl = extras.getString("image");
			uploadingImageView.setImageUrl(imageUrl, imageLoader);
			priceEditText.setText(extras.getInt("price") + "");
			shopUrlEditText.setText(extras.getString("shopUrl"));
			break;
		}
		}

		uploadingButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					initRestfit();

					/**
					 * 포스트의 가격 입력은 필수
					 */
					if (priceEditText.getText().length() == 0) {
						Toast.makeText(getApplicationContext(), "What's price of your item?", Toast.LENGTH_SHORT)
								.show();
						return true;
					}

					/**
					 * 카메라와 갤러리 모두 CropActivity에 의해 Crop된 이미지를 업로드하게 된다.
					 */
					switch (handlerId) {
					case SnapConstants.CAMERA_BUTTON:
					case SnapConstants.GALLERY_BUTTON: {
						upload(imageTypedFile);
						break;

					}

					case SnapConstants.INTERNET_BUTTON: {
						upload(imageUrl);
						break;
					}
					}
				}
				return false;
			}

		});

	}

	private void trackActivity(String screenName) {
		Tracker t = ((AppController) getApplication()).getTracker(TrackerName.APP_TRACKER);
		t.setScreenName(screenName);
		t.send(new HitBuilders.AppViewBuilder().build());
	}

	public String getPathFromUri(Uri uri) {
		Cursor cursor = getContentResolver().query(uri, null, null, null, null);
		cursor.moveToNext();
		String path = cursor.getString(cursor.getColumnIndex("_data"));
		cursor.close();

		return path;
	}

	private void initRestfit() {
		if (restAdapter == null) {
			restAdapter = new RestAdapter.Builder().setEndpoint(SnapConstants.SERVER_URL)
					.setConverter(new GsonConverter(new Gson())).build();
		}
		if (service == null) {
			service = restAdapter.create(SnapShopService.class);
		}

	}

	private void upload(TypedFile imageFile) {

		final Handler handler = new Handler();

		new Thread() {
			public void run() {
				handler.post(new Runnable() {

					@Override
					public void run() {
						progressDialog = ProgressDialog.show(UploadPostView.this, "", "Uploading...");
					}
				});

				RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(SnapConstants.SERVER_URL)
						.setConverter(new GsonConverter(new Gson())).build();
				SnapShopService service = restAdapter.create(SnapShopService.class);

				service.uploadSnap(new TypedString(titleEditText.getText().toString()), new TypedString(shopUrlEditText
						.getText().toString()), new TypedString(contentsEditText.getText().toString()), imageTypedFile,
						new TypedString(priceEditText.getText().toString()), pref.getValue(
								SnapPreference.PREF_CURRENT_USER_ID, 0), new Callback<DefaultResponse>() {

							@Override
							public void failure(RetrofitError error) {
								handler.post(new Runnable() {

									@Override
									public void run() {
										progressDialog.cancel();

									}
								});
								Log.i(TAG, error.getLocalizedMessage());
								Toast.makeText(getApplicationContext(),
										"Error(image) - " + error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
								;
							}

							@Override
							public void success(DefaultResponse uploadResponse, Response response) {
								final int status = uploadResponse.getStatus();
								handler.post(new Runnable() {

									@Override
									public void run() {
										progressDialog.cancel();
										if (status == SnapConstants.SUCCESS) {
											Toast.makeText(getApplicationContext(), "Your Snap Successfully Added!",
													Toast.LENGTH_LONG).show();
											didUploadFinishActivity();
										} else if (status == SnapConstants.ERROR) {
											Toast.makeText(getApplicationContext(), "Error(image) - " + status,
													Toast.LENGTH_LONG).show();
										}

									}
								});
							}

						});
			}
		}.start();

	}

	private void upload(String imageUrl) {
		service.uploadSnap(titleEditText.getText().toString(), shopUrlEditText.getText().toString(), contentsEditText
				.getText().toString(), imageUrl, priceEditText.getText().toString(), pref.getValue(
				SnapPreference.PREF_CURRENT_USER_ID, 0), new Callback<DefaultResponse>() {

			@Override
			public void failure(RetrofitError error) {
				Log.i(TAG, "Error(url) - " + error.getLocalizedMessage());
				Toast.makeText(getApplicationContext(), "Error(url) - " + error.getLocalizedMessage(),
						Toast.LENGTH_LONG).show();
				;
			}

			@Override
			public void success(DefaultResponse defResp, Response response) {
				Log.i(TAG, defResp.getStatus() + "");
				if (defResp.getStatus() == SnapConstants.SUCCESS) {
					Toast.makeText(getApplicationContext(), "Your Snap Successfully Added!", Toast.LENGTH_LONG).show();
					didUploadFinishActivity();
				} else if (defResp.getStatus() == SnapConstants.ERROR) {
					Toast.makeText(getApplicationContext(), "Error(url) - " + defResp.getStatus(), Toast.LENGTH_LONG)
							.show();
				}
			}

		});
	}

	/**
	 * 업로드에 성공한 경우, 상위 액티비티(SearchResultView)를 함께 종료하고, NewsfeedView로 돌아가기 위해
	 * resultCode를 설정한다.
	 */
	private void didUploadFinishActivity() {
		((Activity) context).setResult(RESULT_OK);
		finish();
	}

	@Override
	public void onBackPressed() {
		Builder d = new AlertDialog.Builder(this);
		d.setMessage("Do you want to cancel the current post?");
		d.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		d.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		d.show();
	}
}
