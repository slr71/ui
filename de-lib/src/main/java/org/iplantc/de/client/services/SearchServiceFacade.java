package org.iplantc.de.client.services;

import org.iplantc.de.client.models.search.DiskResourceQueryTemplate;
import org.iplantc.de.diskResource.client.presenters.grid.proxy.FolderContentsLoadConfig;
import org.iplantc.de.diskResource.client.presenters.grid.proxy.FolderContentsRpcProxyImpl;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.autobean.shared.AutoBean;

import java.util.List;

/**
 * This service provides the ability to store, retrieve, and execute search queries.
 * 
 * @author jstroot
 * 
 */
public interface SearchServiceFacade {

    public enum SearchType {
        ANY("any"), FILE("file"), FOLDER("folder");

        private final String value;

        private SearchType(final String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }

    }

    String QUERY_TEMPLATE_KEY = "query_templates";

    /**
     * @param queryTemplates the list to be copied
     * @return a list of frozen query templates
     * @see AutoBean#setFrozen(boolean)
     */
    List<DiskResourceQueryTemplate> createFrozenList(List<DiskResourceQueryTemplate> queryTemplates);

    /**
     * Retrieves the list of saved query templates using the {@link #QUERY_TEMPLATE_KEY}.
     * 
     * If there are no saved templates, then an empty list will be returned.
     * 
     * @param callback returns the set of persisted templates on success. These templates will have their
     *            {@link DiskResourceQueryTemplate#isSaved()} flag set to true and the
     *            {@link DiskResourceQueryTemplate#isDirty()} flags set to false.
     */
    void getSavedQueryTemplates(AsyncCallback<List<DiskResourceQueryTemplate>> callback);

    /**
     * Saves the given query templates to the {@link #QUERY_TEMPLATE_KEY}, on the user-data endpoint.
     * 
     * @param queryTemplates
     * @param callback returns the set of persisted templates on success.
     */
    void saveQueryTemplates(List<DiskResourceQueryTemplate> queryTemplates, AsyncCallback<List<DiskResourceQueryTemplate>> callback);

    /**
     * Submits a search query build from the given filter.
     * 
     * Internally, this uses a {@link DataSearchQueryBuilder} to construct the query.
     * 
     * @param template the template used to construct the query string. The query string derived
     *            from this object will be URL encoded.
     * @param searchConfig the load config which defines the offset and limit for the paged request
     * @param queryResultsCallback executed when RPC call completes. The resulting list is in the same order as it is
     *            returned from the endpoint.
     */

    void submitSearchQuery(DiskResourceQueryTemplate template,
                           FolderContentsLoadConfig searchConfig,
                           FolderContentsRpcProxyImpl.SearchResultsCallback queryResultsCallback);

}
