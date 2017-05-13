package rbq2012.wechatcipher;
import android.view.View;
import android.widget.TextView;
import android.widget.EditText;

public class ViewVeri{
	
	static int version=0;
	
	static public boolean isQqTitle(TextView tv){
		if(version==671 && tv.getId()==0x7f0a0419) return true;
		if(version==700 && tv.getId()==0x7f0a0452){
			if(tv.getMaxLines()==2) return false;
			return true;
		}
		return false;
	}
	
	static public boolean isQqTextbox(EditText et){
		if(version==671 && et.getId()==2131363128) return true;
		if(version==700 && et.getId()==2131363175) return true;
		return false;
	}
	
	static public boolean isQqSendbtn(View v){
		if(version==671 && v.getId()==2131363129) return true;
		if(version==700 && v.getId()==2131363176) return true;
		return false;
	}
	
	static public boolean isQqBubtext(TextView tv){
		try{
			return !isQqTextbox((EditText)tv);
		}catch(Exception e){
			return true;
		}
	}
}
