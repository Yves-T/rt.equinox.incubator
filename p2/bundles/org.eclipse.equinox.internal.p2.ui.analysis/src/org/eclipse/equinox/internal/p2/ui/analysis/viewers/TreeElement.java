package org.eclipse.equinox.internal.p2.ui.analysis.viewers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import org.eclipse.equinox.p2.metadata.IArtifactKey;

public class TreeElement implements Comparable {
	private Collection children;
	private String text;

	public TreeElement() {
		this(new String());
	}

	public TreeElement(String text) {
		this.text = text;
		children = new TreeSet(new Comparator() {
			public int compare(Object arg0, Object arg1) {
				if (arg0 != null && arg1 != null)
					return arg0.toString().compareTo(arg1.toString());
				return 0;
			}
		});
	}

	public TreeElement(Comparator comparator) {
		children = new TreeSet(comparator);
	}

	public void clear() {
		children.clear();
	}

	public void addChild(Object obj) {
		children.add(obj);
	}

	public void addChildren(Object[] obj) {
		children.addAll(Arrays.asList(obj));
	}

	public void addChildren(Collection collection) {
		children.addAll(collection);
	}

	public Object[] getChildren() {
		return children.toArray();
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public boolean hasChildren() {
		return !children.isEmpty();
	}

	public static TreeElement getIArtifactKeyTreeElement() {
		TreeElement element = new TreeElement(new Comparator() {
			public int compare(Object arg0, Object arg1) {
				if (arg0 != null && arg1 != null && arg0 instanceof IArtifactKey && arg1 instanceof IArtifactKey)
					return ((IArtifactKey) arg0).getId().compareTo(((IArtifactKey) arg1).getId());
				return 0;
			}
		});
		return element;
	}

	public int compareTo(Object arg0) {
		if (arg0 != null && arg0 instanceof TreeElement)
			return text.compareTo(((TreeElement) arg0).text);
		return 0;
	}

	public String toString() {
		return text;
	}
}
