package TaiGameCore;

import java.util.ArrayList;

/**
 * Classes for easily creating gameDataBase editors (guis)
 */
public class GameDataEditorUtil {
	/**
	 * An (editable?) game entry
	 */
	public abstract static class ViewableGameDB<E extends GameDataBase> {
		public ViewableGameDB() {
			//			//setup the gui
			this.fields = new DataFieldMembers<E>();
		}

		public void updateValues() {
			for (DataFieldMember<? extends E> entry : fields.entries) {
				try {
					/** IF you did silly things, this will throw a classcastexception. **/
					((DataFieldMember<E>) entry).applyValue(currentData);
				} catch (Throwable e) {
					//Here, we can give a nice explanation:
					e.printStackTrace();
					throw new RuntimeException(entry.name);
				}
			}
		}

		public String getHashedString() {
			if (currentData != null) {
				//Update from fields:
				updateValues();
				//Do it!
				return currentData.hashToString();
			}
			return null;
		}

		public void cleanup() {
		};

		private DataFieldMembers<E> fields;
		public E currentData;

		public <F extends E> void setViewingObject(F found) {
			currentData = found;
			for (DataFieldMember<? extends E> k : fields.entries) {
				/** IF you did silly things, this will throw a classcastexception. **/
				((DataFieldMember<F>) k).updateFromDatabase(found);
			}
		}

		public abstract void draw();

		public DataFieldMembers<E> getFields() {
			return fields;
		}

		public <F extends E> void addDataField(DataFieldMember<F> k) {
			fields.addDataField(k);
		}
	}

	public static class DataFieldMembers<E extends GameDataBase> {
		private int numEntries = 0;

		public DataFieldMembers() {
			entries = new ArrayList();
		}

		public int getSize() {
			return numEntries;
		}

		public <F extends E> void addDataField(DataFieldMember<F> k) {
			k.myIndex = numEntries++;
			entries.add(k);
		}

		public ArrayList<DataFieldMember<? extends E>> entries;
	}

	public abstract static class DataFieldMember<E extends GameDataBase> {
		public String name;
		public int myIndex;

		public DataFieldMember(String name) {
			this.name = name;
		}

		public static String toString(int[] arr) {
			StringBuffer sb = new StringBuffer();
			for (int k = 0; k < arr.length; k++) {
				sb.append(arr[k]);
				if (k + 1 < arr.length) {
					sb.append(',');
				}
			}
			return sb.toString();
		}

		public static int[] parseIntArr(String arr) {
			arr = arr.trim();
			if (arr.length() == 0) {
				return new int[0];
			}
			String[] stuff = arr.split(",");
			int[] toRet = new int[stuff.length];
			for (int k = 0; k < stuff.length; k++) {
				toRet[k] = new Integer(stuff[k]);
			}
			return toRet;
		}

		/**
		 * Client code: check that currentData !=null before calling!
		 */
		public abstract void applyValue(E gdb);

		/**
		 * Precondish: gdb !=null
		 */
		public abstract String readValueFrom(E gdb);

		public abstract String getValue();

		public abstract void updateFromDatabase(E found);
	}
}
