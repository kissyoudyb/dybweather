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
	 * �����ʹ�����������ص�city.json����
	 */
	public synchronized static boolean handleCityJsonResponse(DybWeatherDB dybWeatherDB, String response){
		if(!TextUtils.isEmpty(response)){
			try {
				JSONObject jsonObject = new JSONObject(response);
				JSONArray jsonArray = jsonObject.getJSONArray("���д���");
				for(int i=0; i<jsonArray.length(); i++){
					JSONObject json = (JSONObject)jsonArray.get(i);
					
					JSONArray jsonArrayCities = json.getJSONArray("��");
					String city_code = ((JSONObject)jsonArrayCities.get(0)).getString("����");
					String province_code = city_code.substring(0, 5);
					
					Province province = new Province();
					province.setProvinceName(json.getString("ʡ"));
					province.setProvinceCode(province_code);
					dybWeatherDB.saveProvince(province);
					
					Log.d("Utility", "��" + jsonArrayCities.length());
					for(int j=0; j<jsonArrayCities.length(); j++){
						String city_name1 = ((JSONObject)jsonArrayCities.get(j)).getString("����");
						String city_code1 = ((JSONObject)jsonArrayCities.get(j)).getString("����");
						City city = new City();
						city.setCityCode(city_code1);
						city.setCityName(city_name1);
						city.setProvinceId(province_code);
						dybWeatherDB.saveCity(city);
						//Log.d("Utility", city_name1 + ", " + city_code1);
					}
					
					//���������������ݴ��浽Province��
					
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
	 * �������������ص�JSON����,���������������ݴ洢�����ء�
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
	 * �����������ص�����������Ϣ�洢��SharedPreferences�ļ��С�
	 */
	public static void saveWeatherInfo(Context context, String cityName, String weatherCode, String temp1, String temp2, String weatherDesp, String publishTime){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy��M��d��", Locale.CHINA);
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
