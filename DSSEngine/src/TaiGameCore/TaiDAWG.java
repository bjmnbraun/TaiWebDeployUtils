package TaiGameCore;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * My super fancy DAWG implementation that supports all sorts of crazy deserialization / serialization features.
 * 
 * 'Yeah, I'm proud of it. The documentation would overwhelm this code, so please see the report instead.
 * 
 * @author Benjamin
 */
public class TaiDAWG <E>{
	public TaiDAWG(){
		TaiTrees.SiblingNode<WordByRef<E>> createNullNode = (TaiTrees.SiblingNode<WordByRef<E>>)TaiTrees.SiblingNode.createNullNode();
		SuperNode = new WordByRef<E>('0',null,0,createNullNode); //This is arbitrary. It won't ever matter.
	}
	//Null holder at the top of the tree
	private WordByRef<E> SuperNode;
	/**
	 * An arbitrary-linkage-numbered tree-map-node implementation. 
	 * 
	 * Null values are allowed, so be careful.
	 * @author Benjamin
	 */
	public static class WordByRef<E> implements TaiTrees.TaiTreeOTreesNode<WordByRef<E>>, Serializable{
		public static <F> String toString(TaiDAWG.WordByRef<F> data){
			//Crazy "go-up-the-tree" scheme.
			int sizeOfWebURL = data.getNodeDepth();
			char[] toFill = new char[sizeOfWebURL];
			TaiDAWG.WordByRef<F> cur = data;
			for(int k = 0; k < sizeOfWebURL; k++){
				toFill[toFill.length-1-k]=cur.getData();
				cur = cur.getParent();
			}
			return new String(toFill);
		}
		
		private char myChar;
		public static int REFCOUNT = 0;
		private E myValue;
		private WordByRef<E> parent;
		private int depth;
		private int maxWordLength = -1; //Height of an empty tree is -1
		private boolean isWord = false; //Flag to specify that this node of the tree can terminate a word
		/**
		 * All other subtrees branching off from this parent node.
		 */
		private TaiTrees.AATreeForSiblings<WordByRef<E>> levelSiblings;
		public int uniqueID = -1;

		public WordByRef(char theChar, WordByRef<E> parent, int depth, TaiTrees.SiblingNode<WordByRef<E>> treeNullNode){
			REFCOUNT++;
			myChar = theChar;
			this.parent = parent;
			this.depth = depth;
			levelSiblings = new TaiTrees.AATreeForSiblings<WordByRef<E>>(treeNullNode); 
		}
		public void setValue(E value){
			isWord = true; //This node ends a word! Yay!
			this.myValue = value;
		}
		/**
		 * Recursively ensure that the word is represented by our DicTree (that is, the union of subtrees)
		 */
		public WordByRef<E> insert(String word, int length, E value) {
			if (length==0){
				setValue(value);
				return this; //Base case. Done!
			}
			maxWordLength = Math.max(length,maxWordLength);
			//Now, maxWordLength >= 1. It is a valid height
			char nextLevel = word.charAt(word.length()-length);
			WordByRef exists = levelSiblings.get(nextLevel);
			if (exists==null){
				exists = new WordByRef(nextLevel,this,depth+1,levelSiblings.getNullNode());
				levelSiblings.insert(nextLevel,exists);
			}
			return exists.insert(word,length-1,value);
		}
		public E getContentData(){
			return myValue;
		}
		/**
		 * Returns null if no mapping is present for this word.
		 */
		public WordByRef<E> get(String word, int length){
			if (length==0){
				if (isWord){//Is this a full word?
					return this;
				} else {
					return null; //The word is actually a prefix
				}
			}
			char nextLevel = word.charAt(word.length()-length);
			WordByRef<E> exists = levelSiblings.get(nextLevel);
			if (exists==null){ //Definately not a word
				return null;
			}
			return exists.get(word,length-1);
		}
		/**
		 * Recursively checks if the word is "started" in the tree, that is, Prefixes will return true.
		 */
		public boolean isPrefix(String word){
			if (word.length()==0){
				return true; //The empty set is a subset of every set.
			}
			char nextLevel = word.charAt(0);
			WordByRef exists = levelSiblings.get(nextLevel);
			if (exists==null){
				return false;
			}
			return exists.isPrefix(word.substring(1));
		}
		/**
		 * Recursively checks if the word is "started" and successfully ends at a word-node of the tree.
		 * Only whole words will return true.
		 */
		public boolean isContained(String word){
			if (word.length()==0){
				if (isWord){//Is this a full word?
					return true;
				} else {
					return false; //The word is actually a prefix
				}
			}
			char nextLevel = word.charAt(0);
			WordByRef exists = levelSiblings.get(nextLevel);
			if (exists==null){ //Definately not a word
				return false;
			}
			return exists.isContained(word.substring(1));
		}

		public TaiTrees.AATreeForSiblings<WordByRef<E>> getSubTree(){
			return levelSiblings;
		}
		/**
		 * The height of a node is equal to the longest word ever inserted "past" it.
		 */
		public int height() {
			return maxWordLength;
		}
		public char getData() {
			return myChar;
		}
		public boolean isFlaggedNode() {
			return isWord;
		}
		public WordByRef<E> getParent() {
			return parent;
		}
		public int getNodeDepth() {
			return depth;
		}
	}
	/**
	 * Returns an iterator that can be used to traverse over all of the direct descendents of this node
	 */
	public TaiTrees.StringTreeIterator<WordByRef<E>> iterator() {
		return new TaiTrees.StringTreeIterator<WordByRef<E>>(SuperNode);
	}
	private int size = 0; //Running number of unique keys
	/**
	 * Exposed generation methods
	 */
	public TaiDAWG.WordByRef<E> insert(String key, E value) {
		TaiDAWG.WordByRef<E> got = SuperNode.get(key,key.length());
		if (got==null){
			size++;
			return SuperNode.insert(key, key.length(), value);
		}
		got.setValue(value);
		return got;
	}
	public TaiDAWG.WordByRef<E> get(String key) {
		return SuperNode.get(key, key.length());
	}
	/**
	 * Callback for deserializtion: Whenever a node is deserialized, notify this listener.
	 */
	public interface DeserializationOrderedListener<E>{
		public void OrderedDeserialization(WordByRef<E> next);
	}
	/**
	 * A "full" deserialization.
	 */
	public void readInTree(ObjectInput in, DeserializationOrderedListener<E> webSiteReferences) throws IOException, ClassNotFoundException {
		if (LogDeserialization){
			System.err.println("Fully Deserializing (!)");
		}
		readInTree(in,SuperNode,webSiteReferences,null,true);
	}
	private static boolean LogDeserialization = false;
	/*
	 * A "partial" deserialization.
	 */
	public void readInTree(ObjectInput in, DeserializationOrderedListener<E> webSiteReferences, String[] specificQuery) throws IOException, ClassNotFoundException {
		if (LogDeserialization){
			System.out.println("INFO: Partial Deseriazliation towards \""+specificQuery+"\"");
		}
		readInTree(in,SuperNode,webSiteReferences,specificQuery,true);
	}
	private transient char[] readInTreeBuffer = new char[1024];
	/**
	 * Deserialize this TaiDawg from in. This is more easily understood from the report.
	 * 
	 * root: this is a recursive deserializtion; this method will be called recursively on successive children
	 * 
	 * webSiteReferences: If non-null, each new node deseriailzed will trigger webSiteReference's callbacks
	 * (You can do with that what you will... )
	 * 
	 * onlySearch: Here is where the magic happens. We only deserialize the parts of the tree that are fuzzy-matched
	 * to this array of strings. That is, we minimize the size that actually has to be exploded to memory
	 * 
	 * maintainChildren: When we are "passing over" nodes (due to onlySearch telling us NOT to deserialize them),
	 * we need to make sure that child nodes do NOT add themselves to the tree. This basically just ensures that
	 * the resulting tree is valid.
	 */
	private void readInTree(ObjectInput in, WordByRef<E> root, DeserializationOrderedListener<E> webSiteReferences,String[] onlySearch, boolean maintainChildren) throws IOException, ClassNotFoundException {
		int numchildren = -1;
		if (in.readBoolean()){
			numchildren = in.readByte();
		} else {
			numchildren = in.readInt();			
		}
		int treeDepth = root.depth;
		for(int k = 0; k < numchildren; k++){
			char newChar = in.readChar();
			//Read the node
			WordByRef<E> toAdd = new WordByRef<E>(newChar,root,treeDepth+1,root.levelSiblings.getNullNode());
			toAdd.maxWordLength = -2; //UNKNOWN.
			//Decide how we want to handle this node
			boolean maintainChild = false;
			boolean ignoreChild = false;
			if (maintainChildren){ //Do a check to see if we should maintain the next level of children:
				//Ok, we can drop this sibling now if we need to.
				if (onlySearch!=null){
					//We have an instruction to ONLY deserialize up to a certain word.
					readInTreeBuffer[treeDepth]=newChar;
					//If this path matches one of the words...
					boolean fuzzyHolds = false;
					boolean ignoreShortWord = true;
					boolean wordTooShort = true;
					for(String possible : onlySearch){
						boolean fuzzyCond = treeDepth<possible.length();
						if (!fuzzyCond){
							continue; //Shortcircuiting
						}
						//A very broad fuzzy search. We'll use levenshtein distances to prune this later.
						for(int rc = 0; rc < treeDepth; rc++){
							fuzzyCond&=(readInTreeBuffer[rc]==possible.charAt(rc));
						}
						if (fuzzyCond){
							fuzzyHolds = true;
							//break; ''We can't break.
							//Ok, so this word fuzzy-requires this word.
							ignoreShortWord &= (possible.length()-1!=treeDepth);
							wordTooShort &= (possible.length()-3>treeDepth);
						}
					}
					if (fuzzyHolds){ //make this FUZZY BROADER
						//Maintain this child (everything else gets GC'ed.)
						maintainChild = true;
						//Ok, so now let's narrow our fuzzy search (it includes too much):
						if (treeDepth<=3 && ignoreShortWord){ //Now the whole word.
							//When words are less than or equal to 3 letters long, only exact matches are allowed.
							ignoreChild = true;
						} else {
							//An obvious prune: words that are 3 letters too short.
							if (wordTooShort){
								ignoreChild = true; //3 characters off.
							}
							//Fuzzy narrowing: Levenshtein distances
							//This carries into multilingual (i.e. asian) languages as well.
						}
					} else {
						//Just to make sure the GCing goes ok:
						toAdd.parent = null;
					}
				} else {
					//Normal behavior is to always maintain:
					maintainChild = true;
				}
			} //End "maintain children" check. If this block was not executed, continue skipping children.
			//Is it a word?
			boolean isWord = in.readBoolean();
			E value = null;
			if (isWord){
				value = readValue(in);
			}
			if (maintainChild){
				root.levelSiblings.insert(newChar, toAdd);
				SuperNode.maxWordLength = Math.max(SuperNode.maxWordLength, treeDepth+1);
				//If it's a word, do more:
				if (isWord){
					if (webSiteReferences!=null){
						webSiteReferences.OrderedDeserialization(toAdd);
					}
					if (!ignoreChild){ //Recognize its value.
						size++;
						toAdd.setValue(value);
					}
				}
			}
			//Recurse further.
			readInTree(in,toAdd,webSiteReferences,onlySearch,maintainChild);
		}
	}
	/**
	 * Serializes this taiDawg.
	 */
	public void writeOutTree(ObjectOutput out) throws IOException {
		int[] immutableInt = new int[]{0}; //counter
		writeOutTree(out,SuperNode,immutableInt);
		//System.out.println(immutableInt[0]+" nodes written");
	}
	/**
	 * Serializes this taiDawg.
	 * 
	 * superNode2: this is a recursive method, called to serialize successive children
	 * 
	 * immutableInt: an immutable integer (a single-element int array) that contains the number
	 * of nodes written out so far. We use this to assign a unique numbering system to each node
	 * in the tree. Very important for correct serialization!
	 */
	private void writeOutTree(ObjectOutput out, WordByRef<E> superNode2, int[] immutableInt) throws IOException {
		int sizie = superNode2.levelSiblings.size();
		if (sizie < Byte.MAX_VALUE){
			out.writeBoolean(true);
			out.writeByte(sizie);
		} else {
			out.writeBoolean(false);
			out.writeInt(sizie);
		}
		TaiTrees.BSTIterator<WordByRef<E>> iterator = superNode2.levelSiblings.iterator();
		for(int k = 0; k < sizie; k++){
			WordByRef<E> next = iterator.next();
			out.writeChar(next.myChar);
			out.writeBoolean(next.isWord);
			if (next.isWord){
				//Mark the uniqueID, increment so that the next node gets corrected:
				next.uniqueID = immutableInt[0]++;
				if (next.uniqueID > size){
					throw new RuntimeException(""+next.uniqueID);
				}
				writeValue(out,next.myValue);
			}
			writeOutTree(out,next,immutableInt);
		}
	}
	/**
	 * Subclasses of taiDawg can include a custom serialization routine for the E objects.
	 * This is ESPECIALLY useful if E is not serializable!
	 */
	public void writeValue(ObjectOutput out, E myValue) throws IOException {
		((ObjectOutputStream)out).writeUnshared(myValue);	
	}
	public E readValue(ObjectInput in) throws IOException, ClassNotFoundException {
		return (E)((ObjectInputStream)in).readUnshared();
	}
	public int size() {
		return size;
	}
}
