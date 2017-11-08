package org.qumodo.miscaclient.controllers;

import android.content.Context;

import org.qumodo.data.DataManager;
import org.qumodo.data.models.Message;
import org.qumodo.data.models.MiscaWorkflowStep;
import org.qumodo.miscaclient.dataProviders.MessageContentProvider;
import org.qumodo.miscaclient.dataProviders.UserSettingsManager;
import org.qumodo.miscaclient.fragments.MessageListFragment;
import org.qumodo.network.QMessageType;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public class MiscaWorkflowManager {

    private static MiscaWorkflowManager manager;

    public static MiscaWorkflowManager getManager() {
        if (manager == null) {
            manager = new MiscaWorkflowManager();
        }

        return manager;
    }

    private Map<String, MiscaWorkflowStep> workflow;

    private MiscaWorkflowManager() {
        workflow = MiscaWorkflowGenerator.generateWorkflow();
    }

    public MiscaWorkflowStep getStep(String id) {
        return workflow.get(id);
    }

    public void startNewImageWorkflow(String id, Context context, MessageListFragment messageListFragment) {
        String groupID = MessageContentProvider.getGroupID();
        DataManager dm = new DataManager(context);
        Message newMessage = dm.addNewMessage(MiscaWorkflowGenerator.getStartID()+"::"+id, QMessageType.MISCA_QUESTION, groupID, null, UserSettingsManager.getMiscaID(), null);
        messageListFragment.loadNewMessage(newMessage);
    }

    public void addWorkflowStep(String id, MiscaWorkflowStep step) {
        workflow.put(id, step);
    }

    public void removeWorkflowStep(String id) {
        workflow.remove(id);
    }

}
