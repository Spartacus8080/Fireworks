package com.ckroetsch.hanabi.app.chat;

import android.content.Context;

import com.ckroetsch.hanabi.R;
import com.ckroetsch.hanabi.events.game.HintEvent;
import com.ckroetsch.hanabi.events.game.NumberHintEvent;
import com.ckroetsch.hanabi.events.game.SuitHintEvent;
import com.ckroetsch.hanabi.events.socket.GiveHintEvent;

/**
* @author curtiskroetsch
*/
public class HintGivenChatItem extends GameChatItem {

    GiveHintEvent mEvent;
    String mName;

    Context mContext;

    public HintGivenChatItem(Context context, String name, GiveHintEvent event) {
        super(context);
        mContext = context;
        mEvent = event;
        mName = name;
    }

    @Override
    public void bindToView(ViewHolder viewHolder) {
        viewHolder.mName.setText(ChatFragment.getInitials(mEvent.name));
        final int resId = mEvent.hintType.equals(GiveHintEvent.NUMBER) ?
                R.string.chat_game_hint_number : R.string.chat_game_hint_color;
        viewHolder.mMessage.setText(mContext.getString(resId, mEvent.toName, mEvent.hint));
    }

    @Override
    public boolean isMyChatItem() {
        return mEvent.name.equals(mName);
    }
}
