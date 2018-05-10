package codebrain.com.br.themusicpirate;

import android.app.Application;
import android.provider.Settings;

import codebrain.com.br.themusicpirate.helpers.Preferences;

public class ApplicationClass extends Application {
    private Preferences preferences;

    @Override
    public void onCreate() {
        super.onCreate();

        String deviceId = Settings.Secure.getString(this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        preferences = new Preferences(this);
        preferences.setUserId(deviceId);
    }
}
