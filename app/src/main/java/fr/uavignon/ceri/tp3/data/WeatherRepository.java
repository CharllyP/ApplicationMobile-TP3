package fr.uavignon.ceri.tp3.data;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import fr.uavignon.ceri.tp3.data.database.CityDao;
import fr.uavignon.ceri.tp3.data.database.WeatherRoomDatabase;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

import static fr.uavignon.ceri.tp3.data.database.WeatherRoomDatabase.databaseWriteExecutor;

public class WeatherRepository {

    private static final String TAG = WeatherRepository.class.getSimpleName();

    private LiveData<List<City>> allCities;
    private MutableLiveData<City> selectedCity;

    private CityDao cityDao;


    private static volatile WeatherRepository INSTANCE;

    public synchronized static WeatherRepository get(Application application) {
        if (INSTANCE == null) {
            INSTANCE = new WeatherRepository(application);
        }

        return INSTANCE;
    }

    public WeatherRepository(Application application) {
        WeatherRoomDatabase db = WeatherRoomDatabase.getDatabase(application);
        cityDao = db.cityDao();
        allCities = cityDao.getAllCities();
        selectedCity = new MutableLiveData<>();
    }

    public LiveData<List<City>> getAllCities() {
        return allCities;
    }

    public MutableLiveData<City> getSelectedCity() {
        return selectedCity;
    }




    public long insertCity(City newCity) {
        Future<Long> flong = databaseWriteExecutor.submit(() -> {
            return cityDao.insert(newCity);
        });
        long res = -1;
        try {
            res = flong.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (res != -1)
            selectedCity.setValue(newCity);
        return res;
    }

    public int updateCity(City city) {
        Future<Integer> fint = databaseWriteExecutor.submit(() -> {
            return cityDao.update(city);
        });
        int res = -1;
        try {
            res = fint.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (res != -1)
            selectedCity.setValue(city);
        return res;
    }

    public void deleteCity(long id) {
        databaseWriteExecutor.execute(() -> {
            cityDao.deleteCity(id);
        });
    }

    public void getCity(long id)  {
        Future<City> fcity = databaseWriteExecutor.submit(() -> {
            Log.d(TAG,"selected id="+id);
            return cityDao.getCityById(id);
        });
        try {
            selectedCity.setValue(fcity.get());
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
