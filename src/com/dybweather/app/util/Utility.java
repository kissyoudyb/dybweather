package com.dybweather.app.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.dybweather.app.db.DybWeatherDB;
import com.dybweather.app.model.City;
import com.dybweather.app.model.Province;

public class Utility {

	/**
	 * 解析和处理服务器返回的city.json数据
	 */
	public synchronized static boolean handleCityJsonResponse(DybWeatherDB dybWeatherDB, String response){
		if(!TextUtils.isEmpty(response)){
			try {
				JSONObject jsonObject = new JSONObject(response);
				JSONArray jsonArray = jsonObject.getJSONArray("城市代码");
				for(int i=0; i<jsonArray.length(); i++){
					JSONObject json = (JSONObject)jsonArray.get(i);
					
					JSONArray jsonArrayCities = json.getJSONArray("市");
					String city_code = ((JSONObject)jsonArrayCities.get(0)).getString("编码");
					String province_code = city_code.substring(0, 5);
					
					Province province = new Province();
					province.setProvinceName(json.getString("省"));
					province.setProvinceCode(province_code);
					dybWeatherDB.saveProvince(province);
					
					Log.d("Utility", "有" + jsonArrayCities.length());
					for(int j=0; j<jsonArrayCities.length(); j++){
						String city_name1 = ((JSONObject)jsonArrayCities.get(j)).getString("市名");
						String city_code1 = ((JSONObject)jsonArrayCities.get(j)).getString("编码");
						City city = new City();
						city.setCityCode(city_code1);
						city.setCityName(city_name1);
						city.setProvinceId(province_code);
						dybWeatherDB.saveCity(city);
						//Log.d("Utility", city_name1 + ", " + city_code1);
					}
					
					//将解析出来的数据储存到Province表
					
					//Log.d("Utility", province.getProvinceName() + ", " + province_code);
					
				}
				//Log.d("Utility", jsonArray.toString());
				return true;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}
	
	/**
	 * 解析服务器返回的JSON数据,并将解析出的数据存储到本地。
	 */
	public static void handleWeatherResponse(Context context, String response){
		
		try{
			JSONObject jsonObject = new JSONObject(response);
			JSONObject weatherInfo = jsonObject.getJSONObject("weatherinfo");
			String cityName = weatherInfo.getString("city");
			String weatherCode = weatherInfo.getString("cityid");
			String temp1 = weatherInfo.getString("temp1");
			String temp2 = weatherInfo.getString("temp2");
			String weatherDesp = weatherInfo.getString("weather");
			String publishTime = weatherInfo.getString("ptime");
			saveWeatherInfo(context, cityName, weatherCode, temp1, temp2, weatherDesp, publishTime);
		} catch(JSONException e){
			e.printStackTrace();
		}
	}
	
	/**
	 * 将服务器返回的所有天气信息存储到SharedPreferences文件中。
	 */
	public static void saveWeatherInfo(Context context, String cityName, String weatherCode, String temp1, String temp2, String weatherDesp, String publishTime){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy年M月d日", Locale.CHINA);
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
		editor.putBoolean("city_selected", true);
		editor.putString("city_name", cityName);
		editor.putString("weather_code", weatherCode);
		editor.putString("temp1", temp1);
		editor.putString("temp2", temp2);
		editor.putString("weather_desp", weatherDesp);
		editor.putString("publish_time", publishTime);
		editor.putString("current_date", sdf.format(new Date()));
		editor.commit();
	}

}
