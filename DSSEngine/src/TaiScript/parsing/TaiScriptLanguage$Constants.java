package TaiScript.parsing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import BulletGame$2.BulletAccessory;
import BulletGame$2.BulletBackground;
import BulletGame$2.BulletBoss;
import BulletGame$2.BulletGlobals;
import BulletGame$2.BulletLevel;
import BulletGame$2.BulletPattern;
import BulletGame$2.BulletPlayer;
import BulletGame$2.BulletSpellCard;
import TaiGameCore.GameDataBase;

/**
 * All script words are case insensitive.
 * 
 * Multiple languages can be supported, but not simultaneously. So, yeah. to be worked on later.
 */
public interface TaiScriptLanguage$Constants {
	/**
	 * Describes a field that we want to be script editable
	 * This only helps with the documentation tool, really.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface AliasRefersTo {
		public Class<? extends GameDataBase.StringBase> GameDatabaseClass();
		public int displayOrder();
	}
	@AliasRefersTo(GameDatabaseClass=BulletPattern.class, displayOrder = 7)
	public static String BULLET = "bullet";
	@AliasRefersTo(GameDatabaseClass=BulletBoss.class, displayOrder = 3)
	public static String BOSS = "boss";
	public static String TEMPLATE = "template_type";
	@AliasRefersTo(GameDatabaseClass=BulletLevel.class, displayOrder = 1)
	public static String LEVEL = "level";
	@AliasRefersTo(GameDatabaseClass=BulletSpellCard.class, displayOrder = 5)
	public static String SPELLCARD = "spellcard";
	@AliasRefersTo(GameDatabaseClass=BulletAccessory.class, displayOrder = 4)
	public static String ACCESSORY = "accessory";
	@AliasRefersTo(GameDatabaseClass=BulletPlayer.class, displayOrder = 2)
	public static String PLAYER = "player";
	@AliasRefersTo(GameDatabaseClass=BulletGlobals.class, displayOrder = 0)
	public static String GLOBAL = "globals";
	@AliasRefersTo(GameDatabaseClass=BulletBackground.class, displayOrder = 6)
	public static String BACKGROUND = "background";
}
