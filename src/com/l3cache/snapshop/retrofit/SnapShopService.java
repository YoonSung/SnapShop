package com.l3cache.snapshop.retrofit;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Query;
import retrofit.mime.TypedFile;

import com.l3cache.snapshop.login.LoginResponse;
import com.l3cache.snapshop.login.SignUpResponse;
import com.l3cache.snapshop.upload.UploadResponse;

public interface SnapShopService {
	@GET("/app/users/login")
	void login(@Query("email") String email, @Query("password") String password, Callback<LoginResponse> cb);

	@POST("/app/users/new")
	void signUp(@Query("email") String email, @Query("password") String password, Callback<SignUpResponse> cb);

	@Multipart
	@POST("/app/posts/new")
	void uploadSnap(@Part("title") String title, @Part("shopUrl") String shopUrl, @Part("contents") String contents,
			@Part("image") TypedFile image, @Part("price") String price, @Part("id") int id, Callback<UploadResponse> cb);
}