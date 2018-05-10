package codebrain.com.br.themusicpirate;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

import codebrain.com.br.themusicpirate.models.GraphResults;
import codebrain.com.br.themusicpirate.models.Music;
import codebrain.com.br.themusicpirate.models.Total;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AboutActivity extends AppCompatActivity {

    private MusicService service;
    private Total totalMusics;
    private GraphResults graphResults;

    private TextView textAcervo;
    private LineChart chart;
    private BarChart barChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Sobre Nós");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        textAcervo = findViewById(R.id.textAcervo);
        barChart = findViewById(R.id.chart);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(MusicService.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(MusicService.class);

        getTotalMusics();
        getGraphResults();


    }

    public void getTotalMusics() {
        Call<Total> requestTotal = service.getTotalMusics();
        requestTotal.enqueue(new Callback<Total>() {
            @Override
            public void onResponse(Call<Total> call, Response<Total> response) {
                if (response.isSuccessful()) {
                    Total total = response.body();
                    int _total = total.total;
                    textAcervo.setText("Contamos com um acervo de " + _total + " músicas");
                }
            }

            @Override
            public void onFailure(Call<Total> call, Throwable t) {
                Log.d("API ERROR", t.getMessage());
            }
        });
    }

    public void getGraphResults() {
        Call<List<GraphResults>> requestGraphResults = service.getGraph();
        requestGraphResults.enqueue(new Callback<List<GraphResults>>() {
            @Override
            public void onResponse(Call<List<GraphResults>> call, Response<List<GraphResults>> response) {
                if (response.isSuccessful()) {
                    List<GraphResults> graphResults = response.body();

                    List<BarEntry> entries = new ArrayList<>();
                    ArrayList<String> labels = new ArrayList<>();

                    int internalIndex = 0;
                    for (GraphResults g : graphResults) {
                        entries.add(new BarEntry(internalIndex, g.quantity));
                        labels.add(g.month + "");
                        internalIndex++;
                    }

                    barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

                    BarDataSet dataSet = new BarDataSet(entries, "Músicas enviadas em " + graphResults.get(0).year);
                    dataSet.setColor(getResources().getColor(R.color.colorAccent));
                    dataSet.setValueTextColor(getResources().getColor(R.color.colorWhite));

                    BarData barData = new BarData(dataSet);

                    barChart.getDescription().setText("Total");
                    barChart.setAutoScaleMinMaxEnabled(false);
                    barChart.setData(barData);

                    barChart.invalidate();
                }
            }

            @Override
            public void onFailure(Call<List<GraphResults>> call, Throwable t) {
                Log.d("API ERROR", t.getMessage());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
