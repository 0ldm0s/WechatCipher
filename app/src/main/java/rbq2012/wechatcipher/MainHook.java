package rbq2012.wechatcipher;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import java.io.File;
import java.util.List;
import java.util.Set;
import org.json.JSONObject;
import static rbq2012.wechatcipher.Constants.*;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import org.json.JSONArray;
import android.app.AlertDialog;
import org.json.JSONException;
import android.content.DialogInterface;

public class MainHook extends XC_MethodHook
implements IXposedHookZygoteInit, IXposedHookLoadPackage, IXposedHookInitPackageResources{

	static public EditText inpu=null;
	static List<JSONObject> profiles=null;
	static Map<String,CryptoRule> rules=null;
	static int prof;

	@Override
	public void handleLoadPackage(XC_LoadPackage.LoadPackageParam p1) throws Throwable{
		if(!p1.packageName.equals(Constants.PM_MM)) return;
		XSharedPreferences spref=new XSharedPreferences(Constants.PM_THIS,Constants.SPREF_MAIN);
		if(!spref.getBoolean(Constants.SPREF_KEY_WECHAT,false)){
			flog("Disabled.for wechat");
			return;
		}
		flog("handleLoadPackage");

		if(profiles==null){
			getProfiles();
			prof=0;
		}

		/* 在为发送按钮设置OnClickListener时，将微信原设置的Listener
		 * 备份，然后将传入参数替换成自定义的Listener，以便在点击按
		 * 钮前执行自定义的代码，再执行备份的原Listener的点击事件，
		 * 使微信将消息发送出去。
		 */
		XC_MethodHook hooksend=new XC_MethodHook(){
			@Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable{
				View btn=(View)param.thisObject;
				if(btn.getId()!=2131756121) return;
				btn.setTag(btn.getId());
				btn.setTag(param.args[0]);
				param.args[0]=new OnClickListener(){
					@Override
					public void onClick(View p1){
						//插入在触发点击事件之前进行的处理，比如加密输入框的文本
						try{
							//Logger.log("prof="+prof);
							String text=inpu.getText().toString();
							Logger.log("text="+text);
							if(prof!=0){
								JSONObject jso=profiles.get(prof-1);
								CryptoRule rule=rules.get(jso.getString(JSON_KEY_ENCRULE));
								//Logger.log(""+(text==null)+","+(rule==null));
								//Thread.currentThread().sleep(1000);
								String saf=Cryptoo.encrypt(text,rule);
								inpu.setText(saf);
								Logger.log("saf="+saf);
							}
						}
						catch(Exception e){
							Logger.log(e);
						}
						OnClickListener fuck=(View.OnClickListener) (p1.getTag());
						fuck.onClick(p1);
						//插入在触发点击事件之后进行的处理，比如显示提示信息
					}
				};
			}
			@Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable{
			}
		};
		findAndHookMethod(View.class,"setOnClickListener",View.OnClickListener.class,hooksend);
		////

		/* 通过构造函数找到消息输入框，并保存。
		 */
		XC_MethodHook hooket=new XC_MethodHook(){
			@Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable{
				View v=(View) param.thisObject;
				if(v.getId()==2131756115){
					EditText et=(EditText)v;
					et.setBackgroundColor(Color.GREEN);
					inpu=et;
				}
			}
		};
		findAndHookConstructor(EditText.class,Context.class,AttributeSet.class,hooket);
		////

		/* 文字气泡被微信设置文字时，先将文字解密
		 */
		XC_MethodHook hookbub=new XC_MethodHook(){
			@Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable{
				View v=(View)param.thisObject;
				if(v.getId()!=2131755346) return;
				String text=null;
				if(prof!=0)try{
				JSONObject jso=profiles.get(prof-1);
				JSONArray ja=jso.getJSONArray(JSON_KEY_DECRULES);
				String saf=(String) param.args[0];
				for(int i=0;i<ja.length();i++){
					CryptoRule rule=rules.get(ja.getString(i));
					Logger.log("Trying to decrypt with rule "+rule.getName());
					text=Cryptoo.decrypt(saf,rule);
					if(text!=null) break;
				}
				}catch(Exception e){
					//
				}
				if(text!=null){
					param.args[0]=text;
				}else{}
			}
		};
		findAndHookMethod(TextView.class,"setText",CharSequence.class,hookbub);

		//工具栏
		XC_MethodHook hooktit=new XC_MethodHook(){
			@Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable{
				TextView v=(TextView)param.thisObject;
				if(v.getId()!=2131755274) return;
				v.setTextSize(10);
				v.setSingleLine(false);
				v.setMaxLines(2);
				//v.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
				v.setOnClickListener(new OnClickListener(){
						@Override
						public void onClick(View p1){
							selectProfile(p1);
						}
					});
				param.args[0]="点击切换模式\n"+param.args[0];
			}
		};
		findAndHookMethod(TextView.class,"setText",CharSequence.class,hooktit);

	}

	private void selectProfile(View v){
		try{
		String[] list=new String[profiles.size()+2];
		list[0]="不使用";
		for(int i=0;i<profiles.size();i++){
			try{
				list[i+1]=profiles.get(i).getString(JSON_KEY_NAME);
			}
			catch(JSONException e){
				list[i+1]="加载失败";
			}
		}
		list[profiles.size()+1]="刷新列表";
		//list=new String[]{"0","8"};
		AlertDialog dia=new AlertDialog.Builder(v.getContext())
		.setTitle("选择方案")
			.setItems(list,new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface p1,int p2){
					if(p2<profiles.size()+1){
						prof=p2;
						return;
					}
					getProfiles();
				}
			}
		)
		.create();
		dia.show();
		}catch(Exception e){
			Logger.log(e);
		}
	}

	private void getProfiles(){
		try{
			XSharedPreferences xp=new XSharedPreferences(PM_THIS,Constants.SPREF_MAIN);
			Set<String> set=xp.getStringSet(SPREF_KEY_PROFILES_WECHAT,null);
			if(profiles==null) profiles=new ArrayList<JSONObject>();
			else profiles.clear();
			if(set!=null){
				for(String s:set){
					profiles.add(new JSONObject(s));
				}
			}else{
			}
			set=xp.getStringSet(SPREF_KEY_ALLRULES,null);
			if(rules==null) rules=new HashMap<String,CryptoRule>();
			else rules.clear();
			if(set!=null){
				for(String s:set){
					CryptoRule rule=CryptoRule.unserialize(s);
					rules.put(rule.getName(),rule);
				}
			}
		}
		catch(Exception e){
			Logger.log(e);
		}
	}

	@Override
	public void handleInitPackageResources(XC_InitPackageResources.InitPackageResourcesParam p1) throws Throwable{
		/*if(!p1.packageName.equals(PM_MM)) return;
		 flog("handleInitPackageResources");*/
	}

	@Override
	public void initZygote(IXposedHookZygoteInit.StartupParam p1) throws Throwable{
		flog("initZygote");
	}

	static private void flog(String s){
		try{
			Logger.setupIfNeed(new File(Constants.LOG_FILE));
			Logger.log(s);
		}
		catch(Exception e){}
	}

}
