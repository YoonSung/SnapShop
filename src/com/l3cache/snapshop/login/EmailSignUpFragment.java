package com.l3cache.snapshop.login;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
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
import com.l3cache.snapshop.retrofit.DefaultResponse;
import com.l3cache.snapshop.retrofit.SignInResponse;
import com.l3cache.snapshop.retrofit.SnapShopService;

public class EmailSignUpFragment extends DialogFragment {
	private EditText emailField;
	private EditText passwordField;
	private Button signupButton;
	private EditText passwordAgainField;
	private String email;
	private String password;
	private SnapPreference pref;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Tracker t = ((AppController) getActivity().getApplication()).getTracker(TrackerName.APP_TRACKER);
		t.setScreenName(EmailSignUpFragment.class.getSimpleName());
		t.send(new HitBuilders.AppViewBuilder().build());

		pref = new SnapPreference(getActivity());

		View view = inflater.inflate(R.layout.fragment_sign_up_email, container, false);
		signupButton = (Button) view.findViewById(R.id.button_sign_up);
		emailField = (EditText) view.findViewById(R.id.editText_email);
		passwordField = (EditText) view.findViewById(R.id.editText_password);
		passwordAgainField = (EditText) view.findViewById(R.id.editText_password_again);
		signupButton.setOnTouchListener(new OnTouchListener() {

			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_UP) {
					if (emailField.getText().toString().length() == 0) {
						Toast.makeText(getActivity(), "What's your Email Address?", Toast.LENGTH_LONG).show();
						return true;
					}

					/**
					 * 사용자가 입력한 Email 주소의 유효성 여부를 판단합니다.
					 */
					if (isEmailValid(emailField.getText().toString()) == false) {
						Toast.makeText(getActivity(), "Invalid Email Address", Toast.LENGTH_LONG).show();
						return true;
					}

					/**
					 * 사용자가 입력한 비밀번호의 길이가 최소 7인지 확인합니다.
					 */
					if (passwordField.getText().toString().length() <= 6) {
						Toast.makeText(getActivity(), "Password is too short (minimum 7)", Toast.LENGTH_LONG).show();
						return true;
					}

					/**
					 * 사용자가 입력한 두 비밀번호가 서로 일치하는지 확인합니다.
					 */
					if (passwordField.getText().toString().equals(passwordAgainField.getText().toString()) == false) {
						Toast.makeText(getActivity(), "Password does not match", Toast.LENGTH_LONG).show();
						return true;
					}

					/**
					 * 이메일 및 비밀번호 조건 만족 시, 서버에 회원가입 요청
					 */
					email = emailField.getText().toString();
					password = passwordField.getText().toString();
					authorizeSignup();
					return true;
				}
				return false;
			}
		});

		return view;
	}

	protected void authorizeSignup() {
		RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(SnapConstants.SERVER_URL)
				.setConverter(new GsonConverter(new Gson())).build();

		SnapShopService service = restAdapter.create(SnapShopService.class);
		service.signUp(email, password, new Callback<DefaultResponse>() {

			@Override
			public void failure(RetrofitError error) {
				Toast.makeText(getActivity(), "failure", Toast.LENGTH_LONG).show();
			}

			@Override
			public void success(DefaultResponse defResp, Response response) {
				int status = defResp.getStatus();

				switch (status) {
				/**
				 * 회원가입에 성공한 경우, 해당 데이터를 가지고 signin을 진행
				 */
				case SnapConstants.SUCCESS: {
					authorizeSignin();
					break;
				}

				case SnapConstants.EMAIL_DUPLICATION: {
					Toast.makeText(getActivity(), "Email Already Exists", Toast.LENGTH_LONG).show();
					break;
				}

				case SnapConstants.ERROR: {
					Toast.makeText(getActivity(), "Unrecognized Server Error!", Toast.LENGTH_LONG).show();
					break;
				}

				default:
					break;
				}

			}
		});
	}

	private void authorizeSignin() {
		RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(SnapConstants.SERVER_URL)
				.setConverter(new GsonConverter(new Gson())).build();
		SnapShopService service = restAdapter.create(SnapShopService.class);
		service.signIn(email, password, new Callback<SignInResponse>() {

			@Override
			public void failure(RetrofitError error) {
				Toast.makeText(getActivity(), "Network Error", Toast.LENGTH_LONG).show();
			}

			@Override
			public void success(SignInResponse loginResponse, Response response) {
				int status = loginResponse.getStatus();

				switch (status) {
				case SnapConstants.SUCCESS: {
					pref.persistCurrentUser(loginResponse.getId(), email, password);
					Toast.makeText(
							getActivity(),
							"Welcome " + pref.getValue(SnapPreference.PREF_CURRENT_USER_ID, 0) + " - "
									+ pref.getValue(SnapPreference.PREF_CURRENT_USER_EMAIL, "No Email"),
							Toast.LENGTH_SHORT).show();
					;

					intentTabHostActivity();
					break;
				}
				case SnapConstants.EMAIL_ERROR: {
					Toast.makeText(getActivity(), "Email Error " + loginResponse.getId(), Toast.LENGTH_LONG).show();
					break;
				}
				case SnapConstants.PASSWORD_ERROR: {
					Toast.makeText(getActivity(), "Password Error " + loginResponse.getId(), Toast.LENGTH_LONG).show();
					break;
				}

				case SnapConstants.ERROR: {
					Toast.makeText(getActivity(), "Unrecognized Server Error!" + loginResponse.getId(),
							Toast.LENGTH_LONG).show();
					break;
				}

				}

			}

		});

	}

	private void intentTabHostActivity() {
		Intent intent = new Intent("com.l3cache.snapshop.tabhost.MainTabHostView");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);

	}

	/**
	 * 사용자가 입력한 이메일 주소의 유효성 검사
	 * 
	 * @param email
	 * @return 유효할 경우 true, 유효하지 않을 경우 false
	 */
	public static boolean isEmailValid(String email) {
		boolean isValid = false;
		String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
		CharSequence inputStr = email;
		Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(inputStr);
		if (matcher.matches()) {
			isValid = true;
		}
		return isValid;
	}

	@Override
	public void onActivityCreated(Bundle arg0) {
		super.onActivityCreated(arg0);
		getDialog().getWindow().setTitle("Create Account");
	}
}
