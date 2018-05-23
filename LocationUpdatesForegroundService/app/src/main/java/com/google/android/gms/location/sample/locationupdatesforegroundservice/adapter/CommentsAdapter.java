package com.google.android.gms.location.sample.locationupdatesforegroundservice.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.location.sample.locationupdatesforegroundservice.R;

import java.util.ArrayList;
import java.util.List;

public class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int EXPAND = 0x1;
    private static final int COLLAPSE = 0x2;
    private static final int COMMENT_LIKE = 0x3;
    private static final int REPLY = 0x4;

    private final List<Comment> comments = new ArrayList<>();
    private View footer;

    private boolean loading;
    private boolean noComments;
    int expandedCommentPosition = RecyclerView.NO_POSITION;

    public CommentsAdapter(@Nullable View footer, long commentCount, long expandDuration) {
        this.footer = footer;
        noComments = commentCount == 0L;
        loading = !noComments;

    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.item_image;
    }

    @Override
    public int getItemCount() {
        int count = 1; // description
        if (!comments.isEmpty()) {
            count += comments.size();
        } else {
            count++; // either loading or no comments
        }
        if (footer != null) count++;
        return count;
    }

//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        switch (viewType) {
//            case R.layout.dribbble_shot_description:
//                return new SimpleViewHolder(description);
//            case R.layout.dribbble_comment:
//                return createCommentHolder(parent, viewType);
//            case R.layout.loading:
//            case R.layout.dribbble_no_comments:
//                return new SimpleViewHolder(
//                        getLayoutInflater().inflate(viewType, parent, false));
//            case R.layout.dribbble_enter_comment:
//                return new SimpleViewHolder(footer);
//        }
//        throw new IllegalArgumentException();
//    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return createCommentHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case R.layout.item_image:
                bindComment((CommentViewHolder) holder, null/*getComment(position)*/);
                break;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position,
                                 List<Object> partialChangePayloads) {
        if (holder instanceof CommentViewHolder) {
            bindPartialCommentChange(
                    (CommentViewHolder) holder, position, partialChangePayloads);
        } else {
            onBindViewHolder(holder, position);
        }
    }

    Comment getComment(int adapterPosition) {
        return comments.get(adapterPosition - 1); // description
    }

    public void addComments(List<Comment> newComments) {
        hideLoadingIndicator();
        noComments = false;
        comments.addAll(newComments);
        notifyItemRangeInserted(1, newComments.size());
    }

    void removeCommentingFooter() {
        if (footer == null) return;
        int footerPos = getItemCount() - 1;
        footer = null;
        notifyItemRemoved(footerPos);
    }

    private CommentViewHolder createCommentHolder(ViewGroup parent, int viewType) {
        final CommentViewHolder holder = new CommentViewHolder(
                ((LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(viewType, parent, false));


//        holder.avatar.setOnClickListener(v -> {
//            final int position = holder.getAdapterPosition();
//            if (position == RecyclerView.NO_POSITION) return;
//
//            final Comment comment = getComment(position);
//            final Intent player = new Intent(DribbbleShot.this, PlayerActivity.class);
//            player.putExtra(PlayerActivity.EXTRA_PLAYER, comment.user);
//            ActivityOptions options =
//                    ActivityOptions.makeSceneTransitionAnimation(DribbbleShot.this,
//                            Pair.create(holder.itemView,
//                                    getString(R.string.transition_player_background)),
//                            Pair.create((View) holder.avatar,
//                                    getString(R.string.transition_player_avatar)));
//            startActivity(player, options.toBundle());
//        });

        return holder;
    }

    private void bindComment(CommentViewHolder holder, Comment comment) {
        final int position = holder.getAdapterPosition();
        final boolean isExpanded = position == expandedCommentPosition;

        setExpanded(holder, isExpanded);
    }

    private void setExpanded(CommentViewHolder holder, boolean isExpanded) {
        holder.itemView.setActivated(isExpanded);
    }

    private void bindPartialCommentChange(
            CommentViewHolder holder, int position, List<Object> partialChangePayloads) {
        // for certain changes we don't need to rebind data, just update some view state
        if ((partialChangePayloads.contains(EXPAND)
                || partialChangePayloads.contains(COLLAPSE))
                || partialChangePayloads.contains(REPLY)) {
            setExpanded(holder, position == expandedCommentPosition);
        } else if (partialChangePayloads.contains(COMMENT_LIKE)) {
            return; // nothing to do
        } else {
            onBindViewHolder(holder, position);
        }
    }

    private void hideLoadingIndicator() {
        if (!loading) return;
        loading = false;
        notifyItemRemoved(1);
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {

        ImageView avatar;

        CommentViewHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.item_image);
        }
    }
}
