import java.awt.Color;

import com.spaetzle007.MapOfMathematicsLibraries.LinkedString;

public class ColoredString {
	public static final Color[] colors = {Color.decode("#FF8080"), Color.decode("#80FF80"), Color.decode("#FFFF80"), Color.decode("#FF80FF"), Color.decode("#80FFFF")};
	
	private String str;
	private byte type; //0=sup, 1=equal, 2=sub, 3=suchmodus, 4=cross
	
	
	
	public ColoredString(String name, byte num) {
		this.str=name;
		this.type=num;
	}
	public ColoredString(LinkedString str) {
		this.str=str.getName();
		type=(byte) (str.getType()+(byte)4);
	}
	public byte getType() {return type;}
	public String getName() {return str;}
	public String toString() {return str;}
}
