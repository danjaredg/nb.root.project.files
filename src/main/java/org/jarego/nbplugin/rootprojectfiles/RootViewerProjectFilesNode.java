package org.jarego.nbplugin.rootprojectfiles;

import java.awt.Image;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
	private static final List<String> IMPORTANT_FILES = Arrays.asList(
			"pom\\.xml", "pom\\.properties", "nb.+\\.xml", // Archivos importantes de Proyecto Mave
			"build\\.xml", "build\\.properties", "manifest\\.mf" // java pr
	);
	
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
		
		protected int indexImportantFile(FileObject fileObject) {
			int i=0;
			for (String f : IMPORTANT_FILES) {
				if (fileObject.getNameExt().matches(f))
					return i;
				i++;
			}
			return -1;
		}
		
		@Override
		protected boolean createKeys(List<FileObject> toPopulate) {
			FileObject d = project.getProjectDirectory();
			
			// registra archivos de configuración de netbeans
			Map<Integer, FileObject> nbmapfiles = new TreeMap<>();
			int index;
			for (FileObject kid : d.getChildren()) {
				if (!kid.isFolder() && (index = indexImportantFile(kid)) >= 0 && !kid.getNameExt().startsWith("."))
					nbmapfiles.put(index, kid);
			}
			toPopulate.addAll(nbmapfiles.values());
			
			// registrando otros archivos
			ArrayList<FileObject> nbfiles = new ArrayList<>();
			for (FileObject kid : d.getChildren()) {
				if (!kid.isFolder() && indexImportantFile(kid) < 0 && !kid.getNameExt().startsWith("."))
					nbfiles.add(kid);
			}
			Collections.sort(nbfiles, fileObjectComparator);
			toPopulate.addAll(nbfiles);
			return true;
		}
	}
}
