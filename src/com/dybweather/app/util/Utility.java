package com.dybweather.app.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
}
