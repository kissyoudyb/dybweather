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
}
