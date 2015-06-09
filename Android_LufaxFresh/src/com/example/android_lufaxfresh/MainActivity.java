package com.example.android_lufaxfresh;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

import javax.security.auth.PrivateCredentialPermission;

import org.apache.http.util.ByteArrayBuffer;
import org.apache.http.util.EncodingUtils;

import android.R.integer;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	private TextView tv;
	private TimerTask mTimerTask;
	private Timer mTimer;
	private RadioButton rBtn_anying;
	private RadioGroup rBtnG_time;
	private RadioButton rBtn_time5;
	private RadioButton rBtn_time15;
	private RadioButton rBtn_time25;
	private Button btn_period;
	private Button btn_amount;
	private EditText et_period;
	private EditText et_amount;

	private String statusStr = "";
	private Vibrator vibrator;
	private int freshTime = 5000;
	private Handler handler = new Handler();
	private int totalANum = 0;
	private int myPeriod;
	private int myAmount;
	private int checkPeriod = 50;
	private int checkAmount = 200;
	private boolean vibrateSign = false;

	public static String EXISTCODE = "YES";
	public static String DISCODE = "NO";
	public static String ERRORCODE = "ERROR";
	public static String NOTCONNECTED = "网络连接错误";
	public static String URLPATH = "http://list.lufax.com/list/piaoju?minMoney=&maxMoney=&minDays=&maxDays=&minRate=&maxRate=&mode=&trade=&isCx=&currentPage=1&orderType=days&orderAsc=true";
	// public static String STARTPATH =
	// "<a href=\"https://list.lufax.com/list/piaoju\" class=\"cur\">";
	public static String STARTPATH = "https://list.lufax.com/list/all";
	public static String ASTATRTPATH = "特惠项目";
	public static String keyWordStr = "安盈";
	public static String keyWordAName = "安盈-票据";
	public static String keyWordAPeriod = "投资期限";
	public static String keyWordANum = "product-amount";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tv = (TextView) this.findViewById(R.id.textView1);
		rBtn_anying = (RadioButton) this.findViewById(R.id.radio0);
		rBtnG_time = (RadioGroup) this.findViewById(R.id.radioGroup2);
		rBtn_time5 = (RadioButton) this.findViewById(R.id.radio_time5);
		rBtn_time15 = (RadioButton) this.findViewById(R.id.radio_time15);
		rBtn_time25 = (RadioButton) this.findViewById(R.id.radio_time25);
		btn_period = (Button) this.findViewById(R.id.btn_period);
		btn_amount = (Button)this.findViewById(R.id.btn_amount);
		et_period = (EditText)this.findViewById(R.id.edit_period);
		et_amount = (EditText) this.findViewById(R.id.edit_amount);
		
		mTimer = new Timer();
		vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		
//		et_period.setFocusableInTouchMode(true);
//		et_period.clearFocus();
//		et_amount.clearFocus();
//		et_amount.setFocusableInTouchMode(true);
		btn_period.setOnClickListener(new Button.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				String getStr = et_period.getText().toString();
				try {
					checkPeriod = Integer.parseInt(getStr);
					Toast toast=Toast.makeText(getApplicationContext(), "成功将投资时间修改为："+checkPeriod +"天", Toast.LENGTH_LONG); 
					toast.show();
				} catch (Exception e) {
					// TODO: handle exception
				}
				
			}
		});

		rBtnG_time
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						// TODO Auto-generated method stub
						if (checkedId == rBtn_time5.getId())
							freshTime = 5000;
						else if (checkedId == rBtn_time15.getId())
							freshTime = 15000;
						else if (checkedId == rBtn_time25.getId())
							freshTime = 25000;

					}
				});

		handler.postDelayed(runnable, freshTime);
	}
	

	private Runnable runnable = new Runnable() {
		public void run() {
			new Thread() {
				public void run() {
					statusStr = getStatus();
					mHandler.sendEmptyMessage(1);
				};
			}.start();
			handler.postDelayed(this, freshTime);
		}
	};

	protected void onStop() {
		super.onStop();
		vibrator.cancel();
		mTimer.cancel();
	}

	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				if (vibrateSign) {
					long[] pattern = { 100, 400, 100, 400 }; // 停止 开启 停止 开启
					 vibrator.vibrate(pattern, 2); // 重复两次上面的pattern
					// 如果只想震动一次，index设为-1
				}
				else{
					vibrator.cancel();
				}
				SimpleDateFormat formatter = new SimpleDateFormat(
						"yyyy年MM月dd日    HH:mm:ss  ");
				Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
				String curTime = formatter.format(curDate);
				tv.setText(curTime + "\n" + statusStr + "\n");
				break;

			default:
				break;
			}
		};
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 得到网页内容
	 * 
	 * @param url_path
	 * @return
	 */
	public String getWebCon(String url_path) {
		StringBuffer sb = new StringBuffer();

		if (rBtn_anying.isChecked())
			keyWordStr = "安盈";
		else
			keyWordStr = "稳盈";

		try {
			java.net.URL url = new java.net.URL(url_path);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					url.openStream(), "UTF-8"));
			String line;
			String tmp;
			while ((line = in.readLine()) != null) {
				if (line.indexOf(STARTPATH) > 0) {
					while ((tmp = in.readLine()) != null) {
						if (tmp.indexOf(keyWordStr) > 0) {
							int start = 0;
							if ((start = tmp.indexOf("<em>")) > 0) {
								int end = tmp.indexOf("</em>");
								String numStr = tmp.substring(start + 5,
										end - 1);
								totalANum = Integer.parseInt(numStr);
								// return keyWordStr + ": " + EXISTCODE + ": " +
								// numStr;
							} else {
								totalANum = 0;
								return tmp + "\n" + keyWordStr + ": " + DISCODE;
							}
							break;
						}
					}
					// break;
				}
				int spos,endPos;
				String periodStr;
				String subStr;
				if (line.indexOf(ASTATRTPATH) > 0) {
					int count = 0;
					vibrateSign = false;
					while ((tmp = in.readLine()) != null && count < totalANum) {
						if (tmp.indexOf(keyWordAName) > 0) {
							spos = tmp.indexOf(keyWordAName);
							sb.append(tmp.substring(spos, spos + 17) + " : \n");
							String readStr;
							while ((readStr = in.readLine()) != null) {
								if (readStr.indexOf(keyWordAPeriod) > 0) {
									periodStr = in.readLine();
									spos = periodStr.indexOf("p") + 2;
									endPos = periodStr.indexOf("天");
									subStr = periodStr.substring(spos,
											endPos+1);
									myPeriod = Integer.parseInt(subStr.substring(0,subStr.indexOf("天")));
									sb.append(subStr + "    ");
								}
								if (readStr.indexOf(keyWordANum) > 0) {
									in.readLine();
									periodStr = in.readLine();
									spos = periodStr.indexOf("sty")+7;
									endPos = periodStr.indexOf(".");
									myAmount = Integer.parseInt(periodStr.substring(spos,endPos).replace(",", ""));
									sb.append(myAmount + "元\n");
									
									if(myAmount <= checkAmount && myPeriod <= checkPeriod){
										vibrateSign = true;
									}
									break;
								}
							}
							count++;
						}
					}
				}
			}

			in.close();
		} catch (Exception e) { // Report any errors that arise
			sb.append(e.toString());
			System.err.println(e);
			System.err
					.println("Usage:   java   HttpClient   <URL>   [<filename>]");
			statusStr = NOTCONNECTED;
			return statusStr + e.toString();
		}
		return keyWordStr + ": " + EXISTCODE + ": " + totalANum + "\n"
				+ sb.toString();
	}

	/**
	 * 得到状态
	 * 
	 * @return
	 */
	public String getStatus() {
		String str = getWebCon(URLPATH);

		if (str == NOTCONNECTED) {
			return str;
		}

		if (str == null)
			return ERRORCODE;

		return str;

	}
}
