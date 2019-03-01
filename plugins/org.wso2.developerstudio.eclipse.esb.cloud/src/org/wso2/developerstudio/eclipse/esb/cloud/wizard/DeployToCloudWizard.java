/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.

 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at

 *      http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.developerstudio.eclipse.esb.cloud.wizard;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.wso2.developerstudio.eclipse.distribution.project.model.DependencyData;
import org.wso2.developerstudio.eclipse.distribution.project.ui.wizard.DistributionProjectExportWizardPage;
import org.wso2.developerstudio.eclipse.distribution.project.util.DistProjectUtils;
import org.wso2.developerstudio.eclipse.distribution.project.validator.ProjectList;
import org.wso2.developerstudio.eclipse.esb.cloud.Activator;
import org.wso2.developerstudio.eclipse.esb.cloud.job.CloudDeploymentJob;
import org.wso2.developerstudio.eclipse.esb.cloud.resources.CloudDeploymentWizardConstants;
import org.wso2.developerstudio.eclipse.esb.cloud.util.UserSessionManager;
import org.wso2.developerstudio.eclipse.logging.core.IDeveloperStudioLog;
import org.wso2.developerstudio.eclipse.logging.core.Logger;
import org.wso2.developerstudio.eclipse.maven.util.MavenUtils;
import org.wso2.developerstudio.eclipse.platform.core.model.AbstractListDataProvider.ListData;
import org.wso2.developerstudio.eclipse.platform.core.project.export.util.ExportUtil;
import org.wso2.developerstudio.eclipse.platform.core.utils.Constants;

/**
 * Wizard for docker image generation.
 *
 */
public class DeployToCloudWizard extends Wizard implements IExportWizard {

    // Wizard Pages
    private DistributionProjectExportWizardPage mainPage;
    private LoginWizardPage loginPage;
    private AppDetailsWizardPage appDetailsPage;

    // Cloud deployment job
    CloudDeploymentJob cloudDeploymentJob;

    private IFile pomFileRes;
    private File pomFile;
    private IProject selectedProject;
    private MavenProject parentPrj;
    private boolean initError = false;

    private Map<String, DependencyData> projectList = new HashMap<String, DependencyData>();
    private Map<String, Dependency> dependencyMap = new HashMap<String, Dependency>();
    private Map<String, String> serverRoleList = new HashMap<String, String>();
    
    private Display display;

    private static IDeveloperStudioLog log = Logger.getLog(Activator.PLUGIN_ID);

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {

        try {
            display = Display.getCurrent();
            loginPage = new LoginWizardPage();
            appDetailsPage = new AppDetailsWizardPage();
            selectedProject = getSelectedProject(selection);
            pomFileRes = selectedProject.getFile("pom.xml");
            pomFile = pomFileRes.getLocation().toFile();

            if (!selectedProject.hasNature(Constants.DISTRIBUTION_PROJECT_NATURE)) {
                throw new Exception();
            }

            ProjectList projectListProvider = new ProjectList();
            List<ListData> projectListData = projectListProvider.getListData(null, null);

            for (ListData data : projectListData) {
                DependencyData dependencyData = (DependencyData) data.getData();
                projectList.put(data.getCaption(), dependencyData);
            }

            parentPrj = MavenUtils.getMavenProject(pomFile);

            for (Dependency dependency : (List<Dependency>) parentPrj.getDependencies()) {
                dependencyMap.put(DistProjectUtils.getArtifactInfoAsString(dependency), dependency);
                serverRoleList.put(DistProjectUtils.getArtifactInfoAsString(dependency),
                        DistProjectUtils.getServerRole(parentPrj, dependency));
            }

            mainPage = new DistributionProjectExportWizardPage(parentPrj);
            mainPage.setProjectList(projectList);
            mainPage.setDependencyList(dependencyMap);
            mainPage.setMissingDependencyList(
                    (Map<String, Dependency>) ((HashMap<String, Dependency>) mainPage.getDependencyList()).clone());
            mainPage.setServerRoleList(serverRoleList);

            appDetailsPage.setName(parentPrj.getModel().getArtifactId());
            appDetailsPage.setVersion(parentPrj.getModel().getVersion());

        } catch (Exception e) {
            e.printStackTrace();
            initError = true;
            
            Shell shell = display.getActiveShell();
            openMessageBox(shell, CloudDeploymentWizardConstants.DIALOG_TITLE_TEXT,
                    CloudDeploymentWizardConstants.ErrorMessages.SELECT_VALID_CARBON_APP_MESSAGE, SWT.ICON_INFORMATION);
        }
    }

    @Override
    public boolean canFinish() {
        return appDetailsPage.equals(getContainer().getCurrentPage());
    }

    @Override
    public boolean performFinish() {

        String finalFileName = String.format(CloudDeploymentWizardConstants.CAR_FILE_NAME_PLACEHOLDER,
                appDetailsPage.getName().replaceAll(CloudDeploymentWizardConstants.CAR_FILE_SUFFIX,
                        CloudDeploymentWizardConstants.EMPTY_STRING),
                appDetailsPage.getVersion());

        IResource carbonArchive;
        try {
            carbonArchive = ExportUtil.buildCAppProject(selectedProject);

            // Create and schedule a background job to deploy the application
            cloudDeploymentJob = new CloudDeploymentJob(appDetailsPage.getName(), appDetailsPage.getDescription(),
                    appDetailsPage.getVersion(), finalFileName, carbonArchive.getLocation().toFile().getAbsolutePath(),
                    appDetailsPage.getAppIcon(), appDetailsPage.getTags(), appDetailsPage.isNewVersion());
            cloudDeploymentJob.schedule();

        } catch (Exception e) {
            log.error(CloudDeploymentWizardConstants.ErrorMessages.ERROR_CREATING_CAR_FILE_MSG, e);
            openMessageBox(getShell(), CloudDeploymentWizardConstants.DIALOG_TITLE_TEXT,
                    CloudDeploymentWizardConstants.ErrorMessages.ERROR_CREATING_CAR_FILE_MSG
                            + " For more details view the log.\n",
                    SWT.ICON_ERROR);
        }

        return true;
    }

    public static IProject getSelectedProject(Object obj) throws Exception {
        if (obj == null) {
            return null;
        }

        if (obj instanceof IResource) {
            return ((IResource) obj).getProject();
        } else if (obj instanceof IStructuredSelection) {
            return getSelectedProject(((IStructuredSelection) obj).getFirstElement());
        }

        return null;
    }

    public void addPages() {
        if (!initError) {
            if (UserSessionManager.getCurrentSession() == null) {
                addPage(loginPage);
            }
            addPage(mainPage);
            addPage(appDetailsPage);
            super.addPages();
        }
    }

    private int openMessageBox(Shell shell, String title, String message, int style) {
        MessageBox exportMsg = new MessageBox(shell, style);
        exportMsg.setText(title);
        exportMsg.setMessage(message);

        return exportMsg.open();
    }

}
