package codebrain.com.br.themusicpirate.adapters;

import android.app.Activity;
import android.nfc.FormatException;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Locale;

import codebrain.com.br.themusicpirate.R;
import codebrain.com.br.themusicpirate.models.DateHandler;
import codebrain.com.br.themusicpirate.models.Music;

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

        ImageView avatarView = view.findViewById(R.id.imgViewAvatar);

        TextView artista = view.findViewById(R.id.artista);
        TextView musica = view.findViewById(R.id.musica);
        TextView data = view.findViewById(R.id.data);
        TextView hora = view.findViewById(R.id.hora);

        Picasso.get()
                .load(music.avatar)
                .placeholder(R.drawable.ic_music)
                .error(R.drawable.ic_alert_error)
                .into(avatarView);

        artista.setText(music.artist);
        musica.setText(music.name);

        String perera = "Perera da Destruição";

        if(music.artist.length() <= 15 && music.name.length() <= 15){
            DateHandler finalDate = getDate(music.created_at);

            data.setText(finalDate.date);
            hora.setText(finalDate.hour);
        }else{
            data.setVisibility(View.GONE);
            hora.setVisibility(View.GONE);
        }

        return view;
    }

    private DateHandler getDate(String date) {
        SimpleDateFormat formatFrom = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        try {

            java.util.Date tmpDate = formatFrom.parse(date);
            SimpleDateFormat formatToDate = new SimpleDateFormat("dd/MM/yyyy");

            java.util.Date tmpHour = formatFrom.parse(date);
            SimpleDateFormat formatToHour = new SimpleDateFormat("HH:mm");

            DateHandler handler = new DateHandler();
            handler.date = formatToDate.format(tmpDate);
            handler.hour = formatToHour.format(tmpHour);

            return handler;
        } catch (ParseException e) {
            Log.d("ERRO", e.getMessage());
            return new DateHandler();
        }
    }
}
