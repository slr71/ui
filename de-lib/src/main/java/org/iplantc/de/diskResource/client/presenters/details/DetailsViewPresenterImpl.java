package org.iplantc.de.diskResource.client.presenters.details;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanCodex;
import com.google.web.bindery.autobean.shared.AutoBeanUtils;
import com.google.web.bindery.autobean.shared.Splittable;
import org.iplantc.de.client.models.diskResources.DiskResource;
import org.iplantc.de.client.models.search.DiskResourceQueryTemplate;
import org.iplantc.de.client.models.search.SearchAutoBeanFactory;
import org.iplantc.de.client.models.tags.IplantTagAutoBeanFactory;
import org.iplantc.de.client.models.tags.IplantTagList;
import org.iplantc.de.client.models.tags.Tag;
import org.iplantc.de.client.models.viewer.InfoType;
import org.iplantc.de.client.services.DiskResourceServiceFacade;
import org.iplantc.de.client.services.FileSystemMetadataServiceFacade;
import org.iplantc.de.client.services.TagsServiceFacade;
import org.iplantc.de.client.util.DiskResourceUtil;
import org.iplantc.de.commons.client.ErrorHandler;
import org.iplantc.de.commons.client.info.IplantAnnouncer;
import org.iplantc.de.diskResource.client.DetailsView;
import org.iplantc.de.diskResource.client.events.search.SubmitDiskResourceQueryEvent;
import org.iplantc.de.diskResource.client.events.selection.*;
import org.iplantc.de.diskResource.client.presenters.callbacks.TagAttachCallback;
import org.iplantc.de.diskResource.client.presenters.callbacks.TagDetachCallback;
import org.iplantc.de.diskResource.client.presenters.callbacks.TagsFetchCallback;
import org.iplantc.de.diskResource.client.presenters.callbacks.TagsSearchCallback;
import org.iplantc.de.resources.client.messages.I18N;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jstroot
 */
public class DetailsViewPresenterImpl implements DetailsView.Presenter,
                                                 RemoveResourceTagSelected.RemoveResourceTagSelectedHandler,
                                                 UpdateResourceTagSelected.UpdateResourceTagSelectedHandler {

    @Inject IplantAnnouncer announcer;
    @Inject FileSystemMetadataServiceFacade metadataService;
    @Inject DetailsView.Presenter.Appearance appearance;
    @Inject
    TagsServiceFacade tagsService;
    @Inject
    DiskResourceServiceFacade diskResourceService;
    @Inject
    SearchAutoBeanFactory sabFactory;

    private List<Tag> tags = new ArrayList<>();

    private final DetailsView view;

    private HandlerManager handlerManager;

    IplantTagAutoBeanFactory factory;

    DiskResourceUtil diskResourceUtil = DiskResourceUtil.getInstance();

    @Inject
    DetailsViewPresenterImpl(final DetailsView view, final IplantTagAutoBeanFactory factory) {
        this.view = view;
        this.factory = factory;

        view.setPresenter(this);
    }

    @Override
    public HandlerRegistration addSubmitDiskResourceQueryEventHandler(SubmitDiskResourceQueryEvent.SubmitDiskResourceQueryEventHandler handler) {
        return ensureHandlers().addHandler(SubmitDiskResourceQueryEvent.TYPE, handler);
    }

    @Override
    public void onUpdateResourceTagSelected(UpdateResourceTagSelected event) {
        DiskResource resource = event.getDiskResource();
        Tag tag = event.getTag();
        attachTag(tag.getId(), tag.getValue(), resource.getId(), null);

    }

    @Override
    public HandlerRegistration addEditInfoTypeSelectedEventHandler(EditInfoTypeSelected.EditInfoTypeSelectedEventHandler handler) {
        return ensureHandlers().addHandler(EditInfoTypeSelected.TYPE, handler);
    }

    @Override
    public HandlerRegistration addManageSharingSelectedEventHandler(ManageSharingSelected.ManageSharingSelectedEventHandler handler) {
        return ensureHandlers().addHandler(ManageSharingSelected.TYPE, handler);
    }

    @Override
    public HandlerRegistration addSetInfoTypeSelectedHandler(SetInfoTypeSelected.SetInfoTypeSelectedHandler handler) {
        return ensureHandlers().addHandler(SetInfoTypeSelected.TYPE, handler);
    }

    @Override
    public HandlerRegistration addMd5ValueClickedHandler(Md5ValueClicked.Md5ValueClickedHandler handler) {
        return ensureHandlers().addHandler(Md5ValueClicked.TYPE, handler);
    }

    @Override
    public HandlerRegistration addSendToCogeSelectedHandler(SendToCogeSelected.SendToCogeSelectedHandler handler) {
        return ensureHandlers().addHandler(SendToCogeSelected.TYPE, handler);
    }

    @Override
    public HandlerRegistration addSendToEnsemblSelectedHandler(SendToEnsemblSelected.SendToEnsemblSelectedHandler handler) {
        return ensureHandlers().addHandler(SendToEnsemblSelected.TYPE, handler);
    }

    @Override
    public HandlerRegistration addSendToTreeViewerSelectedHandler(SendToTreeViewerSelected.SendToTreeViewerSelectedHandler handler) {
        return ensureHandlers().addHandler(SendToTreeViewerSelected.TYPE, handler);
    }

    @Override
    public void onRemoveResourceTagSelected(RemoveResourceTagSelected event) {
        Tag tag = event.getTag();
        DiskResource resource = event.getResource();
        detachTag(tag.getId(), tag.getValue(), resource.getId(), null);
    }

    @Override
    public void fetchTagsForResource(String diskResourceId, TagsFetchCallback callback) {
        tags.clear();
        metadataService.getTags(diskResourceId, new AsyncCallback<List<Tag>>() {
            @Override
            public void onFailure(Throwable caught) {
                // FIXME Move to appearance
                ErrorHandler.post(appearance.tagFetchError(), caught);
            }

            @Override
            public void onSuccess(List<Tag> result) {
                tags.addAll(result);
                callback.onTagsFetched(tagsToSplittable(tags));
            }
        });
    }

    @Override
    public void searchTags(String searchVal, TagsSearchCallback callback) {
        tagsService.suggestTag(searchVal, 10, new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable caught) {
                ErrorHandler.post(I18N.ERROR.tagRetrieveError(), caught);
            }

            @SuppressWarnings("serial")
            @Override
            public void onSuccess(final String result) {
                AutoBean<IplantTagList> tagListBean =
                        AutoBeanCodex.decode(factory, IplantTagList.class, result);
                List<Tag> tagList = tagListBean.as().getTagList();

                callback.searchComplete(tagsToSplittable(tagList));
            }
        });
    }


    @Override
    public void attachTag(String tagId,
                          String tagValue,
                          String diskResourceId,
                          TagAttachCallback callback) {
        metadataService.attachTags(wrapInList(tagId), diskResourceId, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                ErrorHandler.post(appearance.tagAttachError(), caught);
            }

            @Override
            public void onSuccess(Void result) {
                Tag tag = factory.getTag().as();
                tag.setId(tagId);
                tag.setValue(tagValue);
                List<Tag> found = tags.stream()
                                      .filter(t -> t.getId().equals(tag.getId()))
                                      .collect(Collectors.toList());
                if (found.size() == 0) {
                    tags.add(tag);
                }
                callback.onAttach(tagsToSplittable(tags));
            }
        });
    }

    @Override
    public void detachTag(String tagId,
                          String tagValue,
                          String diskResourceId,
                          TagDetachCallback callback) {
        metadataService.detachTags(wrapInList(tagId), diskResourceId, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                ErrorHandler.post(appearance.tagDetachError(), caught);
            }

            @Override
            public void onSuccess(Void result) {
                Tag tag = factory.getTag().as();
                tag.setId(tagId);
                tag.setValue(tagValue);
                List<Tag> toRemove = tags.stream()
                                         .filter(t -> t.getId().equals(tag.getId()))
                                         .collect(Collectors.toList());
                tags.removeAll(toRemove);
                callback.onDetach(tagsToSplittable(tags));
            }
        });
    }

    @Override
    public void createTag(String tagValue, String diskResourceId, TagAttachCallback callback) {
        tagsService.createTag(tagValue.trim(), new AsyncCallback<Tag>() {
            @Override
            public void onFailure(Throwable caught) {
                ErrorHandler.post(I18N.ERROR.tagCreateError(), caught);
            }

            @Override
            public void onSuccess(Tag result) {
                attachTag(result.getId(), result.getValue(), diskResourceId, callback);
            }
        });
    }

    @Override
    public DetailsView getView() {
        return view;
    }

    @Override
    public void onTagSelection(String tagId, String tagValue) {
        Tag tag = factory.getTag().as();
        tag.setId(tagId);
        tag.setValue(tagValue);
        DiskResourceQueryTemplate queryTemplate = sabFactory.dataSearchFilter().as();
        queryTemplate.setTagQuery(Sets.newHashSet(tag));
        ensureHandlers().fireEvent(new SubmitDiskResourceQueryEvent(queryTemplate));
    }

    @Override
    public void onSharingClicked() {
        ensureHandlers().fireEvent(new ManageSharingSelected(view.getBoundValue()));
    }

    @Override
    public void onSetInfoType(String infoType) {
        if (infoType.equals(DetailsView.INFOTYPE_NOSELECT)) {
            ensureHandlers().fireEvent(new SetInfoTypeSelected(view.getBoundValue(), ""));
        } else {
            ensureHandlers().fireEvent(new SetInfoTypeSelected(view.getBoundValue(), infoType));
        }
    }

    @Override
    public void onSendToClicked(String infoType) {
        DiskResource boundValue = view.getBoundValue();
        if (boundValue == null) {
            return;
        }
        InfoType resInfoType = InfoType.fromTypeString(boundValue.getInfoType());
        if (resInfoType == null) {
            return;
        }

        final ArrayList<DiskResource> resources = Lists.newArrayList(boundValue);
        if (diskResourceUtil.isTreeInfoType(resInfoType)) {
            ensureHandlers().fireEvent(new SendToTreeViewerSelected(resources));
        } else if (diskResourceUtil.isGenomeVizInfoType(resInfoType)) {
            ensureHandlers().fireEvent(new SendToCogeSelected(resources));
        } else if (diskResourceUtil.isEnsemblInfoType(resInfoType)) {
            ensureHandlers().fireEvent(new SendToEnsemblSelected(resources));
        }

    }

    List<String> wrapInList(String tagId) {
        return Lists.newArrayList(tagId);
    }

    private Splittable tagsToSplittable(List<Tag> tagList) {
        IplantTagList tagListAB =factory.getTagList().as();
        tagListAB.setTagList(tagList);
        return AutoBeanCodex.encode(AutoBeanUtils.getAutoBean(tagListAB));
    }

    HandlerManager createHandlerManager() {
        return new HandlerManager(this);
    }

    HandlerManager ensureHandlers() {
        return handlerManager == null ? handlerManager = createHandlerManager() : handlerManager;
    }
}
