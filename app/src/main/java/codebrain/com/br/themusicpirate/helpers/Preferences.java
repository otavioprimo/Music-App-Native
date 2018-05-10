package codebrain.com.br.themusicpirate.helpers;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {
    private Context contexto;
    private SharedPreferences preferences;
    private final String NOME_ARQUIVO = "piratemusics.preferences";
    private final int MODE = 0;
    private SharedPreferences.Editor editor;

    private final String CHAVE_IDENTIFICADOR = "identificadorUsuario";

    public Preferences(Context contextoParametro) {

        contexto = contextoParametro;
        preferences = contexto.getSharedPreferences(NOME_ARQUIVO,MODE);
        editor = preferences.edit();

    }

    public void setUserId(String identificador){
        editor.putString(CHAVE_IDENTIFICADOR,identificador);
        editor.commit();
    }

    public String getUserId(){
        return preferences.getString(CHAVE_IDENTIFICADOR,null);
    }
}
