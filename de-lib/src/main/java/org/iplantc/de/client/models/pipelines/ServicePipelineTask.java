package org.iplantc.de.client.models.pipelines;

import org.iplantc.de.client.models.HasDescription;
import org.iplantc.de.client.models.HasId;
import org.iplantc.de.client.models.apps.AppFileParameters;
import org.iplantc.de.client.models.tool.Tool;

import com.google.gwt.user.client.ui.HasName;

import java.util.List;

/**
 * An AutoBean interface for a service Template provided in service Pipeline JSON.
 * 
 * @author psarando
 * 
 */
public interface ServicePipelineTask extends HasId, HasName, HasDescription {

    Tool getTool();

    void setTool(Tool tool);

    public List<AppFileParameters> getInputs();

    public void setInputs(List<AppFileParameters> inputs);

    public List<AppFileParameters> getOutputs();

    public void setOutputs(List<AppFileParameters> outputs);
}
