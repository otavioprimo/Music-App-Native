package codebrain.com.br.themusicpirate.fragments;


import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.jean.jcplayer.JcAudio;
import com.example.jean.jcplayer.JcPlayerView;

import java.util.ArrayList;
import java.util.List;

import codebrain.com.br.themusicpirate.MusicService;
import codebrain.com.br.themusicpirate.R;
import codebrain.com.br.themusicpirate.adapters.MusicListAdapter;
import codebrain.com.br.themusicpirate.models.Music;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MusicsFragment extends Fragment {

    private SearchView searchView;
    private SearchManager searchManager;

    private ProgressBar loading;
    private ListView musicList;
    private TextView txtSemMusicas;
    private ImageView imgRefresh;
    private SwipeRefreshLayout swipteRefreshLayout;
    private JcPlayerView jcplayerView;

    public List<Music> musics;
    public List<Music> filteredMusics;
    private ArrayList<JcAudio> jcAudios;

    private int page = 1;
    private int limit = 10;

    private int pageFilter = 1;
    private int limitFilter = 100;

    private MusicService service;

    private MusicListAdapter adapter;

    private Music currentTrack;

    private View viewPlaying;

    private Boolean isLoading = false;


    public MusicsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        View rootView = inflater.inflate(R.layout.fragment_musics, container, false);

        //Music Players
        jcplayerView = rootView.findViewById(R.id.jcplayer);

        //Pull to refresh
        swipteRefreshLayout = rootView.findViewById(R.id.swipeRefresh);
        swipteRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));

        //Avisos de que não tem musicas
        txtSemMusicas = rootView.findViewById(R.id.txtSemMusicas);
        imgRefresh = rootView.findViewById(R.id.refresh);

        //Lista de musicas
        musicList = rootView.findViewById(R.id.musicList);

        //Loading
        loading = rootView.findViewById(R.id.loading);

        //Pega as musicas da api
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MusicService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(MusicService.class);

        getMusics(false);

        musicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Music selectedMusic = adapter.getItem(position);

                ImageView imgPlay = view.findViewById(R.id.imagePlay);

                if (currentTrack != null && currentTrack.id == selectedMusic.id) { //Se for clicado na musica que esta tocando
                    if (jcplayerView.isPlaying()) {
                        imgPlay.setImageResource(R.drawable.ic_play);
                        jcplayerView.pause();
                        //isPlaying = false;
                    } else {
                        imgPlay.setImageResource(R.drawable.ic_pause);
                        jcplayerView.continueAudio();
                        //isPlaying = true;
                    }
                } else { //Se for clicado em outra musica
                    if (viewPlaying != null) { //Se não for a primeira musica a ser tocada, salva a view da musica anterior
                        ImageView _previusView = viewPlaying.findViewById(R.id.imagePlay);
                        _previusView.setImageResource(R.drawable.ic_play);
                    }

                    jcplayerView.playAudio(JcAudio.createFromURL(selectedMusic.artist + " - " + selectedMusic.name, selectedMusic.source));
                    jcplayerView.createNotification(R.drawable.ic_music);
                    imgPlay.setImageResource(R.drawable.ic_pause);
                    currentTrack = selectedMusic;
                    viewPlaying = view;
                }
            }
        });

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

        imgRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page = 1;
                getMusics(false);
            }
        });

        swipteRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                page = 1;
                getMusics(true);

            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    //Pega as musicas da api e preenche na lista
    private void getMusics(final Boolean isSwipe) {
        if (!isSwipe) {
            clearMusicList();
            //Mostra o loading
            loading.setVisibility(View.VISIBLE);
        }

        txtSemMusicas.setVisibility(View.GONE);
        imgRefresh.setVisibility(View.GONE);

        //Faz o request para a api
        Call<List<Music>> requestMusics = service.listMusic(page, limit);

        requestMusics.enqueue(new Callback<List<Music>>() {
            @Override
            public void onResponse(Call<List<Music>> call, Response<List<Music>> response) {

                //Se der OK no request
                if (response.isSuccessful()) {
                    //Pega os dados do request
                    final List<Music> _musics = response.body();

                    musics = _musics;

                    //Remove o loading
                    if (!isSwipe) {
                        loading.setVisibility(View.GONE);
                    } else {
                        swipteRefreshLayout.setRefreshing(false);
                    }

                    if (musics.size() != 0) {
                        //Preenche a lista com as musicas
                        fillMusicList();
                    } else {
                        //Mostra um aviso que não encontrou musicas
                        txtSemMusicas.setVisibility(View.VISIBLE);
                        imgRefresh.setVisibility(View.VISIBLE);
                    }

                    isLoading = false;

                    jcAudios = new ArrayList<>();
                    for (Music m : _musics) {
                        jcAudios.add(JcAudio.createFromURL(m.artist + " - " + m.name, m.source));
                    }

                    jcplayerView.initPlaylist(jcAudios);

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
        Call<List<Music>> requestMusics = service.listMusic(page, limit);

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
                            jcAudios.add(JcAudio.createFromURL(m.artist + " - " + m.name, m.source));
                        }

                        jcplayerView.initPlaylist(jcAudios);

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
        adapter = new MusicListAdapter(musics, getActivity());
        musicList.setAdapter(adapter);
    }

    private void clearMusicList() {
        List<Music> _musics = new ArrayList<>();
        adapter = new MusicListAdapter(_musics, getActivity());
        musicList.setAdapter(adapter);

        txtSemMusicas.setVisibility(View.GONE);
        imgRefresh.setVisibility(View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        MenuItem searchViewItem = menu.findItem(R.id.menu_pesquisar);
        searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) searchViewItem.getActionView();

        searchView.setQueryHint("Pesquise uma música");
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        searchView.setIconifiedByDefault(true);

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {
                searchMusic(newText);
                return true;
            }

            public boolean onQueryTextSubmit(String query) {
                loading.setVisibility(View.GONE);
                searchView.clearFocus();
                return true;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                clearMusicList();
                fillMusicList();
                searchView.clearFocus();
                loading.setVisibility(View.GONE);
                return false;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void searchMusic(String text) {
        loading.setVisibility(View.VISIBLE);
        txtSemMusicas.setVisibility(View.GONE);
        isLoading = true;
        //Faz o request para a api
        Call<List<Music>> requestMusics = service.searchMusic(text, pageFilter, limitFilter);

        requestMusics.enqueue(new Callback<List<Music>>() {
            @Override
            public void onResponse(Call<List<Music>> call, Response<List<Music>> response) {

                //Se der OK no request
                if (response.isSuccessful()) {
                    //Pega os dados do request
                    final List<Music> _musics = response.body();

                    filteredMusics = _musics;

                    for (Music m : filteredMusics){
                        Log.d("saasd",m.artist + " - " + m.name);
                    }

                    if (filteredMusics.size() > 0) {
                        //Preenche a lista com as musicas
                        adapter = new MusicListAdapter(filteredMusics, getActivity());
                        musicList.setAdapter(adapter);
                    } else {
                        //Mostra um aviso que não encontrou musicas
                        txtSemMusicas.setVisibility(View.VISIBLE);

                        //Limpa a lista
                        List<Music> _m = new ArrayList<>();
                        adapter = new MusicListAdapter(_musics, getActivity());
                        musicList.setAdapter(adapter);
                    }

                    isLoading = false;
                    loading.setVisibility(View.GONE);
                } else {
                    loading.setVisibility(View.GONE);
                    Log.d("API ERROR CODE:", "" + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Music>> call, Throwable t) {
                Log.d("API ERROR", t.getMessage());
            }
        });
    }
}
