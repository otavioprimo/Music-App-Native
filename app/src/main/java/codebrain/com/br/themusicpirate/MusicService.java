package codebrain.com.br.themusicpirate;

import java.util.List;

import codebrain.com.br.themusicpirate.models.Music;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MusicService {

    public static final String BASE_URL = "http://music-app-com-br.umbler.net/api/v1/";
    //public static final String BASE_URL = "http://192.168.0.10:3000/api/v1/";

    @GET("music")
    Call<List<Music>> listMusic(@Query("page") int page, @Query("limit") int limit);

    @GET("music/search")
    Call<List<Music>> searchMusic(@Query("nome") String musica, @Query("page") int page, @Query("limit") int limit);

    @GET("music/device/{deviceid}")
    Call<List<Music>> getMyMusics(@Path("deviceid") String deviceId, @Query("page") int page, @Query("limit") int limit);

    @GET("music/{id}")
    Call<Music> getMusicById(@Path("id") int id);

    @DELETE("music/{id}")
    Call<ResponseBody> deleteMusic(@Path("id") int id);

    @Multipart
    @PUT("music/{id}")
    Call<ResponseBody> updateMusic(@Path("id") int id, @Part("artista") RequestBody artista, @Part("nome") RequestBody musica, @Part("deviceid") RequestBody deviceId);

    @Multipart
    @POST("music")
    Call<ResponseBody> uploadMusic(@Part MultipartBody.Part file, @Part("artista") RequestBody artista, @Part("nome") RequestBody musica, @Part("deviceid") RequestBody deviceId);
}
