package com.dybweather.app.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dybweather.app.R;
import com.dybweather.app.db.DybWeatherDB;
import com.dybweather.app.model.City;
import com.dybweather.app.model.Province;
import com.dybweather.app.util.HttpCallbackListener;
import com.dybweather.app.util.HttpUtil;
import com.dybweather.app.util.Utility;

public class ChooseAreaActivity extends Activity {
	
	public static final String TAG = "ChooseAreaActivity";

	public static final int LEVEL_PROVINCE = 0;
	
	public static final int LEVEL_CITY = 1;
	
	private ProgressDialog progressDialog;
	private TextView titleText;
	private ListView listView;
	private ArrayAdapter<String> adapter;
	private DybWeatherDB dybWeatherDB;
	private List<String> dataList = new ArrayList<String>();
	
	private List<Province> provinceList;
	private List<City> cityList;
	private Province selectedProvince;
	private City selectedCity;
	
	private int currentLevel;
	
	private boolean isFromWeatherActivity;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isFromWeatherActivity = getIntent().getBooleanExtra("from_weather_activity", false);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		//已经选择了城市且不是从WeatherActivity跳转过来，才会直接跳转到WeatherActivity
		if(prefs.getBoolean("city_selected", false) && !isFromWeatherActivity){
			Intent intent = new Intent(this, WeatherActivity.class);
			startActivity(intent);
			finish();
			return;
		}
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		listView = (ListView) findViewById(R.id.list_view);
		titleText = (TextView) findViewById(R.id.title_text);
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
		listView.setAdapter(adapter);
		dybWeatherDB = DybWeatherDB.getInstance(this);
		queryProvinces(); //加载省级数据
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int index,
					long arg3) {
				if(currentLevel == LEVEL_PROVINCE){
					selectedProvince = provinceList.get(index);
					//Toast.makeText(ChooseAreaActivity.this, "你点击了:" + selectedProvince.getProvinceName() + ", 编码为: " + selectedProvince.getProvinceCode(), Toast.LENGTH_SHORT).show();
					queryCities();
				} else if(currentLevel == LEVEL_CITY){
					selectedCity = cityList.get(index);
					String cityCode = selectedCity.getCityCode();
					String cityName = selectedCity.getCityName();
					//Toast.makeText(ChooseAreaActivity.this, "您选择的城市名为: " + cityName + ", 城市代码为: " + cityCode, Toast.LENGTH_SHORT).show();
					Intent intent = new Intent(ChooseAreaActivity.this, WeatherActivity.class);
					intent.putExtra("city_code", cityCode);
					startActivity(intent);
					finish();
					
				}
			}
			
		});
		
	}
	
	private void queryProvinces(){
		provinceList = dybWeatherDB.loadProvinces();
		if(provinceList.size() > 0){
			dataList.clear();
			for(Province province : provinceList){
				dataList.add(province.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		} else {
			queryFromServer(null, "province");
		}
	}
	
	private void queryCities(){
		cityList = dybWeatherDB.loadCities(selectedProvince.getProvinceCode());
		//Toast.makeText(ChooseAreaActivity.this, "cityList.length(): " + cityList.size(), Toast.LENGTH_SHORT).show();
		if(cityList.size() > 0){
			dataList.clear();
			for(City city : cityList){
				dataList.add(city.getCityName());
			}
			adapter.notifyDataSetChanged();
			listView.setSelection(0);
			titleText.setText(selectedProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		} else {
			
		}
	}
	
	private void queryFromServer(final String code, final String type){
		String address;
		if(!TextUtils.isEmpty(code)){
			address = "http://www.baidu.com";
		} else {
			address = "http://10.0.2.2/android/city.json";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(final String response) {
				boolean result = false;
				if("province".equals(type)){
					result =Utility.handleCityJsonResponse(dybWeatherDB, response);
				}
				
				if(result){
					//通过runOnUiThread()方法返回到主线程处理逻辑
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							closeProgressDialog();
							if("province".equals(type)){
								queryProvinces();
								
								//Toast.makeText(ChooseAreaActivity.this, "加载数据成功,response:" + response, Toast.LENGTH_SHORT).show();
							}
							
						}
					});
				} else {
					//通过runOnUiThread()方法返回到主线程处理逻辑
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							closeProgressDialog();
							Toast.makeText(ChooseAreaActivity.this, "加载失败,result=false", Toast.LENGTH_SHORT).show();
						}
					});
				}
			}
			
			@Override
			public void onError(Exception e) {
				//通过runOnUiThread()方法回到主线程处理逻辑
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						closeProgressDialog();
						Toast.makeText(ChooseAreaActivity.this, "加载失败", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	
	private void showProgressDialog(){
		if(progressDialog == null){
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("正在加载...");
			progressDialog.setCanceledOnTouchOutside(false);
		}
		progressDialog.show();
	}
	
	private void closeProgressDialog(){
		if(progressDialog != null){
			progressDialog.dismiss();
		}
	}
	
	@Override
	public void onBackPressed() {
		Log.d(TAG, "currentLevel==========>:" + currentLevel);
		if(currentLevel == LEVEL_CITY){
			queryProvinces();
		} else {
			if(isFromWeatherActivity){
				Intent intent = new Intent(this, WeatherActivity.class);
				startActivity(intent);
			}
			finish();
		}
	}
}
