package org.iplantc.de.apps.widgets.client.view.editors.arguments;

import org.iplantc.de.apps.widgets.client.models.SelectionItemProperties;
import org.iplantc.de.apps.widgets.client.view.editors.style.AppTemplateWizardAppearance;
import org.iplantc.de.apps.widgets.client.view.editors.widgets.AppWizardComboBox;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class DoubleSelectionEditor extends AppWizardComboBox {

    @Inject
    public DoubleSelectionEditor(@Assisted AppTemplateWizardAppearance appearance,
                                 SelectionItemProperties properties) {
        super(appearance, properties);
    }
}
