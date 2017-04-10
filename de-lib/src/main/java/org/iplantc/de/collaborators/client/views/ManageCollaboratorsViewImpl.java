package org.iplantc.de.collaborators.client.views;

import org.iplantc.de.client.models.collaborators.Collaborator;
import org.iplantc.de.collaborators.client.events.RemoveCollaboratorSelected;
import org.iplantc.de.collaborators.client.events.UserSearchResultSelected.USER_SEARCH_EVENT_TAG;
import org.iplantc.de.collaborators.client.util.UserSearchField;
import org.iplantc.de.collaborators.shared.CollaboratorsModule;
import org.iplantc.de.commons.client.ErrorHandler;
import org.iplantc.de.groups.client.views.dialogs.GroupListDialog;
import org.iplantc.de.resources.client.messages.I18N;
import org.iplantc.de.shared.AsyncProviderWrapper;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import com.sencha.gxt.core.client.IdentityValueProvider;
import com.sencha.gxt.core.client.Style.LayoutRegion;
import com.sencha.gxt.core.client.Style.SelectionMode;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.Composite;
import com.sencha.gxt.widget.core.client.FramedPanel;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.HorizontalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.ViewReadyEvent;
import com.sencha.gxt.widget.core.client.grid.CheckBoxSelectionModel;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent.SelectionChangedHandler;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

import java.util.List;

/**
 * @author jstroot
 */
public class ManageCollaboratorsViewImpl extends Composite implements ManageCollaboratorsView,
                                                                      SelectionChangedHandler<Collaborator> {

    @UiTemplate("ManageCollaboratorsView.ui.xml")
    interface MyUiBinder extends UiBinder<Widget, ManageCollaboratorsViewImpl> {
    }
    @UiField ColumnModel<Collaborator> cm;
    @UiField ListStore<Collaborator> listStore;
    @UiField FramedPanel collaboratorListPnl;
    @UiField BorderLayoutContainer con;
    @UiField TextButton deleteBtn;
    @UiField Grid<Collaborator> grid;
    @UiField TextButton manageBtn;
    @UiField TextButton groupsBtn;
    @UiField(provided = true) UserSearchField searchField;
    @UiField HorizontalLayoutContainer searchPanel;
    @UiField ToolBar toolbar;

    @Inject AsyncProviderWrapper<GroupListDialog> groupDialogProvider;
    
    private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
    private final CheckBoxSelectionModel<Collaborator> checkBoxModel;
    private MODE mode;
    private String baseID;

    @Inject
    public ManageCollaboratorsViewImpl(@Assisted final MODE mode) {
        searchField = new UserSearchField(USER_SEARCH_EVENT_TAG.MANAGE);
        checkBoxModel = new CheckBoxSelectionModel<>(new IdentityValueProvider<Collaborator>());
        initWidget(uiBinder.createAndBindUi(this));
        grid.setSelectionModel(checkBoxModel);
        checkBoxModel.setSelectionMode(SelectionMode.MULTI);
        grid.getView().setEmptyText(I18N.DISPLAY.noCollaborators());
        grid.addViewReadyHandler(new ViewReadyEvent.ViewReadyHandler() {
            @Override
            public void onViewReady(ViewReadyEvent event) {
                setGridCheckBoxDebugIds();
            }
        });
        init();
        setMode(mode);
    }

    @Override
    public void addCollaborators(List<Collaborator> models) {
        listStore.addAll(models);
        setGridCheckBoxDebugIds();
    }

    @Override
    public MODE getMode() {
        return mode;
    }

    @Override
    public List<Collaborator> getSelectedCollaborators() {
        return grid.getSelectionModel().getSelectedItems();
    }

    @Override
    public void loadData(List<Collaborator> models) {
        listStore.clear();
        listStore.addAll(models);
    }

    @Override
    public void mask(String maskText) {
        if (maskText == null || maskText.isEmpty()) {
            collaboratorListPnl.mask(I18N.DISPLAY.loadingMask());
        } else {
            collaboratorListPnl.mask(maskText);
        }
    }

    @Override
    public void onSelectionChanged(SelectionChangedEvent<Collaborator> event) {
        if (event.getSelection() != null
                && event.getSelection().size() > 0
                && MODE.MANAGE.equals(mode)) {
            deleteBtn.enable();
        } else {
            deleteBtn.disable();
        }
    }

    @Override
    public void removeCollaborators(List<Collaborator> models) {
        if (models != null && !models.isEmpty()) {
            for (Collaborator c : models) {
                if (listStore.findModel(c) != null) {
                    listStore.remove(c);
                }
            }
        }
    }

    @Override
    public void setMode(MODE mode) {
        this.mode = mode;
        switch (mode) {
            case MANAGE:
                grid.getView().setEmptyText(I18N.DISPLAY.noCollaborators());
                collaboratorListPnl.setHeading(I18N.DISPLAY.myCollaborators());
                manageBtn.setVisible(false);
                deleteBtn.setVisible(true);
                con.show(LayoutRegion.NORTH);
                break;
            case SELECT:
                grid.getView().setEmptyText(I18N.DISPLAY.noCollaborators());
                con.hide(LayoutRegion.NORTH);
                manageBtn.setVisible(true);
                deleteBtn.setVisible(false);
                collaboratorListPnl.setHeading(I18N.DISPLAY.selectCollabs());
                break;
        }
    }

    @Override
    public void unmask() {
        collaboratorListPnl.unmask();
    }

    @Override
    protected void onEnsureDebugId(String baseID) {
        super.onEnsureDebugId(baseID);
        this.baseID = baseID;
        deleteBtn.ensureDebugId(baseID + CollaboratorsModule.Ids.DELETE);
        //Checkbox column config is at index 0
        grid.getView().getHeader().getHead(0).getElement().setId(baseID + CollaboratorsModule.Ids.CHECKBOX_HEADER);
        searchField.setViewDebugId(CollaboratorsModule.Ids.SEARCH_LIST);
    }

    void setGridCheckBoxDebugIds() {
        for (int i = 0; i < listStore.size(); i++) {
            grid.getView().getCell(i, 0).setId(baseID + CollaboratorsModule.Ids.CHECKBOX_ITEM + i);
        }
    }

    @UiFactory
    ListStore<Collaborator> createListStore() {
        return new ListStore<>(new CollaboratorKeyProvider());
    }

    @UiFactory
    ColumnModel<Collaborator> buildColumnModel() {
        return new CollaboratorsColumnModel(checkBoxModel);
    }

    @UiHandler("deleteBtn")
    void deleteCollaborator(SelectEvent event) {
        fireEvent(new RemoveCollaboratorSelected(grid.getSelectionModel().getSelectedItems()));
    }

    @UiHandler("manageBtn")
    void manageCollaborators(SelectEvent event) {
        setMode(MODE.MANAGE);
    }

    @UiHandler("groupsBtn")
    void manageGroupsSelected(SelectEvent event) {
        groupDialogProvider.get(new AsyncCallback<GroupListDialog>() {
            @Override
            public void onFailure(Throwable caught) {
                ErrorHandler.post(caught);
            }

            @Override
            public void onSuccess(GroupListDialog result) {
                result.show();
            }
        });
    }

    private void init() {
        collaboratorListPnl.setHeading(I18N.DISPLAY.myCollaborators());
        grid.getSelectionModel().addSelectionChangedHandler(this);
    }

    @Override
    public HandlerRegistration addRemoveCollaboratorSelectedHandler(RemoveCollaboratorSelected.RemoveCollaboratorSelectedHandler handler) {
        return addHandler(handler, RemoveCollaboratorSelected.TYPE);
    }
}
