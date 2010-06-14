package org.eclipse.equinox.internal.p2.ui.analysis.viewers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;
import org.eclipse.equinox.p2.metadata.IArtifactKey;

public class TreeElement<F> implements Comparable<TreeElement<F>> {
	private Collection<F> children;
	private String text;

	public TreeElement() {
		this(new String());
	}

	public TreeElement(String text) {
		this.text = text;
		children = new TreeSet<F>(new Comparator<Object>() {
			public int compare(Object arg0, Object arg1) {
				if (arg0 != null && arg1 != null)
					return arg0.toString().compareTo(arg1.toString());
				return 0;
			}
		});
	}

	public TreeElement(Comparator<F> comparator) {
		children = new TreeSet<F>(comparator);
	}

	public void clear() {
		children.clear();
	}

	public void addChild(F obj) {
		children.add(obj);
	}

	public void addChildren(F[] obj) {
		children.addAll(Arrays.asList(obj));
	}

	public void addAll(Collection<F> collection) {
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

	public static TreeElement<IArtifactKey> getIArtifactKeyTreeElement() {
		return new TreeElement<IArtifactKey>(new Comparator<IArtifactKey>() {
			public int compare(IArtifactKey arg0, IArtifactKey arg1) {
				if (arg0 != null && arg1 != null)
					return arg0.getId().compareTo(arg1.getId());
				return 0;
			}
		});
	}

	public int compareTo(TreeElement<F> arg0) {
		if (arg0 != null)
			return text.compareTo(arg0.text);
		return 0;
	}

	public String toString() {
		return text;
	}
}
