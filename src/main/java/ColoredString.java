import java.awt.Color;

public class ColoredString {
	public static final Color[] colors = {Color.decode("#80FF80"), Color.decode("#FFFF80"), Color.decode("#80FFFF"), Color.decode("#FF80FF"), Color.decode("#FF8080")};
	
	private String str;
	private byte type; //0=equal, 1=sub, 2=cross, 3=suchmodus, 4=sup
	
	
	
	public ColoredString(String name, byte num) {
		this.str=name;
		this.type=num;
	}
	public byte getType() {return type;}
	public String getName() {return str;}
	public String toString() {return str;}
}
