package codebrain.com.br.musicaappnativo.Service;

import android.net.Uri;
import android.util.Log;

import java.io.File;
import java.util.List;

import codebrain.com.br.musicaappnativo.MusicService;
import codebrain.com.br.musicaappnativo.models.Music;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiService {

    private Retrofit retrofit;
    private List<Music> musics;

    public ApiService() {
        this.retrofit = new Retrofit.Builder()
                .baseUrl(MusicService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public List<Music> listMusics(int page, int limit) {

        MusicService service = retrofit.create(MusicService.class);
        Call<List<Music>> requestMusics = service.listMusic(page, limit);

        requestMusics.enqueue(new Callback<List<Music>>() {
            @Override
            public void onResponse(Call<List<Music>> call, Response<List<Music>> response) {
                if (response.isSuccessful()) {
                    musics = response.body();
                } else {
                    Log.d("API ERROR CODE:", "" + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Music>> call, Throwable t) {
                Log.d("API ERROR", t.getMessage());
            }
        });

        return musics;
    }

    public void saveMusic(String artista, String musica, Uri fileUri) {
        MusicService service = retrofit.create(MusicService.class);

        File file = new File(fileUri.getPath());

        RequestBody requestFile =
                RequestBody.create(MediaType.parse("multipart/form-data"), file);

        MultipartBody.Part _fileBody =
                MultipartBody.Part.createFormData("arquivo", file.getName(), requestFile);

        RequestBody _artistaBody = RequestBody.create(MediaType.parse("multipart/form-data"), artista);

        RequestBody _musicaBody = RequestBody.create(MediaType.parse("multipart/form-data"), musica);

        //service.upload(_artistaBody,_musicaBody,_fileBody);
    }
}
