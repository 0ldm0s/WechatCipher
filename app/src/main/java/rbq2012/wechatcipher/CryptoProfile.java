package rbq2012.wechatcipher;

public class CryptoProfile{
	
	private String name;
	
	private CryptoProfile(){
		//
	}

	public void setName(String name){
		this.name=name;
	}

	public String getName(){
		return name;
	}
	
	public static CryptoProfile unserialize(String ser){
		CryptoProfile cry=new CryptoProfile();
		return cry;
	}
}
