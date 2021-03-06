package com.ckroetsch.hanabi.app.chat;

import android.content.Context;
import android.view.View;

import com.ckroetsch.hanabi.R;
import com.ckroetsch.hanabi.events.socket.play.CardEvent;
import com.ckroetsch.hanabi.events.socket.play.DiscardEvent;

/**
* @author curtiskroetsch
*/
public class CardPlayedChatItem extends GameChatItem {

    CardEvent mEvent;

    String mName;

    Context mContext;

    public CardPlayedChatItem(Context context, String name, CardEvent event) {
        super(context);
        mContext = context;
        mEvent = event;
        mName = name;
    }

    @Override
    public void bindToView(ViewHolder viewHolder) {
        viewHolder.mCard.bindWithCard(mEvent.card);
        viewHolder.mName.setText(ChatFragment.getInitials(mEvent.name));
        final int stringResId;
        if (mEvent instanceof DiscardEvent) {
            stringResId = R.string.chat_game_discard;
        } else {
            stringResId = R.string.chat_game_played_success;
        }
        viewHolder.mMessage.setText(mContext.getResources().getString(stringResId));
        viewHolder.mCard.setVisibility(View.VISIBLE);
        viewHolder.mCard.bindWithCard(mEvent.card);
    }

    @Override
    public boolean isMyChatItem() {
        return mEvent.name.equals(mName);
    }
}
