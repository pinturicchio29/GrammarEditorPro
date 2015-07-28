/* Copyright (c) 2003, Carl Burch. License information is located in the
 * edu.csbsju.socs.grammar.Main source code and at
 * www.cburch.com/proj/grammar/. */

package edu.csbsju.socs.grammar;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

class Parser {
	private static class FinalTree extends Tree {
		public FinalTree(Object data) { super(data); }
		public FinalTree(Object data, Tree[] children) {
			super(data, children);
		}
	}
	private static class SymbolTree {
		Grammar.Element sym;
		Tree tree;

		SymbolTree(Grammar.Element sym, Tree tree) {
			this.sym = sym;
			this.tree = tree;
		}
		public int hashCode() { return sym.hashCode(); }
		public boolean equals(Object other) {
			return sym.equals(((SymbolTree) other).sym);
		}
		public String toString() { return sym.toString(); }
	}
	private static class Single {
		Grammar.Symbol lhs;
		Grammar.Atom a;

		Single(Grammar.Symbol lhs, Grammar.Atom a) {
			this.lhs = lhs;
			this.a = a;
		}
	}
	private static class Dual {
		SymbolTree lhs;
		Grammar.Symbol a;
		Grammar.Symbol b;

		Dual(SymbolTree lhs, Grammar.Symbol a, Grammar.Symbol b) {
			this.lhs = lhs;
			this.a = a;
			this.b = b;
		}
	}

	private Grammar base;
	private HashMap singles = new HashMap();
	private HashMap duals = new HashMap();
	private HashMap final_orig_rules = new HashMap();
	private Tree null_parse = null;

	public Parser(Grammar g) {
		base = g;
		HashMap orig_rules = new HashMap();
		g = computeNoEpsilons(g, orig_rules);
		g = computeNoUselessSymbols(g, orig_rules);
		g = computeNoUnit(g, orig_rules);
		this.final_orig_rules = orig_rules;
		computeChomsky(g);
	}

	private void printRuleMap(HashMap orig_rules) {
		Iterator it = orig_rules.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry e = (Map.Entry) it.next();
			Tree t = (Tree) e.getValue();
			System.err.print(e.getKey() + " : " + t.getData()
				+ " /");
			Tree[] children = t.getChildren();
			for(int i = 0; i < children.length; i++) {
				System.err.print(" " + children[i].getData());
			}
			System.err.println();
		}
	}

	private Grammar computeNoUselessSymbols(Grammar g,
			HashMap orig_rules) {
		// determine which symbols terminate in some string
		// (cf. page 89, Hopcroft and Ullman)
		HashSet terminating = new HashSet();
		boolean changed = true;
		while(changed) {
			changed = false;
			Iterator it = g.getRules().iterator();
			while(it.hasNext()) {
				Grammar.Rule rule = (Grammar.Rule) it.next();
				Grammar.Symbol lhs = rule.getLeftSide();
				if(terminating.contains(lhs)) continue;
				if(allSymsInSet(rule.getRightSide(), terminating)) {
					terminating.add(lhs);
					changed = true;
				}
			}
		}

		// compute grammar (rules in base with all symbols terminating)
		Grammar ret = new Grammar();
		ret.setRoot(g.getRoot());
		Iterator it = g.getRules().iterator();
		while(it.hasNext()) {
			Grammar.Rule rule = (Grammar.Rule) it.next();
			if(allSymsInSet(rule.getRightSide(), terminating)) {
				ret.add(rule);
			} else {
				orig_rules.remove(rule);
			}
		}
		return ret;
	}
	private boolean allSymsInSet(Grammar.Element[] rhs, Set query) {
		for(int i = 0; i < rhs.length; i++) {
			if(rhs[i] instanceof Grammar.Symbol
					&& !query.contains(rhs[i])) {
				return false;
			}
		}
		return true;
	}

	// orig rules should be empty HashMap
	private Grammar computeNoEpsilons(Grammar g, HashMap orig_rules) {
		// determine nullability
		HashMap nullable = new HashMap();
		boolean changed = true;
		while(changed) {
			changed = false;
			Iterator it = g.getRules().iterator();
			while(it.hasNext()) {
				Grammar.Rule rule = (Grammar.Rule) it.next();
				Grammar.Symbol lhs = rule.getLeftSide();
				if(nullable.get(lhs) != null) continue;
				Grammar.Element[] rhs = rule.getRightSide();
				boolean can_null = true;
				for(int i = 0; can_null && i < rhs.length; i++) {
					can_null = rhs[i] instanceof Grammar.Symbol
						&& nullable.get(rhs[i]) != null;
				}
				if(can_null) {
					Tree tree = treeFor(rule, nullable);
					nullable.put(lhs,
						new FinalTree(tree.getData(), tree.getChildren()));
					changed = true;
				}
			}
		}

		// determine tree for null sentence
		null_parse = (Tree) nullable.get(g.getRoot());

		// compute return grammar
		Grammar ret = new Grammar();
		ret.setRoot(g.getRoot());
		Iterator it = g.getRules().iterator();
		while(it.hasNext()) {
			Grammar.Rule rule = (Grammar.Rule) it.next();
			Grammar.Symbol lhs = rule.getLeftSide();
			Grammar.Element[] rhs = rule.getRightSide();
			Tree[] known = new Tree[rhs.length];
			addNonNullRules(new AddNonNullData(ret, lhs, rhs, known,
				nullable, orig_rules), 0);
		}
		return ret;
	}
	private Tree treeFor(Grammar.Rule rule, HashMap tree_map) {
		Grammar.Element[] rhs = rule.getRightSide();
		Tree[] children = new Tree[rhs.length];
		for(int i = 0; i < children.length; i++) {
			if(rhs[i] instanceof Grammar.Symbol && tree_map != null) {
				children[i] = (Tree) tree_map.get(rhs[i]);
			} else {
				children[i] = Tree.createLeaf(rhs[i]);
			}
		}
		return Tree.createNode(rule.getLeftSide(), children);
	}
	
	private static class AddNonNullData {
		Grammar dest;
		Grammar.Symbol lhs;
		Grammar.Element[] rhs;
		int pos;
		Tree[] known;
		HashMap nullable;
		HashMap orig_rules;
		AddNonNullData(Grammar dest, Grammar.Symbol lhs,
				Grammar.Element[] rhs, Tree[] known,
				HashMap nullable, HashMap orig_rules) {
			this.dest = dest;
			this.lhs = lhs;
			this.rhs = rhs;
			this.pos = pos;
			this.known = known;
			this.nullable = nullable;
			this.orig_rules = orig_rules;
		}
	}
	private void addNonNullRules(AddNonNullData data, int pos) {
		if(pos == data.rhs.length) {
			Tree[] children = new Tree[data.rhs.length];
			int len = 0;
			for(int i = 0; i < data.rhs.length; i++) {
				if(data.known[i] == null) {
					children[i] = Tree.createLeaf(data.rhs[i]);
					++len;
				} else {
					children[i] = data.known[i];
				}
			}
			if(len != 0) {
				Grammar.Element[] new_rhs = new Grammar.Element[len];
				int j = 0;
				for(int i = 0; i < data.rhs.length; i++) {
					if(data.known[i] == null) new_rhs[j++] = data.rhs[i];
				}
				Grammar.Rule r = new Grammar.Rule(data.lhs, new_rhs);
				data.dest.add(r);
				data.orig_rules.put(r, Tree.createNode(data.lhs, children));
			}
		} else {
			Tree null_tree = (Tree) data.nullable.get(data.rhs[pos]);
			if(null_tree != null) {
				data.known[pos] = null_tree;
				addNonNullRules(data, pos + 1);
				data.known[pos] = null;
				addNonNullRules(data, pos + 1);
			} else {
				int j = pos + 1;
				while(j < data.rhs.length
						&& data.nullable.get(data.rhs[j]) == null) {
					++j;
				}
				addNonNullRules(data, j);
			}
		}
	}

	// g must have no useless symbols or epsilon-productions
	private Grammar computeNoUnit(Grammar g, HashMap rule_orig) {
		// divide rules into unit productions and others
		HashMap goesto = new HashMap();
		Grammar ret = new Grammar();
		ret.setRoot(g.getRoot());
		Iterator it = g.getRules().iterator();
		while(it.hasNext()) {
			Grammar.Rule rule = (Grammar.Rule) it.next();
			Grammar.Element[] rhs = rule.getRightSide();
			if(rhs.length == 1 && rhs[0] instanceof Grammar.Symbol) {
				Grammar.Symbol lhs = rule.getLeftSide();
				Grammar.Symbol a = (Grammar.Symbol) rhs[0];
				HashSet dest = (HashSet) goesto.get(lhs);
				if(dest == null) {
					dest = new HashSet();
					goesto.put(lhs, dest);
				}
				Tree t = (Tree) rule_orig.get(rule);
				if(t == null) t = treeFor(rule, null);
				else rule_orig.remove(rule);
				dest.add(new SymbolTree(a, t));
			} else {
				ret.add(rule);
			}
		}

		// compute closure of unit productions
		boolean changed = true;
		while(changed) {
			changed = false;
			it = goesto.entrySet().iterator();
			while(it.hasNext()) {
				Map.Entry entry = (Map.Entry) it.next();
				Grammar.Symbol a = (Grammar.Symbol) entry.getKey();
				Collection bs = (Collection) entry.getValue();
				Collection bs_toadd = new java.util.LinkedList();
				Iterator it2 = bs.iterator();
				while(it2.hasNext()) {
					SymbolTree b = (SymbolTree) it2.next();
					Collection cs = (Collection) goesto.get(b.sym);
					if(cs != null) {
						Iterator it3 = cs.iterator();
						while(it3.hasNext()) {
							SymbolTree c = (SymbolTree) it3.next();
							if(!bs.contains(c)) {
								bs_toadd.add(new SymbolTree(c.sym,
									treeJoin(b.tree, c.tree)));
								changed = true;
							}
						}
					}
				}
				bs.addAll(bs_toadd);
			}
		}

		// add non-unit productions within closure
		it = goesto.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			Grammar.Symbol a = (Grammar.Symbol) entry.getKey();
			Collection bs = (Collection) entry.getValue();
			Iterator it2 = bs.iterator();
			while(it2.hasNext()) {
				SymbolTree b = (SymbolTree) it2.next();
				Iterator it3 = g.getRules((Grammar.Symbol) b.sym).iterator();
				while(it3.hasNext()) {
					Grammar.Rule rule = (Grammar.Rule) it3.next();
					Grammar.Element[] rhs = rule.getRightSide();
					if(rhs.length != 1
							|| !(rhs[0] instanceof Grammar.Symbol)) {
						Grammar.Rule r = new Grammar.Rule(a, rhs);
						ret.add(r);

						Tree t = (Tree) rule_orig.get(rule);
						if(t == null) t = treeFor(rule, null);
						rule_orig.put(r, treeJoin(b.tree, t));
					}
				}
			}
		}

		return ret;
	}
	private Tree treeJoin(Tree a, Tree b) {
		if(a == null) return null;
		if(a.getData() == b.getData()) return b;
		Tree[] children = a.getChildren();
		for(int i = 0; i < children.length; i++) {
			Tree lower = treeJoin(children[i], b);
			if(lower != null) {
				Tree[] new_children = new Tree[children.length];
				System.arraycopy(children, 0, new_children, 0,
					children.length);
				new_children[i] = lower;
				return Tree.createNode(a.getData(), new_children);
			}
		}
		return null;
	}

	// g must have no useless symbols, epsilon-productions, or unit
	// productions
	private void computeChomsky(Grammar g) {
		HashMap smap = new HashMap();

		Iterator it = g.getRules().iterator();
		while(it.hasNext()) {
			Grammar.Rule rule = (Grammar.Rule) it.next();
			Grammar.Symbol lhs = rule.getLeftSide();
			Grammar.Element[] rhs = rule.getRightSide();
			if(rhs.length == 1) {
				Tree map = (Tree) final_orig_rules.get(rule);
				if(map == null) map = treeFor(rule, null);
				Tree t = Tree.createNode(new SymbolTree(lhs, map), new Tree[] {
					Tree.createLeaf(new SymbolTree(rhs[0], null))
				});
				getSingles(rhs[0]).put(lhs, t);
			} else {
				Tree t = (Tree) final_orig_rules.get(rule);
				for(int i = rhs.length - 1; i >= 2; i--) {
					Grammar.Symbol a = new Grammar.Symbol();
					addDual(lhs, a, symFor(rhs[i], smap), t);
					t = null;
					lhs = a;
				}
				Grammar.Symbol a = symFor(rhs[0], smap);
				addDual(lhs, a, symFor(rhs[1], smap), t);
			}
		}
	}
	private HashMap getSingles(Grammar.Element e) {
		HashMap ret = (HashMap) singles.get(e);
		if(ret == null) {
			ret = new HashMap();
			singles.put(e, ret);
		}
		return ret;
	}
	private Grammar.Symbol symFor(Grammar.Element e, HashMap smap) {
		if(e instanceof Grammar.Symbol) return (Grammar.Symbol) e;
		Grammar.Symbol ret = (Grammar.Symbol) smap.get(e);
		if(ret == null) {
			ret = new Grammar.Symbol();
			getSingles(e).put(ret,
				Tree.createNode(new SymbolTree(ret, Tree.createLeaf(e)), new Tree[] {
					Tree.createLeaf(new SymbolTree(e, null))
				}));
			smap.put(e, ret);
		}
		return ret;
	}
	private void addDual(Grammar.Symbol lhs, Grammar.Symbol a,
			Grammar.Symbol b, Tree t) {
		HashSet a_map = (HashSet) duals.get(a);
		if(a_map == null) {
			a_map = new HashSet();
			duals.put(a, a_map);
		}
		a_map.add(new Dual(new SymbolTree(lhs, t), a, b));
	}

	private void printChomsky() {
		Iterator it = singles.entrySet().iterator();
		while(it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			Iterator it2 = ((HashMap) entry.getValue()).keySet().iterator();
			while(it2.hasNext()) {
				System.err.println(it2.next() + " -> "
					+ entry.getKey());
			}
		}
		it = duals.values().iterator();
		while(it.hasNext()) {
			HashSet entry = (HashSet) it.next();
			Iterator it2 = entry.iterator();
			while(it2.hasNext()) {
				Dual d = (Dual) it2.next();
				System.err.println(d.lhs + " -> " + d.a + " " + d.b);
			}
		}
	}

	public Tree parse(Grammar.Atom[] x) {
		if(x == null || x.length == 0) return null_parse;

		// CYK algorithm: V[i][j] represents all symbols that can
		// derive x[i..i+j] (inclusive of x[i+j]).
		int n = x.length;
		Map[][] V = new Map[n][n];
		for(int i = 0; i < x.length; i++) {
			V[i][0] = (Map) singles.get(x[i]);
		}
		for(int j = 1; j < n; j++) {
			for(int i = 0; i + j < n; i++) {
				V[i][j] = new HashMap();
				for(int k = 0; k < j; k++) { // [i..i+k] and [i+k+1..i+j]
					if(V[i][k] == null) continue;
					Collection entries = V[i][k].entrySet();
					Iterator it = entries.iterator();
					while(it.hasNext()) {
						Map.Entry ae = (Map.Entry) it.next();
						Grammar.Symbol a = (Grammar.Symbol) ae.getKey();
						HashSet a_duals = (HashSet) duals.get(a);
						if(a_duals == null) continue;
						Iterator it2 = a_duals.iterator();
						while(it2.hasNext()) {
							Dual dual = (Dual) it2.next();
							Object bt = V[i + k + 1][j - k - 1].get(dual.b);
							if(bt != null) {
								Tree all_t = Tree.createNode(dual.lhs,
									new Tree[] { (Tree) ae.getValue(),
										(Tree) bt });
								V[i][j].put(dual.lhs.sym, all_t);
							}
						}
					}
				}
			}
		}

		// now translate tree back base grammar
		Tree chtree = (Tree) V[0][n - 1].get(base.getRoot());
		if(chtree == null) return null;
		ArrayList subtrees = translateTree(chtree);
		if(subtrees.size() != 1) {
			throw new RuntimeException("unexpected bonus subtrees");
		}
		return (Tree) subtrees.get(0);
	}
	private void printTree(Tree chtree, int depth) {
		SymbolTree root = (SymbolTree) chtree.getData();
		Tree[] children = chtree.getChildren();
		for(int i = 0; i < depth; i++) System.err.print(" ");
		System.err.print(root.sym);
		if(root.tree != null) {
			System.err.print(" (" + root.tree.getData()
				+ " " + root.tree.getChildren().length + ")");
		}
		System.err.println();
		for(int i = 0; i < children.length; i++) {
			printTree(children[i], depth + 1);
		}
	}
	private ArrayList translateTree(Tree n) {
		SymbolTree root = (SymbolTree) n.getData();
		Tree[] children = n.getChildren();
		ArrayList ret = new ArrayList();
		if(root.tree instanceof FinalTree) {
			ret.add(root.tree);
		} else {
			for(int i = 0; i < children.length; i++) {
				ret.addAll(translateTree(children[i]));
			}
			if(root.tree != null) {
				Tree t = joinTree(root.tree, ret);
				ret.add(0, t);
			}
		}
		return ret;
	}
	private Tree joinTree(Tree base, ArrayList leaves) {
		Tree[] children = base.getChildren();
		if(base instanceof FinalTree) {
			return base;
		} else if(children.length == 0) {
			if(leaves.size() > 0) {
				Tree leaf = (Tree) leaves.get(0);
				if(base.getData() == leaf.getData()) {
					leaves.remove(0); // consume leaf
					return leaf;
				}
			}
			return base;
		} else {
			Tree[] newch = new Tree[children.length];
			for(int i = 0; i < children.length; i++) {
				newch[i] = joinTree(children[i], leaves);
			}
			return Tree.createNode(base.getData(), newch);
		}
	}

}
