package codebrain.com.br.musicaappnativo;

import java.util.List;

import codebrain.com.br.musicaappnativo.models.Music;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface MusicService {

    public static final String BASE_URL = "http://music-app-com-br.umbler.net/api/v1/";

    @GET("music")
    Call<List<Music>> listMusic(@Query("page") int page, @Query("limit") int limit);

    @GET("music/search")
    Call<List<Music>> searchMusic(@Query("nome") String musica, @Query("page") int page, @Query("limit") int limit);

    @Multipart
    @POST("music")
    Call<ResponseBody> uploadMusic(@Part MultipartBody.Part file,@Part("artista") RequestBody artista, @Part("nome") RequestBody musica);
}
