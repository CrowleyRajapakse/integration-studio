/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.integrationstudio.distribution.project.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.wso2.integrationstudio.capp.maven.utils.MavenConstants;
import org.wso2.integrationstudio.distribution.project.Activator;
import org.wso2.integrationstudio.distribution.project.model.DependencyData;
import org.wso2.integrationstudio.distribution.project.model.NodeData;
import org.wso2.integrationstudio.distribution.project.util.ArtifactTypeMapping;
import org.wso2.integrationstudio.distribution.project.util.DistProjectUtils;
import org.wso2.integrationstudio.distribution.project.validator.ProjectList;
import org.wso2.integrationstudio.logging.core.IIntegrationStudioLog;
import org.wso2.integrationstudio.logging.core.Logger;
import org.wso2.integrationstudio.maven.util.MavenUtils;
import org.wso2.integrationstudio.platform.core.model.AbstractListDataProvider.ListData;
import org.wso2.integrationstudio.platform.core.project.export.util.ExportUtil;
import org.wso2.integrationstudio.platform.core.utils.Constants;
import org.wso2.integrationstudio.platform.core.utils.IntegrationStudioProviderUtils;
import org.wso2.integrationstudio.platform.core.utils.SWTResourceManager;
import org.wso2.integrationstudio.platform.ui.preferences.CappPreferencesPage;
import org.wso2.integrationstudio.utils.file.FileUtils;
import org.eclipse.jface.dialogs.ErrorDialog;

public class DistProjectEditorPage extends FormPage implements IResourceDeltaVisitor, IResourceChangeListener {

	private static final String SERVER_ROLE_ATTR = "serverRole";

	private static final String REGISTERED_SERVER_ROLES =
	                                                      "org.wso2.integrationstudio.register.server.role";
	private static final String METADATA_TYPE = "synapse/metadata";
	private static final String API_TYPE = "synapse/api";
	private static final String PROXY_SERVICE_TYPE = "synapse/proxy-service";
	private static final String METADATA_SUFFIX = "_metadata";
	private static final String DIR_DOT_METADATA = ".metadata";
	private static final String SWAGGER_SUFFIX = "_swagger";
	private static final String PROXY_METADATA_SUFFIX = "_proxy";
	

	private static IIntegrationStudioLog log = Logger.getLog(Activator.PLUGIN_ID);
	
	private static final int TEXT_BOX_WIDTH = 350;

	private IFile pomFileRes;
	private File pomFile;
	private MavenProject parentPrj;

	private FormToolkit toolkit;
	private ScrolledForm form;
	private Composite body;
	private Text txtGroupId;
	private Text txtArtifactId;
	private Text txtVersion;
	private Text txtDescription;
	private Button btnPublishEnableChecker;
	private boolean pageDirty;

	IStatus editorStatus = new Status(IStatus.OK, Activator.PLUGIN_ID, "");

	private Map<String, Dependency> dependencyList = new LinkedHashMap<String, Dependency>();
	private Map<String, String> serverRoleList = new HashMap<String, String>();
	private SortedMap<String, DependencyData> projectList =
	                                                        Collections.synchronizedSortedMap(new TreeMap<String, DependencyData>(
	                                                                                                                              Collections.reverseOrder()));
	private Map<String, Dependency> missingDependencyList = new HashMap<String, Dependency>();
	private Map<String, String> projectListToDependencyMapping = new LinkedHashMap<String,String>(); 
	private Map<String, String> artifactIdToDependencyMapping = new LinkedHashMap<String,String>(); 

	private Tree trDependencies;
	private HashMap<String, TreeItem> nodesWithSubNodes = new HashMap<String, TreeItem>();
	private TreeEditor editor;

	private String projectName = "";
	private String groupId = "";
	private String artifactId = "";
	private String version = "";
	private String description = "";

	private Action exportAction;
	private Action refreshAction;
	private ArtifactTypeMapping artifactTypeMapping = new ArtifactTypeMapping();
	
	private boolean publishToServiceCatalog = true;

	public DistProjectEditorPage(String id, String title) {
		super(id, title);
	}

	public DistProjectEditorPage(FormEditor editor, String id, String title) {
		super(editor, id, title);
	}

	public void initContent() throws Exception {

		pomFileRes = ((IFileEditorInput) (getEditor().getEditorInput())).getFile();
		pomFile = pomFileRes.getLocation().toFile();

		ProjectList projectListProvider = new ProjectList();
		List<ListData> projectListData = projectListProvider.getListData(null, null);
		SortedMap<String, DependencyData> projectList =
		                                                Collections.synchronizedSortedMap(new TreeMap<String, DependencyData>());
		Map<String, Dependency> dependencyMap = new LinkedHashMap<String, Dependency>();
        try {
            for (ListData data : projectListData) {
                DependencyData dependencyData = (DependencyData) data.getData();
                projectList.put(data.getCaption(), dependencyData);
                IProject mainProject = (IProject) ((DependencyData) data.getData()).getParent();
                if (mainProject != null) {
                    IFolder metaFolder = mainProject.getFolder("src/main/resources/metadata");
                    if (metaFolder.exists()) {
                        String caption = data.getCaption();
                        String keyword = caption.substring(StringUtils.indexOf(caption, ":=") + 2, caption.length());
                        projectListToDependencyMapping.put(keyword, caption);
                        artifactIdToDependencyMapping.put(((DependencyData) data.getData()).getDependency().getArtifactId(),
                                keyword);
                    }
                }
            }
        } catch(Exception e) {
            log.error("Error while loading metadata of the api resources in the Composite Exporter POM file");
        }
		

		parentPrj = MavenUtils.getMavenProject(pomFile);

		/*
		 * if propeties of pom in old format(DevS 3.2 or earlier) reformat it to
		 * new format.
		 */
		updatePomToNewFormat();

		for (Dependency dependency : (List<Dependency>) parentPrj.getDependencies()) {
			dependencyMap.put(DistProjectUtils.getArtifactInfoAsString(dependency), dependency);
			serverRoleList.put(DistProjectUtils.getArtifactInfoAsString(dependency),
			                   DistProjectUtils.getServerRole(parentPrj, dependency));
		}
		setProjectName(parentPrj.getName());
		setArtifactId(parentPrj.getArtifactId());
		setGroupId(parentPrj.getGroupId());
		setVersion(parentPrj.getVersion());
		setDescription(parentPrj.getDescription());
		setProjectList(projectList);
		setDependencyList(dependencyMap);
		setMissingDependencyList((Map<String, Dependency>) ((HashMap) getDependencyList()).clone());
		setPublishToServiceCatalog(isMetaDataDependenciesIncluded());
	}

	public void savePOM() throws Exception {
		if (editorStatus != null) {
			if (!editorStatus.isOK()) {
				ErrorDialog.openError(getSite().getShell(), "Warning",
				                      "The following warning(s) have been detected", editorStatus);
				editorStatus = new Status(IStatus.OK, Activator.PLUGIN_ID, ""); // clear
				                                                                // error
			}
		}
		parentPrj.setGroupId(getGroupId());
		parentPrj.setVersion(getVersion());
		parentPrj.setDescription(getDescription());
		writeProperties();
		parentPrj.setDependencies(new ArrayList<Dependency>(getDependencyList().values()));
		MavenUtils.saveMavenProject(parentPrj, pomFile);
		setPageDirty(false);
		updateDirtyState();
		pomFileRes.getProject().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());

	}

	private void writeProperties() {
		Properties properties = parentPrj.getModel().getProperties();
		identifyNonProjectProperties(properties);
		List<String> arifactinfoList = new ArrayList<String>();
		for (Dependency dependency : getDependencyList().values()) {
			String artifactInfo = DistProjectUtils.getArtifactInfoAsString(dependency);
			arifactinfoList.add(artifactInfo);
			if (serverRoleList.containsKey(artifactInfo)) {
				properties.put(artifactInfo, serverRoleList.get(artifactInfo));
			} else {
				properties.put(artifactInfo, "capp/ApplicationServer");
			}
		}
		properties.put("artifact.types", artifactTypeMapping.getArtifactTypes());
		parentPrj.getModel().setProperties(properties);
	}

	private Properties identifyNonProjectProperties(Properties properties) {
		Map<String, DependencyData> dependencies = getProjectList();
		for (Iterator iterator = dependencies.values().iterator(); iterator.hasNext();) {
			DependencyData dependency = (DependencyData) iterator.next();
			String artifactInfoAsString =
			                              DistProjectUtils.getArtifactInfoAsString(dependency.getDependency());
			if (properties.containsKey(artifactInfoAsString)) {
				properties.remove(artifactInfoAsString);
			}
		}
		// Removing the artifact.type
		properties.remove("artifact.types");
		return properties;
	}

	protected void createFormContent(IManagedForm managedForm) {
		managedForm.getForm()
		           .setImage(SWTResourceManager.getImage(this.getClass(),
		                                                 "/icons/composite-exporter.png"));
		toolkit = managedForm.getToolkit();
		form = managedForm.getForm();
		form.setText(getProjectName());
		form.getToolBarManager().add(getRefreshAction());
		form.getToolBarManager().add(getExportAction());
		body = form.getBody();
		toolkit.decorateFormHeading(form.getForm());
		toolkit.paintBordersFor(body);
		managedForm.getForm().getBody().setLayout(new GridLayout(2, false));

		managedForm.getToolkit().createLabel(managedForm.getForm().getBody(), "Group Id", SWT.NONE);

		txtGroupId =
		             managedForm.getToolkit().createText(managedForm.getForm().getBody(), "",
		                                                 SWT.NONE);
		txtGroupId.setText(getGroupId());
		GridData gd_txtGroupId = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_txtGroupId.widthHint = TEXT_BOX_WIDTH;
		txtGroupId.setLayoutData(gd_txtGroupId);
		txtGroupId.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent evt) {
				setPageDirty(true);
				setGroupId(txtGroupId.getText().trim());
				updateDirtyState();
			}
		});

		managedForm.getToolkit().createLabel(managedForm.getForm().getBody(), "Artifact Id",
		                                     SWT.NONE);

		txtArtifactId =
		                managedForm.getToolkit().createText(managedForm.getForm().getBody(), "",
		                                                    SWT.NONE | SWT.READ_ONLY);
		txtArtifactId.setText(getArtifactId());
		GridData gd_txtArtifactId = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_txtArtifactId.widthHint = TEXT_BOX_WIDTH;
		txtArtifactId.setLayoutData(gd_txtArtifactId);

		managedForm.getToolkit().createLabel(managedForm.getForm().getBody(), "Version", SWT.NONE);

		txtVersion =
		             managedForm.getToolkit().createText(managedForm.getForm().getBody(), "",
		                                                 SWT.NONE);
		txtVersion.setText(getVersion());
		GridData gd_txtVersion = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		gd_txtVersion.widthHint = TEXT_BOX_WIDTH;
		txtVersion.setLayoutData(gd_txtVersion);
		txtVersion.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent evt) {
				setPageDirty(true);
				setVersion(txtVersion.getText().trim());
				updateDirtyState();
			}
		});

		managedForm.getToolkit().createLabel(managedForm.getForm().getBody(),
		                                               "Description", SWT.NONE);

		txtDescription =
		                 managedForm.getToolkit().createText(managedForm.getForm().getBody(), "",
		                                                     SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		txtDescription.setText(getDescription());
		GridData gd_txtDescription = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtDescription.heightHint = 60;
		txtDescription.setLayoutData(gd_txtDescription);
		txtDescription.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent evt) {
				setPageDirty(true);
				setDescription(txtDescription.getText().trim());
				updateDirtyState();
			}
		});
		
		managedForm.getToolkit().createLabel(managedForm.getForm().getBody(), "Publish to Service Catalog", SWT.NONE);
		btnPublishEnableChecker = managedForm.getToolkit().createButton(managedForm.getForm().getBody(), "", SWT.CHECK);
		btnPublishEnableChecker.setSelection(isPublishToServiceCatalog());
        GridData gdBtnPublishEnableChecker = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
        btnPublishEnableChecker.setLayoutData(gdBtnPublishEnableChecker);
        btnPublishEnableChecker.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                setPageDirty(true);
                if (btnPublishEnableChecker.getSelection()) {
                	addMetadataDependencies();
                	setPublishToServiceCatalog(true);
                } else {
                    removeMetadataDependencies();
                    setPublishToServiceCatalog(false);
                }
                updateDirtyState();
            }
        });

		Section sctnDependencies =
		                           managedForm.getToolkit()
		                                      .createSection(managedForm.getForm().getBody(),
		                                                     Section.TWISTIE | Section.TITLE_BAR);
		GridData gd_sctnNewSection = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_sctnNewSection.heightHint = 250;
		gd_sctnNewSection.widthHint = 411;
		gd_sctnNewSection.verticalSpan = 55;
		sctnDependencies.setLayoutData(gd_sctnNewSection);
		managedForm.getToolkit().paintBordersFor(sctnDependencies);
		sctnDependencies.setText("Dependencies");
		sctnDependencies.setExpanded(true);

		Composite composite = managedForm.getToolkit().createComposite(sctnDependencies, SWT.NONE);
		managedForm.getToolkit().paintBordersFor(composite);
		sctnDependencies.setClient(composite);
		composite.setLayout(new GridLayout(3, false));

		trDependencies = managedForm.getToolkit().createTree(composite, SWT.CHECK);
		trDependencies.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
		trDependencies.setHeaderVisible(true);
		managedForm.getToolkit().paintBordersFor(trDependencies);

		TreeColumn trclmnArtifact = new TreeColumn(trDependencies, SWT.NONE);
		trclmnArtifact.setWidth(400);
		trclmnArtifact.setText("Artifact");

		TreeColumn trclmnServerRole = new TreeColumn(trDependencies, SWT.NONE);
		trclmnServerRole.setWidth(200);
		trclmnServerRole.setText("Server Role");

		TreeColumn trclmnVersion = new TreeColumn(trDependencies, SWT.NONE);
		trclmnVersion.setWidth(80);
		trclmnVersion.setText("Version");

		editor = new TreeEditor(trDependencies);
		editor.horizontalAlignment = SWT.LEFT;
		editor.grabHorizontal = true;

		trDependencies.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent evt) {
				final TreeItem item = (TreeItem) evt.item;
				if (item != null) {
					if (evt.detail == SWT.CHECK) {
						handleTreeItemChecked(item);
					} else {
						handleTreeItemEdit(item);
					}
				}
			}

			public void widgetDefaultSelected(SelectionEvent evt) {

			}
		});

		createTreeContent();

		GridData gdBtn = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gdBtn.widthHint = 100;
		Button btnSelectAll =
		                      managedForm.getToolkit().createButton(composite, "Select All",
		                                                            SWT.NONE);
		btnSelectAll.setLayoutData(gdBtn);
		btnSelectAll.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event evt) {
				TreeItem[] items = trDependencies.getItems();
				for (TreeItem item : items) {
					if (!item.getChecked() || item.getGrayed()) {
						item.setChecked(true);
						handleTreeItemChecked(item);
					}
				}
			}
		});

		Button btnDeselectAll =
		                        managedForm.getToolkit().createButton(composite, "Deselect All",
		                                                              SWT.NONE);
		btnDeselectAll.setLayoutData(gdBtn);

		btnDeselectAll.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event evt) {
				TreeItem[] items = trDependencies.getItems();
				for (TreeItem item : items) {
					item.setChecked(false);
					handleTreeItemChecked(item);
				}
			}
		});
		Label lblEmpty = managedForm.getToolkit().createLabel(composite, "");
		GridData gdEmpty = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
		lblEmpty.setLayoutData(gdEmpty);

		form.updateToolBar();
		form.reflow(true);
	}

	/**
	 * handle tree item check event
	 * 
	 * @param item
	 *            Selected tree item
	 */
	private void handleTreeItemChecked(TreeItem item) {
		boolean select = item.getChecked();
		NodeData nodeData = (NodeData) item.getData();
		if (nodeData.hasChildren()) {
			TreeItem[] subItems = item.getItems();
			if (select) {
				boolean conflict = false;
				for (TreeItem subitem : subItems) {
					if (!subitem.getChecked()) {
						NodeData subNodeData = (NodeData) subitem.getData();
						if (!isNameConflict(subNodeData)) {
							subitem.setChecked(true);
							addDependency(subNodeData);
							setPageDirty(true);
						} else {
							conflict = true;
						}
					}
				}
				if (conflict) {
					MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Add dependencies",
					                          "Cannot add multiple dependencies with same identity");
				}
			} else {
				for (TreeItem subitem : subItems) {
					if (subitem.getChecked()) {
						subitem.setChecked(false);
						NodeData subNodeData = (NodeData) subitem.getData();
						removeDependency(subNodeData);
						setPageDirty(true);
					}
				}
			}
			updateCheckState(item);
		} else {
			TreeItem parentItem = item.getParentItem();
			if (select) {
				if (!isNameConflict(nodeData)) {
					addDependency(nodeData);
					setPageDirty(true);
				} else {
					item.setChecked(false);
					MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Add dependencies",
					                          "Cannot add multiple dependencies with same identity");
				}
			} else {
				removeDependency(nodeData);
				setPageDirty(true);
			}
			updateCheckState(parentItem);
		}
		updateDirtyState();
	}

	/**
	 * Check for conflicts
	 * 
	 * @param nodeData
	 * @return
	 */
	private boolean isNameConflict(NodeData nodeData) {
		Dependency dependency = nodeData.getDependency();
		for (Dependency entry : getDependencyList().values()) {
			if (entry.getArtifactId().equalsIgnoreCase(dependency.getArtifactId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * handle tree item edit events
	 * 
	 * @param item
	 *            Selected tree item
	 */
	private void handleTreeItemEdit(final TreeItem item) {
		final NodeData nodeData = (NodeData) item.getData();
		Control lastCtrl = editor.getEditor();
		if (lastCtrl != null) {
			lastCtrl.dispose();
		}

		if (nodeData.hasChildren()) {
			return;
		}

		final String artifactInfo =
		                            DistProjectUtils.getArtifactInfoAsString(nodeData.getDependency());
		final Combo cmbServerRole = new Combo(trDependencies, SWT.SINGLE);
		// retrieving all server roles dynamically, from preference store and
		// extension point
		IEclipsePreferences prefs =
		                            InstanceScope.INSTANCE.getNode(CappPreferencesPage.PREFERENCES_PLUGIN_ID);
		List<String> serverRolesList =
		                               Arrays.asList(prefs.get(CappPreferencesPage.CUSTOM_SERVER_ROLES,
		                                                       "").split(","));
		IntegrationStudioProviderUtils devStudioUtils = new IntegrationStudioProviderUtils();
		IConfigurationElement[] elements =
		                                   devStudioUtils.getExtensionPointmembers(REGISTERED_SERVER_ROLES);
		List<String> defaultServerRoles = new ArrayList<String>();
		for (IConfigurationElement serverRoleElem : elements) {
			String serverRole = serverRoleElem.getAttribute(SERVER_ROLE_ATTR);
			if (!defaultServerRoles.contains(serverRole)) {
				defaultServerRoles.add(serverRole);
			}
		}
		String[] defaultServerRolesArray =
		                                   (String[]) defaultServerRoles.toArray((new String[defaultServerRoles.size()]));
		String[] customServerRolesArray =
		                                  (String[]) serverRolesList.toArray(new String[serverRolesList.size()]);
		final String[] finalizedServerArray =
		                                      combine(customServerRolesArray,
		                                              defaultServerRolesArray);
		cmbServerRole.setItems(finalizedServerArray);

		cmbServerRole.setText(item.getText(1));
		cmbServerRole.setFocus();
		editor.setEditor(cmbServerRole, item, 1);
		trDependencies.redraw();
		trDependencies.layout();

		cmbServerRole.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				String role = cmbServerRole.getText();
				item.setText(1, role);
				nodeData.setServerRole(role);
				if (getDependencyList().containsKey(artifactInfo)) {
					serverRoleList.put(artifactInfo, "capp/" + role);
					setPageDirty(true);
				}
				updateDirtyState();
			}
		});
	}

	/**
	 * Remove a project dependencies from the maven model
	 * 
	 * @param nodeData
	 *            NodeData of selected treeitem
	 */
    private void removeDependency(NodeData nodeData) {
        List<String> removingArtifacts = new ArrayList<>();
        Dependency project = nodeData.getDependency();
        String artifactInfo = DistProjectUtils.getArtifactInfoAsString(project);
        if (projectListToDependencyMapping.containsKey(artifactInfo)) {
            DependencyData dependencyData = projectList.get(projectListToDependencyMapping.get(artifactInfo)); 
            if(API_TYPE.equals(dependencyData.getCApptype())) { 
                String medataName = project.getArtifactId() + METADATA_SUFFIX; 
                String swaggerName = project.getArtifactId() + SWAGGER_SUFFIX; 
                removingArtifacts.add(artifactIdToDependencyMapping.get(medataName)); 
                removingArtifacts.add(artifactIdToDependencyMapping.get(swaggerName)); 
            } else if (PROXY_SERVICE_TYPE.equals(dependencyData.getCApptype())) {
                String medataName = project.getArtifactId() + PROXY_METADATA_SUFFIX + METADATA_SUFFIX;
                removingArtifacts.add(artifactIdToDependencyMapping.get(medataName));
            }
        }
        
        removingArtifacts.add(artifactInfo); 
        removeDependency(removingArtifacts); 
    }

    private void removeDependency(List<String> artifacts) {
        for (String artifactInfo : artifacts) {
            if (getDependencyList().containsKey(artifactInfo)) {
                getDependencyList().remove(artifactInfo);
                if (serverRoleList.containsKey(artifactInfo)) {
                    serverRoleList.remove(artifactInfo);
                }
            }
        }
    }
    
	/**
	 * Remove metadata dependencies from the maven model
	 */
    private void removeMetadataDependencies() {
    	List<String> removingArtifacts = new ArrayList<>();
		List<Dependency> listdep = parentPrj.getDependencies();
		for (Dependency dep : listdep) {
			String groupId = dep.getGroupId();
			String artifactId = dep.getArtifactId();
			if (groupId != null && groupId.endsWith(DIR_DOT_METADATA) && 
					artifactId != null && artifactId.endsWith(METADATA_SUFFIX)) {
				removingArtifacts.add(artifactIdToDependencyMapping.get(dep.getArtifactId()));
			}
		}
		removeDependency(removingArtifacts); 
	}

    /**
	 * Add metadata dependencies to the maven model
	 */
    private void addMetadataDependencies() {

    	List<Dependency> listdep = parentPrj.getDependencies();
		for (Dependency dep : listdep) {
			String groupId = dep.getGroupId();
			if (!(groupId != null && groupId.endsWith(DIR_DOT_METADATA))) {
				String metadataName;
				if (dep.getGroupId().endsWith(".proxy-service")) {
					metadataName = dep.getArtifactId() + PROXY_METADATA_SUFFIX + METADATA_SUFFIX;
				} else {
					metadataName = dep.getArtifactId() + METADATA_SUFFIX;
				}
				String metadataArtifactInfo = artifactIdToDependencyMapping.get(metadataName);
				if (projectListToDependencyMapping.containsKey(metadataArtifactInfo)) {
					Dependency metaDependency = projectList
							.get(projectListToDependencyMapping.get(metadataArtifactInfo)).getDependency();
					if (metaDependency != null && !getDependencyList().containsKey(metadataArtifactInfo)) {
						serverRoleList.put(metadataArtifactInfo, "capp/EnterpriseServiceBus");
						getDependencyList().put(metadataArtifactInfo, metaDependency);
					}
				}
			}
		}
	}

	/**
	 * Check if metadata dependencies are included in the maven model
	 */
	private boolean isMetaDataDependenciesIncluded() {
    	List<Dependency> listdep = parentPrj.getDependencies();
    	if (listdep.size() == 0) {
    		return true;
    	}
		for (Dependency dep : listdep) {
			String groupId = dep.getGroupId();
			String artifactId = dep.getArtifactId();
			if (groupId != null && groupId.endsWith(DIR_DOT_METADATA) && 
					artifactId != null && artifactId.endsWith(METADATA_SUFFIX)) {
				return true;
			}
		}
		return false;
	}
    
	/**
	 * Add a project dependencies to the maven model
	 * 
	 * @param nodeData
	 *            NodeData of selected treeitem
	 */
    private void addDependency(NodeData nodeData) {
        Dependency project = nodeData.getDependency();
        String serverRole = nodeData.getServerRole();
        String artifactInfo = DistProjectUtils.getArtifactInfoAsString(project);

        if (projectListToDependencyMapping.containsKey(artifactInfo)) {
            String dependencyMapping = projectListToDependencyMapping.get(artifactInfo);
            if (projectList.containsKey(dependencyMapping)) {
                DependencyData dependencyData = projectList.get(dependencyMapping);
                if (API_TYPE.equals(dependencyData.getCApptype())) {
                    String medataName = project.getArtifactId() + METADATA_SUFFIX;
                    String swaggerName = project.getArtifactId() + SWAGGER_SUFFIX;
                    String metadataArtifactInfo = artifactIdToDependencyMapping.get(medataName);
                    String swaggerArtifactInfo = artifactIdToDependencyMapping.get(swaggerName);
                    if (projectListToDependencyMapping.containsKey(metadataArtifactInfo)) {
                        Dependency metaDependency = projectList
                                .get(projectListToDependencyMapping.get(metadataArtifactInfo)).getDependency();
                        if (metaDependency != null && !getDependencyList().containsKey(metadataArtifactInfo) 
                        		&& publishToServiceCatalog) {
                            serverRoleList.put(metadataArtifactInfo, "capp/" + serverRole);
                            getDependencyList().put(metadataArtifactInfo, metaDependency);
                        }
                    }
                    if (projectListToDependencyMapping.containsKey(swaggerArtifactInfo)) {
                        Dependency swaggerDependency = projectList
                                .get(projectListToDependencyMapping.get(swaggerArtifactInfo)).getDependency();
                        if (swaggerDependency != null && !getDependencyList().containsKey(swaggerArtifactInfo)) {
                            serverRoleList.put(swaggerArtifactInfo, "capp/" + serverRole);
                            getDependencyList().put(swaggerArtifactInfo, swaggerDependency);
                        }
                    }
                } else if (PROXY_SERVICE_TYPE.equals(dependencyData.getCApptype())) {
                    String medataName = project.getArtifactId() + PROXY_METADATA_SUFFIX + METADATA_SUFFIX;
                    String metadataArtifactInfo = artifactIdToDependencyMapping.get(medataName);
                    if (projectListToDependencyMapping.containsKey(metadataArtifactInfo)) {
                        Dependency metaDependency = projectList
                                .get(projectListToDependencyMapping.get(metadataArtifactInfo)).getDependency();
                        if (metaDependency != null && !getDependencyList().containsKey(metadataArtifactInfo)
                        		&& publishToServiceCatalog) {
                            serverRoleList.put(metadataArtifactInfo, "capp/" + serverRole);
                            getDependencyList().put(metadataArtifactInfo, metaDependency);
                        }
                    }
                }
            }
        }

        if (!getDependencyList().containsKey(artifactInfo)) {

            Dependency dependency = new Dependency();
            dependency.setArtifactId(project.getArtifactId());
            dependency.setGroupId(project.getGroupId());
            dependency.setVersion(project.getVersion());
            dependency.setType(project.getType());

            serverRoleList.put(artifactInfo, "capp/" + serverRole);

            getDependencyList().put(artifactInfo, dependency);
        }
    }

	/**
	 * Update state of treeItem
	 * 
	 * @param item
	 *            Selected tree item
	 */
	private void updateCheckState(TreeItem item) {
		if (item != null) {
			TreeItem[] subItems = item.getItems();
			int i = 0;
			for (TreeItem subItem : subItems) {
				if (subItem.getChecked())
					i++;
			}
			if (i == item.getItemCount()) {
				item.setGrayed(false);
				item.setChecked(true);
			} else if (i < item.getItemCount() && i > 0) {
				item.setChecked(true);
				item.setGrayed(true);

			} else {
				item.setGrayed(false);
				item.setChecked(false);
			}
		}
	}

	/**
	 * Create content of tree control
	 */
	protected void createTreeContent() {
		trDependencies.removeAll();
		nodesWithSubNodes.clear();

		for (String project : getProjectList().keySet()) {
			DependencyData dependencyData = getProjectList().get(project);
			if (METADATA_TYPE.equals(dependencyData.getCApptype())) {
			    continue;
			}
			Object parent = dependencyData.getParent();
			Object self = dependencyData.getSelf();
			if ((parent == null) && (self != null)) {
				if (self instanceof IProject) {
					createNode(trDependencies, dependencyData.getDependency(), true);
				}
			} else if (parent != null) {
				if (parent instanceof IProject) {
					IProject prj = (IProject) parent;
					TreeItem parentNode = null;
					if (nodesWithSubNodes.containsKey(prj.getName())) {
						parentNode = nodesWithSubNodes.get(prj.getName());
					} else {
						parentNode = createNode(trDependencies, prj);
						nodesWithSubNodes.put(prj.getName(), parentNode);
					}
					if (parentNode != null) {
						createNode(parentNode, dependencyData.getDependency(), true);
					}
					updateCheckState(parentNode);
				}
			}
		}

		/**
		 * Disable listing corrupted artifacts for the composite project pom file.
		 * 
		if (getMissingDependencyList().size() > 0) {
			editorStatus =
			               new MultiStatus(
			                               Activator.PLUGIN_ID,
			                               IStatus.WARNING,
			                               "One or more dependencies can't be resolved, for more information click 'details' below",
			                               null);
			for (String dependency : getMissingDependencyList().keySet()) {
				createNode(trDependencies, getMissingDependencyList().get(dependency), false);
				((MultiStatus) editorStatus).add(new Status(IStatus.WARNING, Activator.PLUGIN_ID,
				                                            dependency + " is unresolvable"));
			}
			setPageDirty(true);
			updateDirtyState();
		}
		**/
		trDependencies.layout();
	}

	/**
	 * Create a subItem for a project
	 * 
	 * @param parent
	 *            Parent treeItem
	 * @param project
	 *            Project dependency
	 * @param available
	 *            available of dependency in workspace
	 * @return new TreeItem
	 */
	TreeItem createNode(TreeItem parent, final Dependency project, boolean available) {
		TreeItem item = new TreeItem(parent, SWT.NONE);
		String artifactInfo = DistProjectUtils.getArtifactInfoAsString(project);
		String serverRole =
		                    DistProjectUtils.getDefaultServerRole(getProjectList(), artifactInfo)
		                                    .replaceAll("^capp/", "");
		String version = project.getVersion();

		item.setText(0, DistProjectUtils.getMavenInfoAsString(artifactInfo));

		item.setText(2, version);
		NodeData nodeData = new NodeData(project);
		nodeData.setServerRole(serverRole);
		item.setData(nodeData);

		if (getDependencyList().containsKey(artifactInfo)) {
			item.setChecked(true);
			String role =
			              DistProjectUtils.getServerRole(parentPrj,
			                                             getDependencyList().get(artifactInfo))
			                              .replaceAll("^capp/", "");
			item.setText(1, role);
		} else {
			item.setText(1, serverRole);
		}

		if (getMissingDependencyList().containsKey(artifactInfo)) {
			getMissingDependencyList().remove(artifactInfo);
		}
		item.setImage(0, SWTResourceManager.getImage(this.getClass(), "/icons/artifact.png"));

		return item;
	}

	/**
	 * Create a Item for a dependency
	 * 
	 * @param parent
	 *            Parent tree control
	 * @param project
	 *            Project dependency
	 * @param available
	 *            available of dependency in workspace
	 * @return new TreeItem
	 */
	TreeItem createNode(Tree parent, final Dependency project, boolean available) {
		TreeItem item = new TreeItem(parent, SWT.NONE);
		final String artifactInfo = DistProjectUtils.getArtifactInfoAsString(project);
		final String serverRole =
		                          DistProjectUtils.getDefaultServerRole(getProjectList(),
		                                                                artifactInfo)
		                                          .replaceAll("^capp/", "");
		final String version = project.getVersion();

		item.setText(0, DistProjectUtils.getMavenInfoAsString(artifactInfo));

		item.setText(2, version);

		NodeData nodeData = new NodeData(project);
		nodeData.setServerRole(serverRole);

		item.setData(nodeData);

		if (available) {
			if (getDependencyList().containsKey(artifactInfo)) {
				item.setChecked(true);
				String role =
				              DistProjectUtils.getServerRole(parentPrj,
				                                             getDependencyList().get(artifactInfo))
				                              .replaceAll("^capp/", "");
				item.setText(1, role);
			} else {
				item.setText(1, serverRole);
			}
			if (getMissingDependencyList().containsKey(artifactInfo)) {
				getMissingDependencyList().remove(artifactInfo);
			}

		} else {
			if (getDependencyList().containsKey(artifactInfo)) {
				getDependencyList().remove(artifactInfo);
			}
		}
		if (available) {
			item.setImage(0, SWTResourceManager.getImage(this.getClass(), "/icons/artifact.png"));
		} else {
			item.setImage(0, SWTResourceManager.getImage(this.getClass(), "/icons/cancel_16.png"));
		}
		return item;

	}

	/**
	 * Create a tree Item for a project
	 * 
	 * @param parent
	 *            Parent tree control
	 * @param project
	 *            eclipse project
	 * @return new TreeItem
	 */
	TreeItem createNode(Tree parent, final IProject project) {
		TreeItem item = new TreeItem(parent, SWT.NONE);
		MavenProject mavenProject;
		try {
			mavenProject = DistProjectUtils.getMavenProject(project);
			item.setText(0, project.getName());
			item.setText(1, "--");
			item.setText(2, mavenProject.getModel().getVersion());
			NodeData nodeData = new NodeData(project);
			item.setData(project);

			nodeData.setHaschildren(true);
			item.setData(nodeData);

			if (project.hasNature(Constants.GENERAL_PROJECT_NATURE)) {
				item.setImage(0, SWTResourceManager.getImage(this.getClass(), "/icons/general-project-12.png"));
			} else if (project.hasNature(Constants.CONNECTOR_PROJECT_NATURE)) {
				item.setImage(0, SWTResourceManager.getImage(this.getClass(), "/icons/new-mediator-16x16.png"));
			} else if (project.hasNature(Constants.DATASOURCE_PROJECT_NATURE)) {
				item.setImage(0, SWTResourceManager.getImage(this.getClass(), "/icons/dsource-16x16.png"));
			} else if (project.hasNature(Constants.DS_PROJECT_NATURE)) {
				item.setImage(0, SWTResourceManager.getImage(this.getClass(), "/icons/ds-16x16.png"));
			} else {
				item.setImage(0, SWTResourceManager.getImage(this.getClass(), "/icons/esb-config-folder.png"));
			}
		} catch (Exception e) {
			log.error("createNode fail", e);
			return null;
		}
		return item;

	}

	public Action getExportAction() {
		if (exportAction == null) {
			exportAction =
			               new Action(
			                       "Create Archive",
			                       ImageDescriptor.createFromImage(SWTResourceManager.getImage(this.getClass(),
			                                                                                   "/icons/exportCapp.png"))) {
				               public void run() {
					               exportCar();
				               };
			               };
			exportAction.setToolTipText("Create Archive");
		}
		return exportAction;
	}

	public void exportCar() {
		MessageBox exportMsg =
		                       new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
		                                                .getShell(), SWT.ICON_INFORMATION);
		exportMsg.setText("WSO2 Platform Distribution");
		String finalFileName =
		                       String.format("%s_%s.car", parentPrj.getModel().getArtifactId(),
		                                     parentPrj.getModel().getVersion());
		if (dependencyList.size() == 0) {
			exportMsg.setMessage("Cannot export an empty carbon archive. please tick/check atleast one artifact from the dependencies");
			exportMsg.open();
			return;
		}
		try {
			if (isDirty()) {
				savePOM();
			}
			IResource CarbonArchive = ExportUtil.buildCAppProject(pomFileRes.getProject());
			DirectoryDialog dirDlg =
			                         new DirectoryDialog(PlatformUI.getWorkbench()
			                                                       .getActiveWorkbenchWindow()
			                                                       .getShell());
			String recentExportLocation = getRecentExportLocation(pomFileRes.getProject());
			if (recentExportLocation != null) {
				dirDlg.setFilterPath(recentExportLocation);
			}
			String dirName = dirDlg.open();
			if (dirName != null) {
				setRecentExportLocation(pomFileRes.getProject(), dirName);
				File destFileName = new File(dirName, finalFileName);
				if (destFileName.exists()) {
					org.apache.commons.io.FileUtils.deleteQuietly(destFileName);
				}
				FileUtils.copy(CarbonArchive.getLocation().toFile(), destFileName);
			}
		} catch (Exception e) {
			log.error("An error occurred while creating the carbon archive file", e);
			exportMsg.setMessage("An error occurred while creating the carbon archive file. For more details view the log");
			exportMsg.open();
		}
	}

	public Action getRefreshAction() {
		if (refreshAction == null) {
			refreshAction =
			                new Action(
			                        "Refresh",
			                        ImageDescriptor.createFromImage(SWTResourceManager.getImage(this.getClass(),
			                                                                                    "/icons/refresh.png"))) {
				                public void run() {
					                try {
						                refreshForm();
						                if (getMissingDependencyList().size() == 0) {
							                setPageDirty(false);
							                updateDirtyState();
						                }
					                } catch (Exception e) {
						                log.error("An unexpected error has occurred", e);
					                }

				                };
			                };
			refreshAction.setToolTipText("Refresh");
		}
		return refreshAction;
	}

	/* If the GUI elements are accessed from a thread other than the thread that has created the element 
	 * ERROR_THREAD_INVALID_ACCESS exception will be thrown. To avoid that a new thread is created and 
	 * UISynchronize's asyncExec() is called
	 */
    public void refreshForm() throws Exception {
        initContent();
        new Thread(new Runnable() {
            public void run() {
                Display.getDefault().asyncExec(new Runnable() {
                    public void run() {
                        if (trDependencies.isDisposed()) {
                            return;
                        }
                        createTreeContent();
                        txtVersion.setText(getVersion());
                        txtArtifactId.setText(getArtifactId());
                        txtDescription.setText(getDescription());
                        txtGroupId.setText(getGroupId());
                        btnPublishEnableChecker.setSelection(isPublishToServiceCatalog());
                    }
                });
            }
        }).start();
    }

	public void setDependencyList(Map<String, Dependency> dependencyList) {
		this.dependencyList = dependencyList;
	}

	public Map<String, Dependency> getDependencyList() {
		return dependencyList;
	}

	public void setProjectList(SortedMap<String, DependencyData> projectList) {
		this.projectList = projectList;
	}

	public SortedMap<String, DependencyData> getProjectList() {
		return projectList;
	}

	public void setMissingDependencyList(Map<String, Dependency> missingDependencyList) {
		this.missingDependencyList = missingDependencyList;
	}

	public Map<String, Dependency> getMissingDependencyList() {
		return missingDependencyList;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public boolean isPublishToServiceCatalog() {
        return publishToServiceCatalog;
    }
	
	public void setPublishToServiceCatalog(boolean publishToServiceCatalog) {
        this.publishToServiceCatalog = publishToServiceCatalog;
    }

	public boolean isPageDirty() {
		return pageDirty;
	}

	public void setPageDirty(boolean isPageDirty) {
		this.pageDirty = isPageDirty;
	}

	public void updateDirtyState() {
		((DistProjectEditor) getEditor()).updateDirtyState();;
	}

	public boolean isDirty() {
		return isPageDirty();
	}

	private String getRecentExportLocation(IProject project) {
		try {
			return (String) project.getSessionProperty(new QualifiedName("devStudio.export",
			                                                             "export-path"));
		} catch (CoreException e) {
			log.error("cannot read session propery 'export-path'", e);
			return null;
		}
	}

	private void setRecentExportLocation(IProject project, String location) {
		try {
			project.setSessionProperty(new QualifiedName("devStudio.export", "export-path"),
			                           location);
		} catch (CoreException e) {
			log.error("cannot save session propery 'export-path'", e);
		}
	}

	/*
	 * Check whether format of properties in old fomat. If in old format
	 * transformProperties().
	 */
	private void updatePomToNewFormat() throws Exception {
		List<Dependency> dependency = parentPrj.getDependencies();
		Properties properties = parentPrj.getModel().getProperties();

		for (Dependency dep : dependency) {
			String artifactInfoOld = DistProjectUtils.getArtifactInfoAsStringOld(dep);
			if (properties.containsKey(artifactInfoOld)) {
				transformProperties(properties);
				break;
			}
		}
	}

	private void transformProperties(Properties properties) {
		Map<String, String> oldServerRoleList = new HashMap<String, String>();
		for (Dependency dependency : (List<Dependency>) parentPrj.getDependencies()) {
			oldServerRoleList.put(DistProjectUtils.getArtifactInfoAsString(dependency),
			                      DistProjectUtils.getServerRole(parentPrj, dependency));
		}

		properties.clear();
		Map<String, String> map = oldServerRoleList;
		for (String key : oldServerRoleList.keySet()) {
			properties.setProperty(key, oldServerRoleList.get(key));
		}

		// change maven-car-plugin version to 2.0.5 and car-deploy version 1.0.4
		List<Plugin> pluginList = parentPrj.getBuildPlugins();
		for (Plugin plugin : pluginList) {
			if (plugin.getArtifactId().equalsIgnoreCase("maven-car-plugin")) {
				plugin.setVersion(MavenConstants.MAVEN_CAR_VERSION);
			} else if (plugin.getArtifactId().equalsIgnoreCase("maven-car-deploy-plugin")) {
				plugin.setVersion(MavenConstants.MAVEN_CAR_DEPLOY_VERSION);
			}
		}
		setPageDirty(true);
		updateDirtyState();
	}

	public static String[] combine(String[] a, String[] b) {
		int length = a.length + b.length;
		String[] result = new String[length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}

    @Override
    public boolean visit(IResourceDelta delta) throws CoreException {

        switch (delta.getKind()) {
        case IResourceDelta.REMOVED:
            if (pomFileRes.getLocation() != null) {
        	 try {
                     refreshForm();
                 } catch (Exception e) {
                     log.error("refresh opration failed", e);
                 }
            }            
            break;
        }
        return true;
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event) {

        if (event.getType() != IResourceChangeEvent.POST_CHANGE)
            return;
        try {
            event.getDelta().accept(this);
        } catch (CoreException e) {
            log.error("event is failed to capture", e);
        }
    }

}
