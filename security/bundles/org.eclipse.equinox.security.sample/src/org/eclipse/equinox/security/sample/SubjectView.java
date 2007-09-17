/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.equinox.security.sample;

import java.security.AccessController;
import java.security.Principal;
import java.util.Set;
import javax.security.auth.Subject;
import org.eclipse.equinox.security.auth.SecurePlatform;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class SubjectView extends ViewPart {

	private TreeViewer viewer;
	private Subject subject;

	public SubjectView() {
		super();
	}

	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

		subject = SecurePlatform.isEnabled() ? Subject.getSubject(AccessController.getContext()) : null;

		/* OR: */
		//SecurePlatform.getSubject( );
		viewer.setContentProvider(new SubjectContentProvider());
		viewer.setLabelProvider(new SubjectLabelProvider());
		viewer.setInput(subject);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	private class SubjectLabelProvider extends LabelProvider {

		public String getText(Object object) {
			if (object == subject)
				return "User Subject (" + object.getClass().getName() + ")";
			if (object == subject.getPrincipals())
				return "Principals (" + Set.class.getName() + ")";
			if (object == subject.getPublicCredentials())
				return "Public Credentials (" + Set.class.getName() + ")";
			if (object == subject.getPrivateCredentials())
				return "Private Credentials (" + Set.class.getName() + ")";
			if (object instanceof Principal)
				return "Name: " + ((Principal) object).getName() + " (" + object.getClass().getName() + ")";
			return object.getClass().getName();
		}
	}

	private class SubjectContentProvider implements ITreeContentProvider {

		public Object[] getElements(Object inputElement) {
			if (inputElement instanceof Subject)
				return getChildren(inputElement);
			return new Object[] {};
		}

		public void dispose() {
			// nothing to do
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// nothing to do
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof Subject)
				return new Object[] {((Subject) parentElement).getPrincipals(), ((Subject) parentElement).getPublicCredentials(), ((Subject) parentElement).getPrivateCredentials()};
			else if (parentElement instanceof Set)
				return ((Set) parentElement).toArray();
			else
				return null;
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			if (element instanceof Subject)
				return true;
			else if (element instanceof Set)
				return !((Set) element).isEmpty();
			return false;
		}
	}
}