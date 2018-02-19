package org.jarego.nbplugin;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.Icon;
import javax.swing.UIManager;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;

public class RootViewerProjectFilesNode extends AbstractNode {
	public RootViewerProjectFilesNode(Project project) {
		super(Children.create(new ProjectFileChildren(project), true));
	}
	
	private Image getIcon(boolean opened) {
        Image badge = ImageUtilities.loadImage(
				"org/netbeans/modules/maven/projectfiles-badge.png", true);
        Image img = ImageUtilities.mergeImages(
				getTreeFolderIcon(opened), badge, 8, 8);
        return img;
    }

	@Override
	public boolean canCopy() {
		return false;
	}
	@Override
	public boolean canRename() {
		return false;
	}
	@Override
	public boolean canCut() {
		return false;
	}
	@Override
	public boolean canDestroy() {
		return false;
	}
	
	@Override
	public Image getOpenedIcon(int type) {
		return getIcon(true);
	}
	@Override
	public Image getIcon(int type) {
		return getIcon(false);
	}

	@Override
	public String getDisplayName() {
		return "Project Files";
	}
	
	private static final String ICON_KEY_UIMANAGER = "Tree.closedIcon";
    private static final String OPENED_ICON_KEY_UIMANAGER = "Tree.openIcon";
    private static final String ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.icon";
    private static final String OPENED_ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.openedIcon";
    private static final String ICON_PATH = "org/netbeans/modules/maven/defaultFolder.gif";
    private static final String OPENED_ICON_PATH = "org/netbeans/modules/maven/defaultFolderOpen.gif";
	
	private static Image getTreeFolderIcon(boolean opened) {
        Image base = (Image) UIManager.get(
				opened ? OPENED_ICON_KEY_UIMANAGER_NB : ICON_KEY_UIMANAGER_NB);
        if (base == null) {
            Icon baseIcon = UIManager.getIcon(
					opened ? OPENED_ICON_KEY_UIMANAGER : ICON_KEY_UIMANAGER);
            if (baseIcon != null) {
                base = ImageUtilities.icon2Image(baseIcon);
            } else {
                base = ImageUtilities.loadImage(
						opened ? OPENED_ICON_PATH : ICON_PATH, true);
            }
        }
        assert base != null;
        return base;
    }
	
	private static class ProjectFileChildren extends ChildFactory.Detachable<FileObject> {
		private final Project project;
		private final FileChangeAdapter fileChangeListener;
		private final Comparator<FileObject> fileObjectComparator;

		public ProjectFileChildren(Project project) {
			this.project = project;
			fileChangeListener = new FileChangeAdapter() {
				@Override
				public void fileDataCreated(FileEvent fe) {
					refresh(false);
				}
				@Override
				public void fileDeleted(FileEvent fe) {
					refresh(false);
				}
			};
			fileObjectComparator = new Comparator<FileObject>() {			
				@Override
				public int compare(FileObject o1, FileObject o2) {
					return o1.getNameExt().compareTo(o2.getNameExt());
				}
			};
		}

		@Override
		protected Node createNodeForKey(FileObject key) {
			try {
				return DataObject.find(key).getNodeDelegate().cloneNode();
			} catch (DataObjectNotFoundException e) {
				return null;
			}
		}

		@Override
		protected void addNotify() {
			project.getProjectDirectory().addFileChangeListener(fileChangeListener);
		}
		@Override
		protected void removeNotify() {
			project.getProjectDirectory().removeFileChangeListener(fileChangeListener);
		}
		
		@Override
		protected boolean createKeys(List<FileObject> toPopulate) {
			FileObject d = project.getProjectDirectory();
			// registra archivo pom.xml
			toPopulate.add(d.getFileObject("pom.xml"));
			
			// registra archivos de configuración de netbeans
			List<FileObject> nbfiles = new ArrayList<>();
			for (FileObject kid : d.getChildren()) {
				if (!kid.isFolder() && !kid.getNameExt().startsWith(".") && !"pom.xml".equals(kid.getNameExt())
						&& kid.getNameExt().startsWith("nb") && kid.getNameExt().endsWith(".xml"))
					nbfiles.add(kid);
			}
			Collections.sort(nbfiles, fileObjectComparator);
			toPopulate.addAll(nbfiles);
			
			// registrando otros archivos
			nbfiles.clear();
			for (FileObject kid : d.getChildren()) {
				if (!kid.isFolder() && !kid.getNameExt().startsWith(".") && !"pom.xml".equals(kid.getNameExt())
						&& !(kid.getNameExt().startsWith("nb") && kid.getNameExt().endsWith(".xml")))
					nbfiles.add(kid);
			}
			Collections.sort(nbfiles, fileObjectComparator);
			toPopulate.addAll(nbfiles);
			return true;
		}
	}
}
