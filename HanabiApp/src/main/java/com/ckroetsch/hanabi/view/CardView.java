package com.ckroetsch.hanabi.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.ckroetsch.hanabi.R;
import com.ckroetsch.hanabi.model.Card;
import com.google.inject.Inject;

/**
 * @author curtiskroetsch
 */
public class CardView extends FrameLayout {

    TextView mContent;

    public CardView(Context context) {
        super(context);
    }

    public CardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContent = (TextView) findViewById(R.id.card_number);
    }

    public void bindWithCard(Card card) {
        mContent.setText(card.getValue() + "");
        mContent.setBackgroundResource(card.getSuit().getColorId());
    }

    public void bindWithUnknown() {
        mContent.setText(null);
        mContent.setBackgroundResource(R.drawable.carbon);
    }
}
