package org.qumodo.miscaclient.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.qumodo.data.DataManager;
import org.qumodo.data.MediaLoader;
import org.qumodo.data.MediaLoaderListener;
import org.qumodo.data.MessageCenter;
import org.qumodo.data.models.MiscaWorkflowQuestion;
import org.qumodo.miscaclient.R;
import org.qumodo.miscaclient.dataProviders.MessageContentProvider;
import org.qumodo.miscaclient.dataProviders.MiscaWorkflowGenerator;
import org.qumodo.miscaclient.dataProviders.MiscaWorkflowManager;
import org.qumodo.miscaclient.dataProviders.UserSettingsManager;
import org.qumodo.data.models.Message;
import org.qumodo.network.QMessage;
import org.qumodo.network.QMessageType;

import java.io.File;
import java.util.HashMap;
import java.util.List;


public class MessageRecyclerViewAdapter extends RecyclerView.Adapter<MessageRecyclerViewAdapter.ViewHolder> implements View.OnClickListener {

    private final List<Message> mValues;
    private final MessageListFragment.OnMessageListInteractionListener mListener;

    private final HashMap<String, String> userIDs = new HashMap<>();

    @Override
    public void onClick(View view) {
        Log.d("BINDER", "Button Clicked " + view.getTag());
        String workflowID = ((String) view.getTag()).split("::")[0];
        String imageID = ((String) view.getTag()).split("::")[1];
        Button button = (Button) view;
        DataManager dm = new DataManager(view.getContext());
        Intent updateUI = new Intent();

        if (currentWorkflowID.equals(MiscaWorkflowGenerator.getStartID()) && workflowID.equals("END")) {
            dm.removeMiscaQuestion(currentMiscaQuestionID);
            MessageContentProvider.setup(view.getContext(), currentGroupID);
            updateUI.setAction(MessageCenter.REMOVE_LAST_ITEM);
        } else {
            Message response = dm.updateMiscaQuestionToAnswered(currentMiscaQuestionID, currentGroupID, currentMessageTextForMiscaQuestion, String.valueOf(button.getText()));
            Message toUpdate = MessageContentProvider.ITEM_MAP.get(currentMiscaQuestionID);
            toUpdate.setType(QMessageType.MISCA_TEXT);
            toUpdate.setText(currentMessageTextForMiscaQuestion);
            MessageContentProvider.addItem(response);
            updateUI.setAction(MessageCenter.NEW_LIST_ITEM);
        }

        updateUI.putExtra(QMessage.KEY_GROUP_ID, currentGroupID);
        view.getContext().sendBroadcast(updateUI);
    }

    private static class MessageViewTypes {
        private static final int USER_TEXT      = R.layout.fragment_message_user;
        private static final int USER_IMAGE     = R.layout.fragment_message_user_picture;
        private static final int GROUP_TEXT     = R.layout.fragment_message_group;
        private static final int GROUP_IMAGE    = R.layout.fragment_message_group_picture;
        private static final int MISCA_TEXT     = R.layout.fragment_message_misca_text;
        private static final int MISCA_IMAGE    = R.layout.fragment_message_group_picture;
        private static final int MISCA_QUESTION = R.layout.fragment_message_misca_question;
    }

    public MessageRecyclerViewAdapter(List<Message> items, MessageListFragment.OnMessageListInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    private String getFromID(Message message) {
        String fromID = userIDs.get(message.getId());
        if (fromID == null) {
            fromID = message.getFrom().getId();
            userIDs.put(message.getId(), fromID);
        }
        return fromID;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = mValues.get(position);
        final String userID = UserSettingsManager.getUserID();
        final String miscaID = UserSettingsManager.getMiscaID();
        String fromID = getFromID(message);

        if (fromID.equals(userID)) {
            return message.getType() == QMessageType.TEXT
                    ? MessageViewTypes.USER_TEXT
                    : MessageViewTypes.USER_IMAGE;
        } else if (fromID.equals(miscaID)) {
            if (message.getType() == QMessageType.MISCA_QUESTION)
                return MessageViewTypes.MISCA_QUESTION;
            return message.getType() == QMessageType.MISCA_TEXT
                    ? MessageViewTypes.MISCA_TEXT
                    : MessageViewTypes.MISCA_IMAGE;
        } else {
            return message.getType() == QMessageType.TEXT
                    ? MessageViewTypes.GROUP_TEXT
                    : MessageViewTypes.GROUP_IMAGE;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(viewType, parent, false);
        return new ViewHolder(view);
    }

    private void bindTextMessageView(final ViewHolder holder) {
        holder.messageText.setText(holder.mItem.getText());
        if (holder.mItem.getText().length() < 30) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            lp.weight = 0;
            holder.messageText.setLayoutParams(lp);
        } else {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            lp.weight = 3;
            holder.messageText.setLayoutParams(lp);
        }
    }

    private boolean loading = false;

    private void bindPictureMessage(final ViewHolder holder) {
        loading = true;
        holder.imageView.setVisibility(View.INVISIBLE);
        holder.spinner.setVisibility(View.VISIBLE);
        Log.d("MessageRecycleView", "Loading Message");
        MediaLoader.getMessageImage(holder.mItem.getId(), holder.mView.getContext(), new MediaLoaderListener() {
            @Override
            public void imageHasLoaded(String ref, Bitmap image) {
                Log.d("MessageRecycleView", "Image has loaded");
                holder.imageView.setImageBitmap(image);
                holder.imageView.setVisibility(View.VISIBLE);
                holder.spinner.setVisibility(View.GONE);
                loading = false;
                holder.imageView.getLayoutParams().height = RecyclerView.LayoutParams.WRAP_CONTENT;
                int index = mValues.indexOf(holder.mItem);
                if (index >= mValues.size() - 2) {
                    Intent updateUI = new Intent();
                    updateUI.setAction(MessageListFragment.ACTION_LAST_IMAGE_LOADED);
                    updateUI.putExtra(MessageListFragment.INTENT_LIST_ITEM_LOADED, index);
                    holder.imageView.getContext().sendBroadcast(updateUI);
                }
            }

            @Override
            public void imageHasFailedToLoad(String ref) {
                Log.d("BINDER", "load failed");
                holder.imageView.setImageResource(R.drawable.sample_image);
                loading = false;
            }
        });
    }

    private LinearLayout.LayoutParams getQuestionLayoutParams(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        float height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35, metrics);
        int margin = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, metrics));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, Math.round(height), 1);
        params.setMarginStart(margin);
        params.setMarginEnd(margin);

        return params;
    }

    private Button getMiscaQuestionButton(Context context, String text, String tag) {
        Button question = new Button(context);
        question.setText(text);
        question.setTag(tag);
        question.setLayoutParams(getQuestionLayoutParams(context));
        question.setTextColor(context.getResources().getColor(R.color.colorChatTextWhite, null));
        question.setBackground(context.getResources().getDrawable(R.drawable.rounded_box_blue, null));
        question.setAllCaps(false);
        question.setOnClickListener(this);

        return question;
    }

    private String currentMiscaQuestionID;
    private String currentGroupID;
    private String currentMessageTextForMiscaQuestion;
    private String currentWorkflowID;

    private void bindMiscaQuestionMessage(final ViewHolder holder) {
        currentMiscaQuestionID = holder.mItem.getId();
        currentGroupID = holder.mItem.getGroup().getId();

        String workflowID = holder.mItem.getText().split("::")[0];
        String imageID = holder.mItem.getText().split("::")[1];
        MiscaWorkflowQuestion question = (MiscaWorkflowQuestion) MiscaWorkflowManager
                                                                    .getManager()
                                                                    .getStep(workflowID);
        holder.messageText.setText(question.getMessage());
        currentMessageTextForMiscaQuestion = question.getMessage();
        currentWorkflowID = workflowID;

        holder.questionList.removeAllViews();

        for (MiscaWorkflowQuestion.Question q : question.getQuestions()) {
            holder.questionList.addView(getMiscaQuestionButton(holder.mView.getContext(), q.label, q.id+"::" + imageID));
        }
    }

    private int lastPosition = -1;

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        if (holder.messageTime != null) {
            holder.messageTime.setText(holder.mItem.getSentAsTime());
        }
        if (holder.mItem.getType() == QMessageType.TEXT) {
            bindTextMessageView(holder);
        } else if (holder.mItem.getType() == QMessageType.PICTURE) {
            bindPictureMessage(holder);
        } else if (holder.mItem.getType() == QMessageType.MISCA_QUESTION) {
            bindMiscaQuestionMessage(holder);
        }

        Animation animation = AnimationUtils.loadAnimation(
                holder.mView.getContext(),
                (position > lastPosition)
                        ? R.anim.up_from_bottom
                        : R.anim.down_from_top);
        holder.itemView.startAnimation(animation);
        lastPosition = position;
    }

    @Override
    public void onViewDetachedFromWindow(ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.itemView.clearAnimation();
    }

    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (holder.imageView != null && !loading) {
            holder.imageView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView messageText;
        public final TextView messageTime;
        public final ImageView imageView;
        public final ProgressBar spinner;
        public final LinearLayout questionList;
        public Message mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            messageText = view.findViewById(R.id.message_list_item_text);
            messageTime = view.findViewById(R.id.message_list_item_time);
            spinner = view.findViewById(R.id.spinner);
            imageView = view.findViewById(R.id.image_view);
            questionList = view.findViewById(R.id.question_list);
            if (imageView != null) {
                imageView.setClipToOutline(true);
            }
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }
}
