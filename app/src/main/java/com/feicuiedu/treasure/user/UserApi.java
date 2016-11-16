package com.feicuiedu.treasure.user;

import com.feicuiedu.treasure.user.account.Update;
import com.feicuiedu.treasure.user.account.UpdateResult;
import com.feicuiedu.treasure.user.account.UploadResult;
import com.feicuiedu.treasure.user.login.LoginResult;
import com.feicuiedu.treasure.user.register.RegisterResult;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 *
 * 将用户模块API，转为Java接口
 */
public interface UserApi {

    @POST("/Handler/UserHandler.ashx?action=register")
    Call<RegisterResult> register(@Body User user);

    @POST("/Handler/UserHandler.ashx?action=login")
    Call<LoginResult> login(@Body User user);

    // 头像上传(是一个多部分请求)
    @Multipart
    @POST("/Handler/UserLoadPicHandler1.ashx")
    Call<UploadResult> upload(@Part MultipartBody.Part part);

    // 更新头像
    @POST("/Handler/UserHandler.ashx?action=update")
    Call<UpdateResult> update(@Body Update update);

}