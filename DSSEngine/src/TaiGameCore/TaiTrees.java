package TaiGameCore;

/**
 * This class is a collection of data structures which were necessary for my implementation of Boggle!
 * 
 * It contains:
 * 1) SiblingNode, AATreeForSiblings: AA-balancing tree, with no removal feature
 * 2) AASiblingTreeIterator: a nonrecursive in-order iterator over (1).
 * 3) A LinkedLyst class is used by both of the above
 * 4) A DoubleTreeIterator implementation, StringTreeIterator. This allows us to iterate over trees-of-trees.
 *
 * Number of imports needed to compile this class: 0. Yay for indie code.
 */
public class TaiTrees {

	/*******************
	 * To start: Some marker classes that describe trees
	 ******************/

	/**
	 * A tree node with value type 'E';
	 */
	public static interface TaiBSTNode<E> {
		public TaiBSTNode<E> getLeft();

		public TaiBSTNode<E> getRight();

		public E getData();
	}

	/**
	 * A BST
	 */
	public static interface TaiBST<E> {
		public BSTIterator<E> iterator();

		public TaiBSTNode<E> getNullNode();
	}

	/**
	 * Alright, now this is the hardest of all these classes to understand.
	 * Given the above class (TaiTreeOTrees), this class represents a NODE of that
	 * very special tree.
	 */
	public interface TaiTreeOTreesNode<F extends TaiTreeOTreesNode<F>> {
		public TaiBST<F> getSubTree();

		public int height();

		public char getData();

		public boolean isFlaggedNode();
	}

	/**
	 * Returns an in-order traversor of an arbitrary-node-order tree. Any node of the outer tree is a BST, defining
	 * all of its children.
	 * 
	 * This will allow us to iterate Strings out of a Tree with node-values containing only Characters. It seems like a 
	 * good idea.
	 */
	public static interface DoubleTreeIterator<G, F extends TaiTreeOTreesNode<F>> {
		public abstract boolean hasNext();

		public abstract G next();
	}

	/**********
	 * Now for some actual implementations relevant to the Dictionary Tree we want to make
	 **********/

	/**
	 * The node type used in AATreeForSiblings. 
	 * Character keys index a basic binary tree node
	 */
	public static class SiblingNode<E> implements TaiBSTNode<E> {
		public static SiblingNode createNullNode() {
			SiblingNode nullNode = new SiblingNode((char) 0, null);
			nullNode.left = nullNode.right = nullNode;
			nullNode.level = 0;
			return nullNode;
		}

		public SiblingNode(char key, E data) {
			this.key = key;
			level = 1;
			this.data = data;
		}

		private SiblingNode<E> left, right;
		public char key;
		private E data;
		public int level;

		public E getData() {
			return data;
		}

		public TaiBSTNode<E> getLeft() {
			return left;
		}

		public TaiBSTNode<E> getRight() {
			return right;
		}
	}

	/**
	 * A tree made to organize (sort) the siblings in the tree used in GameDictionary.
	 * 
	 * Rather than representing the siblings at each level of that tree as an array or a linkedlist,
	 * I chose to store them as a balanced AA tree (Trees of Trees! Oh boy!). 
	 * Reason against array: At the low levels of the tree, an array / arraylist for every node wastes memory
	 * Reason against linkedlist: balanced trees are always O(log(n)) for searching, linkedlist is O(n). 
	 * 
	 * This is a rather crude implementation of an AA tree. Specifically, removal is not implemented.
	 * 
	 * Balanced trees should guarantee log(N) lookup time, where N is the number of elements. Note that if you
	 * are using a finite alphabet, this N is bounded... but for my ego lets assume that's not the case ;)
	 */
	public static class AATreeForSiblings<E> implements TaiBST<E> {
		/**
		 * The head of the AA tree
		 */
		private SiblingNode<E> root;
		/**
		 * The skew / split operations rely on leaves being connected bilaterally to the nullnode
		 */
		private transient SiblingNode<E> nullNode;
		private int size = 0;

		public AATreeForSiblings(SiblingNode<E> nullNode) {
			this.nullNode = nullNode;
			root = nullNode;
		}

		/**
		 * Null values are NOT ALLOWED.
		 */
		public void insert(char key, E e) {
			if (e == null) {
				throw new NullPointerException();
			}
			try {
				root = insert(key, e, root);
				size++;
			} catch (DuplicateItemException f) {
				//tree is unchanged
			}
		}

		private static class DuplicateItemException extends RuntimeException {
			/**
			 * Duplicate Item Exception. Caught within tree classes, not exposed.
			 */
			private static final long serialVersionUID = 4200419223242727579L;
		};

		private SiblingNode<E> insert(char x, E data, SiblingNode<E> t) {
			if (t == nullNode) {
				t = new SiblingNode<E>(x, data);
				t.left = nullNode;
				t.right = nullNode;
			} else if (x < t.key) {
				t.left = insert(x, data, t.left);
			} else if (x > t.key) {
				t.right = insert(x, data, t.right);
			} else {
				throw new DuplicateItemException();
			}
			t = skew(t);
			t = split(t);
			return t;
		}

		/** Balancing operations: **/
		private SiblingNode<E> skew(SiblingNode<E> t) {
			if (t.left.level == t.level) {
				t = rotateWithLeftChild(t);
			}
			return t;
		}

		private SiblingNode<E> split(SiblingNode<E> t) {
			if (t.right.right.level == t.level) {
				t = rotateWithRightChild(t);
				t.level++;
			}
			return t;
		}

		private SiblingNode<E> rotateWithLeftChild(SiblingNode<E> k2) {
			SiblingNode<E> k1 = k2.left;
			k2.left = k1.right;
			k1.right = k2;
			return k1;
		}

		private SiblingNode<E> rotateWithRightChild(SiblingNode<E> k1) {
			SiblingNode<E> k2 = k1.right;
			k1.right = k2.left;
			k2.left = k1;
			return k2;
		}

		/**
		 * Returns an iterator that will iterate through the in-order traversal
		 * of this balanced tree.
		 */
		public BSTIterator<E> iterator() {
			return new BSTIterator<E>(root, nullNode);
		}

		/**
		 * Binary search. Returns null if no entry exists with the specified key.
		 * (This is one more reason why I can't allow null data values)
		 */
		public E get(char key) {
			SiblingNode<E> cur = root;
			while (cur != null && cur.data != null) {
				int c = key - cur.key;
				if (c == 0) {
					return cur.data;
				}
				if (c < 0) {
					cur = cur.left;
				} else {
					cur = cur.right;
				}
			}
			return null;
		}

		public SiblingNode<E> getNullNode() {
			return nullNode;
		}

		public int size() {
			return size;
		}
	}

	/**
	 * Minimalistic linked list framework.
	 */
	private static class LinkedLyst<E> {
		public LinkedLyst(E value) {
			this.value = value;
		}

		public E value;
		public LinkedLyst<E> next;
	}

	private static <E> LinkedLyst<E> LLpush(LinkedLyst<E> c, E newObj) {
		LinkedLyst<E> neu = new LinkedLyst<E>(newObj);
		neu.next = c;
		return neu;
	}

	private static <E> LinkedLyst<E> LLpop(LinkedLyst<E> c) {
		return c.next;
	}

	/**
	 * Traversal node-doubles. These are used within the traversal class, to include a "numPopped" field
	 * along with a clone of every node in the tree. 
	 */
	private static class BSTItrNodeDouble<T> {
		private TaiBSTNode<T> value;
		int numPopped = 0;

		public BSTItrNodeDouble(TaiBSTNode<T> lh) {
			value = lh;
		}
	}

	/**
	 * A simple non-recursive implementation of an in-order traversal on a binary tree.
	 */
	public static class BSTIterator<T> {
		private LinkedLyst<BSTItrNodeDouble<T>> current;
		/**
		 * For iterating over a tree where there is a null-node-value, we need this.
		 */
		private TaiBSTNode<T> nullValue;

		/*
		public BSTIterator(TaiBSTNode<T> lh){
			this(lh,null);
		}
		 */
		public BSTIterator(TaiBSTNode<T> lh, TaiBSTNode<T> nullValue) {
			this.nullValue = nullValue;
			current = new LinkedLyst<BSTItrNodeDouble<T>>(
					new BSTItrNodeDouble<T>(lh));
		}

		public boolean hasNext() {
			return current != null && current.value.value != nullValue;
		}

		/**
		 * Nonrecursive in-order transversal. See pg 620 of our assigned textbook.
		 */
		public T next() {
			if (current == null) {
				throw new RuntimeException();
			}
			while (true) { //Note: This is not a recursive method!
				BSTItrNodeDouble<T> cnode = current.value;
				current = LLpop(current);
				if (++cnode.numPopped == 2) { //We have visited this node twice now (not 3x!) so visit it.
					T toRet = cnode.value.getData();
					if (cnode.value.getRight() != nullValue) {
						current = LLpush(current, new BSTItrNodeDouble<T>(
								cnode.value.getRight()));
					}
					return toRet;
				}
				current = LLpush(current, cnode);
				if (cnode.value.getLeft() != nullValue) {
					current = LLpush(current, new BSTItrNodeDouble<T>(
							cnode.value.getLeft()));
				}
			}
		}
	}

	/**
	 * A DoubleTreeIterator that lets us iterate through the words in our dictionary data structure.
	 * 
	 * @author Benjamin
	 */
	public static class StringTreeIterator<F extends TaiTreeOTreesNode<F>>
			implements DoubleTreeIterator<String, F> {
		/**
		 * Simulated recursion by a stack
		 */
		private LinkedLyst<BSTIterator<F>> current;
		private int wordLen;
		private char[] curWord;
		private int maxWordLength;

		public StringTreeIterator(TaiTreeOTreesNode<F> lh) {
			maxWordLength = lh.height();
			if (maxWordLength != -1) {
				curWord = new char[maxWordLength];
				wordLen = 0;
				current = new LinkedLyst<BSTIterator<F>>(lh.getSubTree()
						.iterator());
				tryNext(); //Get first element.
			} else {
				moreAvailable = false;
			}
		}

		public StringTreeIterator(TaiTreeOTreesNode<F> lh,
				boolean generateStrings) {
		}

		public boolean hasNext() {
			return moreAvailable;
		}

		private String nextReturn;
		private TaiTreeOTreesNode<F> nextReturnNode;
		private boolean moreAvailable = true;

		public void tryNext() {
			while (current != null) {
				if (current.value.hasNext()) {
					TaiTreeOTreesNode<F> nextNode = current.value.next();
					TaiBST<F> subTree = nextNode.getSubTree();
					current = LLpush(current, subTree.iterator());
					curWord[wordLen++] = nextNode.getData();
					if (nextNode.isFlaggedNode()) {
						nextReturnNode = nextNode;
						nextReturn = new String(curWord, 0, wordLen);
						return;
					}
				} else {
					current = LLpop(current);
					wordLen--;
				}
			}
			moreAvailable = false;
		}

		public String next() {
			return nextReturn;
		}

		public TaiTreeOTreesNode<F> getCurrentNode() {
			return nextReturnNode;
		}
	}

	/**
	 * Some rudimentary tests on the AA tree coded above.
	 */
	public static void main(String[] args) {
		SiblingNode<String> nullNode = new SiblingNode<String>((char) 0, null);
		nullNode.left = nullNode.right = nullNode;
		nullNode.level = 0;
		AATreeForSiblings<String> bh = new AATreeForSiblings<String>(nullNode);
		String green = "green";
		String blue = "blue";
		String yellow = "yellow";
		bh.insert('A', green);
		bh.insert('A', green);
		bh.insert('A', green);
		bh.insert('A', yellow);
		bh.insert('F', blue);
		bh.insert('B', blue);
		bh.insert('D', green);
		bh.insert('C', yellow);
		bh.insert('E', yellow);
		BSTIterator<String> bi = bh.iterator();
		while (bi.hasNext()) {
			System.out.println(bi.next());
		}
	}
}
