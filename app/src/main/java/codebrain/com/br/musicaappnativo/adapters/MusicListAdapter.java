package codebrain.com.br.musicaappnativo.adapters;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import codebrain.com.br.musicaappnativo.R;
import codebrain.com.br.musicaappnativo.models.Music;

public class MusicListAdapter extends BaseAdapter {

    private final List<Music> musics;
    private final Activity act;
    private View view;

    public MusicListAdapter(List<Music> musics, Activity act) {
        this.musics = musics;
        this.act = act;
    }

    @Override
    public int getCount() {
        return this.musics.size();
    }

    @Override
    public Music getItem(int position) {
        return musics.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        view = act.getLayoutInflater().inflate(R.layout.music_list_view, parent, false);

        Music music = musics.get(position);

        TextView artista = view.findViewById(R.id.artista);
        TextView musica = view.findViewById(R.id.musica);

        ImageView avatarView = view.findViewById(R.id.imgViewAvatar);

        Picasso.get()
                .load(music.avatar)
                .placeholder(R.drawable.ic_music)
                .error(R.drawable.ic_alert_error)
                .into(avatarView);

        artista.setText(music.artist);
        musica.setText(music.name);

        return view;
    }
}
