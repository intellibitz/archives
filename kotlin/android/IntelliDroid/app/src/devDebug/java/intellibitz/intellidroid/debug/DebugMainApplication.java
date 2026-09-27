package intellibitz.intellidroid.debug;

import com.facebook.stetho.Stetho;
import intellibitz.intellidroid.MainApplication;

//import com.facebook.stetho.DumperPluginsProvider;
//import com.facebook.stetho.dumpapp.DumperPlugin;
//import com.facebook.stetho.okhttp3.StethoInterceptor;

//import okhttp3.OkHttpClient;

/**
 */
public class DebugMainApplication extends MainApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
/*
        Stetho.newInitializerBuilder(this)
                .enableDumpapp(
                        Stetho.defaultDumperPluginsProvider(this))
                .enableWebKitInspector(
                        Stetho.defaultInspectorModulesProvider(this))
                .build();
*/
/*
        new OkHttpClient.Builder()
                .addNetworkInterceptor(new StethoInterceptor())
                .build();
*/
/*
        Stetho.initialize(Stetho.newInitializerBuilder(this)
                .enableDumpapp(new DumperPluginsProvider() {
                    @Override
                    public Iterable<DumperPlugin> get() {
                        return new Stetho.DefaultDumperPluginsBuilder(getApplicationContext())
//                                .provide(new MyDumperPlugin())
                                .finish();
                    }
                })
                .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                .build());
*/
    }

}
