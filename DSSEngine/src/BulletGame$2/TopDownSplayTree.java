package BulletGame$2;

/* RISO: an implementation of distributed belief networks.
 * This file (TopDownSplayTree.java) is a translation of splay tree
 * C code written by Danny Sleator, and it is redistributed as part
 * of the RISO project by permission of Danny Sleator, quoted below.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA, 02111-1307, USA,
 * or visit the GNU web site, www.gnu.org.
 */



/* From: "Danny Sleator" <sleator+@cs.cmu.edu>
 * To: "Robert Dodier" <robert_dodier@yahoo.com>
 * Subject: Re: Permission to redistribute splay tree code?
 * Date: Mon, 31 Dec 2001 00:50:32 -0500
 * 
 * You're welcome to distribute your project under any license you want.
 * My splay tree code is unrestricted.  Do whatever you want with it.
 * Of course I'd prefer that you at least leave my name in the comments
 * somewhere, and perhaps even mention that you use splay trees to the
 * person using the software.  But these are only suggestions.
 * 
 *   Daniel Sleator, PhD
 *   Carnegie Mellon University
 *   Phones: 412-268-7563, 412-422-5377, 412-654-9585
 *   Email: sleator@cmu.edu
 */

/** <pre>
 *               An implementation of top-down splaying
 *                   D. Sleator <sleator@cs.cmu.edu>
 *     http://www.cs.cmu.edu/afs/cs.cmu.edu/user/sleator/www/home.html
 *                            March 1992
 * 
 *        [Java translation of splay(), insert(), and delete() by Robert Dodier,
 *         July 1998; max() and min() added by Robert Dodier. Permission to
 *         redistribute under GPL given by Danny Sleator to Robert Dodier.
 *         Following comments are from the original C code.]
 * 
 *   "Splay trees", or "self-adjusting search trees" are a simple and
 *   efficient data structure for storing an ordered set.  The data
 *   structure consists of a binary tree, without parent pointers, and no
 *   additional fields.  It allows searching, insertion, deletion,
 *   deletemin, deletemax, splitting, joining, and many other operations,
 *   all with amortized logarithmic performance.  Since the trees adapt to
 *   the sequence of requests, their performance on real access patterns is
 *   typically even better.  Splay trees are described in a number of texts
 *   and papers [1,2,3,4,5].
 * 
 *   The code here is adapted from simple top-down splay, at the bottom of
 *   page 669 of [3].  It can be obtained via anonymous ftp from
 *   ftp://ftp.cs.cmu.edu/user/sleator/.
 * 
 *   The chief modification here is that the splay operation works even if the
 *   key being splayed is not in the tree, and even if the tree root of the
 *   tree is null.  So the line:
 * 
 *                         t = splay(a, t);
 * 
 *   causes it to search for node with key a in the tree rooted at t.  If it's
 *   there, it is splayed to the root.  If it isn't there, then the node put
 *   at the root is the last one before null that would have been reached in a
 *   normal binary search for a.  (It's a neighbor of a in the tree.)  This
 *   allows many other operations to be easily implemented, as shown below.
 * 
 *   [1] "Fundamentals of data structures in C", Horowitz, Sahni,
 *        and Anderson-Freed, Computer Science Press, pp 542-547.
 *   [2] "Data Structures and Their Algorithms", Lewis and Denenberg,
 *        Harper Collins, 1991, pp 243-251.
 *   [3] "Self-adjusting Binary Search Trees" Sleator and Tarjan,
 *        JACM Volume 32, No 3, July 1985, pp 652-686.
 *   [4] "Data Structure and Algorithm Analysis", Mark Weiss,
 *        Benjamin Cummins, 1992, pp 119-130.
 *   [5] "Data Structures, Algorithms, and Performance", Derick Wood,
 *        Addison-Wesley, 1993, pp 367-375.
 * </pre>
 */
public class TopDownSplayTree<E> implements java.io.Serializable
{
	/** Number of nodes in the tree; not needed for any of the operations.
	 */
	private int size;
	public int size(){
		return size;
	}

	/** Root of this splay tree.
	 */
	private TreeNode<E> root;

	public static class TreeNode<E> implements java.io.Serializable
	{
		private TreeNode<E> left, right;
		private float key;
		private E value;
		public float getKey(){
			return key;
		}
		public E getValue(){
			return value;
		}
	}

	public TreeNode<E> min(){
		return min(root);
	}
	/** Returns a reference to the node containing the minimum
	 * value of the subtree rooted on <tt>t</tt>.
	 * This is just the leftmost child of <tt>t</tt>.
	 */
	public static <F> TreeNode<F> min( TreeNode<F> t )
	{
		if ( t == null ) return null;

		while ( t.left != null )
			t = t.left;

		return t;
	}

	/** Returns a reference to the node containing the maximum
	 * value of the subtree rooted on <tt>t</tt>.
	 * This is just the rightmost child of <tt>t</tt>.
	 */
	public static <F> TreeNode<F> max( TreeNode<F> t )
	{
		if ( t == null ) return null;

		while ( t.right != null )
			t = t.right;

		return t;
	}

	/** Simple top down splay, not requiring <tt>a</tt> to be in the 
	 * subtree rooted on <tt>t</tt>.
	 * What it does is described in the header comment for the class.
	 */
	public static <E> TreeNode<E> splay( float a, TreeNode<E> t )
	{
		if (t == null) return null;

		TreeNode<E> N = new TreeNode(), l, r, y;

		N.left = N.right = null;
		l = r = N;

		for (;;)
		{
			if (a < t.key)
			{
				if (t.left == null) break;
				if (a < t.left.key)
				{
					y = t.left;						   /* rotate right */
					t.left = y.right;
					y.right = t;
					t = y;
					if (t.left == null) break;
				}
				r.left = t;							   /* link right */
				r = t;
				t = t.left;
			}
			else if (a > t.key)
			{
				if (t.right == null) break;
				if (a > t.right.key)
				{
					y = t.right;						  /* rotate left */
					t.right = y.left;
					y.left = t;
					t = y;
					if (t.right == null) break;
				}
				l.right = t;							  /* link left */
				l = t;
				t = t.right;
			}
			else
			{
				break;
			}
		}

		l.right = t.left;								/* assemble */
		r.left = t.right;
		t.left = N.right;
		t.right = N.left;

		return t;
	}

	public TreeNode<E> getLower(float value){
		root = splay(value,root);
		if (root.key > value){
			//Then it's the maximum of left
			if (root.left!=null){
				return max(root.left);
			}
		}
		return root;
	}

	/** Inserts <tt>new_key</tt> into this tree, unless it's already there.
	 */
	public void insert( float new_key, E value )
	{
		TreeNode<E> t = root;
		TreeNode<E> new_node = new TreeNode();

		// System.err.print( "insert: new_key: "+new_key+", value: "+value+" ... " );

		new_node.key = new_key;
		new_node.value = value;

		if (t == null)
		{
			new_node.left = new_node.right = null;
			size = 1;
			root = new_node;
			// System.err.println( "OK (now "+size+" nodes)." );
			return;
		}

		t = splay(new_key,t);
		if (new_key < t.key)
		{
			new_node.left = t.left;
			new_node.right = t;
			t.left = null;
			size ++;
			root = new_node;
			// System.err.println( "OK (now "+size+" nodes)." );
			return;
		}
		else if (new_key > t.key)
		{
			new_node.right = t.right;
			new_node.left = t;
			t.right = null;
			size++;
			root = new_node;
			// System.err.println( "OK (now "+size+" nodes)." );
			return;
		}
		else
		{
			/* We get here if it's already in the tree; don't add it again. */
			// System.err.println( "oops, already in tree." );
			root = t;
			return;
		}
	}

	/** Deletes <tt>some_key</tt> from this tree, if it's there.
	 */
	public void delete( float some_key )
	{
		TreeNode<E> t = root, x;

		if (t==null) return;

		System.err.print( "delete: some_key: "+some_key+"... " );

		t = splay(some_key,t);

		if (some_key == t.key)
		{
			/* found it */
			if (t.left == null)
			{
				x = t.right;
			}
			else
			{
				x = splay(some_key, t.left);
				x.right = t.right;
			}
			size--;
			root = x;
			System.err.println( "OK (now "+size+" nodes)." );
			return;
		}

		System.err.println( "oops, not in tree." );
		root = t;						 /* It wasn't there */
	}


	public static void main(String[] args){
		TopDownSplayTree<Integer> t = new TopDownSplayTree();
		/*
		t.insert(0, 0);
		t.insert(16, 0);
		t.insert(112, 0);
		t.insert(130, 0);
		 */		
		t.insert(0, 0);
		t.insert(16, 0);
		t.insert(112, 0);
		t.insert(130, 0);
		/*
		for(int k = 0; k < 4; k++){
		0.49400058 0.0
		15.494 16.0
		60.494 112.0
		77.369 130.0

		}
		 */
		for(float i = -1; i < 200; i+=1.14){
			System.out.println("Test i: "+i+" "+t.getLower(i).getKey()+": ");
			System.out.println("Test 0: "+0+" "+t.getLower(0).getKey()+": ");

			try {
				System.out.print(t.root.key);
			}catch(Throwable e){};
			try {
				System.out.print(" L"+(t.root.left!=null?t.root.left.key:""));
			}catch(Throwable e){};
			try {
				System.out.print(" R"+(t.root.right!=null?t.root.right.key:""));
			}catch(Throwable e){};
			System.out.println();

		}
	}
	/** A sample use of these functions.  Start with the empty tree,
	 * insert some stuff into it, and then delete it.
	 */
	public static void main2( String[] args )
	{
		int n = 1024;
		if ( args.length > 0 )
			n = Integer.parseInt( args[0] );

		TopDownSplayTree<String> t = new TopDownSplayTree();

		int i;

		for (i = 0; i < n; i++)
			t.insert( ((float)i)/1024, ""+i );

		System.out.println("t.size = "+t.size);

		for(int k = -1; k < 30; k++){
			TreeNode lower = t.getLower(k/1024.f);
			System.out.println(lower.key+" "+lower.value);
		}


		/*
		for (i = 0; i < n; i++)
			t.delete( ((float) ((541*i) & (n-1)))/1024 );
		 */

		System.out.println("t.size = "+t.size);
	}
}
