/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package org.wso2.integrationstudio.gmf.esb.provider;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.WordUtils;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EStructuralFeature;

import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;
import org.eclipse.emf.edit.provider.ItemPropertyDescriptor;
import org.eclipse.emf.edit.provider.ViewerNotification;

import org.wso2.integrationstudio.gmf.esb.CallTemplateParameter;
import org.wso2.integrationstudio.gmf.esb.EsbFactory;
import org.wso2.integrationstudio.gmf.esb.EsbPackage;
import org.wso2.integrationstudio.gmf.esb.PropertyValueType;
import org.wso2.integrationstudio.gmf.esb.RuleOptionType;
import org.wso2.integrationstudio.gmf.esb.presentation.EEFPropertyViewUtil;

/**
 * This is the item provider adapter for a {@link org.wso2.integrationstudio.gmf.esb.CallTemplateParameter} object.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class CallTemplateParameterItemProvider extends EsbNodeItemProvider {
	private static final String NAME_VALUE_SEPERATOR = "  -  ";
	private static final String OPERATING_SYSTEM_WINDOWS = "windows";
    /**
     * This constructs an instance from a factory and a notifier.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public CallTemplateParameterItemProvider(AdapterFactory adapterFactory) {
        super(adapterFactory);
    }

    /**
     * This returns the property descriptors for the adapted class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public List<IItemPropertyDescriptor> getPropertyDescriptors(Object object) {
        if (itemPropertyDescriptors == null) {
            super.getPropertyDescriptors(object);

            addParameterNamePropertyDescriptor(object);
            addTemplateParameterTypePropertyDescriptor(object);
            addParameterValuePropertyDescriptor(object);
        }
        return itemPropertyDescriptors;
    }

    /**
     * This adds a property descriptor for the Parameter Name feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected void addParameterNamePropertyDescriptor(Object object) {
        itemPropertyDescriptors.add
            (createItemPropertyDescriptor
                (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
                 getResourceLocator(),
                 getString("_UI_CallTemplateParameter_parameterName_feature"),
                 getString("_UI_PropertyDescriptor_description", "_UI_CallTemplateParameter_parameterName_feature", "_UI_CallTemplateParameter_type"),
                 EsbPackage.Literals.CALL_TEMPLATE_PARAMETER__PARAMETER_NAME,
                 true,
                 false,
                 false,
                 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
                 null,
                 null));
    }

    /**
     * This adds a property descriptor for the Template Parameter Type feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected void addTemplateParameterTypePropertyDescriptor(Object object) {
        itemPropertyDescriptors.add
            (createItemPropertyDescriptor
                (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
                 getResourceLocator(),
                 getString("_UI_CallTemplateParameter_templateParameterType_feature"),
                 getString("_UI_PropertyDescriptor_description", "_UI_CallTemplateParameter_templateParameterType_feature", "_UI_CallTemplateParameter_type"),
                 EsbPackage.Literals.CALL_TEMPLATE_PARAMETER__TEMPLATE_PARAMETER_TYPE,
                 true,
                 false,
                 false,
                 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
                 null,
                 null));
    }

    /**
     * This adds a property descriptor for the Parameter Value feature.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected void addParameterValuePropertyDescriptor(Object object) {
        itemPropertyDescriptors.add
            (createItemPropertyDescriptor
                (((ComposeableAdapterFactory)adapterFactory).getRootAdapterFactory(),
                 getResourceLocator(),
                 getString("_UI_CallTemplateParameter_parameterValue_feature"),
                 getString("_UI_PropertyDescriptor_description", "_UI_CallTemplateParameter_parameterValue_feature", "_UI_CallTemplateParameter_type"),
                 EsbPackage.Literals.CALL_TEMPLATE_PARAMETER__PARAMETER_VALUE,
                 true,
                 false,
                 false,
                 ItemPropertyDescriptor.GENERIC_VALUE_IMAGE,
                 null,
                 null));
    }

    /**
     * This specifies how to implement {@link #getChildren} and is used to deduce an appropriate feature for an
     * {@link org.eclipse.emf.edit.command.AddCommand}, {@link org.eclipse.emf.edit.command.RemoveCommand} or
     * {@link org.eclipse.emf.edit.command.MoveCommand} in {@link #createCommand}.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Collection<? extends EStructuralFeature> getChildrenFeatures(Object object) {
        if (childrenFeatures == null) {
            super.getChildrenFeatures(object);
            childrenFeatures.add(EsbPackage.Literals.CALL_TEMPLATE_PARAMETER__PARAMETER_EXPRESSION);
        }
        return childrenFeatures;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected EStructuralFeature getChildFeature(Object object, Object child) {
        // Check the type of the specified child object and return the proper feature to use for
        // adding (see {@link AddCommand}) it as a child.

        return super.getChildFeature(object, child);
    }

    /**
     * This returns CallTemplateParameter.gif.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public Object getImage(Object object) {
        return overlayImage(object, getResourceLocator().getImage("full/obj16/CallTemplateParameter"));
    }

    /**
     * This returns the label text for the adapted class.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated NOT
     */
    @Override
    public String getText(Object object) {
        String parameterName = ((CallTemplateParameter) object).getParameterName();
        String parameterNameLabel = WordUtils.abbreviate(parameterName, 100, 105, "...");
        String parameterType = ((CallTemplateParameter) object).getTemplateParameterType().toString();
        String parameterValue = ((CallTemplateParameter) object).getParameterValue();
        String parameterExpression = ((CallTemplateParameter) object).getParameterExpression().toString();

        if (parameterType.equalsIgnoreCase(RuleOptionType.VALUE.getName())) {
            if (parameterValue != null) {
            	String operatingSystem = System.getProperty("os.name");
            	if (operatingSystem != null && operatingSystem.toLowerCase().contains(OPERATING_SYSTEM_WINDOWS)) {
            		return parameterName == null || parameterName.length() == 0
                            ? getString("_UI_CallTemplateParameter_type")
                            :parameterNameLabel + NAME_VALUE_SEPERATOR + parameterValue;
            	}
                return parameterName == null || parameterName.length() == 0
                        ? getString("_UI_CallTemplateParameter_type")
                        : EEFPropertyViewUtil.spaceFormat(parameterNameLabel)
                                + EEFPropertyViewUtil.spaceFormat(parameterValue);
            } else {
                return parameterName == null || parameterName.length() == 0
                        ? getString("_UI_CallTemplateParameter_type")
                        : EEFPropertyViewUtil.spaceFormat(parameterNameLabel);
            }
        } else {
            return parameterName == null || parameterName.length() == 0 ? getString("_UI_CallTemplateParameter_type")
                    : EEFPropertyViewUtil.spaceFormat(parameterNameLabel)
                            + EEFPropertyViewUtil.spaceFormat(parameterExpression);
        }
    }

    /**
     * This handles model notifications by calling {@link #updateChildren} to update any cached
     * children and by creating a viewer notification, which it passes to {@link #fireNotifyChanged}.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public void notifyChanged(Notification notification) {
        updateChildren(notification);

        switch (notification.getFeatureID(CallTemplateParameter.class)) {
            case EsbPackage.CALL_TEMPLATE_PARAMETER__PARAMETER_NAME:
            case EsbPackage.CALL_TEMPLATE_PARAMETER__TEMPLATE_PARAMETER_TYPE:
            case EsbPackage.CALL_TEMPLATE_PARAMETER__PARAMETER_VALUE:
                fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), false, true));
                return;
            case EsbPackage.CALL_TEMPLATE_PARAMETER__PARAMETER_EXPRESSION:
                fireNotifyChanged(new ViewerNotification(notification, notification.getNotifier(), true, false));
                return;
        }
        super.notifyChanged(notification);
    }

    /**
     * This adds {@link org.eclipse.emf.edit.command.CommandParameter}s describing the children
     * that can be created under this object.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    protected void collectNewChildDescriptors(Collection<Object> newChildDescriptors, Object object) {
        super.collectNewChildDescriptors(newChildDescriptors, object);

        newChildDescriptors.add
            (createChildParameter
                (EsbPackage.Literals.CALL_TEMPLATE_PARAMETER__PARAMETER_EXPRESSION,
                 EsbFactory.eINSTANCE.createNamespacedProperty()));
    }

}
