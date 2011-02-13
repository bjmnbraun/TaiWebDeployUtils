package TaiGameCore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import TaiGameCore.TaiDAWG.WordByRef;


/**
 * Game databases are basically maps of data with string keys.
 * 
 * This class was written on Saturday, December 12, 2009 7:36 PM
 * 
 * and it shouldn't be edited.
 */
public abstract class GameDataBase {

	public static interface StringBase<Assignment> {
		public static interface Validator{
			public void validate(String fieldname, String data) throws ValidationException;
		}
		public static class ValidationException extends Exception{
			public ValidationException(String message, String fieldname, Throwable source){
				super(message,source);
				this.fieldname = fieldname;
			}
			public ValidationException(String message, String fieldname){
				this(message,fieldname,null);
			}
			public String fieldname;
		};

		public ArrayList<Exception> parseFromStrings(TaiDAWG<Assignment> data, Validator ... valid) throws FieldRequiredException, ValidationException;

		/**
		 * Describes a field that we want to be script editable
		 */
		@Retention(RetentionPolicy.RUNTIME)
		@Target(ElementType.FIELD)
		public @interface FromScript {
		}
		
		/**
		 * Causes a template being read to autogenerate the getter methods for its fields.
		 */
		@Retention(RetentionPolicy.RUNTIME)
		@Target(ElementType.TYPE)
		public @interface MakeGetters {
		}
		
		/**
		 * When the autowriter is run on a class with this annotation, the corresponding template classes
		 * are loaded and invoked in writing the class.
		 */
		@Retention(RetentionPolicy.RUNTIME)
		@Target(ElementType.TYPE)
		public @interface ExtendsData {
			public Class[] parents();
		}

		/**
		 * Describes a field that must be provided
		 */
		@Retention(RetentionPolicy.RUNTIME)
		@Target(ElementType.FIELD)
		public @interface CriticalScriptField {
		}


		/**
		 * Describes a field that must be validated, by the nth validator
		 */
		@Retention(RetentionPolicy.RUNTIME)
		@Target(ElementType.FIELD)
		public @interface HasValidator {
			public int num();
		}
		
		/**
		 * Describes fields that are processed before being validated, depending on
		 * their lexical scope.
		 * 
		 * The method called to do the processing must reside in the same class as the field declarer.
		 */
		@Retention(RetentionPolicy.RUNTIME)
		@Target(ElementType.TYPE)
		public @interface ScopePreValidate {
			public String method();
		}

		/**
		 * Describes a field that bypasses the ScopePreValidate step.
		 */
		@Retention(RetentionPolicy.RUNTIME)
		@Target(ElementType.FIELD)
		public @interface IsUnscoped {
		}
		
		public static class FieldRequiredException extends Exception{
			public FieldRequiredException(String msg){
				super(msg);
			}
		}
	}

	/**
	 * Describes the DefaultValue that accompanies a given field in a database.
	 * If a given field cannot be given a default value, also add the 
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface DefaultValue {
		String value();
	}
	public static void hashToClipboard(GameDataBase gdb){
		//Put it onto the clipboard.
		TaiScriptEditor tse = new TaiScriptEditor();
		String got = gdb.hashToString();
		if(got==null){
			return;
		}
		tse.insertText(got);
		tse.selectLine(0);
		tse.copy();
	}

	public abstract void autoWrittenDeSerializeCode();
	public abstract void autoWrittenSerializeCode();

	private DataBaseDawg currentData;

	/*
	 * New database: (or a subclass):
		GameDataBase gdb = new GameDataBase("");
		gdb.writeField("Name", new StringEntry("Garglesmash attaack"));
		String val = gdb.hashToString();

	   Reading a database:
		GameDataBase readBack = new GameDataBase(val);
		String got = ((StringEntry)readBack.readField("Name", new StringEntry("Unknown Pokemon"))).getString();
		System.out.println(got);
	 */

	public GameDataBase(String hash) {
		initFromHash(hash);
	}

	/**
	 * Modifiers
	 */
	public void writeField(String key, DataEntry val) {
		currentData.insert(key, val);
	}

	public DataEntry readField(String name, DataEntry defaultValue) {
		DataEntry toRet = defaultValue;
		WordByRef<DataEntry> wordByRef = currentData.get(name);
		if (wordByRef!=null){
			DataEntry got = wordByRef.getContentData();
			if (got != null) {
				toRet = got;
			}
		}
		return toRet;
	}

	/**
	 * Kinds of data entries
	 */
	public static abstract class DataEntry {
		public abstract void readExternal(ObjectInput in) throws IOException;

		public abstract void writeExternal(ObjectOutput out) throws IOException;
	}

	public static class IntEntry extends DataEntry {
		public static final int BYTE_TYPE = 0;

		public IntEntry() {
			// Deserialization.
		}

		public IntEntry(int value) {
			val = value;
		}

		private int val;

		public int getInt() {
			return val;
		}

		public void readExternal(ObjectInput in) throws IOException {
			val = in.readInt();
		}

		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeInt(val);
		}
	}

	public static class StringEntry extends DataEntry {
		public static final int BYTE_TYPE = 1;

		public StringEntry() {
			// Deserialization.
		}

		public StringEntry(String value) {
			val = value;
		}

		private String val;

		public String getString() {
			return val;
		}

		public void readExternal(ObjectInput in) throws IOException {
			int length = in.readInt();
			char[] len = new char[length];
			for(int k = 0; k < length; k++){
				len[k] = in.readChar();
			}
			val = new String(len);
		}

		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeInt(val.length());
			for(char k : val.toCharArray()){
				out.writeChar(k);
			}
		}
	}

	public static class IntArrayEntry extends DataEntry {
		public static final int BYTE_TYPE = 2;

		public IntArrayEntry() {
			// Deserialization.
		}

		public IntArrayEntry(int[] value) {
			val = value;
		}

		private int[] val;

		public int[] getIntArray() {
			return val;
		}

		public void readExternal(ObjectInput in) throws IOException {
			int length = in.readInt();
			int[] len = new int[length];
			for(int k = 0; k < length; k++){
				len[k] = in.readInt();
			}
			val = len;
		}

		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeInt(val.length);
			for(int k : val){
				out.writeInt(k);
			}
		}
	}

	public static class ByteArrayEntry extends DataEntry {
		public static final int BYTE_TYPE = 3;

		public ByteArrayEntry() {
			// Deserialization.
		}

		public ByteArrayEntry(byte[] value) {
			val = value;
		}

		private byte[] val;

		public byte[] getByteArray() {
			return val;
		}

		public void readExternal(ObjectInput in) throws IOException {
			int length = in.readInt();
			byte[] len = new byte[length];
			for(int k = 0; k < length; k++){
				len[k] = in.readByte();
			}
			val = len;
		}

		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeInt(val.length);
			for(int k : val){
				out.writeByte(k);
			}
		}
	}

	public static class DoubleArrayEntry extends DataEntry {
		public static final int BYTE_TYPE = 4;

		public DoubleArrayEntry() {
			// Deserialization.
		}

		public DoubleArrayEntry(double[] value) {
			val = value;
		}

		private double[] val;

		public double[] getDoubleArray() {
			return val;
		}

		public void readExternal(ObjectInput in) throws IOException {
			int length = in.readInt();
			double[] len = new double[length];
			for(int k = 0; k < length; k++){
				len[k] = in.readDouble();
			}
			val = len;
		}

		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeInt(val.length);
			for(double k : val){
				out.writeDouble(k);
			}
		}
	}

	public static class DoubleEntry extends DataEntry {
		public static final int BYTE_TYPE = 5;

		public DoubleEntry() {
			// Deserialization.
		}

		public DoubleEntry(double value) {
			val = value;
		}

		private double val;

		public double getDouble() {
			return val;
		}

		public void readExternal(ObjectInput in) throws IOException {
			val = in.readDouble();
		}

		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeDouble(val);
		}
	}

	public static class FloatArrayEntry extends DataEntry {
		public static final int BYTE_TYPE = 6;

		public FloatArrayEntry() {
			// Deserialization.
		}

		public FloatArrayEntry(float[] value) {
			val = value;
		}

		private float[] val;

		public float[] getFloatArray() {
			return val;
		}

		public void readExternal(ObjectInput in) throws IOException {
			int length = in.readInt();
			float[] len = new float[length];
			for(int k = 0; k < length; k++){
				len[k] = in.readFloat();
			}
			val = len;
		}

		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeInt(val.length);
			for(float k : val){
				out.writeFloat(k);
			}
		}
	}

	
	private static class DataBaseDawg extends TaiDAWG<DataEntry> {
		public void writeValue(ObjectOutput oos, DataEntry value)
		throws IOException {
			if (value instanceof IntEntry) {
				oos.writeByte(IntEntry.BYTE_TYPE);
			} else if (value instanceof StringEntry){
				oos.writeByte(StringEntry.BYTE_TYPE);
			} else if (value instanceof IntArrayEntry){
				oos.writeByte(IntArrayEntry.BYTE_TYPE);
			} else if (value instanceof ByteArrayEntry){
				oos.writeByte(ByteArrayEntry.BYTE_TYPE);
			} else if (value instanceof DoubleArrayEntry){
				oos.writeByte(DoubleArrayEntry.BYTE_TYPE);
			} else if (value instanceof DoubleEntry){
				oos.writeByte(DoubleEntry.BYTE_TYPE);
			} else if (value instanceof FloatArrayEntry){
				oos.writeByte(FloatArrayEntry.BYTE_TYPE);
			} else {
				throw new RuntimeException("Unknwon DataEntryType: "
						+ value.getClass());
			}
			value.writeExternal(oos);
		}

		public DataEntry readValue(ObjectInput ois) throws IOException {
			DataEntry toRet = null;
			byte type = ois.readByte();
			switch (type) {
			case IntEntry.BYTE_TYPE:
				toRet = new IntEntry();
				break;
			case StringEntry.BYTE_TYPE:
				toRet = new StringEntry();
				break;
			case IntArrayEntry.BYTE_TYPE:
				toRet = new IntArrayEntry();
				break;
			case ByteArrayEntry.BYTE_TYPE:
				toRet = new ByteArrayEntry();
				break;
			case DoubleArrayEntry.BYTE_TYPE:
				toRet = new DoubleArrayEntry();
				break;
			case DoubleEntry.BYTE_TYPE:
				toRet = new DoubleEntry();
				break;
			case FloatArrayEntry.BYTE_TYPE:
				toRet = new FloatArrayEntry();
				break;
			}
			if (toRet==null){
				throw new RuntimeException("Unknown DatEntryType: " + type);
			}
			toRet.readExternal(ois);
			return toRet;
		}
	}
	private final void initFromHash(String hash) {
		//New tree!
		currentData = new DataBaseDawg();
		//Read the tree
		if (hash.length() > 0) {
			try {
				byte[] input = decodeString(hash);
				ByteArrayInputStream bais = new ByteArrayInputStream(input);
				ZipInputStream zis = new ZipInputStream(bais);
				zis.getNextEntry();
				ObjectInputStream ois = new ObjectInputStream(zis);
				currentData.readInTree(ois,null);
			} catch (Throwable e){
				e.printStackTrace();
				throw new RuntimeException("Invalid hash "+hash);
			}
		} else {
			// Base case: This case is used only by the utility that creates new
			// databases.
			//
			// Ok.
		}
		//Read it in!
		autoWrittenDeSerializeCode();
		//Clear it up!
		currentData = null;
	}

	public final String hashAllToString(GameDataBase[] stuffs){
		StringBuffer sb = new StringBuffer();
		for(int k = 0; k < stuffs.length; k++){
			if (stuffs[k]!=null){
				sb.append(stuffs[k].hashToString());
			}
			if (k+1<stuffs.length){
				sb.append(",");
			}
		}
		return sb.toString();
	}

	public final String hashToString() {
		//New tree!
		currentData = new DataBaseDawg();
		//Write to the tree..
		autoWrittenSerializeCode();
		//Encode the tree...
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ZipOutputStream zos = new ZipOutputStream(baos);
			zos.putNextEntry(new ZipEntry("A"));
			ObjectOutputStream oos = new ObjectOutputStream(zos);
			currentData.writeOutTree(oos);
			oos.flush();
			zos.closeEntry();
			zos.finish();
			zos.flush();
			baos.flush();
			byte[] array = baos.toByteArray();
			return encodeString(array);
		} catch (Throwable e) {
			System.err.println("This code should always be safe!!!!!");
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			//Clear the tree.
			currentData = null;
		}
	}

	/**
	 * HASHING TRANSFORMATION:
	 */
	private static final char[] charMap = ("ABCDEFGH" + "qrstuvwx" + "IJKLMNOP"
			+ "01234567").toCharArray();
	private static final int[] invCharMap = new int[255];
	static {
		for (int k = 0; k < charMap.length; k++) {
			invCharMap[(int) charMap[k]] = k;
		}
	}

	public static byte[] decodeString(String hash) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(
				hash.length() * 2);
		for (int l = 0; l < hash.length(); l += 2) {
			int lowByte = invCharMap[hash.charAt(l)];
			lowByte = (lowByte / 2);
			int hiByte = invCharMap[hash.charAt(l + 1)];
			hiByte = (hiByte - 1) / 2;
			int together = hiByte * 16 + lowByte;
			baos.write((byte) (together - 128));
		}
		return baos.toByteArray();
	}

	public static String encodeString(byte[] toEncode) {
		StringBuffer sb = new StringBuffer();
		for (byte k : toEncode) {
			int val = k + 128;
			sb.append(charMap[(val % 16) * 2]);
			sb.append(charMap[(val / 16) * 2 + 1]);
		}
		return sb.toString();
	}

}
