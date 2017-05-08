package rbq2012.wechatcipher;

import android.app.*;
import android.os.*;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;
import de.robv.android.xposed.XSharedPreferences;
import android.content.SharedPreferences;
import android.content.Intent;

public class MainActivity extends Activity{
	
	SharedPreferences spref;
	
	public void switchWechatEncrypt(View v){
		Switch sw=(Switch) v;
		spref.edit().putBoolean(Constants.SPREF_KEY_WECHAT,sw.isChecked()).apply();
	}
	
	public void editProfiles(View v){
		startActivity(new Intent(this,ProfilesActivity.class)
			.putExtra(Constants.EXTRA_KEY_PROFILEFOR,Constants.SPREF_KEY_PROFILES_WECHAT)
		);
	}
	
	public void editPolicies(View v){
		startActivity(new Intent(this,ManageRulesActivity.class));
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mmain);
		spref=getSharedPreferences(Constants.SPREF_MAIN,Activity.MODE_WORLD_READABLE);
		Switch sw=(Switch) findViewById(R.id.mainSwWechat);
		sw.setChecked(spref.getBoolean(Constants.SPREF_KEY_WECHAT,false));
    }
}
