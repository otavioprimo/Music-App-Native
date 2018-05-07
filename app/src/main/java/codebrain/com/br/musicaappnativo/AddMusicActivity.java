package codebrain.com.br.musicaappnativo;

import android.Manifest;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.net.URISyntaxException;

import codebrain.com.br.musicaappnativo.models.ResponseApi;
import codebrain.com.br.musicaappnativo.utils.FileManager;
import codebrain.com.br.musicaappnativo.utils.Permission;
import codebrain.com.br.musicaappnativo.utils.ProgressRequestBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddMusicActivity extends AppCompatActivity implements ProgressRequestBody.UploadCallbacks{

    TextView txtArquivoSelecionado;
    TextView txtPercent;

    AppCompatEditText artista;
    AppCompatEditText musica;

    TextInputLayout textLayoutArtista;
    TextInputLayout textLayoutMusica;

    Button btnSalvar;
    Button btnArquivo;

    ProgressBar progressBar;

    String selectedFilePath;

    Retrofit retrofit;

    MusicService service;

    final static int RQS_OPEN_AUDIO_MP3 = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_music);

        Permission.askPermissions(this);

        retrofit = new Retrofit.Builder()
                .baseUrl(MusicService.BASE_URL)
                .build();

        service = retrofit.create(MusicService.class);

        setTitle("New Music");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        progressBar = findViewById(R.id.progressFile);

        artista = findViewById(R.id.artista);
        musica = findViewById(R.id.musica);

        textLayoutArtista = findViewById(R.id.textLayoutArtista);
        textLayoutMusica = findViewById(R.id.textLayoutMusica);

        btnArquivo = findViewById(R.id.btnArquivo);
        btnSalvar = findViewById(R.id.btnSalvar);

        txtArquivoSelecionado = findViewById(R.id.txtArquivoSelecionado);
        txtPercent = findViewById(R.id.porcentagem);

        btnSalvar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean canContinue = validateForm();
                if (canContinue) {
                    upload(artista.getText().toString().trim(), musica.getText().toString().trim(), selectedFilePath);
                }
            }
        });

        btnArquivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("audio/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(
                        intent, "Selecione um arquivo mp3"), RQS_OPEN_AUDIO_MP3);
            }
        });
    }

    private boolean validateForm() {
        if (artista.getText().toString().isEmpty()) {
            textLayoutArtista.setErrorEnabled(true);
            textLayoutArtista.setError("Preencha com o nome do artista");
            return false;
        } else {
            textLayoutArtista.setErrorEnabled(false);
        }

        if (musica.getText().toString().isEmpty()) {
            textLayoutMusica.setErrorEnabled(true);
            textLayoutMusica.setError("Preencha com o nome da banda");
            return false;
        } else {
            textLayoutMusica.setErrorEnabled(false);
        }

        if (selectedFilePath == null) {
            Toast.makeText(this, "Selecione uma música", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == RQS_OPEN_AUDIO_MP3) {

            Uri audioFileUri = data.getData();

            txtArquivoSelecionado.setVisibility(View.VISIBLE);

            selectedFilePath = FileManager.getPath(this, audioFileUri);
        }
    }

    private void upload(String _artista, String _musica, String filePath) {
        progressBar.setVisibility(View.VISIBLE);
        txtPercent.setVisibility(View.VISIBLE);
        disableButtons();

        MusicService service = retrofit.create(MusicService.class);

        File file = new File(filePath);

        //RequestBody body = RequestBody.create(MediaType.parse("audio/*"), file);
        ProgressRequestBody body = new ProgressRequestBody(file, this);

        MultipartBody.Part _fileBody = MultipartBody.Part.createFormData("arquivo", file.getName(), body);
        RequestBody _artistaBody = RequestBody.create(MediaType.parse("text/plain"), _artista);
        RequestBody _musicaBody = RequestBody.create(MediaType.parse("text/plain"), _musica);

        Call<ResponseBody> req = service.uploadMusic(_fileBody, _artistaBody, _musicaBody);

        req.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Salvo com sucesso", Toast.LENGTH_SHORT).show();

                    Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
                    mainActivity.putExtra("newMusic",true);
                    setResult(1000, mainActivity);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Arquivo não enviado", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    txtPercent.setVisibility(View.GONE);
                    enableButtons();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                progressBar.setVisibility(View.GONE);
                enableButtons();
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void disableButtons(){
        btnSalvar.setEnabled(false);
        btnSalvar.setVisibility(View.GONE);
        btnArquivo.setEnabled(false);
        artista.setEnabled(false);
        musica.setEnabled(false);
    }

    private void enableButtons(){
        btnSalvar.setEnabled(true);
        btnSalvar.setVisibility(View.VISIBLE);
        btnArquivo.setEnabled(true);
        artista.setEnabled(true);
        musica.setEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
            mainActivity.putExtra("newMusic",false);
            setResult(1000, mainActivity);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
        mainActivity.putExtra("newMusic",false);
        setResult(1000, mainActivity);
        finish();
    }

    @Override
    public void onProgressUpdate(int percentage) {
        txtPercent.setText(percentage + " %");
        // set current progress
        progressBar.setProgress(percentage);
    }

    @Override
    public void onError() {
        // do something on error
    }

    @Override
    public void onFinish() {
        // do something on upload finished
        // for example start next uploading at queue
        progressBar.setProgress(100);
    }
}
