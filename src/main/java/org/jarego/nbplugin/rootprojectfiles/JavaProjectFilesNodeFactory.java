package org.jarego.nbplugin.rootprojectfiles;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.support.NodeFactory;
import org.netbeans.spi.project.ui.support.NodeFactorySupport;
import org.netbeans.spi.project.ui.support.NodeList;

@NodeFactory.Registration(projectType="org-netbeans-modules-java-j2seproject", position=10000)
public class JavaProjectFilesNodeFactory implements NodeFactory {
	@Override
	public NodeList<?> createNodes(Project project) {
		return NodeFactorySupport.fixedNodeList(
				new RootViewerProjectFilesNode(project));
	}
}
