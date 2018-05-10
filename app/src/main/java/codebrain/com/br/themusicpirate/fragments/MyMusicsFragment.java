package codebrain.com.br.themusicpirate.fragments;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import codebrain.com.br.themusicpirate.AddMusicActivity;
import codebrain.com.br.themusicpirate.MusicService;
import codebrain.com.br.themusicpirate.R;
import codebrain.com.br.themusicpirate.UpdateMusicActivity;
import codebrain.com.br.themusicpirate.adapters.MyMusicListAdapter;
import codebrain.com.br.themusicpirate.helpers.Preferences;
import codebrain.com.br.themusicpirate.models.Music;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MyMusicsFragment extends Fragment {

    private FloatingActionButton fab;
    private ProgressBar loading;
    private ListView musicList;
    private TextView txtSemMusicas;

    private Boolean isLoading = false;
    private List<Music> musics;

    private int page = 1;
    private int limit = 10;
    private String deviceId;

    private MusicService service;

    private MyMusicListAdapter adapter;

    public MyMusicsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_my_musics, container, false);

        Preferences preferences = new Preferences(getContext());
        deviceId = preferences.getUserId();

        txtSemMusicas = rootView.findViewById(R.id.txtSemMusicas);

        //Avisos de que não tem musicas
        txtSemMusicas = rootView.findViewById(R.id.txtSemMusicas);

        //Lista de musicas
        musicList = rootView.findViewById(R.id.musicList);

        //Loading
        loading = rootView.findViewById(R.id.loading);

        fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AddMusicActivity.class);
                startActivityForResult(intent, 1000);
            }
        });

        //Pega as musicas da api
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MusicService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(MusicService.class);

        musicList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount != 0) {
                    if (!isLoading) {
                        page += 1;
                        getMusicsPaginated();
                    }
                }
            }
        });

        musicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Music selectedMusic = adapter.getItem(position);

                Intent intent = new Intent(getContext(), UpdateMusicActivity.class);
                intent.putExtra("music_id",selectedMusic.id);
                intent.putExtra("music_artist",selectedMusic.artist);
                intent.putExtra("music_name",selectedMusic.name);
                startActivityForResult(intent, 100);
            }
        });

        getMusics();

        return rootView;
    }

    //Pega as musicas da api e preenche na lista
    private void getMusics() {
        clearMusicList();
        //Mostra o loading
        loading.setVisibility(View.VISIBLE);

        txtSemMusicas.setVisibility(View.GONE);

        //Faz o request para a api
        Call<List<Music>> requestMusics = service.getMyMusics(deviceId, page, limit);

        requestMusics.enqueue(new Callback<List<Music>>() {
            @Override
            public void onResponse(Call<List<Music>> call, Response<List<Music>> response) {

                //Se der OK no request
                if (response.isSuccessful()) {
                    //Pega os dados do request
                    final List<Music> _musics = response.body();

                    musics = _musics;

                    if (musics.size() != 0) {
                        //Preenche a lista com as musicas
                        fillMusicList();
                    } else {
                        //Mostra um aviso que não encontrou musicas
                        txtSemMusicas.setVisibility(View.VISIBLE);
                    }

                    loading.setVisibility(View.GONE);
                    isLoading = false;
                } else {
                    Log.d("API ERROR CODE:", "" + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Music>> call, Throwable t) {
                Log.d("API ERROR", t.getMessage());
            }
        });
    }

    private void getMusicsPaginated() {
        isLoading = true;
        //Faz o request para a api
        Call<List<Music>> requestMusics = service.getMyMusics(deviceId, page, limit);

        requestMusics.enqueue(new Callback<List<Music>>() {
            @Override
            public void onResponse(Call<List<Music>> call, Response<List<Music>> response) {

                //Se der OK no request
                if (response.isSuccessful()) {
                    //Pega os dados do request
                    final List<Music> _musics = response.body();

                    int index = musicList.getFirstVisiblePosition();//Pega a posição atual da lista
                    View v = musicList.getChildAt(0);
                    int top = (v == null) ? 0 : v.getTop();//Pega a distancia do topo da lista

                    if (_musics.size() > 0) {
                        for (Music m : _musics) {
                            musics.add(m);
                        }

                        //Move a lista para a posição antiga, sem isso o scroll da lista sobe pro inicio
                        musicList.setSelectionFromTop(index, top);

                        isLoading = false;
                    } else {
                        page -= 1;
                    }
                } else {
                    page -= 1;
                    Log.d("API ERROR CODE:", "" + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Music>> call, Throwable t) {
                Log.d("API ERROR", t.getMessage());
            }
        });
    }

    private void fillMusicList() {
        //Adapter para preencher a lista
        adapter = new MyMusicListAdapter(musics, getActivity());
        musicList.setAdapter(adapter);
    }

    private void clearMusicList() {
        List<Music> _musics = new ArrayList<>();
        adapter = new MyMusicListAdapter(_musics, getActivity());
        musicList.setAdapter(adapter);

        txtSemMusicas.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Boolean res = data.getBooleanExtra("newMusic", false);
        Boolean res2 = data.getBooleanExtra("updated", false);
        if (res || res2) {
            page = 1;
            getMusics();
        }
    }

}
