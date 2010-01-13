package org.eclipse.equinox.p2.diagnostic;

import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.osgi.service.component.ComponentContext;

public class DiagnosticComponent {
	private RepositoryDumper repositoryDumper;

	  protected void activate(ComponentContext context) {
	    IMetadataRepositoryManager repoMgr =
	      (IMetadataRepositoryManager) context.locateService("repoMgr");
	    repositoryDumper = new RepositoryDumper();
	    repositoryDumper.setRepositoryManager(repoMgr);
	    
	    Activator.setComponentInstance(context.getComponentInstance());
	  }

	  protected void deactivate(ComponentContext context) {
		  repositoryDumper.setRepositoryManager(null);
	  }

	  public RepositoryDumper getDumper() {
		  return repositoryDumper;
	  }
}
