package com.ckroetsch.hanabi.app;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ckroetsch.hanabi.R;
import com.ckroetsch.hanabi.events.BusSingleton;
import com.ckroetsch.hanabi.events.socket.meta.EndGameEvent;
import com.ckroetsch.hanabi.events.socket.play.DiscardEvent;
import com.ckroetsch.hanabi.events.socket.common.HanabiErrorEvent;
import com.ckroetsch.hanabi.events.socket.meta.JoinGameEvent;
import com.ckroetsch.hanabi.events.socket.play.GiveHintEvent;
import com.ckroetsch.hanabi.events.socket.play.PlayCardEvent;
import com.ckroetsch.hanabi.events.socket.SocketEvent;
import com.ckroetsch.hanabi.events.socket.play.StartGameEvent;
import com.ckroetsch.hanabi.game.GameContext;
import com.ckroetsch.hanabi.model.Card;
import com.ckroetsch.hanabi.model.Game;
import com.ckroetsch.hanabi.model.Player;
import com.ckroetsch.hanabi.network.HanabiError;
import com.ckroetsch.hanabi.network.HanabiFrontEndAPI;
import com.ckroetsch.hanabi.util.JsonUtil;
import com.ckroetsch.hanabi.view.CardView;
import com.google.inject.Inject;
import com.squareup.otto.Subscribe;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

/**
 * @author curtiskroetsch
 */
public class GameFragment extends RoboFragment {

    public static final String KEY_GAME = "game";
    public static final String KEY_NAME = "name";

    private static final String TAG = GameFragment.class.getName();

    @InjectView(R.id.players)
    ListView mPlayers;

    @InjectView(R.id.board_container_parent)
    View mBoardContainer;

    @InjectView(R.id.board_container)
    LinearLayout mBoard;

    @InjectView(R.id.button_join)
    Button mJoinButton;

    @InjectView(R.id.button_start)
    Button mStartButton;

    @InjectView(R.id.discard)
    TextView mDiscard;

    @InjectView(R.id.play_card)
    TextView mPlayCard;

    @InjectView(R.id.play_area)
    View mPlayArea;

    @InjectView(R.id.game_hints)
    TextView mGameHints;

    @InjectView(R.id.game_lives)
    TextView mGameLives;

    @InjectView(R.id.game_cards_left)
    TextView mGameCardsLeft;

    @InjectView(R.id.game_score)
    TextView mGameScore;

    @InjectView(R.id.game_status)
    TextView mGameStatus;

    @Inject
    LayoutInflater mInflater;

    @Inject
    HanabiFrontEndAPI mHanabiAPI;

    Game mGame;

    String mName;

    Handler mHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String gameJSON = getArguments().getString(KEY_GAME);
        BusSingleton.get().register(this);
        mName = getArguments().getString(KEY_NAME);
        Log.d(TAG, "gameJSON = " + gameJSON);
        final Game game = JsonUtil.jsonToObject(gameJSON, Game.class);
        mGame = game;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                bindGame(mGame);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game, container, false);
    }

    @Subscribe
    public void onJoin(JoinGameEvent event) {
        Log.d(TAG, "join success");
        mJoinButton.setVisibility(View.INVISIBLE);
        mStartButton.setVisibility(View.VISIBLE);
        mGame = event.game;
        bindGame(mGame);
    }

    private void onJoinFailed() {
        mJoinButton.setVisibility(View.GONE);
    }

    @Subscribe
    public void onStart(StartGameEvent event) {
        Log.e(TAG, "start success");
        mStartButton.setVisibility(View.GONE);
        Toast.makeText(getActivity(), "Started", Toast.LENGTH_SHORT).show();
        mGame = event.game;
        bindGame(mGame);
    }

    @Subscribe
    public void onDiscard(DiscardEvent event) {
        Log.d(TAG, "successfully discarded " + event.card);
        Toast.makeText(getActivity(), "Discarded: " + event.card, Toast.LENGTH_SHORT).show();
        mGame = event.game;
        bindGame(mGame);
    }

    @Subscribe
    public void onPlay(PlayCardEvent event) {
        Log.d(TAG, "successfully played " + event.card);
        Toast.makeText(getActivity(), "Played: " + event.card, Toast.LENGTH_SHORT).show();
        mGame = event.game;
        bindGame(mGame);
    }

    @Subscribe
    public void onHintGiven(GiveHintEvent event) {
        Log.d(TAG, "hint given ");
        if (event.to.equals(mName)) {
            new AlertDialog.Builder(getActivity())
                    .setTitle(event.from + " Gave you a " + event.hintType + " hint!")
                    .setMessage("Cards " + Arrays.toString(event.cardsHinted.toArray()) + " are " + event.hint)
                    .create().show();
        }
        mGame = event.game;
        bindGame(mGame);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mGame.getHasStarted()) {
            mJoinButton.setVisibility(View.GONE);
            mStartButton.setVisibility(View.GONE);
        } else {
            mBoardContainer.setVisibility(View.GONE);
            mJoinButton.setVisibility(View.VISIBLE);
            for (Player player : mGame.getPlayers()) {
                if (player.getName().equals(mName)) {
                    mJoinButton.setVisibility(View.GONE);
                    mStartButton.setVisibility(View.VISIBLE);
                    break;
                }
            }
        }
        mJoinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHanabiAPI.joinGame(mName, mGame.getId());
            }
        });
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHanabiAPI.startGame(mGame.getId());
            }
        });
        mDiscard.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(final View view, DragEvent dragEvent) {
                if (dragEvent.getAction() == DragEvent.ACTION_DRAG_STARTED) {
                    mPlayArea.setAlpha(0f);
                    mPlayArea.animate().alpha(1f).start();
                    mBoardContainer.animate().alpha(0.1f).start();
                    return true;
                } else if (dragEvent.getAction() == DragEvent.ACTION_DROP) {
                    final CharSequence indexString = dragEvent.getClipData().getItemAt(0).coerceToText(getActivity());
                    final int index = Integer.parseInt(indexString.toString());
                    Log.d(TAG, "discarding " + index);
                    mHanabiAPI.discardCard(mName, mGame.getId(), index);
                    return true;
                } else if (dragEvent.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                    mDiscard.setTextColor(getResources().getColor(R.color.pastel_red));
                    return true;
                } else if (dragEvent.getAction() == DragEvent.ACTION_DRAG_EXITED) {
                    mDiscard.setTextColor(getResources().getColor(R.color.pastel_white));
                    return true;
                } else if (dragEvent.getAction() == DragEvent.ACTION_DRAG_ENDED) {
                    mPlayArea.animate().alpha(0f);
                    mBoardContainer.animate().alpha(1f);
                    return true;
                }
                return true;
            }
        });
        mPlayCard.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View view, DragEvent dragEvent) {
                if (dragEvent.getAction() == DragEvent.ACTION_DRAG_STARTED) {
                    return true;
                } else if (dragEvent.getAction() == DragEvent.ACTION_DROP) {
                    final CharSequence indexString = dragEvent.getClipData().getItemAt(0).coerceToText(getActivity());
                    final int index = Integer.parseInt(indexString.toString());
                    Log.d(TAG, "playing " + index);
                    mHanabiAPI.playCard(mName, mGame.getId(), index);
                    return true;
                } else if (dragEvent.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
                    mPlayCard.setTextColor(getResources().getColor(R.color.pastel_green));
                    return true;
                } else if (dragEvent.getAction() == DragEvent.ACTION_DRAG_EXITED) {
                    mPlayCard.setTextColor(getResources().getColor(R.color.pastel_white));
                    return true;
                } else if (dragEvent.getAction() == DragEvent.ACTION_DRAG_ENDED) {
                    return true;
                }
                return true;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "onStart()");
        bindGame(mGame);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "onStop()");
    }

    @Override
    public void onDestroy() {
        BusSingleton.get().unregister(this);
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
    }

    private void bindGame(Game game) {
        bindBoard(game);
        bindStatus(game);
        Log.d(TAG, "mPlayers: " + mPlayers);
        mPlayers.setAdapter(new PlayersAdapter(getActivity(), game.getPlayers()));
        if (game.getHasEnded()) {
            bindGameOver(game);
        }
    }

    private void bindStatus(Game game) {
        if (game.getHasEnded()) {
            mGameStatus.setText("Game has finished, play again?");
        } else if (!game.getHasStarted()) {
            boolean mInGame = false;
            for (Player player : game.getPlayers()) {
                if (player.getName().equals(mName)) {
                    mInGame = true;
                    break;
                }
            }
            if (mInGame) {
                mGameStatus.setText("Start the game once your friends have all joined");
            } else {
                mGameStatus.setText("Join the game to play");
            }
        } else if (game.getCurrentPlayer().equals(mName)) {
            mGameStatus.setText("It's your turn to play");
        } else {
            mGameStatus.setText(String.format("It's %s's turn to play", game.getCurrentPlayer()));
        }
    }

    private void bindGameOver(Game game) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Game Over")
                .setMessage("Final score: " + game.getScore())
                .setPositiveButton("Play again?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        ((MainActivity) getActivity()).openStartFragment();
                    }
                }).create().show();
    }

    private void bindBoard(Game game) {
        mGameHints.setText(Integer.toString(game.getNumHints()));
        mGameLives.setText(Integer.toString(game.getNumLives()));
        mGameScore.setText(Integer.toString(game.getScore()));
        mGameCardsLeft.setText(Integer.toString(game.getNumCardsRemaining()));
        mBoard.removeAllViews();
        for (Card card : game.getPlayed()) {
            bindCard(card);
        }
    }

    private void bindCard(Card card) {
        final CardView cardView = CardView.createCard(getActivity(), mInflater, mBoard, CardView.Size.MEDIUM);
        cardView.bindWithCard(card);
        mBoard.addView(cardView);
    }

    class PlayersAdapter extends ArrayAdapter<Player> {

        final LayoutInflater mInflater;

        public PlayersAdapter(Context context, List<Player> players) {
            super(context, 0, players);
            mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }


        @Override
        public View getView(int position, final View convertView, final ViewGroup parent) {
            final Player player = getItem(position);
            final View handView = mInflater.inflate(R.layout.view_hand, parent, false);
            final TextView nameView = (TextView) handView.findViewById(R.id.hand_name);
            final LinearLayout cardContainer = (LinearLayout) handView.findViewById(R.id.hand_container);
            final View blinker = handView.findViewById(R.id.blinker);

            nameView.setText(player.getName());
            cardContainer.removeAllViews();
            boolean isMe = player.getName().equals(mName);
            if (player.getName().equals(mGame.getCurrentPlayer())) {
                blinker.setAlpha(0f);
                blinker.setVisibility(View.VISIBLE);
                ObjectAnimator animator = ObjectAnimator.ofFloat(blinker, View.ALPHA, 0.5f);
                animator.setRepeatMode(ValueAnimator.REVERSE);
                animator.setRepeatCount(ValueAnimator.INFINITE);
                animator.setDuration(400);
                animator.start();
            } else {
                blinker.setVisibility(View.INVISIBLE);
            }

            handView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    HintDialogFragment.create(mName, mGame.getId(), player).show(getFragmentManager(), null);
                }
            });

            int index = 0;
            for (final Card card : player.getHand()) {
                final int cardIndex = index;
                final CardView cardView = CardView.createCard(getActivity(), mInflater, cardContainer, CardView.Size.LARGE);
                if (isMe) {
                    cardView.bindWithUnknown(card);
                } else {
                    cardView.bindWithCard(card);
                }
                if (isMe) {
                    cardView.setOnDragListener(new CardDragListener(player.getName(), cardIndex));
                    cardView.setOnTouchListener(new View.OnTouchListener() {

                        @Override
                        public boolean onTouch(View view, MotionEvent event) {
                            final ClipData dragData = ClipData.newPlainText("data", cardIndex + "");
                            final View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                            final CardState state = new CardState();
                            state.index = cardIndex;
                            state.player = player.getName();
                            state.card = card;
                            view.startDrag(dragData, shadowBuilder, state, 0);
                            return true;
                        }
                    });
                } else {
                    cardView.setClickable(false);
                }
                cardContainer.addView(cardView);
                index++;
            }
            return handView;
        }
    }

    @Subscribe
    public void onFailure(HanabiErrorEvent event) {
        final HanabiError error = event.getError();
        final String eventString = event.getError().getEvent();
        final SocketEvent socketEvent = SocketEvent.getEvent(eventString);
        if (socketEvent == null) {
            Log.e(TAG, "Unknown error event : " + eventString);
            return;
        }
        Log.e(TAG, error.getEvent() + ":" + error.getReason());
        switch (socketEvent) {
            case JOIN_GAME:
                onJoinFailed();
                break;
            default:
                break;
        }
        Toast.makeText(getActivity(), error.getReason(), Toast.LENGTH_SHORT).show();
    }

    static class CardState {
        Card card;
        String player;
        int index;
    }
}
