package codebrain.com.br.themusicpirate;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import codebrain.com.br.themusicpirate.helpers.Preferences;
import codebrain.com.br.themusicpirate.models.Music;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class UpdateMusicActivity extends AppCompatActivity {

    Retrofit retrofit;
    MusicService service;

    AppCompatEditText edtArtista;
    AppCompatEditText edtMusica;

    TextInputLayout textLayoutArtista;
    TextInputLayout textLayoutMusica;

    Button btnSalvar;

    int musicId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_music);

        retrofit = new Retrofit.Builder()
                .baseUrl(MusicService.BASE_URL)
                .build();

        service = retrofit.create(MusicService.class);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Alterar Música");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        edtArtista = findViewById(R.id.artista);
        edtMusica = findViewById(R.id.musica);
        textLayoutArtista = findViewById(R.id.textLayoutArtista);
        textLayoutMusica = findViewById(R.id.textLayoutMusica);

        btnSalvar = findViewById(R.id.btnSalvar);

        String artista = getIntent().getStringExtra("music_artist");
        String musica = getIntent().getStringExtra("music_name");

        musicId = getIntent().getIntExtra("music_id", 0);

        edtArtista.setText(artista);
        edtMusica.setText(musica);

        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean canContinue = validateForm();
                if (canContinue) {
                    updateMusic(musicId, edtArtista.getText().toString().trim(), edtMusica.getText().toString().trim());
                }
            }
        });
    }

    private boolean validateForm() {
        if (edtArtista.getText().toString().isEmpty()) {
            textLayoutArtista.setErrorEnabled(true);
            textLayoutArtista.setError("Preencha com o nome do artista/banda");
            return false;
        } else {
            textLayoutArtista.setErrorEnabled(false);
        }

        if (edtMusica.getText().toString().isEmpty()) {
            textLayoutMusica.setErrorEnabled(true);
            textLayoutMusica.setError("Preencha com o nome da música");
            return false;
        } else {
            textLayoutMusica.setErrorEnabled(false);
        }

        return true;
    }

    private void updateMusic(int _id, String _artista, String _musica) {

        final ProgressDialog dialog = ProgressDialog.show(this, "",
                "Alterando Música", true);

        dialog.show();

        Preferences preferences = new Preferences(this);
        String deviceId = preferences.getUserId();

        RequestBody _artistaBody = RequestBody.create(MediaType.parse("text/plain"), _artista);
        RequestBody _musicaBody = RequestBody.create(MediaType.parse("text/plain"), _musica);
        RequestBody _deviceidBody = RequestBody.create(MediaType.parse("text/plain"), deviceId);

        Call<ResponseBody> req = service.updateMusic(_id, _artistaBody,_musicaBody,_deviceidBody);

        req.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Alterado com sucesso", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();

                    Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                    mainActivity.putExtra("updated",true);
                    setResult(1000, mainActivity);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Ocorreu um problema ao atualizar música", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void showDialogDelete(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Deletar")
                .setMessage("Deseja deletar "+ edtMusica.getText().toString() +"?")
                .setPositiveButton("Deletar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteMusic();
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //Cancel handler
                    }
                });

        builder.create().show();
    }

    private void deleteMusic(){
        final ProgressDialog dialog = ProgressDialog.show(this, "",
                "Deletando esta música", true);

        dialog.show();

        Call<ResponseBody> req = service.deleteMusic(musicId);

        req.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Deletado com sucesso", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();

                    Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                    mainActivity.putExtra("updated",true);
                    setResult(1000, mainActivity);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Ocorreu um problema ao deletar a música", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar_music_update,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
            mainActivity.putExtra("updated", false);
            setResult(1000, mainActivity);
            finish();
        }else{
            switch (item.getItemId()){
                case R.id.deletar:{
                    showDialogDelete();
                    break;
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
        mainActivity.putExtra("updated", false);
        setResult(1000, mainActivity);
        finish();
    }
}
